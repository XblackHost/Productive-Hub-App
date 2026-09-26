package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings
import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.FocusPreferences
import com.example.PomodoroState
import com.example.ShortsReelsBlockerService
import com.example.ui.theme.*

@Composable
fun FocusScreen(context: Context) {
    val stats by FocusPreferences.statsFlow.collectAsState()
    val pomodoroState by PomodoroState.stateFlow.collectAsState()

    var isAccessibilityActive by remember { mutableStateOf(false) }
    var isIgnoringBattery by remember { mutableStateOf(false) }

    // Dialog States
    var showAdminPinDialog by remember { mutableStateOf(false) }
    var showAdminSetupDialog by remember { mutableStateOf(false) }
    var showAdminManageDialog by remember { mutableStateOf(false) }
    var showAdminRecoveryDialog by remember { mutableStateOf(false) }
    var pendingAction by remember { mutableStateOf<(() -> Unit)?>(null) }

    LaunchedEffect(Unit) {
        isAccessibilityActive = ShortsReelsBlockerService.isServiceRunning(context)
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
        isIgnoringBattery = powerManager?.isIgnoringBatteryOptimizations(context.packageName) ?: false
    }

    // Helper to guard action behind Admin Lock if active
    val performProtectedAction: (() -> Unit) -> Unit = { action ->
        if (stats.isAdminLockEnabled) {
            pendingAction = action
            showAdminPinDialog = true
        } else {
            action()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Master Blocker Switch Card
        val switchBorderColor by animateColorAsState(
            targetValue = if (stats.isBlockingEnabled) XboxNeonGreen else XboxOutline,
            label = "border"
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = XboxDarkSurface),
            border = BorderStroke(1.dp, switchBorderColor)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = "Shield",
                                tint = if (stats.isBlockingEnabled) XboxNeonGreen else XboxTextSecondary
                            )
                            Text(
                                text = "Shorts & Reels Shield",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = XboxTextPrimary
                            )
                        }
                        Text(
                            text = if (stats.isBlockingEnabled)
                                "Enforcing custom timers & auto-exit on YouTube Shorts and Instagram Reels."
                            else
                                "Distraction shield is paused. YouTube & Instagram are unmonitored.",
                            style = MaterialTheme.typography.bodySmall,
                            color = XboxTextSecondary,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    Switch(
                        checked = stats.isBlockingEnabled,
                        onCheckedChange = { newValue ->
                            performProtectedAction {
                                FocusPreferences.setBlockingEnabled(context, newValue)
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = XboxBlack,
                            checkedTrackColor = XboxNeonGreen,
                            uncheckedThumbColor = XboxTextSecondary,
                            uncheckedTrackColor = XboxDarkSurfaceVariant
                        )
                    )
                }

                if (stats.isAdminLockEnabled) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = XboxDarkGreen.copy(alpha = 0.5f),
                        border = BorderStroke(1.dp, XboxNeonGreen.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Locked",
                                tint = XboxNeonGreen,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Admin Lock Active: Shield switch and timer limits protected by peer.",
                                style = MaterialTheme.typography.bodySmall,
                                color = XboxLightGreen,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        // Peer Admin Lock Management Card
        AdminLockCard(
            stats = stats,
            onSetupClick = { showAdminSetupDialog = true },
            onManageClick = {
                pendingAction = { showAdminManageDialog = true }
                showAdminPinDialog = true
            },
            onEmergencyRecoveryClick = { showAdminRecoveryDialog = true }
        )

        // Daily Allowance Timer Limits
        DailyTimerAllowanceCard(
            stats = stats,
            onUpdateYt = { newMinutes ->
                performProtectedAction {
                    FocusPreferences.setYoutubeLimitMinutes(context, newMinutes)
                }
            },
            onUpdateIg = { newMinutes ->
                performProtectedAction {
                    FocusPreferences.setInstagramLimitMinutes(context, newMinutes)
                }
            },
            onResetToday = {
                performProtectedAction {
                    FocusPreferences.resetTodayUsage(context)
                    Toast.makeText(context, "Today's scroll usage reset to 0.", Toast.LENGTH_SHORT).show()
                }
            }
        )

        // Rescues & Activity Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = XboxDarkSurface),
            border = BorderStroke(1.dp, XboxOutline)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Focus Activity & Rescues",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = XboxTextPrimary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricBox(
                        modifier = Modifier.weight(1f),
                        title = "Blocks Today",
                        count = "${stats.todayBlocks}",
                        subtitle = "impulsive scrolls caught",
                        color = XboxNeonGreen
                    )
                    MetricBox(
                        modifier = Modifier.weight(1f),
                        title = "Total Rescues",
                        count = "${stats.totalBlocks}",
                        subtitle = "all-time time saves",
                        color = StatusInfoCyan
                    )
                }
            }
        }

        // Pomodoro Focus Timer Card
        PomodoroCard(
            state = pomodoroState,
            onStart = { PomodoroState.start() },
            onPause = { PomodoroState.pause() },
            onReset = { PomodoroState.reset() },
            onSetMode = { isBreak, minutes -> PomodoroState.setMode(isBreak, minutes) }
        )

        // Infinix Hot 60i / XOS 15 & Accessibility Status Card
        SystemSetupCard(
            context = context,
            isAccessibilityActive = isAccessibilityActive,
            isIgnoringBattery = isIgnoringBattery
        )
    }

    // ==========================================
    // ADMIN LOCK DIALOGS
    // ==========================================

    if (showAdminPinDialog) {
        AdminPinDialog(
            context = context,
            onDismiss = {
                showAdminPinDialog = false
                pendingAction = null
            },
            onSuccess = {
                showAdminPinDialog = false
                pendingAction?.invoke()
                pendingAction = null
            },
            onForgotPassword = {
                showAdminPinDialog = false
                pendingAction = null
                showAdminRecoveryDialog = true
            }
        )
    }

    if (showAdminSetupDialog) {
        AdminSetupDialog(
            context = context,
            onDismiss = { showAdminSetupDialog = false },
            onSuccess = {
                showAdminSetupDialog = false
                Toast.makeText(context, "Admin Lock successfully activated!", Toast.LENGTH_LONG).show()
            }
        )
    }

    if (showAdminManageDialog) {
        AdminManageDialog(
            context = context,
            stats = stats,
            onDismiss = { showAdminManageDialog = false }
        )
    }

    if (showAdminRecoveryDialog) {
        AdminRecoveryDialog(
            context = context,
            stats = stats,
            onDismiss = { showAdminRecoveryDialog = false }
        )
    }
}

// ==========================================
// SUB-COMPONENTS
// ==========================================

@Composable
fun AdminLockCard(
    stats: FocusPreferences.FocusStats,
    onSetupClick: () -> Unit,
    onManageClick: () -> Unit,
    onEmergencyRecoveryClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = XboxDarkSurface),
        border = BorderStroke(1.dp, if (stats.isAdminLockEnabled) XboxGreen else XboxOutline)
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = if (stats.isAdminLockEnabled) Icons.Default.Lock else Icons.Default.LockOpen,
                        contentDescription = "Admin Lock",
                        tint = if (stats.isAdminLockEnabled) XboxNeonGreen else XboxTextSecondary
                    )
                    Column {
                        Text(
                            text = if (stats.isAdminLockEnabled) "Peer Admin Lock Active" else "Peer Admin Lock (Anti-Relapse)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = XboxTextPrimary
                        )
                        Text(
                            text = if (stats.isAdminLockEnabled)
                                "Protected by your trusted friend/parent"
                            else
                                "Have a peer set a password so you can't bypass limits",
                            style = MaterialTheme.typography.bodySmall,
                            color = XboxTextSecondary
                        )
                    }
                }
            }

            if (!stats.isAdminLockEnabled) {
                Button(
                    onClick = onSetupClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = XboxGreen, contentColor = XboxTextPrimary)
                ) {
                    Icon(Icons.Default.VpnKey, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Set Up Admin Lock", fontWeight = FontWeight.Bold)
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onManageClick,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = XboxDarkSurfaceVariant, contentColor = XboxNeonGreen)
                    ) {
                        Text("Manage / Disable", fontWeight = FontWeight.SemiBold)
                    }
                    OutlinedButton(
                        onClick = onEmergencyRecoveryClick,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, XboxOutline)
                    ) {
                        Text("Recovery", color = XboxTextSecondary)
                    }
                }
            }
        }
    }
}

@Composable
fun DailyTimerAllowanceCard(
    stats: FocusPreferences.FocusStats,
    onUpdateYt: (Int) -> Unit,
    onUpdateIg: (Int) -> Unit,
    onResetToday: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = XboxDarkSurface),
        border = BorderStroke(1.dp, XboxOutline)
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
                Column {
                    Text(
                        text = "Daily Scrolling Allowance",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = XboxTextPrimary
                    )
                    Text(
                        text = "Auto-backs when daily limit expires",
                        style = MaterialTheme.typography.bodySmall,
                        color = XboxTextSecondary
                    )
                }

                TextButton(onClick = onResetToday) {
                    Text("Reset Today", color = XboxNeonGreen, style = MaterialTheme.typography.labelMedium)
                }
            }

            // YouTube Limit Section
            TimerRow(
                appName = "YouTube Shorts",
                icon = Icons.Default.PlayArrow,
                currentLimitMinutes = stats.youtubeLimitMinutes,
                usedSeconds = stats.youtubeUsedSeconds,
                onSelectLimit = onUpdateYt
            )

            HorizontalDivider(color = XboxOutline.copy(alpha = 0.5f))

            // Instagram Limit Section
            TimerRow(
                appName = "Instagram Reels",
                icon = Icons.Default.CameraAlt,
                currentLimitMinutes = stats.instagramLimitMinutes,
                usedSeconds = stats.instagramUsedSeconds,
                onSelectLimit = onUpdateIg
            )
        }
    }
}

@Composable
fun TimerRow(
    appName: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    currentLimitMinutes: Int,
    usedSeconds: Int,
    onSelectLimit: (Int) -> Unit
) {
    val usedMinutes = usedSeconds / 60
    val presetOptions = listOf(0, 5, 15, 30, 60)

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(imageVector = icon, contentDescription = appName, tint = XboxNeonGreen, modifier = Modifier.size(18.dp))
                Text(appName, style = MaterialTheme.typography.titleSmall, color = XboxTextPrimary, fontWeight = FontWeight.Bold)
            }
            Text(
                text = if (currentLimitMinutes == 0) "Strict 0m (Instant Exit)" else "$usedMinutes of $currentLimitMinutes mins used",
                style = MaterialTheme.typography.bodySmall,
                color = if (currentLimitMinutes == 0) XboxNeonGreen else XboxTextSecondary
            )
        }

        // Preset chips (Scrollable row for HD+ screens)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            presetOptions.forEach { mins ->
                val isSelected = currentLimitMinutes == mins
                FilterChip(
                    selected = isSelected,
                    onClick = { onSelectLimit(mins) },
                    label = {
                        Text(
                            text = if (mins == 0) "0m Strict" else "${mins}m",
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = XboxNeonGreen,
                        selectedLabelColor = XboxBlack,
                        containerColor = XboxDarkSurfaceVariant,
                        labelColor = XboxTextSecondary
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = XboxOutline,
                        selectedBorderColor = XboxNeonGreen
                    )
                )
            }
        }
    }
}

@Composable
fun PomodoroCard(
    state: PomodoroState.State,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onReset: () -> Unit,
    onSetMode: (Boolean, Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = XboxDarkSurface),
        border = BorderStroke(1.dp, XboxOutline)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (state.isBreak) "Break Time" else "Focus Interval",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = XboxTextPrimary
                    )
                    Text(
                        text = "${state.completedPomodoros} focus sessions completed today",
                        style = MaterialTheme.typography.bodySmall,
                        color = XboxTextSecondary
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (state.isBreak) StatusInfoCyan.copy(alpha = 0.2f) else XboxGreen.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = if (state.isBreak) "RECHARGE" else "DEEP WORK",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (state.isBreak) StatusInfoCyan else XboxNeonGreen,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Big Timer Display
            Text(
                text = state.formattedTime,
                style = MaterialTheme.typography.displayMedium,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.ExtraBold,
                color = if (state.isBreak) StatusInfoCyan else XboxNeonGreen
            )

            LinearProgressIndicator(
                progress = { state.progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (state.isBreak) StatusInfoCyan else XboxNeonGreen,
                trackColor = XboxDarkSurfaceVariant
            )

            // Controls
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = if (state.isRunning) onPause else onStart,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (state.isRunning) XboxDarkSurfaceVariant else XboxNeonGreen,
                        contentColor = if (state.isRunning) XboxTextPrimary else XboxBlack
                    )
                ) {
                    Icon(if (state.isRunning) Icons.Default.Pause else Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text(if (state.isRunning) "Pause" else "Start", fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onReset,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, XboxOutline)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Reset", tint = XboxTextSecondary)
                }
            }

            // Quick Interval Selectors
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                AssistChip(
                    modifier = Modifier.weight(1f),
                    onClick = { onSetMode(false, 25) },
                    label = { Text("25m Focus", fontSize = 10.sp, maxLines = 1) }
                )
                AssistChip(
                    modifier = Modifier.weight(1f),
                    onClick = { onSetMode(false, 50) },
                    label = { Text("50m Deep", fontSize = 10.sp, maxLines = 1) }
                )
                AssistChip(
                    modifier = Modifier.weight(1f),
                    onClick = { onSetMode(true, 5) },
                    label = { Text("5m Break", fontSize = 10.sp, maxLines = 1) }
                )
            }
        }
    }
}

@Composable
fun SystemSetupCard(
    context: Context,
    isAccessibilityActive: Boolean,
    isIgnoringBattery: Boolean
) {
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
            Text(
                text = "Infinix Hot 60i / XOS 15 Status",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = XboxTextPrimary
            )

            StatusItemRow(
                label = "Accessibility Blocker Service",
                isActive = isAccessibilityActive,
                activeDesc = "Active & monitoring Shorts & Reels",
                inactiveDesc = "Disabled in Android Settings",
                actionLabel = "Open Settings",
                onAction = {
                    val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                }
            )

            HorizontalDivider(color = XboxOutline.copy(alpha = 0.5f))

            StatusItemRow(
                label = "XOS Background Battery Policy",
                isActive = isIgnoringBattery,
                activeDesc = "Unrestricted (XOS won't kill service)",
                inactiveDesc = "Optimized (Service may sleep)",
                actionLabel = "App Info",
                onAction = {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                }
            )
        }
    }
}

@Composable
fun StatusItemRow(
    label: String,
    isActive: Boolean,
    activeDesc: String,
    inactiveDesc: String,
    actionLabel: String,
    onAction: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (isActive) XboxNeonGreen else StatusErrorRed)
                )
                Text(label, style = MaterialTheme.typography.titleSmall, color = XboxTextPrimary)
            }
            Text(
                text = if (isActive) activeDesc else inactiveDesc,
                style = MaterialTheme.typography.bodySmall,
                color = if (isActive) XboxLightGreen else StatusWarningAmber
            )
        }

        TextButton(onClick = onAction) {
            Text(actionLabel, color = XboxNeonGreen, fontSize = 12.sp)
        }
    }
}

@Composable
fun MetricBox(
    modifier: Modifier = Modifier,
    title: String,
    count: String,
    subtitle: String,
    color: Color
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = XboxDarkSurfaceVariant,
        border = BorderStroke(1.dp, XboxOutline)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(title, style = MaterialTheme.typography.labelSmall, color = XboxTextSecondary)
            Text(count, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, color = color)
            Text(subtitle, style = MaterialTheme.typography.labelSmall, color = XboxTextMuted, fontSize = 10.sp)
        }
    }
}

// ==========================================
// ADMIN DIALOG IMPLEMENTATIONS
// ==========================================

@Composable
fun AdminPinDialog(
    context: Context,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit,
    onForgotPassword: () -> Unit
) {
    var pin by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = XboxDarkSurface,
        title = { Text("Enter Admin Password", color = XboxTextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "This setting is locked by your trusted peer. Enter the admin password to proceed:",
                    style = MaterialTheme.typography.bodySmall,
                    color = XboxTextSecondary
                )
                OutlinedTextField(
                    value = pin,
                    onValueChange = {
                        pin = it
                        errorMsg = null
                    },
                    label = { Text("Admin Password") },
                    visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                    isError = errorMsg != null,
                    supportingText = { errorMsg?.let { Text(it, color = StatusErrorRed) } },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = XboxNeonGreen,
                        unfocusedBorderColor = XboxOutline
                    )
                )
                TextButton(onClick = onForgotPassword) {
                    Text("Forgot Password? Use Recovery", color = XboxNeonGreen, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (FocusPreferences.verifyAdminPin(context, pin)) {
                        onSuccess()
                    } else {
                        errorMsg = "Incorrect admin password."
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = XboxNeonGreen, contentColor = XboxBlack)
            ) {
                Text("Unlock")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = XboxTextSecondary) }
        }
    )
}

@Composable
fun AdminSetupDialog(
    context: Context,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    var pin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var question by remember { mutableStateOf("What was the name of your first childhood pet?") }
    var answer by remember { mutableStateOf("") }
    var recoveryKey by remember { mutableStateOf(FocusPreferences.generateNewRecoveryKey()) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    val clipboardManager = LocalClipboardManager.current

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = XboxDarkSurface,
        title = { Text("Peer Admin Lock Setup", color = XboxTextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    "Hand your phone to your trusted friend or parent. They will configure a secret password that you do not know.",
                    style = MaterialTheme.typography.bodySmall,
                    color = XboxTextSecondary
                )

                OutlinedTextField(
                    value = pin,
                    onValueChange = { pin = it },
                    label = { Text("Admin Password / PIN") },
                    visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = XboxNeonGreen, unfocusedBorderColor = XboxOutline)
                )

                OutlinedTextField(
                    value = confirmPin,
                    onValueChange = { confirmPin = it },
                    label = { Text("Confirm Password") },
                    visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = XboxNeonGreen, unfocusedBorderColor = XboxOutline)
                )

                OutlinedTextField(
                    value = question,
                    onValueChange = { question = it },
                    label = { Text("Security Question") },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = XboxNeonGreen, unfocusedBorderColor = XboxOutline)
                )

                OutlinedTextField(
                    value = answer,
                    onValueChange = { answer = it },
                    label = { Text("Secret Answer") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = XboxNeonGreen, unfocusedBorderColor = XboxOutline)
                )

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = XboxDarkSurfaceVariant,
                    border = BorderStroke(1.dp, XboxOutline)
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Master Recovery Key (Save this):", style = MaterialTheme.typography.labelSmall, color = XboxTextSecondary)
                        Text(recoveryKey, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = XboxNeonGreen, fontFamily = FontFamily.Monospace)
                        TextButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(recoveryKey))
                                Toast.makeText(context, "Recovery Key copied!", Toast.LENGTH_SHORT).show()
                            },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("Copy Key", color = XboxNeonGreen, fontSize = 12.sp)
                        }
                    }
                }

                errorMsg?.let { Text(it, color = StatusErrorRed, style = MaterialTheme.typography.bodySmall) }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (pin.length < 4) {
                        errorMsg = "Password must be at least 4 characters."
                    } else if (pin != confirmPin) {
                        errorMsg = "Passwords do not match."
                    } else if (answer.trim().isEmpty()) {
                        errorMsg = "Please provide an answer to the security question."
                    } else {
                        FocusPreferences.enableAdminLock(context, pin, question, answer, recoveryKey)
                        onSuccess()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = XboxNeonGreen, contentColor = XboxBlack)
            ) {
                Text("Lock Settings")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = XboxTextSecondary) }
        }
    )
}

@Composable
fun AdminManageDialog(
    context: Context,
    stats: FocusPreferences.FocusStats,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = XboxDarkSurface,
        title = { Text("Admin Lock Management", color = XboxTextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("You have verified the peer admin password. You can now disable the Admin Lock or inspect recovery parameters.", color = XboxTextSecondary, style = MaterialTheme.typography.bodySmall)
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = XboxDarkSurfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("Recovery Key:", style = MaterialTheme.typography.labelSmall, color = XboxTextSecondary)
                        Text(stats.recoveryKey, color = XboxNeonGreen, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    FocusPreferences.disableAdminLock(context)
                    Toast.makeText(context, "Admin Lock disabled.", Toast.LENGTH_SHORT).show()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = StatusErrorRed, contentColor = XboxBlack)
            ) {
                Text("Disable Admin Lock")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Close", color = XboxTextSecondary) }
        }
    )
}

@Composable
fun AdminRecoveryDialog(
    context: Context,
    stats: FocusPreferences.FocusStats,
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    var answerInput by remember { mutableStateOf("") }
    var keyInput by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = XboxDarkSurface,
        title = { Text("Admin Password Recovery", color = XboxTextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = XboxDarkSurfaceVariant,
                    contentColor = XboxNeonGreen
                ) {
                    Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                        Text("Question", modifier = Modifier.padding(8.dp), fontSize = 12.sp)
                    }
                    Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                        Text("Key", modifier = Modifier.padding(8.dp), fontSize = 12.sp)
                    }
                    Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }) {
                        Text("24h Reset", modifier = Modifier.padding(8.dp), fontSize = 12.sp)
                    }
                }

                when (selectedTab) {
                    0 -> {
                        Text("Question: ${stats.securityQuestion.ifEmpty { "What does Hatif Wants?" }}", color = XboxTextPrimary, fontWeight = FontWeight.Medium)
                        OutlinedTextField(
                            value = answerInput,
                            onValueChange = { answerInput = it },
                            label = { Text("Answer") },
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = XboxNeonGreen, unfocusedBorderColor = XboxOutline)
                        )
                    }
                    1 -> {
                        Text("Enter your 10-character master recovery key:", color = XboxTextSecondary, style = MaterialTheme.typography.bodySmall)
                        OutlinedTextField(
                            value = keyInput,
                            onValueChange = { keyInput = it },
                            label = { Text("FH-XXXX-XXXX") },
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = XboxNeonGreen, unfocusedBorderColor = XboxOutline)
                        )
                    }
                    2 -> {
                        Text("Emergency 24-Hour Delayed Reset:", color = XboxTextPrimary, fontWeight = FontWeight.Bold)
                        Text(
                            "If you lost your password and recovery key, a 24-hour delayed reset ensures you don't relapse impulsively. Once 24 hours pass, the lock releases.",
                            color = XboxTextSecondary,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                errorMsg?.let { Text(it, color = StatusErrorRed, style = MaterialTheme.typography.bodySmall) }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    when (selectedTab) {
                        0 -> {
                            if (FocusPreferences.verifySecurityAnswer(context, answerInput)) {
                                FocusPreferences.disableAdminLock(context)
                                Toast.makeText(context, "Recovery verified! Lock disabled.", Toast.LENGTH_LONG).show()
                                onDismiss()
                            } else {
                                errorMsg = "Incorrect answer."
                            }
                        }
                        1 -> {
                            if (FocusPreferences.verifyRecoveryKey(context, keyInput)) {
                                FocusPreferences.disableAdminLock(context)
                                Toast.makeText(context, "Master key accepted! Lock disabled.", Toast.LENGTH_LONG).show()
                                onDismiss()
                            } else {
                                errorMsg = "Invalid recovery key."
                            }
                        }
                        2 -> {
                            FocusPreferences.startEmergencyReset(context)
                            Toast.makeText(context, "24-hour emergency countdown started.", Toast.LENGTH_LONG).show()
                            onDismiss()
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = XboxNeonGreen, contentColor = XboxBlack)
            ) {
                Text(if (selectedTab == 2) "Start 24h Reset" else "Verify & Unlock")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = XboxTextSecondary) }
        }
    )
}
