package com.example.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.preferences.UserPreferences
import com.example.data.repository.ApiKeyManager
import com.example.data.repository.ChatRepository
import com.example.domain.model.ApiKeyConfig
import com.example.domain.model.AppSettings
import com.example.domain.model.KeyUsageMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val apiKeyManager: ApiKeyManager,
    private val chatRepository: ChatRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {

    val appSettings: StateFlow<AppSettings> = userPreferences.appSettingsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AppSettings()
    )

    val apiKeys: StateFlow<List<ApiKeyConfig>> = apiKeyManager.apiKeysFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _testingSlots = MutableStateFlow<Set<Int>>(emptySet())
    val testingSlots: StateFlow<Set<Int>> = _testingSlots.asStateFlow()

    private val _testResultMessage = MutableStateFlow<String?>(null)
    val testResultMessage: StateFlow<String?> = _testResultMessage.asStateFlow()

    fun setKeyUsageMode(mode: KeyUsageMode) {
        viewModelScope.launch {
            userPreferences.setKeyUsageMode(mode)
        }
    }

    fun saveKey(slotIndex: Int, rawKey: String) {
        viewModelScope.launch {
            val result = apiKeyManager.saveKey(slotIndex, rawKey)
            if (result.isFailure) {
                _testResultMessage.value = "Failed to save key: ${result.exceptionOrNull()?.message}"
            }
        }
    }

    fun clearKey(slotIndex: Int) {
        viewModelScope.launch {
            apiKeyManager.clearKey(slotIndex)
        }
    }

    fun setKeySelected(slotIndex: Int, isSelected: Boolean) {
        viewModelScope.launch {
            apiKeyManager.setKeySelected(slotIndex, isSelected)
        }
    }

    fun selectAllKeys() {
        viewModelScope.launch {
            apiKeyManager.selectAllConfigured()
        }
    }

    fun clearAllKeySelections() {
        viewModelScope.launch {
            apiKeyManager.clearAllSelections()
        }
    }

    fun testConnection(slotIndex: Int) {
        viewModelScope.launch {
            _testingSlots.value = _testingSlots.value + slotIndex
            _testResultMessage.value = null
            val model = appSettings.value.model
            val result = apiKeyManager.testKeyConnection(slotIndex, model)
            _testingSlots.value = _testingSlots.value - slotIndex

            result.fold(
                onSuccess = { reply ->
                    _testResultMessage.value = "Slot $slotIndex Verified! Connection test successful: \"$reply\""
                },
                onFailure = { err ->
                    _testResultMessage.value = "Slot $slotIndex Test Failed: ${err.message}"
                }
            )
        }
    }

    fun dismissTestResult() {
        _testResultMessage.value = null
    }

    fun updateModel(model: String) {
        viewModelScope.launch {
            userPreferences.setModel(model.trim())
        }
    }

    fun updateSystemPrompt(prompt: String) {
        viewModelScope.launch {
            userPreferences.setSystemPrompt(prompt)
        }
    }

    fun resetSystemPrompt() {
        viewModelScope.launch {
            userPreferences.setSystemPrompt(AppSettings.DEFAULT_SYSTEM_PROMPT)
        }
    }

    fun updateTemperature(temp: Float) {
        viewModelScope.launch {
            userPreferences.setTemperature(temp)
        }
    }

    fun updateMaxTokens(tokens: Int) {
        viewModelScope.launch {
            userPreferences.setMaxTokens(tokens)
        }
    }

    fun updateStreaming(enabled: Boolean) {
        viewModelScope.launch {
            userPreferences.setStreamingEnabled(enabled)
        }
    }

    fun updateEnterToSend(enabled: Boolean) {
        viewModelScope.launch {
            userPreferences.setEnterToSend(enabled)
        }
    }

    fun updateLanguage(lang: String) {
        viewModelScope.launch {
            userPreferences.setLanguage(lang)
        }
    }

    fun updateTheme(theme: String) {
        viewModelScope.launch {
            userPreferences.setThemeMode(theme)
        }
    }

    fun updateBackendUrl(url: String) {
        viewModelScope.launch {
            userPreferences.setBackendUrl(url.trim())
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            chatRepository.clearAllConversations()
        }
    }

    suspend fun exportJson(): String {
        return chatRepository.exportConversationsAsJson()
    }
}
