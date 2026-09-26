package com.example.ui.screens

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import com.example.AppSettings
import com.example.DiaryRepository
import com.example.FocusPreferences
import com.example.GradesRepository
import com.example.HatifSecurityManager
import com.example.PomodoroState
import com.example.ReflectionRepository
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    context: Context,
    onNavigate: (Int) -> Unit // 0: Home, 1: Focus, 2: Diary, 3: Reflection, 4: Grades, 5: Settings, 6: Manual
) {
    val focusStats by FocusPreferences.statsFlow.collectAsState()
    val dailyUsage by FocusPreferences.dailyUsageFlow.collectAsState()
    val isDiaryUnlocked by HatifSecurityManager.isDiaryUnlocked.collectAsState()
    val diaryEntries by DiaryRepository.entriesFlow.collectAsState()
    val pomodoroState by PomodoroState.stateFlow.collectAsState()
    val courses by GradesRepository.coursesFlow.collectAsState()
    val settingsState by AppSettings.settingsState.collectAsState()

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
        // Hero Header Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
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
                            text = "PRODUCTIVE HUB",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        )
                        Text(
                            text = "Command Center",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    if (settingsState.featureShortsBlockerEnabled) {
                        Surface(
                            shape = CircleShape,
                            color = if (focusStats.isBlockingEnabled) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else StatusErrorRed.copy(alpha = 0.15f),
                            border = BorderStroke(1.5.dp, if (focusStats.isBlockingEnabled) MaterialTheme.colorScheme.primary else StatusErrorRed)
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
                                        .background(if (focusStats.isBlockingEnabled) MaterialTheme.colorScheme.primary else StatusErrorRed)
                                )
                                Text(
                                    text = if (focusStats.isBlockingEnabled) "SHIELD ACTIVE" else "SHIELD OFF",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (focusStats.isBlockingEnabled) MaterialTheme.colorScheme.primary else StatusErrorRed,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }

                Text(
                    text = "All-in-one personal workspace for focused productivity, habit building, academic performance, and mindful attention.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )

                // Quick metrics row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (settingsState.featureShortsBlockerEnabled) {
                        QuickMetricPill(
                            modifier = Modifier.weight(1f),
                            label = "Blocks Today",
                            value = "${focusStats.todayBlocks}",
                            accent = MaterialTheme.colorScheme.primary
                        )
                    }
                    if (settingsState.featureReflectionEnabled) {
                        QuickMetricPill(
                            modifier = Modifier.weight(1f),
                            label = "30D Reflection",
                            value = "$completedDays/30d",
                            accent = StatusInfoCyan
                        )
                    }
                    if (settingsState.featureGradesEnabled) {
                        QuickMetricPill(
                            modifier = Modifier.weight(1f),
                            label = "School Avg",
                            value = if (avgScore > 0.0) "$avgScore%" else "N/A",
                            accent = StatusPurple
                        )
                    }
                }
            }
        }

        // Weekly Progress Analytics Card (Canvas Chart & Insights)
        if (settingsState.featureShortsBlockerEnabled) {
            WeeklyProgressCard(dailyUsage = dailyUsage)
        }

        // Module 1: Focus & Shorts Blocker Quick Card
        if (settingsState.featureShortsBlockerEnabled) {
            ModuleHubCard(
                title = "Focus & Shorts Blocker",
                subtitle = if (focusStats.isBlockingEnabled) "YouTube & Instagram auto-exit active" else "Distraction blocker disabled",
                tag = "Active Shield",
                icon = Icons.Default.Shield,
                accentColor = MaterialTheme.colorScheme.primary,
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
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${focusStats.totalBlocks} total rescues",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Module 2: Pomodoro Focus Timer Quick Card
        if (settingsState.featurePomodoroEnabled) {
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
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Button(
                        onClick = {
                            if (pomodoroState.isRunning) PomodoroState.pause() else PomodoroState.start()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (pomodoroState.isRunning) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primary,
                            contentColor = if (pomodoroState.isRunning) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Text(if (pomodoroState.isRunning) "Pause" else "Start 25m")
                    }
                }
            }
        }

        // Module 3: Encrypted Diary Quick Card
        if (settingsState.featureDiaryEnabled) {
            ModuleHubCard(
                title = "Private Diary",
                subtitle = if (isDiaryUnlocked) "${diaryEntries.size} entries · Unlocked" else "Locked · AES-256 KeyStore protected",
                tag = if (isDiaryUnlocked) "UNLOCKED" else "LOCKED",
                icon = Icons.Default.Lock,
                accentColor = if (isDiaryUnlocked) MaterialTheme.colorScheme.primary else StatusWarningAmber,
                onClick = { onNavigate(2) }
            ) {
                Text(
                    text = if (isDiaryUnlocked) "Vault open. Tap to view and write confidential entries." else "Tap to authenticate with fingerprint or secret password.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Module 4: 30-Day Self-Reflection Quick Card
        if (settingsState.featureReflectionEnabled) {
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
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }

        // Module 5: Academic Grades & Study Tracker
        if (settingsState.featureGradesEnabled) {
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
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Module 6: User Manual & Migration Guide
        ModuleHubCard(
            title = "User Manual & Migration",
            subtitle = "Zero-loss device transfer, battery optimization & recovery",
            tag = "HANDBOOK",
            icon = Icons.Default.MenuBook,
            accentColor = MaterialTheme.colorScheme.primary,
            onClick = { onNavigate(6) }
        ) {
            Text(
                text = "Detailed guide on transferring to a new phone without packet loss, battery conservation secrets, and Admin Lock recovery.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun WeeklyProgressCard(
    dailyUsage: Map<String, FocusPreferences.DailyUsage>
) {
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US) }
    val dayLabels = remember {
        val list = mutableListOf<String>()
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -6)
        for (i in 0..6) {
            val d = SimpleDateFormat("E", Locale.US).format(cal.time).take(1)
            list.add(d)
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        list
    }

    val past14Dates = remember {
        val list = mutableListOf<String>()
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -13)
        for (i in 0..13) {
            list.add(dateFormat.format(cal.time))
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        list
    }

    val lastWeekDates = past14Dates.subList(0, 7)
    val thisWeekDates = past14Dates.subList(7, 14)

    val lastWeekSeconds = lastWeekDates.sumOf { (dailyUsage[it]?.ytSeconds ?: 0L) + (dailyUsage[it]?.igSeconds ?: 0L) }
    val thisWeekSeconds = thisWeekDates.sumOf { (dailyUsage[it]?.ytSeconds ?: 0L) + (dailyUsage[it]?.igSeconds ?: 0L) }

    val thisWeekBlocks = thisWeekDates.sumOf { dailyUsage[it]?.blocks ?: 0 }
    val last7DayBlocks = thisWeekDates.map { dailyUsage[it]?.blocks ?: 0 }

    // Week-over-week calculation
    val percentText: String
    val percentColor: Color
    if (lastWeekSeconds > 0) {
        val diff = (((thisWeekSeconds - lastWeekSeconds).toDouble() / lastWeekSeconds) * 100).toInt()
        if (diff <= 0) {
            percentText = "↓ ${kotlin.math.abs(diff)}% vs last week"
            percentColor = StatusActiveGreen
        } else {
            percentText = "↑ ${diff}% vs last week"
            percentColor = StatusErrorRed
        }
    } else {
        if (thisWeekSeconds == 0L) {
            percentText = "0% vs last week"
            percentColor = StatusActiveGreen
        } else {
            percentText = "New activity this week"
            percentColor = MaterialTheme.colorScheme.primary
        }
    }

    // Scroll time saved insight
    val savedMinutes = thisWeekBlocks * 12
    val savedHours = savedMinutes / 60
    val savedMinsRemaining = savedMinutes % 60
    val insightText = if (savedMinutes > 0) {
        if (savedHours > 0) "You saved ~${savedHours}h ${savedMinsRemaining}m of scroll time this week."
        else "You saved ~${savedMinutes}m of scroll time this week."
    } else {
        "Stay intentional with your daily screen time."
    }

    val totalRecordedDays = dailyUsage.keys.size
    val firstDate = dailyUsage.keys.sorted().firstOrNull() ?: SimpleDateFormat("MMM d", Locale.US).format(Date())
    val daysRemainingToUnlock = (7 - totalRecordedDays).coerceAtLeast(0)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f, fill = false).padding(end = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.TrendingUp,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = "Your Progress",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Weekly distraction analytics",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = percentColor.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, percentColor.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = percentText,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = percentColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 11.sp
                    )
                }
            }

            // 7-day Bar Chart
            val barColor = MaterialTheme.colorScheme.primary
            val emptyBarColor = MaterialTheme.colorScheme.surfaceVariant
            val maxBlocks = (last7DayBlocks.maxOrNull() ?: 1).coerceAtLeast(1)

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp)
                ) {
                    val count = 7
                    val spacing = 12.dp.toPx()
                    val totalSpacing = spacing * (count - 1)
                    val barWidth = ((size.width - totalSpacing) / count).coerceAtLeast(8.dp.toPx())
                    val maxHeight = size.height - 4.dp.toPx()

                    for (i in 0 until count) {
                        val blocks = last7DayBlocks.getOrElse(i) { 0 }
                        val x = i * (barWidth + spacing)
                        val barHeight = if (blocks > 0) {
                            (blocks.toFloat() / maxBlocks.toFloat() * maxHeight).coerceAtLeast(6.dp.toPx())
                        } else {
                            4.dp.toPx()
                        }
                        val y = size.height - barHeight

                        // Background pillar
                        drawRoundRect(
                            color = emptyBarColor,
                            topLeft = androidx.compose.ui.geometry.Offset(x, 0f),
                            size = androidx.compose.ui.geometry.Size(barWidth, size.height),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx(), 6.dp.toPx())
                        )

                        // Filled active bar
                        if (blocks > 0) {
                            drawRoundRect(
                                color = barColor,
                                topLeft = androidx.compose.ui.geometry.Offset(x, y),
                                size = androidx.compose.ui.geometry.Size(barWidth, barHeight),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx(), 6.dp.toPx())
                            )
                        }
                    }
                }

                // Day labels
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    dayLabels.forEach { label ->
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            // Insight line
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = insightText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            if (totalRecordedDays < 7) {
                Text(
                    text = "Tracking since $firstDate. Full insights unlock in $daysRemainingToUnlock days.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
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
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
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
                color = MaterialTheme.colorScheme.onSurfaceVariant,
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
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
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
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
