package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.tabs.*
import com.example.ui.theme.*
import com.example.viewmodel.CompanionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: CompanionViewModel,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    
    val ramUsage by viewModel.ramUsage.collectAsState()
    val cpuUsage by viewModel.cpuUsage.collectAsState()
    val bots by viewModel.bots.collectAsState()
    val activeBotsCount = bots.count { it.isActive }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(end = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "MC AI COMPANION",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = MineDiamondBlue
                            )
                            Text(
                                "Offline Local LAN Client • v1.4",
                                fontSize = 10.sp,
                                color = MineTextSecondary
                            )
                        }

                        // Top bar performance monitors
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Active bot badge count
                            PerformanceBadge(
                                icon = "🤖",
                                value = "$activeBotsCount Connected",
                                color = MineGreenAccent
                            )

                            // CPU usage badge
                            PerformanceBadge(
                                icon = "⚡",
                                value = "$cpuUsage% CPU",
                                color = MineRedstone
                            )

                            // RAM usage badge
                            PerformanceBadge(
                                icon = "💾",
                                value = "$ramUsage MB",
                                color = MineGoldAccent
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MineDarkBg,
                    titleContentColor = MineTextPrimary
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MineCardBg,
                tonalElevation = 6.dp,
                modifier = Modifier.testTag("main_navigation_bar")
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Share, contentDescription = "Lobby") },
                    label = { Text("Lobby", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = MineDiamondBlue,
                        indicatorColor = MineDiamondBlue,
                        unselectedIconColor = MineTextSecondary,
                        unselectedTextColor = MineTextSecondary
                    ),
                    modifier = Modifier.testTag("tab_lobby")
                )

                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Dashboard") },
                    label = { Text("Overview", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = MineGreenAccent,
                        indicatorColor = MineGreenAccent,
                        unselectedIconColor = MineTextSecondary,
                        unselectedTextColor = MineTextSecondary
                    ),
                    modifier = Modifier.testTag("tab_dashboard")
                )

                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.Build, contentDescription = "Directives") },
                    label = { Text("Directives", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = MineGoldAccent,
                        indicatorColor = MineGoldAccent,
                        unselectedIconColor = MineTextSecondary,
                        unselectedTextColor = MineTextSecondary
                    ),
                    modifier = Modifier.testTag("tab_directives")
                )

                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Default.MailOutline, contentDescription = "AI Chat") },
                    label = { Text("AI Link", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = MineDiamondBlue,
                        indicatorColor = MineDiamondBlue,
                        unselectedIconColor = MineTextSecondary,
                        unselectedTextColor = MineTextSecondary
                    ),
                    modifier = Modifier.testTag("tab_chat")
                )

                NavigationBarItem(
                    selected = selectedTab == 4,
                    onClick = { selectedTab = 4 },
                    icon = { Icon(Icons.Default.Search, contentDescription = "AI Vision") },
                    label = { Text("Vision Scanner", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = MineDiamondBlue,
                        indicatorColor = MineDiamondBlue,
                        unselectedIconColor = MineTextSecondary,
                        unselectedTextColor = MineTextSecondary
                    ),
                    modifier = Modifier.testTag("tab_vision")
                )
            }
        },
        containerColor = MineDarkBg
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> LobbyTab(viewModel = viewModel)
                1 -> BotDashboardTab(viewModel = viewModel)
                2 -> {
                    // Unified Directives tab (Missions + Blueprint sub tabs)
                    var directivesSubTab by remember { mutableIntStateOf(0) }
                    Column(modifier = Modifier.fillMaxSize()) {
                        TabRow(
                            selectedTabIndex = directivesSubTab,
                            containerColor = MineDarkBg,
                            contentColor = MineGoldAccent,
                            indicator = { tabPositions ->
                                TabRowDefaults.SecondaryIndicator(
                                    modifier = Modifier.tabIndicatorOffset(tabPositions[directivesSubTab]),
                                    color = MineGoldAccent
                                )
                            }
                        ) {
                            Tab(
                                selected = directivesSubTab == 0,
                                onClick = { directivesSubTab = 0 },
                                text = { Text("📋 Queue Directives", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                            )
                            Tab(
                                selected = directivesSubTab == 1,
                                onClick = { directivesSubTab = 1 },
                                text = { Text("🏡 Build Blueprints", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                            )
                        }

                        if (directivesSubTab == 0) {
                            TasksTab(viewModel = viewModel)
                        } else {
                            BlueprintsTab(viewModel = viewModel)
                        }
                    }
                }
                3 -> ChatTab(viewModel = viewModel)
                4 -> VisionTab(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun PerformanceBadge(icon: String, value: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(MineCardBg)
            .border(1.dp, MineCardStroke, RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(icon, fontSize = 10.sp)
        Text(
            text = value,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = color
        )
    }
}
