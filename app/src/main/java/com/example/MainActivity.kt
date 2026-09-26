package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.*
import com.example.ui.theme.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize all offline repositories
        FocusPreferences.init(applicationContext)
        DiaryRepository.init(applicationContext)
        ReflectionRepository.init(applicationContext)
        GradesRepository.init(applicationContext)

        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                HatifWorkspaceApp(context = this)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HatifWorkspaceApp(context: ComponentActivity) {
    var selectedTab by remember { mutableIntStateOf(0) }
    // 0: Home, 1: Focus, 2: Diary, 3: Reflection, 4: Grades, 5: Settings

    val isDiaryUnlocked by HatifSecurityManager.isDiaryUnlocked.collectAsState()

    Scaffold(
        containerColor = XboxBlack,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(XboxNeonGreen)
                        )
                        Column {
                            Text(
                                text = "HATIF WORKSPACE",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.5.sp,
                                color = XboxTextPrimary
                            )
                            Text(
                                text = "Personal Platform · Offline & Private",
                                style = MaterialTheme.typography.labelSmall,
                                color = XboxTextSecondary,
                                fontSize = 10.sp
                            )
                        }
                    }
                },
                actions = {
                    // Quick Diary Lock/Unlock Indicator & Button
                    IconButton(onClick = {
                        if (isDiaryUnlocked) {
                            HatifSecurityManager.lockDiary()
                        } else {
                            selectedTab = 2 // Navigate to Diary
                        }
                    }) {
                        Icon(
                            imageVector = if (isDiaryUnlocked) Icons.Default.LockOpen else Icons.Default.Lock,
                            contentDescription = if (isDiaryUnlocked) "Vault Unlocked" else "Vault Locked",
                            tint = if (isDiaryUnlocked) XboxNeonGreen else XboxTextSecondary
                        )
                    }

                    // Manual & Guide Button
                    IconButton(onClick = { selectedTab = 6 }) {
                        Icon(
                            imageVector = Icons.Default.MenuBook,
                            contentDescription = "Manual & Migration",
                            tint = if (selectedTab == 6) XboxNeonGreen else XboxTextSecondary
                        )
                    }

                    // Settings Button
                    IconButton(onClick = { selectedTab = 5 }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = if (selectedTab == 5) XboxNeonGreen else XboxTextSecondary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = XboxDarkSurface,
                    titleContentColor = XboxTextPrimary
                )
            )
        },
        bottomBar = {
            Surface(
                color = XboxDarkSurface,
                border = BorderStroke(1.dp, XboxOutline)
            ) {
                NavigationBar(
                    containerColor = XboxDarkSurface,
                    contentColor = XboxTextSecondary,
                    tonalElevation = 0.dp
                ) {
                    val navItems = listOf(
                        Triple(0, "Home", Icons.Default.Dashboard),
                        Triple(1, "Focus", Icons.Default.Shield),
                        Triple(2, "Diary", Icons.Default.Book),
                        Triple(3, "Reflection", Icons.Default.Psychology),
                        Triple(4, "Grades", Icons.Default.School)
                    )

                    navItems.forEach { (index, label, icon) ->
                        val isSelected = selectedTab == index
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { selectedTab = index },
                            icon = {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = label,
                                    tint = if (isSelected) XboxNeonGreen else XboxTextSecondary
                                )
                            },
                            label = {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) XboxNeonGreen else XboxTextSecondary
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = XboxDarkGreen.copy(alpha = 0.6f),
                                selectedIconColor = XboxNeonGreen,
                                selectedTextColor = XboxNeonGreen,
                                unselectedIconColor = XboxTextSecondary,
                                unselectedTextColor = XboxTextSecondary
                            )
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(XboxBlack)
        ) {
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "ScreenTransition"
            ) { targetTab ->
                when (targetTab) {
                    0 -> DashboardScreen(context = context, onNavigate = { selectedTab = it })
                    1 -> FocusScreen(context = context)
                    2 -> DiaryScreen(context = context)
                    3 -> ReflectionScreen(context = context)
                    4 -> GradesScreen(context = context)
                    5 -> SettingsScreen(context = context, onNavigate = { selectedTab = it })
                    6 -> ManualScreen(context = context, onBack = { selectedTab = 0 })
                    else -> DashboardScreen(context = context, onNavigate = { selectedTab = it })
                }
            }
        }
    }
}
