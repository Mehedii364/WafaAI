package com.example

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.data.local.database.AppDatabase
import com.example.data.local.preferences.UserPreferences
import com.example.data.remote.OpenRouterService
import com.example.data.repository.ApiKeyManager
import com.example.data.repository.ChatRepository
import com.example.ui.chat.ChatViewModel
import com.example.ui.conversations.ConversationsViewModel
import com.example.ui.navigation.MainAppContainer
import com.example.ui.settings.SettingsViewModel
import com.example.ui.theme.MyApplicationTheme
import java.util.Locale

class MainActivity : ComponentActivity() {

    private lateinit var appDatabase: AppDatabase
    private lateinit var userPreferences: UserPreferences
    private lateinit var openRouterService: OpenRouterService
    private lateinit var apiKeyManager: ApiKeyManager
    private lateinit var chatRepository: ChatRepository

    private val chatViewModel: ChatViewModel by viewModels {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ChatViewModel(chatRepository, apiKeyManager, userPreferences) as T
            }
        }
    }

    private val conversationsViewModel: ConversationsViewModel by viewModels {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ConversationsViewModel(chatRepository) as T
            }
        }
    }

    private val settingsViewModel: SettingsViewModel by viewModels {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return SettingsViewModel(apiKeyManager, chatRepository, userPreferences) as T
            }
        }
    }

    override fun attachBaseContext(newBase: Context) {
        // Enforce default Bengali locale or read persisted
        val prefs = newBase.getSharedPreferences("wafa_ai_locale_cache", Context.MODE_PRIVATE)
        val lang = prefs.getString("lang", "bn") ?: "bn"
        val locale = Locale(lang)
        Locale.setDefault(locale)
        val config = Configuration(newBase.resources.configuration)
        config.setLocale(locale)
        val localizedContext = newBase.createConfigurationContext(config)
        super.attachBaseContext(localizedContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        appDatabase = AppDatabase.getDatabase(applicationContext)
        userPreferences = UserPreferences(applicationContext)
        openRouterService = OpenRouterService()
        apiKeyManager = ApiKeyManager(
            apiKeyDao = appDatabase.apiKeyDao(),
            userPreferences = userPreferences,
            openRouterService = openRouterService
        )
        chatRepository = ChatRepository(
            conversationDao = appDatabase.conversationDao(),
            messageDao = appDatabase.messageDao(),
            apiKeyManager = apiKeyManager,
            openRouterService = openRouterService,
            userPreferences = userPreferences
        )

        setContent {
            val settings by settingsViewModel.appSettings.collectAsState()

            // Update cached language for next process launch
            val prefs = getSharedPreferences("wafa_ai_locale_cache", Context.MODE_PRIVATE)
            if (prefs.getString("lang", "") != settings.selectedLanguage) {
                prefs.edit().putString("lang", settings.selectedLanguage).apply()
            }

            val isDark = when (settings.themeMode) {
                "dark" -> true
                "light" -> false
                else -> isSystemInDarkTheme()
            }

            MyApplicationTheme(darkTheme = isDark) {
                MainAppContainer(
                    chatViewModel = chatViewModel,
                    conversationsViewModel = conversationsViewModel,
                    settingsViewModel = settingsViewModel
                )
            }
        }
    }
}
