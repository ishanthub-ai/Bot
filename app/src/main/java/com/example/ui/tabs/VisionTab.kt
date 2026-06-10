package com.example.ui.tabs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.viewmodel.CompanionViewModel
import com.example.viewmodel.DetectedObject

@Composable
fun VisionTab(
    viewModel: CompanionViewModel,
    modifier: Modifier = Modifier
) {
    val isScanning by viewModel.isScanningLan.collectAsState() // Scan indicators
    val isVisionScanning by viewModel.isVisionScanning.collectAsState()
    val detections by viewModel.visionDetections.collectAsState()
    val imageIndex by viewModel.currentVisionImageIndex.collectAsState()
    val modelInfo by viewModel.visionModelStatus.collectAsState()

    val scenes = listOf("Underground Cave Ores", "Cottage Woods Exterior", "Monster Dungeon Spawner")

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Vision parameters header
        item {
            VisionModelHeader(modelInfo = modelInfo)
        }

        // Camera Slide Selector
        item {
            Text(
                "Offline Camera Feed / Screenshot Slide Selector",
                style = MaterialTheme.typography.titleSmall,
                color = MineGoldAccent,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                scenes.forEachIndexed { idx, title ->
                    val isSelected = imageIndex == idx
                    Box(
                        modifier = Modifier
                            .weight(1.0f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSelected) MineDiamondBlue.copy(alpha = 0.2f) else MineCardBg)
                            .border(1.dp, if (isSelected) MineDiamondBlue else MineCardStroke, RoundedCornerShape(6.dp))
                            .clickable { viewModel.changeVisionImageIndex(idx) }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = title.replace(" ", "\n"),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            color = if (isSelected) MineTextPrimary else MineTextSecondary
                        )
                    }
                }
            }
        }

        // Visual Viewer Sandbox Viewport
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.77f) // 16:9 aspect ratio standard player
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black)
                    .border(2.dp, MineCardStroke, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                // Draw simulated pixel screenshot depending on model
                MinecraftSimulatedScene(index = imageIndex)

                // Overlay bounding boxes canvas
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val canvasWidth = size.width
                    val canvasHeight = size.height

                    detections.forEach { obj ->
                        val xMinPx = obj.xMin * canvasWidth
                        val yMinPx = obj.yMin * canvasHeight
                        val boxWidth = (obj.xMax - obj.xMin) * canvasWidth
                        val boxHeight = (obj.yMax - obj.yMin) * canvasHeight
                        val drawColor = Color(android.graphics.Color.parseColor(obj.colorHex))

                        // Draw bounding rectangular frame
                        drawRect(
                            color = drawColor,
                            topLeft = Offset(xMinPx, yMinPx),
                            size = Size(boxWidth, boxHeight),
                            style = Stroke(width = 2.dp.toPx())
                        )
                    }
                }

                // Custom Overlay Anchor Tags
                detections.forEach { obj ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(2.dp)
                    ) {
                        // Place dynamic tags (this mimics text layout on graphics canvas)
                        val drawColor = Color(android.graphics.Color.parseColor(obj.colorHex))
                        Box(
                            modifier = Modifier
                                .absoluteOffset(
                                    x = (obj.xMin * 260).dp, // approximate coordinate multipliers
                                    y = (obj.yMin * 140).dp
                                )
                                .background(drawColor.copy(alpha = 0.85f), RoundedCornerShape(2.dp))
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "${obj.label} ${(obj.confidence * 100).toInt()}%",
                                fontSize = 8.sp,
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                if (isVisionScanning) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.6f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CircularProgressIndicator(color = MineDiamondBlue, modifier = Modifier.size(28.dp))
                            Text(
                                "Local ML Tensor Inference...",
                                fontSize = 11.sp,
                                color = MineDiamondBlue,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                } else if (detections.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.3f))
                            .clickable { viewModel.executeVisionScan() },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = "Ready to scan",
                                tint = MineDiamondBlue,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Tap Here to Scan Frame",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MineTextPrimary
                            )
                            Text(
                                "Detect blocks & Bedrock objects offline",
                                fontSize = 9.sp,
                                color = MineTextSecondary
                            )
                        }
                    }
                }
            }
        }

        // Run analyzer trigger button
        item {
            Button(
                onClick = { viewModel.executeVisionScan() },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("run_vision_scan")
                    .minimumInteractiveComponentSize(),
                colors = ButtonDefaults.buttonColors(containerColor = MineDiamondBlue, contentColor = Color.Black),
                enabled = !isVisionScanning
            ) {
                Text("Analyze Frame (Offline Neural CV)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }

        // Table list of model outcomes
        if (detections.isNotEmpty() && !isVisionScanning) {
            item {
                Text(
                    "Tensorflow Detection Outcomes:",
                    style = MaterialTheme.typography.titleSmall,
                    color = MineGreenAccent,
                    fontWeight = FontWeight.Bold
                )
            }

            items(detections) { detection ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MineCardBg),
                    border = BorderStroke(1.dp, MineCardStroke)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(android.graphics.Color.parseColor(detection.colorHex)))
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = detection.label,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MineTextPrimary
                            )
                        }
                        Text(
                            text = "Accuracy: ${(detection.confidence * 100).toInt()}%",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = MineGoldAccent
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun VisionModelHeader(modelInfo: com.example.viewmodel.VisionModelInfo) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MineCardBg),
        border = BorderStroke(1.dp, MineCardStroke)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Info, contentDescription = "Neural Hub", tint = MineDiamondBlue, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1.0f)) {
                Text(
                    text = "On-Device block Classifier",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MineTextPrimary
                )
                Text(
                    text = "Model: ${modelInfo.name}  •  File: ${modelInfo.size}",
                    fontSize = 10.sp,
                    color = MineTextSecondary
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(MineGreenAccent.copy(alpha = 0.2f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = modelInfo.precision,
                    fontSize = 10.sp,
                    color = MineGreenAccent,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
fun MinecraftSimulatedScene(index: Int) {
    // In order to make the screenshot look incredibly real, we can paint a layered color gradient background
    // representing Minecraft ores, stone columns, lava lakes!
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                when (index) {
                    0 -> Brush.verticalGradient(
                        colors = listOf(Color(0xFF212121), Color(0xFF141414), Color(0xFFE64A19)) // lava cave depths glow
                    )
                    1 -> Brush.verticalGradient(
                        colors = listOf(Color(0xFF81D4FA), Color(0xFFE8F5E9), Color(0xFF4CAF50)) // sky and grass
                    )
                    else -> Brush.verticalGradient(
                        colors = listOf(Color(0xFF37474F), Color(0xFF263238), Color(0xFF0F1213)) // dark spawner dungeon
                    )
                }
            )
            .padding(12.dp)
    ) {
        // Draw basic layout representations
        when (index) {
            0 -> {
                // Underground Cave
                Column(verticalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxSize()) {
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("🧱 Deepslate Vault", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                        Text("Y = -54", fontSize = 9.sp, color = MineDiamondBlue, fontFamily = FontFamily.Monospace)
                    }
                    Text(
                        text = "💎    💎\n\n\n     🔥 LAVA LAKE",
                        color = MineGoldAccent,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            1 -> {
                // Forest exterior
                Column(verticalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxSize()) {
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("🏡 Survival Meadow", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        Text("Y = 64", fontSize = 9.sp, color = MineGreenAccent, fontFamily = FontFamily.Monospace)
                    }
                    Text(
                        text = "🪵 OAK LOGS    🏠 WOOD ROOF\n\n\n     👾 CREEPER IN PROGRESS",
                        color = MineRedstone,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            else -> {
                // Spawner Dungeon
                Column(verticalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxSize()) {
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("💀 Stone Dungeon", fontSize = 10.sp, color = Color.LightGray, fontWeight = FontWeight.Bold)
                        Text("Y = 12", fontSize = 9.sp, color = MineRedstone, fontFamily = FontFamily.Monospace)
                    }
                    Text(
                        text = "📦 MOB SPAWNER\n\n\n    🧟 ZOMBIE    💀 SKELETON OUTBREAK",
                        color = MineTextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
