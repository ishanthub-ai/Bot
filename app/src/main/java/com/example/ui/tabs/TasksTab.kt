package com.example.ui.tabs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import com.example.data.entity.TaskEntity
import com.example.data.db.AppDatabase
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import com.example.ui.theme.*
import com.example.viewmodel.CompanionViewModel

@Composable
fun TasksTab(
    viewModel: CompanionViewModel,
    modifier: Modifier = Modifier
) {
    val bots by viewModel.bots.collectAsState()
    val tasks by viewModel.tasks.collectAsState()
    val selectedBot by viewModel.selectedBot.collectAsState()

    val activeBots = bots.filter { it.isActive }
    var showAddTaskDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome and Quick Spawn stats
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "AI Mission Directives",
                    style = MaterialTheme.typography.titleMedium,
                    color = MineGreenAccent,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${tasks.size} orders scheduled in database",
                    style = MaterialTheme.typography.labelSmall,
                    color = MineTextSecondary
                )
            }
            if (activeBots.isNotEmpty()) {
                Button(
                    onClick = { showAddTaskDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = MineDiamondBlue, contentColor = Color.Black),
                    modifier = Modifier
                        .testTag("add_task_trigger")
                        .minimumInteractiveComponentSize()
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add task")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New Mission", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Quick Preset Mission Templates
        if (activeBots.isNotEmpty()) {
            Text(
                "Preset Minecraft Action Blueprints",
                style = MaterialTheme.typography.titleSmall,
                color = MineGoldAccent,
                fontWeight = FontWeight.Bold
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PresetTaskBadge(title = "⛏ Deep Ore Dig Y=-58", category = "Mining", onSelect = {
                    viewModel.addNewTask(
                        "Deep Ore Shaft Tunnel",
                        "Excavate branch mines coordinates deep down near bedrock layers looking for diamonds.",
                        "Mining"
                    )
                })
                PresetTaskBadge(title = "🏹 Sentry Protection", category = "Combat", onSelect = {
                    viewModel.addNewTask(
                        "Perimeter Sentinel Sweep",
                        "Activate AI combat sweep program surrounding player home to suppress spiders and creepers.",
                        "Combat"
                    )
                })
                PresetTaskBadge(title = "🌾 Auto Harvest Crop", category = "Farming", onSelect = {
                    viewModel.addNewTask(
                        "Automatic Wheats & Carrots",
                        "Locate dynamic water irrigation channels to plant wheat crops and feed surrounding sheep.",
                        "Farming"
                    )
                })
            }
        }

        Divider(color = MineCardStroke)

        // Subtask lists
        if (tasks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.0f)
                    .border(1.dp, MineCardStroke, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No mission tasks yet. Connect a LAN Bot and tap 'New Mission' to queue AI routines.",
                    textAlign = TextAlign.Center,
                    color = MineTextSecondary,
                    modifier = Modifier.padding(24.dp)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.0f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(tasks) { task ->
                    val assignedBot = bots.find { it.id == task.botId }
                    TaskItemCard(
                        task = task,
                        botName = assignedBot?.name ?: "Unknown Bot",
                        onStatusChange = { newStatus ->
                            val progressValue = if (newStatus == "Completed") 1.0f else task.progress
                            viewModel.updateTaskStatus(task, newStatus, progressValue)
                        },
                        onDelete = {
                            viewModel.viewModelScope.launch {
                                viewModel.deleteTaskStatus(task) // If custom DB delete is inside Dao/Repo
                            }
                        }
                    )
                }
            }
        }
    }

    if (showAddTaskDialog) {
        val activeSelectedBot = selectedBot ?: activeBots.firstOrNull()
        if (activeSelectedBot != null) {
            AddTaskDialog(
                bot = activeSelectedBot,
                onDismiss = { showAddTaskDialog = false },
                onConfirm = { title, desc, cat ->
                    viewModel.addNewTask(title, desc, cat)
                    showAddTaskDialog = false
                }
            )
        }
    }
}

// Extension function to repo/viewmodel mock deletion stability
fun CompanionViewModel.deleteTaskStatus(task: TaskEntity) {
    viewModelScope.launch {
        // Just delete from DB via repository
        viewModelScope.launch {
            val db = AppDatabase.getDatabase(getApplication())
            db.companionDao().deleteTask(task)
            addConsoleLog("Deleted mission task directive: \"${task.title}\"")
        }
    }
}

@Composable
fun PresetTaskBadge(
    title: String,
    category: String,
    onSelect: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(MineCardBg)
            .border(1.dp, MineCardStroke, RoundedCornerShape(6.dp))
            .clickable { onSelect() }
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Text(title, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MineTextPrimary)
    }
}

@Composable
fun TaskItemCard(
    task: TaskEntity,
    botName: String,
    onStatusChange: (String) -> Unit,
    onDelete: () -> Unit
) {
    val categoryColor = when (task.category) {
        "Mining" -> MineDiamondBlue
        "Building" -> MineGoldAccent
        "Farming" -> MineGreenAccent
        "Combat" -> MineRedstone
        else -> MineTextSecondary
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MineCardBg),
        border = BorderStroke(
            1.dp,
            if (task.status == "In Progress") categoryColor.copy(alpha = 0.5f) else MineCardStroke
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(categoryColor)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MineTextPrimary
                    )
                }

                // Status Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            when (task.status) {
                                "Completed" -> MineGreenAccent.copy(alpha = 0.2f)
                                "In Progress" -> MineDiamondBlue.copy(alpha = 0.2f)
                                else -> MineTextSecondary.copy(alpha = 0.2f)
                            }
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = task.status,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (task.status) {
                            "Completed" -> MineGreenAccent
                            "In Progress" -> MineDiamondBlue
                            else -> MineTextSecondary
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Assigned to: $botName  •  Category: ${task.category}",
                fontSize = 11.sp,
                color = MineTextSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = task.description,
                style = MaterialTheme.typography.bodySmall,
                color = MineTextPrimary
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Progress Slider-like bar indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "Progress:",
                    fontSize = 10.sp,
                    color = MineTextSecondary
                )
                LinearProgressIndicator(
                    progress = { task.progress },
                    modifier = Modifier
                        .weight(1.0f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = categoryColor,
                    trackColor = MineCardStroke
                )
                Text(
                    "${(task.progress * 100).toInt()}%",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = MineTextPrimary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Divider(color = MineCardStroke)
            Spacer(modifier = Modifier.height(8.dp))

            // Task completion actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (task.status != "Completed") {
                    Button(
                        onClick = { onStatusChange("Completed") },
                        colors = ButtonDefaults.buttonColors(containerColor = MineGreenAccent, contentColor = Color.Black),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                        modifier = Modifier
                            .testTag("complete_task_${task.id}")
                            .minimumInteractiveComponentSize()
                    ) {
                        Text("Sign Off Complete", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .testTag("delete_task_${task.id}")
                        .size(36.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete directive", tint = MineRedstone, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@Composable
fun AddTaskDialog(
    bot: BotEntity,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Mining") }
    val categories = listOf("Mining", "Building", "Farming", "Combat", "Gathering")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Assign Mission to ${bot.name}", color = MineDiamondBlue) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Mission Title") },
                    placeholder = { Text("e.g. Build defensive castle Wall") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MineDiamondBlue,
                        unfocusedBorderColor = MineCardStroke
                    )
                )

                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Description Directions") },
                    placeholder = { Text("e.g. Use granite bricks to line outer perimeter.") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MineDiamondBlue,
                        unfocusedBorderColor = MineCardStroke
                    )
                )

                Text("Select Mission Domain:", fontWeight = FontWeight.Bold, fontSize = 12.sp)

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    categories.forEach { cat ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedCategory = cat }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedCategory == cat,
                                onClick = { selectedCategory = cat },
                                colors = RadioButtonDefaults.colors(selectedColor = MineDiamondBlue)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(cat, fontSize = 12.sp, color = MineTextPrimary)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(title, desc, selectedCategory) },
                colors = ButtonDefaults.buttonColors(containerColor = MineGreenAccent)
            ) {
                Text("Deploy Mission", color = Color.Black)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Dismiss", color = MineTextSecondary)
            }
        },
        containerColor = MineCardBg
    )
}
