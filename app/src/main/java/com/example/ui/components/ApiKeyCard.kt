package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.ApiKeyConfig
import com.example.domain.model.KeyStatus
import com.example.domain.model.KeyUsageMode
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.RoseError
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ApiKeyCard(
    keyConfig: ApiKeyConfig,
    keyUsageMode: KeyUsageMode,
    isTesting: Boolean,
    onSaveKey: (rawKey: String) -> Unit,
    onClearKey: () -> Unit,
    onTestKey: () -> Unit,
    onSelectionChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var rawInputText by remember { mutableStateOf("") }
    var isInputVisible by remember { mutableStateOf(false) }
    var isEditing by remember { mutableStateOf(!keyConfig.hasKey) }

    NeumorphicCard(
        modifier = modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header Row: Checkbox (if in Selected mode), Slot Label, Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (keyUsageMode == KeyUsageMode.SELECTED_KEYS) {
                        Checkbox(
                            checked = keyConfig.isSelected && keyConfig.hasKey,
                            onCheckedChange = { onSelectionChanged(it) },
                            enabled = keyConfig.hasKey,
                            colors = CheckboxDefaults.colors(
                                checkedColor = MaterialTheme.colorScheme.primary,
                                checkmarkColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    } else {
                        Icon(
                            imageVector = Icons.Default.Key,
                            contentDescription = null,
                            tint = if (keyConfig.hasKey) CyanAccent else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    Text(
                        text = keyConfig.label,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                StatusBadge(status = keyConfig.status, hasKey = keyConfig.hasKey)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Body: If already configured and not actively editing, show masked ID
            if (keyConfig.hasKey && !isEditing) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = keyConfig.maskedKey.ifBlank { "••••••••••••••••" },
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
                    )

                    Row {
                        OutlinedButton(
                            onClick = {
                                isEditing = true
                                rawInputText = ""
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text("Edit", fontSize = 12.sp)
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        OutlinedButton(
                            onClick = onClearKey,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = RoseError),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text("Clear", fontSize = 12.sp)
                        }
                    }
                }
            } else {
                // Editing / entering key input
                OutlinedTextField(
                    value = rawInputText,
                    onValueChange = { rawInputText = it },
                    label = { Text("Enter OpenRouter Key (sk-or-v1-...)") },
                    placeholder = { Text("Paste key here") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = if (isInputVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { isInputVisible = !isInputVisible }) {
                            Icon(
                                imageVector = if (isInputVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (isInputVisible) "Hide key" else "Show key"
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    if (keyConfig.hasKey) {
                        OutlinedButton(
                            onClick = {
                                isEditing = false
                                rawInputText = ""
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Text("Cancel", fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    Button(
                        onClick = {
                            if (rawInputText.isNotBlank()) {
                                onSaveKey(rawInputText)
                                rawInputText = ""
                                isEditing = false
                            }
                        },
                        enabled = rawInputText.isNotBlank(),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Save", fontSize = 12.sp)
                    }
                }
            }

            // Stats & Test Connection action
            if (keyConfig.hasKey) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        val lastUsedStr = keyConfig.lastUsedTimestamp?.let {
                            val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
                            sdf.format(Date(it))
                        } ?: "Never"

                        Text(
                            text = "Last used: $lastUsedStr",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Reqs: ${keyConfig.requestCount}  |  Errs: ${keyConfig.errorCount}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Button(
                        onClick = onTestKey,
                        enabled = !isTesting,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        if (isTesting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(14.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Testing…", fontSize = 11.sp)
                        } else {
                            Icon(
                                imageVector = Icons.Default.NetworkCheck,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Test Connection", fontSize = 11.sp)
                        }
                    }
                }

                // If last error present
                if (!keyConfig.lastError.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Last error: ${keyConfig.lastError}",
                        fontSize = 11.sp,
                        color = RoseError
                    )
                }
            }
        }
    }
}

@Composable
fun StatusBadge(status: KeyStatus, hasKey: Boolean) {
    val (label, bg, fg) = when {
        !hasKey -> Triple("Not Configured", Color.Gray.copy(alpha = 0.2f), Color.Gray)
        status == KeyStatus.AVAILABLE -> Triple("Available", EmeraldSuccess.copy(alpha = 0.2f), EmeraldSuccess)
        status == KeyStatus.ACTIVE -> Triple("Active", CyanAccent.copy(alpha = 0.25f), CyanAccent)
        status == KeyStatus.COOLDOWN -> Triple("In Cooldown", AmberWarning.copy(alpha = 0.2f), AmberWarning)
        status == KeyStatus.RATE_LIMITED -> Triple("Rate Limited", AmberWarning.copy(alpha = 0.25f), AmberWarning)
        status == KeyStatus.INVALID -> Triple("Invalid Key", RoseError.copy(alpha = 0.2f), RoseError)
        status == KeyStatus.ERROR -> Triple("Error", RoseError.copy(alpha = 0.2f), RoseError)
        else -> Triple("Configured", EmeraldSuccess.copy(alpha = 0.2f), EmeraldSuccess)
    }

    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(fg)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = fg
        )
    }
}
