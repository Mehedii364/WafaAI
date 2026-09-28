package com.example.domain.model

data class ApiKeyConfig(
    val slotIndex: Int, // 1 to 10
    val label: String,  // "API Key 01", etc.
    val hasKey: Boolean = false,
    val maskedKey: String = "",
    val isSelected: Boolean = false,
    val status: KeyStatus = KeyStatus.NOT_CONFIGURED,
    val requestCount: Int = 0,
    val errorCount: Int = 0,
    val lastUsedTimestamp: Long? = null,
    val cooldownUntilTimestamp: Long? = null,
    val lastError: String? = null
) {
    val isReadyToUse: Boolean
        get() {
            if (!hasKey) return false
            if (status == KeyStatus.INVALID) return false
            val now = System.currentTimeMillis()
            if (cooldownUntilTimestamp != null && cooldownUntilTimestamp > now) {
                return false
            }
            return true
        }
}
