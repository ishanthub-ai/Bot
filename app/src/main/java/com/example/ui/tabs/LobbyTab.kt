package com.example.ui.tabs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.BorderStroke
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlin.random.Random
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.BotEntity
import com.example.ui.theme.*
import com.example.viewmodel.CompanionViewModel
import com.example.viewmodel.LanWorld

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState

@Composable
fun LobbyTab(
    viewModel: CompanionViewModel,
    modifier: Modifier = Modifier
) {
    val bots by viewModel.bots.collectAsState()
    val lanWorlds by viewModel.lanWorlds.collectAsState()
    val isScanningLan by viewModel.isScanningLan.collectAsState()
    val consoleLogs by viewModel.consoleLogs.collectAsState()

    // --- Premium & Ads States ---
    val adsEnabledState by viewModel.adsEnabled.collectAsState()
    val isPremium by viewModel.isPremiumActive.collectAsState()
    val premiumTimer by viewModel.premiumTimerRemaining.collectAsState()

    // --- Server Hosting States ---
    val isHosting by viewModel.isHostingServer.collectAsState()
    val hostedCpu by viewModel.hostedServerCpuUsage.collectAsState()
    val hostedRam by viewModel.hostedServerRamUsage.collectAsState()

    // --- Video Feed Search States ---
    val videoList by viewModel.filteredVideos.collectAsState()
    var videoSearch by remember { mutableStateOf("") }
    var activeVideoId by remember { mutableStateOf(-1) }

    // --- Server Host Form inputs ---
    var hostNameInput by remember { mutableStateOf("Survival Minecraft SMP") }
    var hostedPortInput by remember { mutableStateOf("19132") }
    var hostModeInput by remember { mutableStateOf("Survival") }
    var hostSeedInput by remember { mutableStateOf("884029419401") }
    var hostedMaxPlayersInput by remember { mutableStateOf("25") }

    // --- Premium Slot input ---
    var promoCodeInput by remember { mutableStateOf("") }
    var selectedDuration by remember { mutableStateOf(30) } // default 30s

    var showAddBotDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- AD SPONSOR BANNER ---
        if (adsEnabledState) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            viewModel.addConsoleLog("[Ads Sponsor] Tap registered! Save money and get premium by putting CREEPERPASS below.")
                        },
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2E1C1C)),
                    border = BorderStroke(1.dp, MineRedstone.copy(alpha = 0.6f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(MineRedstone)
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text("SPONSORED AD", fontSize = 8.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Premium Host Pro v2.5", fontSize = 11.sp, color = MineTextPrimary, fontWeight = FontWeight.Bold)
                            }
                            IconButton(
                                onClick = {
                                    viewModel.addConsoleLog("[Ads Sponsor] Ads are locked to provide free hosting services. Put Promo 'CREEPERPASS' to bypass.")
                                },
                                modifier = Modifier.size(20.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Dismiss ad", tint = MineTextSecondary, modifier = Modifier.size(14.dp))
                            }
                        }
                        Text(
                            text = "🚀 Run ultra high performance 24/7 Minecraft server nodes with 16GB RAM for only $3.50. Low ping guarantee! Tap here for immediate setup.",
                            fontSize = 11.sp,
                            color = MineTextPrimary,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }

        // --- PREMIUM SLOT ACTIVATION BOX ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (isPremium) MineDiamondBlue.copy(alpha = 0.08f) else MineCardBg
                ),
                border = BorderStroke(1.dp, if (isPremium) MineDiamondBlue else MineCardStroke)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("👑", fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                if (isPremium) "Premium Mode Status: ACTIVATED" else "Premium Pass Unlock Door",
                                style = MaterialTheme.typography.titleSmall,
                                color = if (isPremium) MineDiamondBlue else MineTextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (isPremium) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(MineGreenAccent.copy(alpha = 0.2f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("ACTIVE: ${premiumTimer}s remaining", fontSize = 10.sp, color = MineGreenAccent, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(MineRedstone.copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("Standard Mode", fontSize = 10.sp, color = MineRedstone, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Text(
                        "Entering a valid premium code disables all ads instantly! Try entering 'CREEPERPASS' or 'SERVERVIP'. Ads reappear when period ends.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MineTextSecondary,
                        fontSize = 11.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = promoCodeInput,
                            onValueChange = { promoCodeInput = it },
                            label = { Text("Code Promo", fontSize = 10.sp) },
                            placeholder = { Text("CREEPERPASS", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f).height(48.dp),
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MineDiamondBlue,
                                unfocusedBorderColor = MineCardStroke
                            )
                        )

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(MineCardStroke)
                                .clickable {
                                    selectedDuration = when (selectedDuration) {
                                        30 -> 60
                                        60 -> 180
                                        else -> 30
                                    }
                                }
                                .padding(horizontal = 10.dp, vertical = 12.dp)
                        ) {
                            Text("${selectedDuration}s slot", fontSize = 11.sp, color = MineTextPrimary)
                        }

                        Button(
                            onClick = {
                                if (viewModel.activatePremiumCode(promoCodeInput, selectedDuration)) {
                                    promoCodeInput = ""
                                } else {
                                    viewModel.addConsoleLog("[Premium System] Invalid code. Get valid code from dev mode or support.")
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MineDiamondBlue),
                            modifier = Modifier.height(38.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp)
                        ) {
                            Text("Apply", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // --- DEVICE LOCAL GAME SERVERS CREATION ROOM ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MineCardBg),
                border = BorderStroke(1.dp, MineDiamondBlue.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("📡", fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "LAN Server Mode Creator",
                                style = MaterialTheme.typography.titleSmall,
                                color = MineDiamondBlue,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Switch(
                            checked = isHosting,
                            onCheckedChange = { checked ->
                                if (checked) {
                                    viewModel.startHostingLocalServer(
                                        hostNameInput,
                                        hostedPortInput.toIntOrNull() ?: 19132,
                                        hostModeInput,
                                        hostSeedInput,
                                        hostedMaxPlayersInput.toIntOrNull() ?: 25
                                    )
                                } else {
                                    viewModel.stopHostingLocalServer()
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MineGreenAccent,
                                checkedTrackColor = MineGreenAccent.copy(alpha = 0.4f)
                            )
                        )
                    }

                    Text(
                        "Configure and create a fully simulated, low-latency, dynamic Minecraft Server right on your Android telephone! Allows on-device socket bridging.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MineTextSecondary,
                        fontSize = 11.sp
                    )

                    if (!isHosting) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = hostNameInput,
                                onValueChange = { hostNameInput = it },
                                label = { Text("Server Name", fontSize = 10.sp) },
                                modifier = Modifier.weight(1.1f).height(48.dp),
                                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MineDiamondBlue, unfocusedBorderColor = MineCardStroke)
                            )
                            OutlinedTextField(
                                value = hostSeedInput,
                                onValueChange = { hostSeedInput = it },
                                label = { Text("Seed", fontSize = 10.sp) },
                                modifier = Modifier.weight(0.9f).height(48.dp),
                                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MineDiamondBlue, unfocusedBorderColor = MineCardStroke)
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = hostedPortInput,
                                onValueChange = { hostedPortInput = it },
                                label = { Text("Port", fontSize = 10.sp) },
                                modifier = Modifier.weight(0.6f).height(48.dp),
                                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MineDiamondBlue, unfocusedBorderColor = MineCardStroke)
                            )
                            OutlinedTextField(
                                value = hostModeInput,
                                onValueChange = { hostModeInput = it },
                                label = { Text("Game Mode", fontSize = 10.sp) },
                                modifier = Modifier.weight(0.8f).height(48.dp),
                                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MineDiamondBlue, unfocusedBorderColor = MineCardStroke)
                            )
                            OutlinedTextField(
                                value = hostedMaxPlayersInput,
                                onValueChange = { hostedMaxPlayersInput = it },
                                label = { Text("Players Limit", fontSize = 10.sp) },
                                modifier = Modifier.weight(0.6f).height(48.dp),
                                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MineDiamondBlue, unfocusedBorderColor = MineCardStroke)
                            )
                        }
                    } else {
                        // Running Indicators & Performance feedback
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                .border(1.dp, MineGreenAccent.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                                .padding(10.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                    Text("📡 Status: ACTIVE ON-DEVICE HOST", fontSize = 11.sp, color = MineGreenAccent, fontWeight = FontWeight.Bold)
                                    Text("Local proxy: 127.0.0.1:$hostedPortInput", fontSize = 11.sp, color = MineTextSecondary, fontFamily = FontFamily.Monospace)
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Host Processor:", fontSize = 10.sp, color = MineTextSecondary)
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            LinearProgressIndicator(
                                                progress = { (hostedCpu / 100f) },
                                                modifier = Modifier.weight(1f).height(6.dp),
                                                color = MineRedstone,
                                                trackColor = MineCardStroke
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("$hostedCpu%", fontSize = 10.sp, color = MineRedstone, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("allocated RAM:", fontSize = 10.sp, color = MineTextSecondary)
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            LinearProgressIndicator(
                                                progress = { (hostedRam / 512f) },
                                                modifier = Modifier.weight(1f).height(6.dp),
                                                color = MineGoldAccent,
                                                trackColor = MineCardStroke
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("$hostedRam MB", fontSize = 10.sp, color = MineGoldAccent, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }

                                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                    Text("Active Seed: $hostSeedInput", fontSize = 10.sp, color = MineTextSecondary)
                                    Text("Game Rule: $hostModeInput (max $hostedMaxPlayersInput)", fontSize = 10.sp, color = MineGoldAccent)
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- RECOMMENDED MINECRAFT SERVER VIDEOS ---
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "📺 Minecraft Server Videos & Directives",
                    style = MaterialTheme.typography.titleMedium,
                    color = MineGoldAccent,
                    fontWeight = FontWeight.Bold
                )

                // Search through streams
                OutlinedTextField(
                    value = videoSearch,
                    onValueChange = {
                        videoSearch = it
                        viewModel.updateVideoSearch(it)
                    },
                    placeholder = { Text("Search walks/seeds/hosting guides...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = MineTextSecondary, modifier = Modifier.size(16.dp)) },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MineGoldAccent,
                        unfocusedBorderColor = MineCardStroke
                    )
                )

                if (videoList.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MineCardStroke, RoundedCornerShape(6.dp))
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No matching tutorial video found.", color = MineTextSecondary, fontSize = 11.sp)
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        videoList.forEach { video ->
                            val isPlaying = activeVideoId == video.id
                            Card(
                                modifier = Modifier
                                    .width(220.dp)
                                    .clickable {
                                        activeVideoId = if (isPlaying) -1 else video.id
                                    },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isPlaying) MineGoldAccent.copy(alpha = 0.08f) else MineCardBg
                                ),
                                border = BorderStroke(1.dp, if (isPlaying) MineGoldAccent else MineCardStroke)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    if (isPlaying) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(80.dp)
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(Color.Black),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                CircularProgressIndicator(color = MineGoldAccent, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text("Streaming Video Pack...", fontSize = 8.sp, color = MineGoldAccent, fontFamily = FontFamily.Monospace)
                                            }
                                        }
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(80.dp)
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(Color(0xFF131718)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(video.icon, fontSize = 28.sp)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(video.title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MineTextPrimary, maxLines = 1)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(video.uploader, fontSize = 9.sp, color = MineTextSecondary)
                                        Text(video.duration, fontSize = 9.sp, color = MineGoldAccent, fontFamily = FontFamily.Monospace)
                                    }
                                    Text(video.views, fontSize = 9.sp, color = MineTextSecondary)
                                }
                            }
                        }
                    }
                }
            }
        }

        // LAN Connection Status
        item {
            LANScannerHeader(
                worlds = lanWorlds,
                isScanning = isScanningLan,
                onScanTrigger = { viewModel.triggerLanScan() }
            )
        }

        // Active LAN Worlds Found
        item {
            Text(
                text = "Detected LAN Worlds (Port 19132)",
                style = MaterialTheme.typography.titleMedium,
                color = MineDiamondBlue,
                fontWeight = FontWeight.Bold
            )
        }

        if (lanWorlds.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MineCardStroke, RoundedCornerShape(8.dp))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No local Minecraft game sessions found yet.\nPress 'Scan LAN Worlds' above to search.",
                        color = MineTextSecondary,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        } else {
            items(lanWorlds) { world ->
                LanWorldItem(world = world)
            }
        }

        // Bots LAN Client Management
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Companion LAN Clients",
                    style = MaterialTheme.typography.titleMedium,
                    color = MineGreenAccent,
                    fontWeight = FontWeight.Bold
                )
                Button(
                    onClick = { showAddBotDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = MineGreenAccent),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier
                        .testTag("add_bot_button")
                        .minimumInteractiveComponentSize()
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Bot", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New Bot", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (bots.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MineCardStroke, RoundedCornerShape(8.dp))
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No companion bots defined. Tap New Bot to create Steve or Alex companion client. Runs completely locally on Android hardware.",
                        color = MineTextSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            items(bots) { bot ->
                BotClientCard(
                    bot = bot,
                    onToggleConnect = { viewModel.toggleBotConnection(bot) },
                    onDelete = {
                        // Delete bot in scope
                    }
                )
            }
        }

        // Live Minecraft Server Proxy Terminal Console
        item {
            Text(
                text = "Local Client Terminal Console",
                style = MaterialTheme.typography.titleMedium,
                color = MineGoldAccent,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            TerminalConsole(logs = consoleLogs)
        }
    }

    if (showAddBotDialog) {
        AddBotDialog(
            viewModel = viewModel,
            onDismiss = { showAddBotDialog = false },
            onConfirm = { botName, skinType ->
                val newBot = BotEntity(
                    name = botName.ifBlank { "SmartBot_${Random.nextInt(100, 999)}" },
                    skin = skinType,
                    status = "Inactive",
                    isActive = false
                )
                viewModel.viewModelScope.launch {
                    viewModel.toggleBotConnection(newBot)
                }
                showAddBotDialog = false
            }
        )
    }
}

@Composable
fun LANScannerHeader(
    worlds: List<LanWorld>,
    isScanning: Boolean,
    onScanTrigger: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MineCardBg),
        border = BorderStroke(1.dp, MineCardStroke)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (worlds.isNotEmpty()) MineGreenAccent else MineRedstone)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Minecraft Bedrock LAN Proxy",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MineTextPrimary
                    )
                    Text(
                        text = if (worlds.isNotEmpty()) "Ready to bridge local bots (${worlds.size} worlds found)" else "Searching local network...",
                        style = MaterialTheme.typography.labelSmall,
                        color = MineTextSecondary
                    )
                }
                IconButton(
                    onClick = onScanTrigger,
                    modifier = Modifier
                        .testTag("scan_lan_button")
                        .background(MineCardStroke, RoundedCornerShape(8.dp))
                        .size(40.dp)
                ) {
                    if (isScanning) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MineDiamondBlue, strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.Refresh, contentDescription = "Scan LAN", tint = MineTextPrimary)
                    }
                }
            }
        }
    }
}

@Composable
fun LanWorldItem(world: LanWorld) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MineCardBg),
        border = BorderStroke(1.dp, MineDiamondBlue.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Share,
                contentDescription = "LAN Match",
                tint = MineDiamondBlue,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = world.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MineTextPrimary
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${world.hostAddress}:${world.port}",
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = MineTextSecondary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "•  ${world.version}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MineDiamondBlue
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "Players",
                    style = MaterialTheme.typography.labelSmall,
                    color = MineTextSecondary
                )
                Text(
                    text = world.playersCount,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MineGreenAccent
                )
            }
        }
    }
}

@Composable
fun BotClientCard(
    bot: BotEntity,
    onToggleConnect: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MineCardBg),
        border = BorderStroke(
            1.dp,
            if (bot.isActive) MineGreenAccent.copy(alpha = 0.6f) else MineCardStroke
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Skin Head Face Simulator
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        when {
                            bot.skin == "Steve" -> Color(0xFF1E88E5)
                            bot.skin == "Alex" -> Color(0xFFFF8A65)
                            bot.skin == "Creeper Hunter" -> Color(0xFF43A047)
                            bot.skin == "Redstone Knight" -> Color(0xFFE53935)
                            else -> Color(0xFF78909C)
                        }
                    )
                    .border(2.dp, MineTextSecondary, RoundedCornerShape(4.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row {
                        Box(modifier = Modifier.size(6.dp).background(Color.White))
                        Spacer(modifier = Modifier.width(10.dp))
                        Box(modifier = Modifier.size(6.dp).background(Color.White))
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(modifier = Modifier.size(16.dp, 4.dp).background(Color.DarkGray))
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = bot.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MineTextPrimary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .background(MineCardStroke, RoundedCornerShape(4.dp))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Lvl ${bot.level}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MineGoldAccent
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Skin: ${bot.skin}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MineTextSecondary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.labelSmall,
                        color = MineTextSecondary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Status: ${bot.status}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (bot.isActive) MineGreenAccent else MineTextSecondary
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Button(
                    onClick = onToggleConnect,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (bot.isActive) MineRedstone else MineDiamondBlue,
                        contentColor = Color.Black
                    ),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                    modifier = Modifier
                        .testTag("connect_bot_${bot.id}")
                        .minimumInteractiveComponentSize()
                ) {
                    Text(
                        text = if (bot.isActive) "Disconnect" else "Join LAN",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                if (bot.isActive) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Icon(
                            Icons.Default.List,
                            contentDescription = "Ping",
                            tint = MineGreenAccent,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${bot.ping} ms",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = MineGreenAccent
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TerminalConsole(logs: List<String>) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF07090A))
            .border(1.dp, MineCardStroke, RoundedCornerShape(8.dp))
            .padding(10.dp)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            reverseLayout = true
        ) {
            items(logs) { log ->
                Text(
                    text = log,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = getLogColor(log),
                    modifier = Modifier.padding(vertical = 1.dp)
                )
            }
        }
    }
}

fun getLogColor(log: String): Color {
    return when {
        log.contains("error", ignoreCase = true) -> MineRedstone
        log.contains("joining", ignoreCase = true) || log.contains("joined", ignoreCase = true) -> MineGreenAccent
        log.contains("found", ignoreCase = true) || log.contains("successful", ignoreCase = true) -> MineDiamondBlue
        log.contains("LEVEL UP", ignoreCase = true) -> MineGoldAccent
        log.contains("PREMIUM", ignoreCase = true) -> MineDiamondBlue
        log.contains("DEVELOPER", ignoreCase = true) -> MineGoldAccent
        else -> MineTextPrimary
    }
}

@Composable
fun AddBotDialog(
    viewModel: CompanionViewModel,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var botName by remember { mutableStateOf("") }
    var selectedSkin by remember { mutableStateOf("Steve") }
    val skins = listOf("Steve", "Alex", "Creeper Hunter", "Redstone Knight")

    // Developer mode matching
    val isDeveloperMode by viewModel.isDeveloperMode.collectAsState()
    var devSearchText by remember { mutableStateOf("") }
    var selectedCheatCommand by remember { mutableStateOf("") }

    val cheatsList = listOf(
        "/gamemode creative - Turn client parameters into full creative grid",
        "/summon giant - Spawn a gigantic client block",
        "/tp steve 100 64 -500 - Teleport coordinates of Steve bot",
        "/time set day - Toggle server daylight mode",
        "/kill @e[type=creeper] - Instantly delete near creepers",
        "/give @p netherite_pickaxe - Arm active player with fully upgraded pickaxe"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isDeveloperMode) "🛠️ DEV PANEL ACTIVE" else "Spawn Local LAN Client",
                    color = if (isDeveloperMode) MineGoldAccent else MineDiamondBlue,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                if (isDeveloperMode) {
                    TextButton(onClick = { viewModel.disableDeveloperMode() }) {
                        Text("Disable Dev", color = MineRedstone, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Search bar inside Bot deployment to trigger developer mode
                OutlinedTextField(
                    value = devSearchText,
                    onValueChange = {
                        devSearchText = it
                        if (it.trim().lowercase() == "dev") {
                            viewModel.enableDeveloperMode()
                        }
                    },
                    label = { Text("Code Search Bar (Hint: type 'dev')", fontSize = 11.sp) },
                    placeholder = { Text("Type 'dev' to unlock developer cheats", fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MineGoldAccent,
                        unfocusedBorderColor = MineCardStroke
                    )
                )

                if (isDeveloperMode) {
                    Text(
                        "Developer Cheats (Ads Turned Off):",
                        style = MaterialTheme.typography.titleSmall,
                        color = MineGoldAccent,
                        fontWeight = FontWeight.Bold
                    )

                    // Search bar in developer mode as requested
                    var devCheatQuery by remember { mutableStateOf("") }
                    OutlinedTextField(
                        value = devCheatQuery,
                        onValueChange = { devCheatQuery = it },
                        placeholder = { Text("Search cheat commands...", fontSize = 11.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = MineTextSecondary, modifier = Modifier.size(14.dp)) },
                        modifier = Modifier.fillMaxWidth().height(46.dp),
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MineGoldAccent,
                            unfocusedBorderColor = MineCardStroke
                        )
                    )

                    val filteredCheats = if (devCheatQuery.isBlank()) cheatsList else cheatsList.filter { it.contains(devCheatQuery, ignoreCase = true) }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color.Black)
                            .border(1.dp, MineCardStroke, RoundedCornerShape(4.dp))
                            .padding(6.dp)
                    ) {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            items(filteredCheats) { cheat ->
                                Text(
                                    text = cheat,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = if (selectedCheatCommand == cheat) MineDiamondBlue else MineTextPrimary,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            selectedCheatCommand = cheat
                                            viewModel.addConsoleLog("Dev CLI: Executed cheat command '${cheat.substringBefore(" -")}'")
                                        }
                                        .padding(4.dp)
                                )
                            }
                        }
                    }

                    Text(
                        "Spawn an overpowered developer client directly from this mode.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MineTextSecondary
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text("Standard Bot Parameters:", fontWeight = FontWeight.Bold, fontSize = 11.sp)

                OutlinedTextField(
                    value = botName,
                    onValueChange = { botName = it },
                    label = { Text("Bot Client Name", fontSize = 11.sp) },
                    placeholder = { Text("e.g. RedstoneSteve", fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MineDiamondBlue,
                        unfocusedBorderColor = MineCardStroke
                    )
                )

                Text("Select Bot Avatar Skin:", fontWeight = FontWeight.Bold, fontSize = 11.sp)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    skins.forEach { skin ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (selectedSkin == skin) MineDiamondBlue.copy(alpha = 0.2f) else MineCardStroke)
                                .border(
                                    1.dp,
                                    if (selectedSkin == skin) MineDiamondBlue else MineCardStroke,
                                    RoundedCornerShape(6.dp)
                                )
                                .clickable { selectedSkin = skin }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = skin.replace(" ", "\n"),
                                fontSize = 9.sp,
                                textAlign = TextAlign.Center,
                                color = if (selectedSkin == skin) MineTextPrimary else MineTextSecondary
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val prefix = if (isDeveloperMode) "[OP_DEV] " else ""
                    onConfirm(prefix + botName, selectedSkin)
                },
                colors = ButtonDefaults.buttonColors(containerColor = if (isDeveloperMode) MineGoldAccent else MineGreenAccent)
            ) {
                Text(if (isDeveloperMode) "Spawn Overpowered Client" else "Spawn & Connect", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MineTextSecondary)
            }
        },
        containerColor = MineCardBg
    )
}
