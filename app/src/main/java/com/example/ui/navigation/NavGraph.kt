package com.example.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.chat.ChatScreen
import com.example.ui.chat.ChatViewModel
import com.example.ui.conversations.ConversationsScreen
import com.example.ui.conversations.ConversationsViewModel
import com.example.ui.explore.ExploreScreen
import com.example.ui.settings.SettingsScreen
import com.example.ui.settings.SettingsViewModel
import com.example.ui.theme.CyanAccent

enum class AppDestination(val labelRes: Int, val icon: ImageVector, val tag: String) {
    CHAT(R.string.nav_chat, Icons.AutoMirrored.Filled.Chat, "nav_chat"),
    CONVERSATIONS(R.string.nav_conversations, Icons.Default.History, "nav_conversations"),
    EXPLORE(R.string.nav_explore, Icons.Default.Explore, "nav_explore"),
    SETTINGS(R.string.nav_settings, Icons.Default.Settings, "nav_settings")
}

@Composable
fun MainAppContainer(
    chatViewModel: ChatViewModel,
    conversationsViewModel: ConversationsViewModel,
    settingsViewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    var currentDestination by remember { mutableStateOf(AppDestination.CHAT) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                AppDestination.entries.forEach { dest ->
                    val isSelected = currentDestination == dest
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentDestination = dest },
                        icon = {
                            Icon(
                                imageVector = dest.icon,
                                contentDescription = stringResource(dest.labelRes),
                                tint = if (isSelected) CyanAccent else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        label = {
                            Text(
                                text = stringResource(dest.labelRes),
                                fontSize = 11.sp,
                                color = if (isSelected) CyanAccent else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = CyanAccent.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.testTag(dest.tag)
                    )
                }
            }
        }
    ) { innerPadding ->
        when (currentDestination) {
            AppDestination.CHAT -> {
                ChatScreen(
                    viewModel = chatViewModel,
                    onNavigateToSettings = { currentDestination = AppDestination.SETTINGS },
                    modifier = Modifier.padding(innerPadding)
                )
            }
            AppDestination.CONVERSATIONS -> {
                ConversationsScreen(
                    viewModel = conversationsViewModel,
                    onSelectConversation = { convId ->
                        chatViewModel.selectConversation(convId)
                        currentDestination = AppDestination.CHAT
                    },
                    onNewChat = {
                        chatViewModel.startNewChat()
                        currentDestination = AppDestination.CHAT
                    },
                    modifier = Modifier.padding(innerPadding)
                )
            }
            AppDestination.EXPLORE -> {
                ExploreScreen(
                    onSelectPrompt = { prompt ->
                        chatViewModel.startNewChat()
                        chatViewModel.sendMessage(prompt)
                        currentDestination = AppDestination.CHAT
                    },
                    modifier = Modifier.padding(innerPadding)
                )
            }
            AppDestination.SETTINGS -> {
                SettingsScreen(
                    viewModel = settingsViewModel,
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}
