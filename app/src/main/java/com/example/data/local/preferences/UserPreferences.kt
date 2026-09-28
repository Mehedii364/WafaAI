package com.example.data.local.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.domain.model.AppSettings
import com.example.domain.model.KeyUsageMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "wafa_ai_preferences")

class UserPreferences(private val context: Context) {

    private object PreferencesKeys {
        val SELECTED_LANGUAGE = stringPreferencesKey("selected_language")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val MODEL = stringPreferencesKey("model")
        val SYSTEM_PROMPT = stringPreferencesKey("system_prompt")
        val TEMPERATURE = floatPreferencesKey("temperature")
        val MAX_TOKENS = intPreferencesKey("max_tokens")
        val STREAMING_ENABLED = booleanPreferencesKey("streaming_enabled")
        val ENTER_TO_SEND = booleanPreferencesKey("enter_to_send")
        val KEY_USAGE_MODE = stringPreferencesKey("key_usage_mode")
        val BACKEND_URL = stringPreferencesKey("backend_url")
    }

    val appSettingsFlow: Flow<AppSettings> = context.dataStore.data.map { preferences ->
        val language = preferences[PreferencesKeys.SELECTED_LANGUAGE] ?: "bn" // Default বাংলা
        val themeMode = preferences[PreferencesKeys.THEME_MODE] ?: "system"
        val model = preferences[PreferencesKeys.MODEL] ?: "google/gemini-2.0-flash-001"
        val systemPrompt = preferences[PreferencesKeys.SYSTEM_PROMPT] ?: AppSettings.DEFAULT_SYSTEM_PROMPT
        val temperature = preferences[PreferencesKeys.TEMPERATURE] ?: 0.7f
        val maxTokens = preferences[PreferencesKeys.MAX_TOKENS] ?: 4096
        val streaming = preferences[PreferencesKeys.STREAMING_ENABLED] ?: true
        val enterToSend = preferences[PreferencesKeys.ENTER_TO_SEND] ?: true
        val modeStr = preferences[PreferencesKeys.KEY_USAGE_MODE] ?: KeyUsageMode.ACTIVE_ALL.name
        val keyMode = try {
            KeyUsageMode.valueOf(modeStr)
        } catch (_: Exception) {
            KeyUsageMode.ACTIVE_ALL
        }
        val backendUrl = preferences[PreferencesKeys.BACKEND_URL] ?: ""

        AppSettings(
            selectedLanguage = language,
            themeMode = themeMode,
            model = model,
            systemPrompt = systemPrompt,
            temperature = temperature,
            maxTokens = maxTokens,
            streamingEnabled = streaming,
            enterToSend = enterToSend,
            keyUsageMode = keyMode,
            backendUrl = backendUrl
        )
    }

    suspend fun setLanguage(language: String) {
        context.dataStore.edit { it[PreferencesKeys.SELECTED_LANGUAGE] = language }
    }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { it[PreferencesKeys.THEME_MODE] = mode }
    }

    suspend fun setModel(model: String) {
        context.dataStore.edit { it[PreferencesKeys.MODEL] = model }
    }

    suspend fun setSystemPrompt(prompt: String) {
        context.dataStore.edit { it[PreferencesKeys.SYSTEM_PROMPT] = prompt }
    }

    suspend fun setTemperature(temp: Float) {
        context.dataStore.edit { it[PreferencesKeys.TEMPERATURE] = temp }
    }

    suspend fun setMaxTokens(tokens: Int) {
        context.dataStore.edit { it[PreferencesKeys.MAX_TOKENS] = tokens }
    }

    suspend fun setStreamingEnabled(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.STREAMING_ENABLED] = enabled }
    }

    suspend fun setEnterToSend(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.ENTER_TO_SEND] = enabled }
    }

    suspend fun setKeyUsageMode(mode: KeyUsageMode) {
        context.dataStore.edit { it[PreferencesKeys.KEY_USAGE_MODE] = mode.name }
    }

    suspend fun setBackendUrl(url: String) {
        context.dataStore.edit { it[PreferencesKeys.BACKEND_URL] = url }
    }

    suspend fun resetToDefaults() {
        context.dataStore.edit {
            it[PreferencesKeys.SELECTED_LANGUAGE] = "bn"
            it[PreferencesKeys.THEME_MODE] = "system"
            it[PreferencesKeys.MODEL] = "google/gemini-2.0-flash-001"
            it[PreferencesKeys.SYSTEM_PROMPT] = AppSettings.DEFAULT_SYSTEM_PROMPT
            it[PreferencesKeys.TEMPERATURE] = 0.7f
            it[PreferencesKeys.MAX_TOKENS] = 4096
            it[PreferencesKeys.STREAMING_ENABLED] = true
            it[PreferencesKeys.ENTER_TO_SEND] = true
            it[PreferencesKeys.KEY_USAGE_MODE] = KeyUsageMode.ACTIVE_ALL.name
            it[PreferencesKeys.BACKEND_URL] = ""
        }
    }
}
