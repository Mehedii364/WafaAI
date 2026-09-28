package com.example.data.local.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.database.entity.ApiKeyEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ApiKeyDao {
    @Query("SELECT * FROM api_keys ORDER BY slotIndex ASC")
    fun getAllApiKeys(): Flow<List<ApiKeyEntity>>

    @Query("SELECT * FROM api_keys ORDER BY slotIndex ASC")
    suspend fun getAllApiKeysSync(): List<ApiKeyEntity>

    @Query("SELECT * FROM api_keys WHERE slotIndex = :slotIndex LIMIT 1")
    suspend fun getApiKeyBySlot(slotIndex: Int): ApiKeyEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(apiKey: ApiKeyEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertDefaultSlots(keys: List<ApiKeyEntity>)

    @Update
    suspend fun updateKey(apiKey: ApiKeyEntity)

    @Query("UPDATE api_keys SET isSelected = :isSelected WHERE slotIndex = :slotIndex")
    suspend fun updateSelection(slotIndex: Int, isSelected: Boolean)

    @Query("UPDATE api_keys SET isSelected = :isSelected")
    suspend fun updateAllSelection(isSelected: Boolean)

    @Query("UPDATE api_keys SET encryptedKey = '', maskedKey = '', status = 'NOT_CONFIGURED', isSelected = 0 WHERE slotIndex = :slotIndex")
    suspend fun clearKey(slotIndex: Int)

    @Query("UPDATE api_keys SET status = :status, lastError = :lastError WHERE slotIndex = :slotIndex")
    suspend fun updateStatus(slotIndex: Int, status: String, lastError: String? = null)

    @Query("""
        UPDATE api_keys 
        SET status = :status, 
            cooldownUntilTimestamp = :cooldownUntil, 
            errorCount = errorCount + 1, 
            lastError = :lastError 
        WHERE slotIndex = :slotIndex
    """)
    suspend fun recordError(slotIndex: Int, status: String, cooldownUntil: Long?, lastError: String?)

    @Query("""
        UPDATE api_keys 
        SET status = 'AVAILABLE', 
            requestCount = requestCount + 1, 
            lastUsedTimestamp = :timestamp, 
            lastError = null 
        WHERE slotIndex = :slotIndex
    """)
    suspend fun recordSuccess(slotIndex: Int, timestamp: Long = System.currentTimeMillis())
}
