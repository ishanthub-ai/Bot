package com.example.ui.tabs

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Info
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
import com.example.data.entity.BlueprintEntity
import com.example.ui.theme.*
import com.example.viewmodel.CompanionViewModel

@Composable
fun BlueprintsTab(
    viewModel: CompanionViewModel,
    modifier: Modifier = Modifier
) {
    val blueprints by viewModel.blueprints.collectAsState()
    val bots by viewModel.bots.collectAsState()
    val selectedBot by viewModel.selectedBot.collectAsState()

    val activeBots = bots.filter { it.isActive }
    var selectedBlueprint by remember { mutableStateOf<BlueprintEntity?>(null) }
    var projectScale by remember { mutableFloatStateOf(1.0f) }

    // Auto-select first blueprint
    if (selectedBlueprint == null && blueprints.isNotEmpty()) {
        selectedBlueprint = blueprints.first()
    }

    Row(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Left Column: Catalog list (weight 0.45)
        LazyColumn(
            modifier = Modifier
                .weight(0.9f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Text(
                    "Blueprint Market",
                    style = MaterialTheme.typography.titleMedium,
                    color = MineGreenAccent,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Offline local memory templates",
                    style = MaterialTheme.typography.labelSmall,
                    color = MineTextSecondary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            items(blueprints) { bp ->
                val isSelected = selectedBlueprint?.id == bp.id
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            selectedBlueprint = bp
                            projectScale = 1.0f // reset scale
                        },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) MineDiamondBlue.copy(alpha = 0.15f) else MineCardBg
                    ),
                    border = BorderStroke(1.dp, if (isSelected) MineDiamondBlue else MineCardStroke)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = bp.name,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MineTextPrimary
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Difficulty: ${bp.difficulty}",
                                fontSize = 10.sp,
                                color = MineTextSecondary
                            )
                            Text(
                                text = bp.type,
                                fontSize = 10.sp,
                                color = MineGoldAccent,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Custom import button mock
            item {
                Button(
                    onClick = {
                        viewModel.addConsoleLog("MCI Blueprint: Custom JSON blueprints can be dropped in. Verified offline import/export system.")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MineCardStroke),
                    modifier = Modifier.fillMaxWidth().padding(top = 10.dp).testTag("import_blueprint_btn"),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Text("Import Offline JSON", fontSize = 11.sp, color = MineTextPrimary)
                }
            }
        }

        // Right Column: Blueprint details & Material Estimator (weight 0.55)
        val currentBp = selectedBlueprint
        if (currentBp == null) {
            Box(
                modifier = Modifier
                    .weight(1.1f)
                    .fillMaxHeight()
                    .border(1.dp, MineCardStroke, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("Select a blueprint catalog entry", color = MineTextSecondary, textAlign = TextAlign.Center)
            }
        } else {
            Column(
                modifier = Modifier
                    .weight(1.1f)
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Detail Header
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MineCardBg),
                    border = BorderStroke(1.dp, MineCardStroke)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = currentBp.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = MineDiamondBlue,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = currentBp.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MineTextPrimary
                        )
                    }
                }

                // Layout Slices Graphic Simulator
                Text(
                    "Structural Foundation Layer Slice",
                    style = MaterialTheme.typography.titleSmall,
                    color = MineGoldAccent,
                    fontWeight = FontWeight.Bold
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF0F1213))
                        .border(1.dp, MineCardStroke, RoundedCornerShape(8.dp))
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Draw a 5x5 grid of Minecraft blocks to look like slice
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        repeat(5) { r ->
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                repeat(5) { c ->
                                    val blockColor = when {
                                        r == 0 || r == 4 || c == 0 || c == 4 -> {
                                            if (currentBp.type == "House") Color(0xFF8B5A2B) // Oak Wood Log trim
                                            else Color(0xFF708090) // Cobble Stone border
                                        }
                                        r == 2 && c == 2 -> {
                                            if (currentBp.type == "House") Color(0xFFE0F7FA) // Glass block glow
                                            else Color(0xFFFFCC80) // Torch fire glow
                                        }
                                        else -> Color(0xFF2E7D32) // Grass dirt fill
                                    }
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clip(RoundedCornerShape(2.dp))
                                            .background(blockColor)
                                            .border(1.dp, Color.Black.copy(alpha = 0.3f), RoundedCornerShape(2.dp))
                                    )
                                }
                            }
                        }
                    }
                }

                // Material Requirement Calculator Slider
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MineCardBg),
                    border = BorderStroke(1.dp, MineCardStroke)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Material Calculator",
                                style = MaterialTheme.typography.titleSmall,
                                color = MineGoldAccent,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Multiplier: x${projectScale.toInt()}",
                                fontSize = 11.sp,
                                color = MineDiamondBlue,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Slider to change scale multiplier
                        Slider(
                            value = projectScale,
                            onValueChange = { projectScale = it },
                            valueRange = 1.0f..10.0f,
                            steps = 8,
                            colors = SliderDefaults.colors(
                                thumbColor = MineDiamondBlue,
                                activeTrackColor = MineDiamondBlue,
                                inactiveTrackColor = MineCardStroke
                            ),
                            modifier = Modifier.testTag("scale_slider")
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Table of required raw resources
                        Text("Estimated Raw Blocks Required:", fontSize = 10.sp, color = MineTextSecondary)
                        Spacer(modifier = Modifier.height(4.dp))

                        val mult = projectScale.toInt()
                        ResourceRow(name = "Cobblestone / Stone", base = currentBp.stoneCount, scale = mult, icon = "🪨")
                        ResourceRow(name = "Oak logs / Wood Planks", base = currentBp.woodCount, scale = mult, icon = "🪵")
                        ResourceRow(name = "Iron Ore Barings", base = currentBp.ironCount, scale = mult, icon = "🧱")
                        ResourceRow(name = "Glass Window Panes", base = currentBp.glassCount, scale = mult, icon = "🥛")
                    }
                }

                // Button: Schedule Blueprint Build Job
                val botToAssign = activeBots.firstOrNull()
                Button(
                    onClick = {
                        if (botToAssign != null) {
                            val scaling = projectScale.toInt()
                            viewModel.addNewTask(
                                "Build ${currentBp.name} (x$scaling)",
                                "Carry out offline autonomous construct template protocols for ${currentBp.name} multiplied directly to scale size x$scaling.",
                                "Building"
                            )
                            viewModel.addConsoleLog("Scribing blueprint parameters to ${botToAssign.name}: Build scheduled for ${currentBp.name} x$scaling.")
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("schedule_blueprint_btn")
                        .minimumInteractiveComponentSize(),
                    enabled = botToAssign != null,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MineGreenAccent,
                        disabledContainerColor = MineCardStroke,
                        contentColor = Color.Black,
                        disabledContentColor = MineTextSecondary
                    )
                ) {
                    Icon(Icons.Default.Build, contentDescription = "Deploy", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (botToAssign != null) "Deploy Blueprint Build" else "No connected Bot to build",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun ResourceRow(name: String, base: Int, scale: Int, icon: String) {
    if (base > 0) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 3.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(icon, fontSize = 12.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(name, fontSize = 11.sp, color = MineTextPrimary)
            }
            Text(
                text = "${base * scale} blocks",
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = MineGoldAccent
            )
        }
    }
}
