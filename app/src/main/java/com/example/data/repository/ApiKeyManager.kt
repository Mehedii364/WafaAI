package com.example.data.repository

import com.example.data.local.database.dao.ApiKeyDao
import com.example.data.local.database.entity.ApiKeyEntity
import com.example.data.local.preferences.UserPreferences
import com.example.data.local.security.KeySecurityHelper
import com.example.data.remote.ApiException
import com.example.data.remote.OpenRouterService
import com.example.domain.model.ApiKeyConfig
import com.example.domain.model.KeyStatus
import com.example.domain.model.KeyUsageMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class ApiKeyManager(
    private val apiKeyDao: ApiKeyDao,
    private val userPreferences: UserPreferences,
    private val openRouterService: OpenRouterService
) {
    private val rotationMutex = Mutex()
    private var currentRotationIndex = 0

    val apiKeysFlow: Flow<List<ApiKeyConfig>> = apiKeyDao.getAllApiKeys().map { entities ->
        entities.map { it.toDomain() }
    }

    suspend fun saveKey(slotIndex: Int, rawKey: String): Result<Unit> = withContext(Dispatchers.IO) {
        val trimmed = rawKey.trim()
        if (trimmed.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("Key cannot be empty"))
        }
        val encrypted = KeySecurityHelper.encrypt(trimmed)
        val masked = KeySecurityHelper.maskKey(trimmed)
        val existing = apiKeyDao.getApiKeyBySlot(slotIndex)

        val updated = ApiKeyEntity(
            slotIndex = slotIndex,
            label = existing?.label ?: String.format("API Key %02d", slotIndex),
            encryptedKey = encrypted,
            maskedKey = masked,
            isSelected = true,
            status = KeyStatus.AVAILABLE.name,
            requestCount = existing?.requestCount ?: 0,
            errorCount = 0,
            lastUsedTimestamp = existing?.lastUsedTimestamp,
            cooldownUntilTimestamp = null,
            lastError = null
        )
        apiKeyDao.insertOrUpdate(updated)
        Result.success(Unit)
    }

    suspend fun clearKey(slotIndex: Int): Unit = withContext(Dispatchers.IO) {
        apiKeyDao.clearKey(slotIndex)
    }

    suspend fun setKeySelected(slotIndex: Int, isSelected: Boolean): Unit = withContext(Dispatchers.IO) {
        val key = apiKeyDao.getApiKeyBySlot(slotIndex)
        if (key != null && key.encryptedKey.isNotBlank()) {
            apiKeyDao.updateSelection(slotIndex, isSelected)
        }
    }

    suspend fun selectAllConfigured(): Unit = withContext(Dispatchers.IO) {
        val all = apiKeyDao.getAllApiKeysSync()
        all.forEach { key ->
            if (key.encryptedKey.isNotBlank()) {
                apiKeyDao.updateSelection(key.slotIndex, true)
            }
        }
    }

    suspend fun clearAllSelections(): Unit = withContext(Dispatchers.IO) {
        apiKeyDao.updateAllSelection(false)
    }

    suspend fun testKeyConnection(slotIndex: Int, model: String): Result<String> = withContext(Dispatchers.IO) {
        val entity = apiKeyDao.getApiKeyBySlot(slotIndex)
            ?: return@withContext Result.failure(IllegalStateException("Slot $slotIndex not found"))

        if (entity.encryptedKey.isBlank()) {
            return@withContext Result.failure(IllegalStateException("No key configured in this slot"))
        }

        apiKeyDao.updateStatus(slotIndex, KeyStatus.ACTIVE.name, null)
        val rawKey = KeySecurityHelper.decrypt(entity.encryptedKey)
        if (rawKey.isBlank()) {
            apiKeyDao.updateStatus(slotIndex, KeyStatus.ERROR.name, "Decryption failed")
            return@withContext Result.failure(IllegalStateException("Could not decrypt key"))
        }

        val testResult = openRouterService.testConnection(rawKey, model)
        testResult.fold(
            onSuccess = { reply ->
                apiKeyDao.recordSuccess(slotIndex)
                Result.success(reply)
            },
            onFailure = { error ->
                val status = if (error is ApiException && error.statusCode == 401) {
                    KeyStatus.INVALID
                } else if (error is ApiException && error.statusCode == 429) {
                    KeyStatus.RATE_LIMITED
                } else {
                    KeyStatus.ERROR
                }
                apiKeyDao.recordError(
                    slotIndex = slotIndex,
                    status = status.name,
                    cooldownUntil = if (status == KeyStatus.RATE_LIMITED) System.currentTimeMillis() + 60_000 else null,
                    lastError = error.message
                )
                Result.failure(error)
            }
        )
    }

    /**
     * Resolves the candidate keys based on active mode (Active All vs Selected Keys).
     * Filters out non-configured keys, invalid keys, and keys in active cooldown.
     */
    suspend fun getEligibleKeys(): List<DecryptedKeyInfo> = withContext(Dispatchers.IO) {
        val settings = userPreferences.appSettingsFlow.first()
        val allEntities = apiKeyDao.getAllApiKeysSync()
        val now = System.currentTimeMillis()

        val candidateEntities = when (settings.keyUsageMode) {
            KeyUsageMode.ACTIVE_ALL -> {
                allEntities.filter { it.encryptedKey.isNotBlank() }
            }
            KeyUsageMode.SELECTED_KEYS -> {
                allEntities.filter { it.encryptedKey.isNotBlank() && it.isSelected }
            }
        }

        candidateEntities
            .filter { entity ->
                // Check if invalid
                if (entity.status == KeyStatus.INVALID.name) return@filter false
                // Check cooldown
                if (entity.cooldownUntilTimestamp != null && entity.cooldownUntilTimestamp > now) {
                    return@filter false
                }
                true
            }
            .mapNotNull { entity ->
                val raw = KeySecurityHelper.decrypt(entity.encryptedKey)
                if (raw.isNotBlank()) {
                    DecryptedKeyInfo(
                        slotIndex = entity.slotIndex,
                        rawKey = raw,
                        label = entity.label,
                        maskedKey = entity.maskedKey
                    )
                } else null
            }
    }

    /**
     * Picks the next key using rotation distribution.
     */
    suspend fun pickNextKey(): DecryptedKeyInfo? = rotationMutex.withLock {
        val eligible = getEligibleKeys()
        if (eligible.isEmpty()) return null

        currentRotationIndex = (currentRotationIndex) % eligible.size
        val chosen = eligible[currentRotationIndex]
        currentRotationIndex = (currentRotationIndex + 1) % eligible.size
        chosen
    }

    suspend fun handleKeySuccess(slotIndex: Int): Unit = withContext(Dispatchers.IO) {
        apiKeyDao.recordSuccess(slotIndex)
    }

    suspend fun handleKeyFailure(slotIndex: Int, error: Throwable): Unit = withContext(Dispatchers.IO) {
        val (status, cooldownMillis) = when {
            error is ApiException && error.statusCode == 401 -> {
                Pair(KeyStatus.INVALID, null)
            }
            error is ApiException && error.statusCode == 429 -> {
                Pair(KeyStatus.RATE_LIMITED, 60_000L) // 1 min cooldown
            }
            error is ApiException && error.statusCode in 500..599 -> {
                Pair(KeyStatus.COOLDOWN, 30_000L) // 30 sec cooldown
            }
            else -> {
                Pair(KeyStatus.ERROR, 15_000L)
            }
        }

        val cooldownUntil = cooldownMillis?.let { System.currentTimeMillis() + it }
        apiKeyDao.recordError(
            slotIndex = slotIndex,
            status = status.name,
            cooldownUntil = cooldownUntil,
            lastError = error.message
        )
    }
}

data class DecryptedKeyInfo(
    val slotIndex: Int,
    val rawKey: String,
    val label: String,
    val maskedKey: String
)
