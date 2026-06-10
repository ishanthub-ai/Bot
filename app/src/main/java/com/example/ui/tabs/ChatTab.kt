package com.example.ui.tabs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.ChatMessage
import com.example.ui.theme.*
import com.example.viewmodel.CompanionViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ChatTab(
    viewModel: CompanionViewModel,
    modifier: Modifier = Modifier
) {
    val bots by viewModel.bots.collectAsState()
    val selectedBot by viewModel.selectedBot.collectAsState()
    val chatMessages by viewModel.chatMessages.collectAsState()

    val activeBots = bots.filter { it.isActive }
    val currentBot = selectedBot?.takeIf { it.isActive } ?: activeBots.firstOrNull()

    var textInput by remember { mutableStateOf("") }
    var voiceModeActive by remember { mutableStateOf(false) }
    var simulatedVoiceTranscript by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    // Scroll chat list to bottom whenever messages arrive
    LaunchedEffect(chatMessages.size) {
        if (chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    if (activeBots.isEmpty() || currentBot == null) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    Icons.Default.MailOutline,
                    contentDescription = "No Chat Active",
                    tint = MineDiamondBlue,
                    modifier = Modifier.size(64.dp)
                )
                Text(
                    text = "No Companions Online",
                    style = MaterialTheme.typography.titleLarge,
                    color = MineTextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Connect a LAN client in the Lobby tab to open active local chat channels with your helper bot.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MineTextSecondary,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.widthIn(max = 300.dp)
                )
            }
        }
    } else {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Channel Header Selector
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MineCardBg),
                border = BorderStroke(1.dp, MineCardStroke)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(RoundedCornerShape(5.dp))
                                .background(MineGreenAccent)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                "Local AI Companion Link",
                                fontSize = 11.sp,
                                color = MineTextSecondary
                            )
                            Text(
                                "Connected: ${currentBot.name}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MineDiamondBlue
                            )
                        }
                    }

                    // Bot Quick personality settings
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Box(
                            modifier = Modifier
                                .background(MineCardStroke, RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                "Worker Brain",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = MineGoldAccent
                            )
                        }
                    }
                }
            }

            // Quick Voice Command Trigger Panel
            Text(
                "Offline Speech Commands (STT Sim)",
                style = MaterialTheme.typography.titleSmall,
                color = MineGoldAccent,
                fontWeight = FontWeight.Bold
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MineCardBg, RoundedCornerShape(8.dp))
                    .border(1.dp, MineCardStroke, RoundedCornerShape(8.dp))
                    .padding(8.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val voiceCommands = listOf(
                    "Follow me",
                    "Mine diamonds",
                    "Build a house",
                    "Protect me",
                    "Go home",
                    "Gather wood",
                    "Stop working"
                )

                voiceCommands.forEach { phrase ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(MineCardStroke)
                            .clickable {
                                // Simulate Voice Trigger Speaking
                                coroutineScope.launch {
                                    voiceModeActive = true
                                    simulatedVoiceTranscript = phrase
                                    delay(1000)
                                    viewModel.sendPlayerMessage(phrase)
                                    voiceModeActive = false
                                }
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Speak", tint = MineDiamondBlue, modifier = Modifier.size(12.dp))
                            Text(phrase, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MineTextPrimary)
                        }
                    }
                }
            }

            // Chat Scroll Stream
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.0f)
                    .background(Color(0xFF0F1112), RoundedCornerShape(10.dp))
                    .border(1.dp, MineCardStroke, RoundedCornerShape(10.dp))
                    .padding(8.dp)
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(chatMessages) { msg ->
                        ChatMessageItem(msg = msg, currentBotName = currentBot.name)
                    }
                }

                // Speaking Voice Overlay animation
                if (voiceModeActive) {
                    Card(
                        modifier = Modifier.align(Alignment.Center),
                        colors = CardDefaults.cardColors(containerColor = Color.Black),
                        border = BorderStroke(1.dp, MineDiamondBlue),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CircularProgressIndicator(color = MineDiamondBlue, modifier = Modifier.size(24.dp))
                            Text("Microphone Active...", fontSize = 11.sp, color = MineTextSecondary)
                            Text(
                                text = "\"$simulatedVoiceTranscript\"",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MineDiamondBlue,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }

            // Chat Input Box
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = textInput,
                    onValueChange = { textInput = it },
                    placeholder = { Text("Command bot client locally...") },
                    modifier = Modifier
                        .weight(1.0f)
                        .testTag("chat_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MineDiamondBlue,
                        unfocusedBorderColor = MineCardStroke
                    )
                )

                IconButton(
                    onClick = {
                        if (textInput.isNotBlank()) {
                            viewModel.sendPlayerMessage(textInput)
                            textInput = ""
                        }
                    },
                    modifier = Modifier
                        .testTag("send_chat_button")
                        .size(48.dp)
                        .background(MineGreenAccent, RoundedCornerShape(6.dp))
                ) {
                    Icon(
                        Icons.Default.Send,
                        contentDescription = "Send",
                        tint = Color.Black
                    )
                }
            }
        }
    }
}

@Composable
fun ChatMessageItem(msg: ChatMessage, currentBotName: String) {
    val isPlayer = msg.sender == "Player"
    val alignEnd = isPlayer

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = if (alignEnd) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Column(
            horizontalAlignment = if (alignEnd) Alignment.End else Alignment.Start,
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Text(
                text = msg.sender,
                fontSize = 10.sp,
                color = if (isPlayer) MineGreenAccent else MineDiamondBlue,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
            )

            Box(
                modifier = Modifier
                    .clip(
                        RoundedCornerShape(
                            topStart = 8.dp,
                            topEnd = 8.dp,
                            bottomStart = if (alignEnd) 8.dp else 0.dp,
                            bottomEnd = if (alignEnd) 0.dp else 8.dp
                        )
                    )
                    .background(if (isPlayer) MineGreenAccent.copy(alpha = 0.15f) else MineCardBg)
                    .border(
                        1.dp,
                        if (isPlayer) MineGreenAccent.copy(alpha = 0.4f) else MineCardStroke,
                        RoundedCornerShape(
                            topStart = 8.dp,
                            topEnd = 8.dp,
                            bottomStart = if (alignEnd) 8.dp else 0.dp,
                            bottomEnd = if (alignEnd) 0.dp else 8.dp
                        )
                    )
                    .padding(8.dp)
            ) {
                Text(
                    text = msg.message,
                    fontSize = 12.sp,
                    color = MineTextPrimary,
                    fontFamily = FontFamily.SansSerif
                )
            }
        }
    }
}
