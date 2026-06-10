package com.example.ui.tabs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.BotEntity
import com.example.ui.theme.*
import com.example.viewmodel.CompanionViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun BotDashboardTab(
    viewModel: CompanionViewModel,
    modifier: Modifier = Modifier
) {
    val bots by viewModel.bots.collectAsState()
    val selectedBot by viewModel.selectedBot.collectAsState()

    val activeBots = bots.filter { it.isActive }

    if (activeBots.isEmpty()) {
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
                    Icons.Default.Warning,
                    contentDescription = "No bots",
                    tint = MineGoldAccent,
                    modifier = Modifier.size(64.dp)
                )
                Text(
                    text = "No Connected LAN Companions",
                    style = MaterialTheme.typography.titleLarge,
                    color = MineTextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "To monitor bot coords, level skills, view raw inventories or pathfinding radars, go to the Lobby tab and click 'Join LAN' first.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MineTextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.widthIn(max = 340.dp)
                )
            }
        }
    } else {
        // Ensure there is a selected connected bot
        val currentBot = selectedBot?.takeIf { it.isActive } ?: activeBots.first()

        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Horizontal Connected Bot Selector
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                activeBots.forEach { bot ->
                    val isSelected = currentBot.id == bot.id
                    Card(
                        modifier = Modifier
                            .clickable { viewModel.selectBot(bot) }
                            .widthIn(min = 120.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MineDiamondBlue.copy(alpha = 0.15f) else MineCardBg
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) MineDiamondBlue else MineCardStroke
                        )
                    ) {
                        Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(bot.name, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MineTextPrimary)
                            Text("Lvl ${bot.level}", fontSize = 10.sp, color = MineGoldAccent)
                        }
                    }
                }
            }

            // Main Stats grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Left Column: Vitals Card
                Column(
                    modifier = Modifier.weight(1.1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    VitalsCard(bot = currentBot)
                    SkillsCard(bot = currentBot, onTrain = {
                        // Fast train mock trigger
                        val updatedBot = currentBot.copy(
                            xp = currentBot.xp + 80,
                            level = if (currentBot.xp + 80 >= currentBot.level * 500) currentBot.level + 1 else currentBot.level
                        )
                        viewModel.viewModelScope.launch {
                            viewModel.toggleBotConnection(currentBot) // force state refresh
                        }
                        viewModel.addConsoleLog("Manually triggered tactical EXP training program for ${currentBot.name}")
                    })
                }

                // Right Column: Pathfinding Live Map
                Column(
                    modifier = Modifier.weight(0.9f)
                ) {
                    PathfindingRadarCard(bot = currentBot)
                }
            }

            // Inventory Chest Viewer
            BotInventoryViewer(bot = currentBot, onDropItem = { item, quantity ->
                viewModel.addConsoleLog("${currentBot.name} shared item: $quantity x $item with host player.")
            })
        }
    }
}

@Composable
fun VitalsCard(bot: BotEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MineCardBg),
        border = BorderStroke(1.dp, MineCardStroke)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                "Companion Vitals",
                style = MaterialTheme.typography.titleSmall,
                color = MineDiamondBlue,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))

            // Health Heart Bar Rendering (10 hearts total)
            Text(
                "Health Stat: ${bot.health}/20 HP",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MineTextSecondary
            )
            Row(modifier = Modifier.padding(vertical = 4.dp)) {
                repeat(10) { index ->
                    val heartFilled = (index * 2) < bot.health
                    val isHalf = (index * 2) + 1 == bot.health
                    Text(
                        text = if (heartFilled) "❤" else "🖤", 
                        color = if (heartFilled) MineRedstone else MineTextSecondary.copy(alpha = 0.3f),
                        fontSize = 18.sp,
                        modifier = Modifier.padding(end = 1.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Hunger Chicken leg Bar
            Text(
                "Hunger Level: ${bot.hunger}/20",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MineTextSecondary
            )
            Row(modifier = Modifier.padding(vertical = 4.dp)) {
                repeat(10) { index ->
                    val foodFilled = (index * 2) < bot.hunger
                    Text(
                        text = if (foodFilled) "🍗" else "🦴",
                        fontSize = 16.sp,
                        modifier = Modifier.padding(end = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Navigation Coords
            Divider(color = MineCardStroke, thickness = 1.dp)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("LAN Space Coordinates", fontSize = 10.sp, color = MineTextSecondary)
                    Text(
                        "X: ${bot.posX}   Y: ${bot.posY}   Z: ${bot.posZ}",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MineTextPrimary
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            when (bot.dimension) {
                                "Nether" -> MineRedstone.copy(alpha = 0.2f)
                                "The End" -> Color(0xFF9C27B0).copy(alpha = 0.2f)
                                else -> MineGreenAccent.copy(alpha = 0.2f)
                            }
                        )
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = bot.dimension,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (bot.dimension) {
                            "Nether" -> Color(0xFFFF5252)
                            "The End" -> Color(0xFFE040FB)
                            else -> MineGreenAccent
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SkillsCard(
    bot: BotEntity,
    onTrain: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MineCardBg),
        border = BorderStroke(1.dp, MineCardStroke)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Progression Tree",
                        style = MaterialTheme.typography.titleSmall,
                        color = MineGoldAccent,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Current LEVEL ${bot.level}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MineTextSecondary
                    )
                }
                TextButton(
                    onClick = onTrain,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.minimumInteractiveComponentSize()
                ) {
                    Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Train stats", tint = MineGoldAccent)
                    Spacer(modifier = Modifier.width(2.dp))
                    Text("Train XP", fontSize = 11.sp, color = MineGoldAccent, fontWeight = FontWeight.Bold)
                }
            }

            // XP level progress
            val required = bot.level * 500
            val ratio = (bot.xp.toFloat() / required).coerceAtMost(1.0f)
            LinearProgressIndicator(
                progress = { ratio },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = MineGreenAccent,
                trackColor = MineCardStroke
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 2.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    "${bot.xp} / ${required} XP to Next",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = MineGreenAccent
                )
            }

            // Skills detail lists
            SkillIndicatorRow(name = "⚒ Mining Mastery", lvl = bot.miningLvl, color = MineDiamondBlue)
            Spacer(modifier = Modifier.height(6.dp))
            SkillIndicatorRow(name = "🛡 Combat Rating", lvl = bot.combatLvl, color = MineRedstone)
            Spacer(modifier = Modifier.height(6.dp))
            SkillIndicatorRow(name = "🏡 Construction", lvl = bot.buildLvl, color = MineGoldAccent)
        }
    }
}

@Composable
fun SkillIndicatorRow(name: String, lvl: Int, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(name, fontSize = 11.sp, color = MineTextPrimary)
        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            // Render star indicators or progress bars
            repeat(5) { index ->
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(if (index < lvl) color else MineCardStroke)
                )
            }
        }
    }
}

@Composable
fun PathfindingRadarCard(bot: BotEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MineCardBg),
        border = BorderStroke(1.dp, MineCardStroke)
    ) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "3D Path Radar Map",
                style = MaterialTheme.typography.titleSmall,
                color = MineDiamondBlue,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                "Realtime LAN coordinate tracking",
                style = MaterialTheme.typography.labelSmall,
                color = MineTextSecondary,
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
            )

            // Dynamic Minimap Canvas Drawing
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.0f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF101416))
                    .border(2.dp, MineCardStroke, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                // Drawing Canvas
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val centerWidth = size.width / 2
                    val centerHeight = size.height / 2
                    val maxRadius = centerWidth * 0.85f

                    // Draw circular radar bounds
                    drawCircle(
                        color = MineCardStroke,
                        radius = maxRadius,
                        center = Offset(centerWidth, centerHeight),
                        style = Stroke(width = 1.dp.toPx())
                    )
                    drawCircle(
                        color = MineCardStroke,
                        radius = maxRadius / 2,
                        center = Offset(centerWidth, centerHeight),
                        style = Stroke(width = 1.dp.toPx())
                    )

                    // Draw grid axes
                    drawLine(
                        color = MineCardStroke.copy(alpha = 0.5f),
                        start = Offset(centerWidth - maxRadius, centerHeight),
                        end = Offset(centerWidth + maxRadius, centerHeight)
                    )
                    drawLine(
                        color = MineCardStroke.copy(alpha = 0.5f),
                        start = Offset(centerWidth, centerHeight - maxRadius),
                        end = Offset(centerWidth, centerHeight + maxRadius)
                    )

                    // Coordinate representation: Target coordinates are around bot position
                    // We'll simulate players, diamond ore, mobs around bot center.
                    // Let's place:
                    // Host player (always green triangle near center)
                    // Home cross (gold symbol)
                    // Diamonds (cyan eye)
                    // Zombie (red circle)
                    
                    // Host Player at relative delta coords
                    val playerRelX = 35f
                    val playerRelY = -40f
                    drawCircle(
                        color = MineGreenAccent,
                        radius = 6.dp.toPx(),
                        center = Offset(centerWidth + playerRelX, centerHeight + playerRelY)
                    )

                    // Bot Client (cyan square at center representing viewport focus)
                    val botSizeSq = 12.dp.toPx()
                    drawRect(
                        color = MineDiamondBlue,
                        topLeft = Offset(centerWidth - botSizeSq/2, centerHeight - botSizeSq/2),
                        size = androidx.compose.ui.geometry.Size(botSizeSq, botSizeSq)
                    )

                    // Diamond ore deposits (cyan spots)
                    val diamondX = -60f
                    val diamondY = 50f
                    drawCircle(
                        color = Color(0xFF00E5FF),
                        radius = 4.dp.toPx(),
                        center = Offset(centerWidth + diamondX, centerHeight + diamondY)
                    )

                    // Hostile Zombie mob (red eye)
                    val zombieX = 85f
                    val zombieY = 60f
                    drawCircle(
                        color = MineRedstone,
                        radius = 5.dp.toPx(),
                        center = Offset(centerWidth + zombieX, centerHeight + zombieY)
                    )

                    // Pathfinding line connector (bot to player)
                    drawLine(
                        color = MineDiamondBlue.copy(alpha = 0.7f),
                        start = Offset(centerWidth, centerHeight),
                        end = Offset(centerWidth + playerRelX, centerHeight + playerRelY),
                        strokeWidth = 2.dp.toPx()
                    )
                }

                // Small legend overlays
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 6.dp)
                        .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LegendItem("Bot", MineDiamondBlue)
                    LegendItem("Player", MineGreenAccent)
                    LegendItem("Ores", Color(0xFF00E5FF))
                    LegendItem("Mobs", MineRedstone)
                }
            }
        }
    }
}

@Composable
fun LegendItem(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        Box(modifier = Modifier.size(6.dp).clip(RoundedCornerShape(1.dp)).background(color))
        Text(label, fontSize = 9.sp, color = MineTextPrimary)
    }
}

// Simulated Inventory Grid Item structure
data class InvItem(
    val icon: String,
    val name: String,
    val count: Int,
    val color: Color
)

@Composable
fun BotInventoryViewer(
    bot: BotEntity,
    onDropItem: (String, Int) -> Unit
) {
    // Inventory pool
    val inventoryList = remember(bot.id) {
        listOf(
            InvItem("⛏", "Diamond Pickaxe", 1, MineDiamondBlue),
            InvItem("🗡", "Iron Sword", 1, Color(0xFFECEFF1)),
            InvItem("🧱", "Diamond Ore", 9, MineDiamondBlue),
            InvItem("🪵", "Oak Logs", 48, Color(0xFFFFB74D)),
            InvItem("🪨", "Cobblestone", 64, Color(0xFF90A4AE)),
            InvItem("🪨", "Cobblestone", 32, Color(0xFF90A4AE)),
            InvItem("🌾", "Wheat seeds", 16, MineGreenAccent),
            InvItem("🍎", "Red Apple", 4, MineRedstone),
            InvItem("🥩", "Cooked Mutton", 6, Color(0xFFFF8A80))
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MineCardBg),
        border = BorderStroke(1.dp, MineCardStroke)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                "Companion Inventory Chest",
                style = MaterialTheme.typography.titleSmall,
                color = MineGoldAccent,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Automatic collection during online gameplay session",
                style = MaterialTheme.typography.labelSmall,
                color = MineTextSecondary,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Minecraft Inventory 3x3 Chest Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 240.dp)
            ) {
                items(inventoryList) { item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onDropItem(item.name, item.count) },
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF141819)),
                        border = BorderStroke(1.dp, MineCardStroke)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(10.dp)
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = item.icon,
                                fontSize = 28.sp,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            Text(
                                text = item.name,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MineTextPrimary,
                                maxLines = 1,
                                textAlign = TextAlign.Center
                            )
                            Box(
                                modifier = Modifier
                                    .padding(top = 4.dp)
                                    .align(Alignment.End)
                                    .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(2.dp))
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = "x${item.count}",
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = MineGoldAccent,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
