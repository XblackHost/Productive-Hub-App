package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.DiaryRepository
import com.example.ReflectionRepository
import com.example.ui.theme.*

@Composable
fun SettingsScreen(context: Context, onNavigate: ((Int) -> Unit)? = null) {
    val clipboardManager = LocalClipboardManager.current
    var showImportDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App Info & Security Banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = XboxDarkSurface),
            border = BorderStroke(1.dp, XboxNeonGreen.copy(alpha = 0.5f))
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
                        color = XboxNeonGreen.copy(alpha = 0.15f),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = XboxNeonGreen, modifier = Modifier.size(26.dp))
                        }
                    }
                    Column {
                        Text(
                            text = "Hatif Workspace v2.0",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = XboxTextPrimary
                        )
                        Text(
                            text = "Private Personal Platform",
                            style = MaterialTheme.typography.bodySmall,
                            color = XboxTextSecondary
                        )
                    }
                }

                Text(
                    text = "Engineered with zero network access (no INTERNET permission in AndroidManifest). All diary records and reflections are encrypted using AES-256 with keys stored inside Android KeyStore hardware.",
                    style = MaterialTheme.typography.bodySmall,
                    color = XboxTextSecondary
                )
            }
        }

        // Privacy & Security Audit
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
                    text = "Security Architecture",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = XboxTextPrimary
                )

                SettingsAuditRow(
                    icon = Icons.Default.CloudOff,
                    title = "100% Offline",
                    subtitle = "No internet permission. No network sockets. No cloud sync."
                )
                SettingsAuditRow(
                    icon = Icons.Default.Key,
                    title = "AES-256-GCM Encryption",
                    subtitle = "Data encrypted at rest via Android KeyStore hardware."
                )
                SettingsAuditRow(
                    icon = Icons.Default.Fingerprint,
                    title = "Biometric & Security Question",
                    subtitle = "Dual-layer authentication with custom password support."
                )
                SettingsAuditRow(
                    icon = Icons.Default.Block,
                    title = "Package-Restricted Service",
                    subtitle = "Accessibility service restricted only to YouTube & Instagram."
                )
            }
        }

        // Export & Data Portability
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
                    text = "Data Portability & Export",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = XboxTextPrimary
                )

                Text(
                    text = "You own 100% of your data. Export your 30-day reflection report or encrypted diary backup at any time.",
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
                        colors = ButtonDefaults.buttonColors(containerColor = XboxNeonGreen, contentColor = XboxBlack)
                    ) {
                        Text("Export AI .txt", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            val backup = DiaryRepository.exportEncryptedBackup(context)
                            clipboardManager.setText(AnnotatedString(backup))
                            Toast.makeText(context, "Encrypted diary backup copied.", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, XboxOutline)
                    ) {
                        Text("Copy Diary Backup", color = XboxTextPrimary, fontSize = 12.sp)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val json = DiaryRepository.exportPortableJson()
                            clipboardManager.setText(AnnotatedString(json))
                            Toast.makeText(context, "Portable JSON copied.", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, XboxOutline)
                    ) {
                        Text("Copy Portable JSON", color = XboxTextPrimary, fontSize = 11.sp)
                    }

                    Button(
                        onClick = { showImportDialog = true },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = XboxDarkSurfaceVariant, contentColor = XboxNeonGreen),
                        border = BorderStroke(1.dp, XboxNeonGreen.copy(alpha = 0.5f))
                    ) {
                        Text("Restore Data", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }

                if (onNavigate != null) {
                    Button(
                        onClick = { onNavigate(6) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = XboxDarkSurfaceElevated, contentColor = XboxNeonGreen),
                        border = BorderStroke(1.dp, XboxNeonGreen.copy(alpha = 0.5f))
                    ) {
                        Icon(Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Open Full User Manual & Migration Guide", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        // System Settings Shortcuts
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
                Text(
                    text = "Device Configuration (Infinix Hot 60i / XOS 15)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = XboxTextPrimary
                )

                OutlinedButton(
                    onClick = {
                        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, XboxOutline)
                ) {
                    Icon(Icons.Default.Accessibility, contentDescription = null, tint = XboxNeonGreen, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Android Accessibility Settings", color = XboxTextPrimary)
                }

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
                    Icon(Icons.Default.BatteryChargingFull, contentDescription = null, tint = XboxNeonGreen, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("App Info & Battery Settings", color = XboxTextPrimary)
                }
            }
        }
    }

    if (showImportDialog) {
        ImportBackupDialog(
            context = context,
            onDismiss = { showImportDialog = false },
            onSuccess = {
                showImportDialog = false
                Toast.makeText(context, "Entries restored without loss.", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
fun SettingsAuditRow(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(imageVector = icon, contentDescription = title, tint = XboxNeonGreen, modifier = Modifier.size(22.dp))
        Column {
            Text(title, style = MaterialTheme.typography.titleSmall, color = XboxTextPrimary, fontWeight = FontWeight.SemiBold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = XboxTextSecondary)
        }
    }
}
