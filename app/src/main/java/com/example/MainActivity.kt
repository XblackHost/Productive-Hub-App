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
        AppSettings.init(applicationContext)

        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                ProductiveHubApp(context = this)
            }
        }
    }
}

data class NavItem(
    val id: Int,
    val label: String,
    val icon: ImageVector
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductiveHubApp(context: ComponentActivity) {
    var selectedTab by remember { mutableIntStateOf(0) }
    // 0: Home, 1: Focus, 2: Diary, 3: Reflection, 4: Grades, 5: Settings, 6: Manual

    val isDiaryUnlocked by HatifSecurityManager.isDiaryUnlocked.collectAsState()
    val settingsState by AppSettings.settingsState.collectAsState()

    // Dynamically filter bottom navigation tabs based on enabled modules
    val navItems = remember(settingsState) {
        val list = mutableListOf<NavItem>()
        list.add(NavItem(0, "Home", Icons.Default.Dashboard))

        val focusEnabled = settingsState.featureShortsBlockerEnabled ||
                settingsState.featurePomodoroEnabled ||
                settingsState.featureAdminLockEnabled
        if (focusEnabled) {
            list.add(NavItem(1, "Focus", Icons.Default.Shield))
        }

        if (settingsState.featureDiaryEnabled) {
            list.add(NavItem(2, "Diary", Icons.Default.Book))
        }

        if (settingsState.featureReflectionEnabled) {
            list.add(NavItem(3, "Reflection", Icons.Default.Psychology))
        }

        if (settingsState.featureGradesEnabled) {
            list.add(NavItem(4, "Grades", Icons.Default.School))
        }
        list
    }

    // Safety fallback if the currently selected tab was disabled
    LaunchedEffect(navItems, selectedTab) {
        if (selectedTab in 1..4 && navItems.none { it.id == selectedTab }) {
            selectedTab = 0
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
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
                                .background(MaterialTheme.colorScheme.primary)
                        )
                        Column {
                            Text(
                                text = "PRODUCTIVE HUB",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.5.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Personal Platform · Offline & Private",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 10.sp
                            )
                        }
                    }
                },
                actions = {
                    // Quick Diary Lock/Unlock Indicator & Button (if diary module is active)
                    if (settingsState.featureDiaryEnabled) {
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
                                tint = if (isDiaryUnlocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Manual & Guide Button
                    IconButton(onClick = { selectedTab = 6 }) {
                        Icon(
                            imageVector = Icons.Default.MenuBook,
                            contentDescription = "Manual & Migration",
                            tint = if (selectedTab == 6) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Settings Button
                    IconButton(onClick = { selectedTab = 5 }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = if (selectedTab == 5) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            ) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    tonalElevation = 0.dp
                ) {
                    navItems.forEach { item ->
                        val isSelected = selectedTab == item.id
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { selectedTab = item.id },
                            icon = {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.label,
                                    tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            label = {
                                Text(
                                    text = item.label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
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
                .background(MaterialTheme.colorScheme.background)
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
