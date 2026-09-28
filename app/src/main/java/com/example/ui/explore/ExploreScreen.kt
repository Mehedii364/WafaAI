package com.example.ui.explore

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.NeumorphicCard
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.IndigoAccent
import com.example.ui.theme.PurpleAccent

data class PromptCategory(
    val title: String,
    val icon: ImageVector,
    val items: List<PromptItem>
)

data class PromptItem(
    val title: String,
    val prompt: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen(
    onSelectPrompt: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val categories = listOf(
        PromptCategory(
            title = "Programming & Android",
            icon = Icons.Default.Code,
            items = listOf(
                PromptItem("Jetpack Compose StateFlow", "Write a clean Kotlin Jetpack Compose MVVM screen using StateFlow, ViewModel, and Material 3."),
                PromptItem("Room Database Entity & DAO", "Show me how to define a Room database entity with foreign keys and a reactive DAO returning Flow."),
                PromptItem("API Key Failover Engine", "Explain the architectural pattern for client-side API key rotation with cooldowns and circuit breakers in Kotlin.")
            )
        ),
        PromptCategory(
            title = "বাংলা সাহিত্য ও অনুবাদ (Bangla)",
            icon = Icons.Default.Language,
            items = listOf(
                PromptItem("বাংলা প্রবন্ধ রচনা", "কৃত্রিম বুদ্ধিমত্তা এবং বাংলাদেশের ভবিষ্যৎ প্রযুক্তি সম্ভাবনা নিয়ে একটি চমৎকার বিশ্লেষণমূলক প্রবন্ধ লিখুন।"),
                PromptItem("কাব্য ও সাহিত্যিক ভাবার্থ", "কাজী নজরুল ইসলামের 'বিদ্রোহী' কবিতার মূল চেতনা ও বিপ্লবী দর্শন সংক্ষেপে ব্যাখ্যা করুন।"),
                PromptItem("অনুবাদ সহায়তা", "একটি ইংরেজি ব্যবসায়িক প্রস্তাবনার পেশাদার ও প্রাতিষ্ঠানিক বাংলা অনুবাদ প্রস্তুত করুন।")
            )
        ),
        PromptCategory(
            title = "Professional & Business",
            icon = Icons.Default.TrendingUp,
            items = listOf(
                PromptItem("Executive Summary", "Draft a compelling executive summary for an AI startup offering high-availability multi-model solutions."),
                PromptItem("Customer Support Template", "Create a polite, empathetic response to a client reporting an unexpected service outage."),
                PromptItem("Cold Outreach Email", "Write a short, engaging 3-sentence email pitching a tech partnership.")
            )
        ),
        PromptCategory(
            title = "العربية والترجمة (Arabic)",
            icon = Icons.Default.School,
            items = listOf(
                PromptItem("قواعد اللغة العربية", "اشرح الفرق بين النعت والحال في اللغة العربية مع أمثلة واضحة."),
                PromptItem("رسالة رسمية", "اكتب مسودة بريد إلكتروني رسمي لطلب استشارة تقنية لشركة ذكاء اصطناعي."),
                PromptItem("حكمة عربية", "اذكر ثلاث حكم عربية شهيرة مع شرح أثرها في الحياة اليومية.")
            )
        )
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.nav_explore),
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
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item {
                NeumorphicCard(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = CyanAccent,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Wafa AI Prompt Explorer",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Tap any prompt to immediately test with real OpenRouter AI intelligence and key rotation.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            items(categories) { category ->
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                    ) {
                        Icon(
                            imageVector = category.icon,
                            contentDescription = null,
                            tint = CyanAccent,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = category.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    category.items.forEach { item ->
                        Surface(
                            onClick = { onSelectPrompt(item.prompt) },
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.title,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = item.prompt,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 2
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = CyanAccent,
                                    modifier = Modifier.size(16.dp).padding(start = 8.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
