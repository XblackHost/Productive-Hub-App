package com.example.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ReflectionRepository
import com.example.ui.theme.*

@Composable
fun ReflectionScreen(context: Context) {
    val reflections by ReflectionRepository.reflectionsFlow.collectAsState()
    val completedCount = ReflectionRepository.calculateCompletedDays()
    val avgRating = ReflectionRepository.calculateAverageRating()

    var showPreviewDialog by remember { mutableStateOf(false) }

    // Form inputs for today's reflection
    val nextDay = remember(reflections) { ReflectionRepository.getNextDayNumberToRecord() }
    var selectedDay by remember { mutableStateOf(nextDay) }
    var rating by remember { mutableStateOf(8f) }
    var reflectionText by remember { mutableStateOf("") }
    var highlights by remember { mutableStateOf("") }
    var challenges by remember { mutableStateOf("") }

    // Keep selectedDay synced when reflections update
    LaunchedEffect(nextDay) {
        selectedDay = nextDay
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Progress Card with Xbox Neon Style
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = XboxDarkSurface),
            border = BorderStroke(1.dp, StatusInfoCyan.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "30-DAY GROWTH CYCLE",
                            style = MaterialTheme.typography.labelSmall,
                            color = StatusInfoCyan,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        )
                        Text(
                            text = "Self-Reflection",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = XboxTextPrimary
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = StatusInfoCyan.copy(alpha = 0.2f),
                        border = BorderStroke(1.dp, StatusInfoCyan)
                    ) {
                        Text(
                            text = "DAY $completedCount / 30",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = StatusInfoCyan,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }

                Text(
                    text = "A dedicated 30-day journaling journey. Rate each day 1–10, log your mindset, and generate an AI-ready .txt assessment on Day 30.",
                    style = MaterialTheme.typography.bodySmall,
                    color = XboxTextSecondary
                )

                // Progress Bar
                LinearProgressIndicator(
                    progress = { completedCount.toFloat() / 30f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = StatusInfoCyan,
                    trackColor = XboxDarkSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "$completedCount of 30 days recorded",
                        style = MaterialTheme.typography.bodySmall,
                        color = XboxTextSecondary
                    )
                    Text(
                        text = "Avg Rating: ${if (avgRating > 0) "$avgRating / 10" else "N/A"}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = StatusInfoCyan
                    )
                }
            }
        }

        // AI 30-Day Report Generator Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = XboxDarkSurface),
            border = BorderStroke(1.dp, XboxOutline)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = StatusInfoCyan)
                    Column {
                        Text(
                            text = "AI-Ready Growth Report (.txt)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = XboxTextPrimary
                        )
                        Text(
                            text = "Formatted for ChatGPT, Claude & Gemini",
                            style = MaterialTheme.typography.bodySmall,
                            color = XboxTextSecondary
                        )
                    }
                }

                Text(
                    text = "Compiles all 30 days of ratings and reflections into a clean .txt file with an evaluation prompt asking the AI to analyze your emotional patterns, consistency, and next steps.",
                    style = MaterialTheme.typography.bodySmall,
                    color = XboxTextSecondary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { ReflectionRepository.shareExportText(context) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = StatusInfoCyan, contentColor = XboxBlack)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Export / Share .txt", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = { showPreviewDialog = true },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, XboxOutline)
                    ) {
                        Text("Preview Report", color = XboxTextPrimary, fontSize = 12.sp)
                    }
                }
            }
        }

        // Daily Reflection Input Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = XboxDarkSurface),
            border = BorderStroke(1.dp, XboxOutline)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Log Day $selectedDay Reflection",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = XboxTextPrimary
                    )

                    // Day selection quick dropdown/stepper
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        IconButton(
                            onClick = { if (selectedDay > 1) selectedDay-- },
                            enabled = selectedDay > 1
                        ) {
                            Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Day", tint = XboxTextSecondary)
                        }
                        Text("Day $selectedDay", color = XboxNeonGreen, fontWeight = FontWeight.Bold)
                        IconButton(
                            onClick = { if (selectedDay < 30) selectedDay++ },
                            enabled = selectedDay < 30
                        ) {
                            Icon(Icons.Default.ChevronRight, contentDescription = "Next Day", tint = XboxTextSecondary)
                        }
                    }
                }

                // 1 to 10 Rating Slider
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Rate Today: ${rating.toInt()} / 10",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = XboxTextPrimary
                        )

                        val moodLabel = when (rating.toInt()) {
                            in 1..3 -> "🌧️ Tough Day"
                            in 4..5 -> "⛅ Fair / Challenging"
                            in 6..7 -> "🌤️ Steady Progress"
                            in 8..9 -> "⚡ High Performance"
                            else -> "🌟 Peak Day"
                        }
                        Text(moodLabel, style = MaterialTheme.typography.bodySmall, color = StatusInfoCyan)
                    }

                    Slider(
                        value = rating,
                        onValueChange = { rating = it },
                        valueRange = 1f..10f,
                        steps = 8,
                        colors = SliderDefaults.colors(
                            thumbColor = StatusInfoCyan,
                            activeTrackColor = StatusInfoCyan,
                            inactiveTrackColor = XboxDarkSurfaceVariant
                        )
                    )
                }

                // Reflection Text
                OutlinedTextField(
                    value = reflectionText,
                    onValueChange = { reflectionText = it },
                    label = { Text("What happened today? How did you feel?") },
                    minLines = 4,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = StatusInfoCyan,
                        unfocusedBorderColor = XboxOutline
                    )
                )

                // Key Win
                OutlinedTextField(
                    value = highlights,
                    onValueChange = { highlights = it },
                    label = { Text("Key Win / Breakthrough") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = StatusInfoCyan,
                        unfocusedBorderColor = XboxOutline
                    )
                )

                // Struggle
                OutlinedTextField(
                    value = challenges,
                    onValueChange = { challenges = it },
                    label = { Text("Challenge or Friction Point") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = StatusInfoCyan,
                        unfocusedBorderColor = XboxOutline
                    )
                )

                Button(
                    onClick = {
                        if (reflectionText.trim().isNotEmpty()) {
                            ReflectionRepository.saveReflection(
                                context = context,
                                dayNumber = selectedDay,
                                rating = rating.toInt(),
                                reflectionText = reflectionText,
                                highlights = highlights,
                                challenges = challenges
                            )
                            Toast.makeText(context, "Day $selectedDay reflection saved!", Toast.LENGTH_SHORT).show()
                            reflectionText = ""
                            highlights = ""
                            challenges = ""
                        } else {
                            Toast.makeText(context, "Please enter your reflection thoughts.", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = StatusInfoCyan, contentColor = XboxBlack)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Save Day $selectedDay Reflection", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Completed Reflections History
        if (reflections.isNotEmpty()) {
            Text(
                text = "Recorded Days (${reflections.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = XboxTextPrimary
            )

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                reflections.forEach { entry ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = XboxDarkSurface),
                        border = BorderStroke(1.dp, XboxOutline)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = StatusInfoCyan.copy(alpha = 0.2f)
                                    ) {
                                        Text(
                                            text = "DAY ${entry.dayNumber}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = StatusInfoCyan,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    Text(entry.dateString, style = MaterialTheme.typography.bodySmall, color = XboxTextSecondary)
                                }

                                Surface(
                                    shape = CircleShape,
                                    color = XboxDarkSurfaceVariant,
                                    border = BorderStroke(1.dp, StatusInfoCyan.copy(alpha = 0.4f))
                                ) {
                                    Text(
                                        text = "${entry.rating} / 10",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = StatusInfoCyan,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Text(
                                text = entry.reflectionText,
                                style = MaterialTheme.typography.bodyMedium,
                                color = XboxTextPrimary
                            )

                            if (entry.highlights.isNotEmpty()) {
                                Text(
                                    text = "Win: ${entry.highlights}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = XboxNeonGreen
                                )
                            }
                            if (entry.challenges.isNotEmpty()) {
                                Text(
                                    text = "Challenge: ${entry.challenges}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = StatusWarningAmber
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // ==========================================
    // PREVIEW REPORT DIALOG
    // ==========================================

    if (showPreviewDialog) {
        val clipboardManager = LocalClipboardManager.current
        val reportText = remember { ReflectionRepository.generateAiExportText() }

        AlertDialog(
            onDismissRequest = { showPreviewDialog = false },
            containerColor = XboxDarkSurface,
            title = { Text("AI Evaluation Report (.txt)", color = XboxTextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        "This report includes all your reflections along with an expert analysis prompt. You can copy it directly into ChatGPT, Claude, or Gemini.",
                        style = MaterialTheme.typography.bodySmall,
                        color = XboxTextSecondary
                    )

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = XboxDarkSurfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = reportText,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = StatusInfoCyan,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(reportText))
                        Toast.makeText(context, "Full report copied to clipboard!", Toast.LENGTH_SHORT).show()
                        showPreviewDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusInfoCyan, contentColor = XboxBlack)
                ) {
                    Text("Copy Report")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPreviewDialog = false }) { Text("Close", color = XboxTextSecondary) }
            }
        )
    }
}
