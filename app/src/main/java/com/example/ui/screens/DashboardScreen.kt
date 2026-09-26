package com.example.ui.screens

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.DiaryRepository
import com.example.FocusPreferences
import com.example.GradesRepository
import com.example.HatifSecurityManager
import com.example.PomodoroState
import com.example.ReflectionRepository
import com.example.ui.theme.*

@Composable
fun DashboardScreen(
    context: Context,
    onNavigate: (Int) -> Unit // 0: Home, 1: Focus, 2: Diary, 3: Reflection, 4: Grades, 5: Settings
) {
    val focusStats by FocusPreferences.statsFlow.collectAsState()
    val isDiaryUnlocked by HatifSecurityManager.isDiaryUnlocked.collectAsState()
    val diaryEntries by DiaryRepository.entriesFlow.collectAsState()
    val reflections by ReflectionRepository.reflectionsFlow.collectAsState()
    val pomodoroState by PomodoroState.stateFlow.collectAsState()
    val courses by GradesRepository.coursesFlow.collectAsState()

    val completedDays = ReflectionRepository.calculateCompletedDays()
    val avgRating = ReflectionRepository.calculateAverageRating()
    val avgScore = GradesRepository.calculateAveragePercent()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Header Card with Xbox Glow
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = XboxDarkSurface),
            border = BorderStroke(1.5.dp, XboxNeonGreen.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f, fill = false).padding(end = 8.dp)) {
                        Text(
                            text = "HATIF WORKSPACE",
                            style = MaterialTheme.typography.labelSmall,
                            color = XboxNeonGreen,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        )
                        Text(
                            text = "Command Center",
                            style = MaterialTheme.typography.titleLarge,
                            color = XboxTextPrimary,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    Surface(
                        shape = CircleShape,
                        color = if (focusStats.isBlockingEnabled) XboxGreen.copy(alpha = 0.25f) else Color(0xFF2E1C1C),
                        border = BorderStroke(1.5.dp, if (focusStats.isBlockingEnabled) XboxNeonGreen else StatusErrorRed)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (focusStats.isBlockingEnabled) XboxNeonGreen else StatusErrorRed)
                            )
                            Text(
                                text = if (focusStats.isBlockingEnabled) "SHIELD ACTIVE" else "SHIELD OFF",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (focusStats.isBlockingEnabled) XboxNeonGreen else StatusErrorRed,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        }
                    }
                }

                Text(
                    text = "Private, hardware-encrypted personal environment for focus, reflection, academic tracking, and distraction elimination.",
                    style = MaterialTheme.typography.bodySmall,
                    color = XboxTextSecondary,
                    lineHeight = 18.sp
                )

                // Quick metrics row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuickMetricPill(
                        modifier = Modifier.weight(1f),
                        label = "Blocks Today",
                        value = "${focusStats.todayBlocks}",
                        accent = XboxNeonGreen
                    )
                    QuickMetricPill(
                        modifier = Modifier.weight(1f),
                        label = "30D Reflection",
                        value = "$completedDays/30d",
                        accent = StatusInfoCyan
                    )
                    QuickMetricPill(
                        modifier = Modifier.weight(1f),
                        label = "School Avg",
                        value = if (avgScore > 0.0) "$avgScore%" else "N/A",
                        accent = StatusPurple
                    )
                }
            }
        }

        // Module 1: Focus & Shorts Blocker Quick Card
        ModuleHubCard(
            title = "Focus & Shorts Blocker",
            subtitle = if (focusStats.isBlockingEnabled) "YouTube & Instagram auto-exit active" else "Distraction blocker disabled",
            tag = "Focus Hatif Engine",
            icon = Icons.Default.Shield,
            accentColor = XboxNeonGreen,
            onClick = { onNavigate(1) }
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Daily limit: ${if (focusStats.youtubeLimitMinutes == 0) "Strict 0m" else "${focusStats.youtubeLimitMinutes}m"} (YT) / ${if (focusStats.instagramLimitMinutes == 0) "Strict 0m" else "${focusStats.instagramLimitMinutes}m"} (IG)",
                    style = MaterialTheme.typography.bodySmall,
                    color = XboxTextSecondary
                )
                Text(
                    text = "${focusStats.totalBlocks} total rescues",
                    style = MaterialTheme.typography.bodySmall,
                    color = XboxNeonGreen,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // Module 2: Pomodoro Focus Timer Quick Card
        ModuleHubCard(
            title = "Focus Session (Pomodoro)",
            subtitle = if (pomodoroState.isRunning) "Session running · ${pomodoroState.formattedTime} left" else "25 min focus interval",
            tag = "${pomodoroState.completedPomodoros} Completed",
            icon = Icons.Default.HourglassTop,
            accentColor = StatusWarningAmber,
            onClick = { onNavigate(1) }
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = pomodoroState.formattedTime,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = XboxTextPrimary
                )
                Button(
                    onClick = {
                        if (pomodoroState.isRunning) PomodoroState.pause() else PomodoroState.start()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (pomodoroState.isRunning) XboxDarkSurfaceVariant else XboxNeonGreen,
                        contentColor = if (pomodoroState.isRunning) XboxTextPrimary else XboxBlack
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text(if (pomodoroState.isRunning) "Pause" else "Start 25m")
                }
            }
        }

        // Module 3: Encrypted Diary Quick Card
        ModuleHubCard(
            title = "Private Diary",
            subtitle = if (isDiaryUnlocked) "${diaryEntries.size} entries · Unlocked" else "Locked · AES-256 KeyStore protected",
            tag = if (isDiaryUnlocked) "UNLOCKED" else "LOCKED",
            icon = Icons.Default.Lock,
            accentColor = if (isDiaryUnlocked) XboxNeonGreen else StatusWarningAmber,
            onClick = { onNavigate(2) }
        ) {
            Text(
                text = if (isDiaryUnlocked) "Vault open. Tap to view and write confidential entries." else "Tap to authenticate with fingerprint or secret password.",
                style = MaterialTheme.typography.bodySmall,
                color = XboxTextSecondary
            )
        }

        // Module 4: 30-Day Self-Reflection Quick Card
        ModuleHubCard(
            title = "30-Day Self-Reflection",
            subtitle = "Day $completedDays of 30 · Average Score: ${if (avgRating > 0) "$avgRating/10" else "No entries"}",
            tag = "AI .TXT READY",
            icon = Icons.Default.Psychology,
            accentColor = StatusInfoCyan,
            onClick = { onNavigate(3) }
        ) {
            LinearProgressIndicator(
                progress = { completedDays.toFloat() / 30f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = StatusInfoCyan,
                trackColor = XboxDarkSurfaceVariant
            )
        }

        // Module 5: High School Academic Grades & Study Tracker
        ModuleHubCard(
            title = "Academic Grades",
            subtitle = "${courses.size} subjects tracked · Goal progress",
            tag = if (avgScore > 0.0) "$avgScore%" else "TRACKING",
            icon = Icons.Default.School,
            accentColor = StatusPurple,
            onClick = { onNavigate(4) }
        ) {
            Text(
                text = "Track subject percentages, target goals, and study time completion.",
                style = MaterialTheme.typography.bodySmall,
                color = XboxTextSecondary
            )
        }

        // Module 6: User Manual & Migration Guide
        ModuleHubCard(
            title = "User Manual & Migration",
            subtitle = "Zero-loss device transfer, battery optimization & recovery",
            tag = "HANDBOOK",
            icon = Icons.Default.MenuBook,
            accentColor = XboxNeonGreen,
            onClick = { onNavigate(6) }
        ) {
            Text(
                text = "Detailed guide on transferring to a new phone without packet loss, battery conservation secrets, and Admin Lock recovery.",
                style = MaterialTheme.typography.bodySmall,
                color = XboxTextSecondary
            )
        }

        // Privacy Guarantee Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = XboxDarkSurfaceVariant),
            border = BorderStroke(1.dp, XboxOutline)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.VerifiedUser,
                    contentDescription = "Privacy Verified",
                    tint = XboxNeonGreen,
                    modifier = Modifier.size(28.dp)
                )
                Column {
                    Text(
                        text = "100% Offline & Private",
                        style = MaterialTheme.typography.titleSmall,
                        color = XboxTextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Zero internet permissions. Zero tracking. Hardware KeyStore cryptography. Your data never leaves your device.",
                        style = MaterialTheme.typography.bodySmall,
                        color = XboxTextSecondary
                    )
                }
            }
        }
    }
}

@Composable
fun QuickMetricPill(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    accent: Color
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = XboxDarkSurfaceVariant,
        border = BorderStroke(1.dp, XboxOutline)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = accent
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = XboxTextSecondary,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
fun ModuleHubCard(
    title: String,
    subtitle: String,
    tag: String,
    icon: ImageVector,
    accentColor: Color,
    onClick: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = XboxDarkSurface),
        border = BorderStroke(1.dp, XboxOutline)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f, fill = false).padding(end = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = accentColor.copy(alpha = 0.15f),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = icon,
                                contentDescription = title,
                                tint = accentColor,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = XboxTextPrimary
                        )
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = XboxTextSecondary
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = accentColor.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, accentColor.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = tag,
                        style = MaterialTheme.typography.labelSmall,
                        color = accentColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            content()
        }
    }
}
