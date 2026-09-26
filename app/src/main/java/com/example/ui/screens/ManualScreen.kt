package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.DiaryRepository
import com.example.ReflectionRepository
import com.example.ui.theme.*

@Composable
fun ManualScreen(context: Context, onBack: (() -> Unit)? = null) {
    val clipboardManager = LocalClipboardManager.current
    var showImportDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (onBack != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = XboxNeonGreen
                    )
                }
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "Back to Productive Hub",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Hero Manual Banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        modifier = Modifier.size(46.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.MenuBook, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(26.dp))
                        }
                    }
                    Column {
                        Text(
                            text = "Productive Hub User Manual",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Migration Guide, Optimization & Device Tips",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Text(
                    text = "Everything you need to master your private workspace, migrate to a new device without packet loss, preserve battery life, and maintain total digital privacy.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )
            }
        }

        // SECTION 1: MIGRATION & ZERO PACKET LOSS
        ManualChapterCard(
            chapterNumber = "01",
            title = "Zero-Loss Device Migration",
            subtitle = "How to shift to a new phone without losing any diary or reflection records",
            icon = Icons.Default.PhoneAndroid,
            accentColor = XboxNeonGreen
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Productive Hub is 100% offline—it contains no cloud servers or network sockets. When moving to a new Android phone, follow these exact steps to ensure zero packet loss:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 18.sp
                )

                ManualStepItem(
                    stepNumber = "1",
                    title = "Export Portable JSON / Encrypted Payload",
                    description = "Open your Diary or Settings screen. Tap 'Copy Portable JSON' below or 'Export Backup'. This generates a complete structured UTF-8 payload of your entries."
                )

                ManualStepItem(
                    stepNumber = "2",
                    title = "Transfer to Your New Phone",
                    description = "Send this text via Bluetooth, save it to an offline USB-C flash drive, or paste it in a trusted note app. No internet connection is needed."
                )

                ManualStepItem(
                    stepNumber = "3",
                    title = "Install App on New Phone & Import",
                    description = "Install Productive Hub APK on your new phone, navigate to this Manual or Diary screen, tap 'Restore / Import Backup', and paste your data. The engine merges records with zero loss."
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            val json = DiaryRepository.exportPortableJson()
                            clipboardManager.setText(AnnotatedString(json))
                            Toast.makeText(context, "Portable JSON copied to clipboard (${json.length} chars).", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = XboxNeonGreen, contentColor = XboxBlack)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Copy Portable JSON", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = { showImportDialog = true },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, XboxNeonGreen)
                    ) {
                        Icon(Icons.Default.FileDownload, contentDescription = null, tint = XboxNeonGreen, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Import Backup", fontSize = 11.sp, color = XboxNeonGreen, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // SECTION 2: BATTERY OPTIMIZATION & ZERO DRAIN
        ManualChapterCard(
            chapterNumber = "02",
            title = "Battery & Performance Optimization",
            subtitle = "Keeping the app lightweight, fast, and conserving CPU power",
            icon = Icons.Default.BatteryChargingFull,
            accentColor = StatusInfoCyan
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "How Productive Hub is engineered to consume virtually 0% standby battery:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 18.sp
                )

                ManualBulletPoint(
                    title = "No Background Internet Sockets",
                    description = "The app has zero network permissions. It never polls servers, never syncs in the background, and cannot ping telemetry."
                )

                ManualBulletPoint(
                    title = "Package-Filtered Accessibility Engine",
                    description = "The Blocker engine only wakes up when YouTube or Instagram are actively in the foreground. It sleeps completely while you use other apps or when your screen is locked."
                )

                ManualBulletPoint(
                    title = "2-Second Dynamic Tick Interval",
                    description = "The active scroll timer checks state at battery-friendly intervals rather than continuous loops, reducing CPU wake-locks."
                )

                ManualBulletPoint(
                    title = "Hardware AES-256 KeyStore Acceleration",
                    description = "Cryptographic operations use the dedicated Android Secure Element / TrustZone hardware processor rather than software cycles."
                )

                OutlinedButton(
                    onClick = {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, XboxOutline)
                ) {
                    Icon(Icons.Default.Settings, contentDescription = null, tint = StatusInfoCyan, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Check App Battery Usage in Android Settings", color = XboxTextPrimary, fontSize = 12.sp)
                }
            }
        }

        // SECTION 3: PEER ADMIN LOCK & SHORTS RESCUE
        ManualChapterCard(
            chapterNumber = "03",
            title = "Peer Admin Lock & Habit Rescue",
            subtitle = "How the anti-relapse lock works and how recovery is guaranteed",
            icon = Icons.Default.Shield,
            accentColor = StatusWarningAmber
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "To eliminate doomscrolling, you can give your phone to a trusted parent or friend to set a Peer Admin Password.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 18.sp
                )

                ManualStepItem(
                    stepNumber = "A",
                    title = "Locking the Blocker",
                    description = "Once enabled, the toggle cannot be switched off without the peer password. YouTube Shorts & Instagram Reels are instantly backed out."
                )

                ManualStepItem(
                    stepNumber = "B",
                    title = "Instagram Stories & DMs are Preserved",
                    description = "Stories and direct message threads are never blocked. Only full-screen Reels viewer is rescued."
                )

                ManualStepItem(
                    stepNumber = "C",
                    title = "What if the Peer Forgets the Password?",
                    description = "Three emergency recovery channels are built in: 1) Secret security question, 2) 10-character Master Key (FH-XXXX-XXXX), 3) 24-Hour Delayed Reset timer."
                )
            }
        }

        // SECTION 4: 30-DAY SELF-REFLECTION & AI EXPORT
        ManualChapterCard(
            chapterNumber = "04",
            title = "30-Day Self-Reflection Cycle",
            subtitle = "Daily scoring, habit tracking, and exporting your master summary",
            icon = Icons.Default.Psychology,
            accentColor = StatusPurple
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Reflect once a day for 30 consecutive days. Track your mood, highlight of the day, habits completed, and an overall score out of 10.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 18.sp
                )

                ManualBulletPoint(
                    title = "Export to Gemini / Claude / ChatGPT",
                    description = "Tap 'Export 30-Day AI Report' in the Reflection screen to generate a prompt-ready markdown report to paste into an AI for deep psychological analysis."
                )
            }
        }

        // SECTION 5: APP UPDATES & INSTALLING OVER PREVIOUS VERSIONS
        ManualChapterCard(
            chapterNumber = "05",
            title = "App Updates & APK Reinstalls",
            subtitle = "Will installing a new APK update automatically over the previous version?",
            icon = Icons.Default.SystemUpdate,
            accentColor = StatusInfoCyan
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Yes! Android's Package Manager automatically updates an app over the previous version without wiping any data, provided two key conditions are met:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 18.sp
                )

                ManualStepItem(
                    stepNumber = "1",
                    title = "Same Package ID & Keystore Signature",
                    description = "When you build or download a new APK of Productive Hub, it retains the exact same package namespace and signing keystore. Android sees it as a direct upgrade."
                )

                ManualStepItem(
                    stepNumber = "2",
                    title = "Preserved SharedPreferences & Vault",
                    description = "Your diary entries, reflection log, subject grades, and peer admin passwords are saved in your app's private sandbox storage. During an APK update, Android leaves this sandbox completely untouched."
                )

                ManualStepItem(
                    stepNumber = "3",
                    title = "Golden Rule: Never Uninstall First",
                    description = "If you tap 'Uninstall', Android deletes the app's internal sandbox. Always tap the new APK directly to choose 'Update' so that your existing diary and settings stay 100% intact!"
                )
            }
        }
    }

    if (showImportDialog) {
        ImportBackupDialog(
            context = context,
            onDismiss = { showImportDialog = false },
            onSuccess = {
                showImportDialog = false
                Toast.makeText(context, "Backup successfully imported without loss!", Toast.LENGTH_LONG).show()
            }
        )
    }
}

@Composable
fun ManualChapterCard(
    chapterNumber: String,
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: androidx.compose.ui.graphics.Color,
    content: @Composable () -> Unit
) {
    var expanded by remember { mutableStateOf(true) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
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
                            Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(22.dp))
                        }
                    }
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "CHAPTER $chapterNumber",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = accentColor,
                                letterSpacing = 1.sp
                            )
                        }
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Toggle",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier.padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    content()
                }
            }
        }
    }
}

@Composable
fun ManualStepItem(
    stepNumber: String,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
            modifier = Modifier.size(24.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = stepNumber,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
            Text(text = description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 17.sp)
        }
    }
}

@Composable
fun ManualBulletPoint(
    title: String,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("•", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
            Text(text = description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 17.sp)
        }
    }
}

@Composable
fun ImportBackupDialog(
    context: Context,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    var payloadInput by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = { Text("Restore / Import Diary Data", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "Paste your exported JSON or AES-256 encrypted backup payload below. The engine will parse and merge all entries with zero loss:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = payloadInput,
                    onValueChange = {
                        payloadInput = it
                        errorMsg = null
                    },
                    placeholder = { Text("Paste JSON or encrypted string here...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )
                errorMsg?.let { Text(it, color = StatusErrorRed, style = MaterialTheme.typography.bodySmall) }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (payloadInput.trim().isEmpty()) {
                        errorMsg = "Please paste a backup string."
                    } else {
                        val success = DiaryRepository.importBackupPayload(context, payloadInput)
                        if (success) {
                            onSuccess()
                        } else {
                            errorMsg = "Invalid backup format or decryption failed."
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text("Restore Entries")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
    )
}
