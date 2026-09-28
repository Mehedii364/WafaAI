package com.example.data.local.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.domain.model.ApiKeyConfig
import com.example.domain.model.KeyStatus

@Entity(tableName = "api_keys")
data class ApiKeyEntity(
    @PrimaryKey
    val slotIndex: Int, // 1 to 10
    val label: String,
    val encryptedKey: String = "",
    val maskedKey: String = "",
    val isSelected: Boolean = false,
    val status: String = "NOT_CONFIGURED",
    val requestCount: Int = 0,
    val errorCount: Int = 0,
    val lastUsedTimestamp: Long? = null,
    val cooldownUntilTimestamp: Long? = null,
    val lastError: String? = null
) {
    fun toDomain(): ApiKeyConfig = ApiKeyConfig(
        slotIndex = slotIndex,
        label = label,
        hasKey = encryptedKey.isNotBlank(),
        maskedKey = maskedKey,
        isSelected = isSelected,
        status = try { KeyStatus.valueOf(status) } catch (_: Exception) { KeyStatus.NOT_CONFIGURED },
        requestCount = requestCount,
        errorCount = errorCount,
        lastUsedTimestamp = lastUsedTimestamp,
        cooldownUntilTimestamp = cooldownUntilTimestamp,
        lastError = lastError
    )
}
