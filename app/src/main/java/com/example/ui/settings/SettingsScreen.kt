package com.example.ui.settings

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.domain.model.KeyUsageMode
import com.example.ui.components.ApiKeyCard
import com.example.ui.components.NeumorphicCard
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.RoseError
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.appSettings.collectAsState()
    val apiKeys by viewModel.apiKeys.collectAsState()
    val testingSlots by viewModel.testingSlots.collectAsState()
    val testResultMessage by viewModel.testResultMessage.collectAsState()

    var showClearHistoryDialog by remember { mutableStateOf(false) }
    var modelDropdownExpanded by remember { mutableStateOf(false) }
    var backendUrlInput by remember { mutableStateOf(settings.backendUrl) }
    var systemPromptInput by remember { mutableStateOf(settings.systemPrompt) }

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    val popularModels = listOf(
        "google/gemini-2.0-flash-001",
        "google/gemini-3.8-flash",
        "anthropic/claude-3.5-sonnet",
        "deepseek/deepseek-r1",
        "openai/gpt-4o",
        "meta-llama/llama-3.3-70b-instruct"
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.settings_title),
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Test connection result alert banner
            if (testResultMessage != null) {
                item {
                    val isSuccess = testResultMessage?.contains("Verified") == true
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSuccess) EmeraldSuccess.copy(alpha = 0.15f) else RoseError.copy(alpha = 0.15f))
                            .border(1.dp, if (isSuccess) EmeraldSuccess else RoseError, RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = testResultMessage ?: "",
                            fontSize = 13.sp,
                            color = if (isSuccess) EmeraldSuccess else RoseError,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { viewModel.dismissTestResult() }, modifier = Modifier.size(24.dp)) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Dismiss", tint = if (isSuccess) EmeraldSuccess else RoseError)
                        }
                    }
                }
            }

            // ==========================================
            // 1. OPENROUTER & 10 API KEYS
            // ==========================================
            item {
                SectionHeader(
                    title = stringResource(R.string.openrouter_title),
                    icon = Icons.Default.Key
                )
            }

            // API Key Usage Mode Card
            item {
                NeumorphicCard(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        Text(
                            text = stringResource(R.string.api_key_usage_mode),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        // Option A: Active All Keys
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { viewModel.setKeyUsageMode(KeyUsageMode.ACTIVE_ALL) }
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = settings.keyUsageMode == KeyUsageMode.ACTIVE_ALL,
                                onClick = { viewModel.setKeyUsageMode(KeyUsageMode.ACTIVE_ALL) },
                                colors = RadioButtonDefaults.colors(selectedColor = CyanAccent)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = stringResource(R.string.mode_active_all),
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = stringResource(R.string.mode_active_all_desc),
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Option B: Selected Keys
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { viewModel.setKeyUsageMode(KeyUsageMode.SELECTED_KEYS) }
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = settings.keyUsageMode == KeyUsageMode.SELECTED_KEYS,
                                onClick = { viewModel.setKeyUsageMode(KeyUsageMode.SELECTED_KEYS) },
                                colors = RadioButtonDefaults.colors(selectedColor = CyanAccent)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = stringResource(R.string.mode_selected_keys),
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = stringResource(R.string.mode_selected_keys_desc),
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // If Selected Keys mode is active: Action buttons and validation warning
                        AnimatedVisibility(visible = settings.keyUsageMode == KeyUsageMode.SELECTED_KEYS) {
                            Column {
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    OutlinedButton(
                                        onClick = { viewModel.selectAllKeys() },
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.height(34.dp)
                                    ) {
                                        Text(stringResource(R.string.select_all), fontSize = 12.sp)
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    OutlinedButton(
                                        onClick = { viewModel.clearAllKeySelections() },
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.height(34.dp)
                                    ) {
                                        Text(stringResource(R.string.clear_all), fontSize = 12.sp)
                                    }
                                }

                                val anySelected = apiKeys.any { it.hasKey && it.isSelected }
                                if (!anySelected) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = AmberWarning, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = stringResource(R.string.error_no_selected_keys),
                                            color = AmberWarning,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 10 API Key Cards
            items(apiKeys, key = { it.slotIndex }) { keyConfig ->
                ApiKeyCard(
                    keyConfig = keyConfig,
                    keyUsageMode = settings.keyUsageMode,
                    isTesting = testingSlots.contains(keyConfig.slotIndex),
                    onSaveKey = { rawKey -> viewModel.saveKey(keyConfig.slotIndex, rawKey) },
                    onClearKey = { viewModel.clearKey(keyConfig.slotIndex) },
                    onTestKey = { viewModel.testConnection(keyConfig.slotIndex) },
                    onSelectionChanged = { isSelected -> viewModel.setKeySelected(keyConfig.slotIndex, isSelected) }
                )
            }

            // ==========================================
            // 2. MODEL CONFIGURATION
            // ==========================================
            item {
                SectionHeader(
                    title = stringResource(R.string.section_ai_config),
                    icon = Icons.Default.Tune
                )
            }

            item {
                NeumorphicCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        // Model Dropdown
                        ExposedDropdownMenuBox(
                            expanded = modelDropdownExpanded,
                            onExpandedChange = { modelDropdownExpanded = !modelDropdownExpanded }
                        ) {
                            OutlinedTextField(
                                value = settings.model,
                                onValueChange = { viewModel.updateModel(it) },
                                label = { Text(stringResource(R.string.model_selection)) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = modelDropdownExpanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )

                            ExposedDropdownMenu(
                                expanded = modelDropdownExpanded,
                                onDismissRequest = { modelDropdownExpanded = false }
                            ) {
                                popularModels.forEach { modelName ->
                                    DropdownMenuItem(
                                        text = { Text(modelName) },
                                        onClick = {
                                            viewModel.updateModel(modelName)
                                            modelDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // Temperature Slider
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = stringResource(R.string.temperature_label), fontSize = 13.sp)
                                Text(text = String.format("%.1f", settings.temperature), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = CyanAccent)
                            }
                            Slider(
                                value = settings.temperature,
                                onValueChange = { viewModel.updateTemperature(it) },
                                valueRange = 0.0f..1.5f,
                                steps = 14,
                                colors = SliderDefaults.colors(
                                    thumbColor = CyanAccent,
                                    activeTrackColor = CyanAccent
                                )
                            )
                        }

                        // Max Tokens Slider
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = stringResource(R.string.max_tokens_label), fontSize = 13.sp)
                                Text(text = "${settings.maxTokens}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = CyanAccent)
                            }
                            Slider(
                                value = settings.maxTokens.toFloat(),
                                onValueChange = { viewModel.updateMaxTokens(it.toInt()) },
                                valueRange = 512f..8192f,
                                steps = 14,
                                colors = SliderDefaults.colors(
                                    thumbColor = CyanAccent,
                                    activeTrackColor = CyanAccent
                                )
                            )
                        }

                        // Real-time Streaming Switch
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = stringResource(R.string.streaming_label), fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                Text(text = "Renders text tokens progressively as they arrive", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(
                                checked = settings.streamingEnabled,
                                onCheckedChange = { viewModel.updateStreaming(it) },
                                colors = SwitchDefaults.colors(checkedThumbColor = CyanAccent)
                            )
                        }
                    }
                }
            }

            // ==========================================
            // 3. SYSTEM PROMPT
            // ==========================================
            item {
                SectionHeader(
                    title = stringResource(R.string.system_prompt_label),
                    icon = Icons.Default.AutoAwesome
                )
            }

            item {
                NeumorphicCard(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        OutlinedTextField(
                            value = systemPromptInput,
                            onValueChange = {
                                systemPromptInput = it
                                viewModel.updateSystemPrompt(it)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3,
                            maxLines = 6,
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(
                                onClick = {
                                    viewModel.resetSystemPrompt()
                                    systemPromptInput = com.example.domain.model.AppSettings.DEFAULT_SYSTEM_PROMPT
                                }
                            ) {
                                Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(stringResource(R.string.reset_defaults), fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // ==========================================
            // 4. LANGUAGE
            // ==========================================
            item {
                SectionHeader(
                    title = stringResource(R.string.section_language),
                    icon = Icons.Default.Language
                )
            }

            item {
                NeumorphicCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        LanguageChip(
                            label = "বাংলা",
                            isSelected = settings.selectedLanguage == "bn",
                            onClick = { viewModel.updateLanguage("bn") }
                        )
                        LanguageChip(
                            label = "English",
                            isSelected = settings.selectedLanguage == "en",
                            onClick = { viewModel.updateLanguage("en") }
                        )
                        LanguageChip(
                            label = "العربية",
                            isSelected = settings.selectedLanguage == "ar",
                            onClick = { viewModel.updateLanguage("ar") }
                        )
                    }
                }
            }

            // ==========================================
            // 5. APPEARANCE
            // ==========================================
            item {
                SectionHeader(
                    title = stringResource(R.string.section_appearance),
                    icon = Icons.Default.Palette
                )
            }

            item {
                NeumorphicCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        ThemeChip(
                            label = stringResource(R.string.theme_system),
                            isSelected = settings.themeMode == "system",
                            onClick = { viewModel.updateTheme("system") }
                        )
                        ThemeChip(
                            label = stringResource(R.string.theme_dark),
                            isSelected = settings.themeMode == "dark",
                            onClick = { viewModel.updateTheme("dark") }
                        )
                        ThemeChip(
                            label = stringResource(R.string.theme_light),
                            isSelected = settings.themeMode == "light",
                            onClick = { viewModel.updateTheme("light") }
                        )
                    }
                }
            }

            // ==========================================
            // 6. CHAT PREFERENCES
            // ==========================================
            item {
                SectionHeader(
                    title = stringResource(R.string.section_chat_prefs),
                    icon = Icons.Default.Tune
                )
            }

            item {
                NeumorphicCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = stringResource(R.string.enter_is_send), fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            Text(text = "Send message immediately when pressing Enter key", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = settings.enterToSend,
                            onCheckedChange = { viewModel.updateEnterToSend(it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = CyanAccent)
                        )
                    }
                }
            }

            // ==========================================
            // 7. OPTIONAL BACKEND (INFINITYFREE / PHP)
            // ==========================================
            item {
                SectionHeader(
                    title = "PHP Backend Gateway (Optional)",
                    icon = Icons.Default.Dns
                )
            }

            item {
                NeumorphicCard(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        Text(
                            text = "InfinityFree / Custom Domain Backend",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "If deployed on InfinityFree or private server, enter URL below to proxy requests through PHP backend. Leave empty for direct OpenRouter.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = backendUrlInput,
                            onValueChange = {
                                backendUrlInput = it
                                viewModel.updateBackendUrl(it)
                            },
                            placeholder = { Text("https://yourdomain.com") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // ==========================================
            // 8. DATA & STORAGE
            // ==========================================
            item {
                SectionHeader(
                    title = stringResource(R.string.section_data_storage),
                    icon = Icons.Default.FileDownload
                )
            }

            item {
                NeumorphicCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(
                            onClick = {
                                coroutineScope.launch {
                                    val json = viewModel.exportJson()
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("Wafa_AI_Export", json))
                                    Toast.makeText(context, "Exported JSON copied to clipboard", Toast.LENGTH_SHORT).show()
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(imageVector = Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.export_data))
                        }

                        OutlinedButton(
                            onClick = { showClearHistoryDialog = true },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = RoseError),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.clear_all_history))
                        }
                    }
                }
            }

            // ==========================================
            // 9. ABOUT
            // ==========================================
            item {
                SectionHeader(
                    title = stringResource(R.string.section_about),
                    icon = Icons.Default.Info
                )
            }

            item {
                NeumorphicCard(modifier = Modifier.fillMaxWidth()) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = stringResource(R.string.app_name),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = CyanAccent
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.app_tagline),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = stringResource(R.string.developer_credit),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Version 1.0 (Production Build)",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Clear history confirm dialog
    if (showClearHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showClearHistoryDialog = false },
            title = { Text(stringResource(R.string.clear_all_history)) },
            text = { Text("Are you sure you want to permanently delete all conversations and message history?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAllHistory()
                        showClearHistoryDialog = false
                        Toast.makeText(context, "History cleared", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text(stringResource(R.string.confirm), color = RoseError)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearHistoryDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
fun SectionHeader(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(start = 4.dp, top = 6.dp)
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
fun LanguageChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) CyanAccent else MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSelected) CyanAccent else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        ),
        modifier = Modifier.width(96.dp).height(40.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun ThemeChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) CyanAccent else MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSelected) CyanAccent else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        ),
        modifier = Modifier.width(96.dp).height(40.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
