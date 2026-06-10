package com.example.data.repository

import com.example.data.db.CompanionDao
import com.example.data.entity.BlueprintEntity
import com.example.data.entity.BotEntity
import com.example.data.entity.ChatMessage
import com.example.data.entity.TaskEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class CompanionRepository(private val companionDao: CompanionDao) {

    val allBots: Flow<List<BotEntity>> = companionDao.getAllBots()
    val allTasks: Flow<List<TaskEntity>> = companionDao.getAllTasks()
    fun getTasksForBot(botId: Int): Flow<List<TaskEntity>> = companionDao.getTasksForBot(botId)
    fun getChatForBot(botId: Int): Flow<List<ChatMessage>> = companionDao.getChatForBot(botId)
    val allBlueprints: Flow<List<BlueprintEntity>> = companionDao.getAllBlueprints()

    suspend fun getBotById(id: Int): BotEntity? = companionDao.getBotById(id)

    suspend fun insertBot(bot: BotEntity): Long = companionDao.insertBot(bot)
    suspend fun updateBot(bot: BotEntity) = companionDao.updateBot(bot)
    suspend fun deleteBot(bot: BotEntity) = companionDao.deleteBot(bot)

    suspend fun insertTask(task: TaskEntity) = companionDao.insertTask(task)
    suspend fun updateTask(task: TaskEntity) = companionDao.updateTask(task)
    suspend fun deleteTask(task: TaskEntity) = companionDao.deleteTask(task)

    suspend fun insertChatMessage(message: ChatMessage) = companionDao.insertChatMessage(message)

    suspend fun insertBlueprint(blueprint: BlueprintEntity) = companionDao.insertBlueprint(blueprint)

    suspend fun seedInitialData() {
        val bots = companionDao.getAllBots().firstOrNull()
        if (bots.isNullOrEmpty()) {
            // Seed Bots
            val steveId = companionDao.insertBot(
                BotEntity(
                    name = "PocketBot_Steve",
                    skin = "Steve",
                    level = 3,
                    xp = 450,
                    miningLvl = 2,
                    combatLvl = 1,
                    buildLvl = 1,
                    status = "Idle",
                    posX = 120,
                    posY = 64,
                    posZ = -350,
                    dimension = "Overworld",
                    ping = 0,
                    isActive = false
                )
            ).toInt()

            val alexId = companionDao.insertBot(
                BotEntity(
                    name = "Companion_Alex",
                    skin = "Alex",
                    level = 5,
                    xp = 1200,
                    miningLvl = 1,
                    combatLvl = 2,
                    buildLvl = 3,
                    status = "Idle",
                    posX = 122,
                    posY = 64,
                    posZ = -348,
                    dimension = "Overworld",
                    ping = 0,
                    isActive = false
                )
            ).toInt()

            val guardId = companionDao.insertBot(
                BotEntity(
                    name = "IronGuard_Bot",
                    skin = "Redstone Knight",
                    level = 10,
                    xp = 4500,
                    miningLvl = 1,
                    combatLvl = 5,
                    buildLvl = 1,
                    status = "Idle",
                    posX = 115,
                    posY = 65,
                    posZ = -360,
                    dimension = "Overworld",
                    ping = 0,
                    isActive = false
                )
            ).toInt()

            // Seed Tasks for Steve
            companionDao.insertTask(
                TaskEntity(
                    botId = steveId,
                    title = "Gather Wood Logs",
                    description = "Collect 64 Oak Logs from the nearby forest canopy.",
                    category = "Gathering",
                    status = "Pending",
                    progress = 0.0f
                )
            )
            companionDao.insertTask(
                TaskEntity(
                    botId = steveId,
                    title = "Mine Deepslate Iron",
                    description = "Excavate iron ore veins at Y=-15.",
                    category = "Mining",
                    status = "Completed",
                    progress = 1.0f
                )
            )

            // Seed Tasks for Alex
            companionDao.insertTask(
                TaskEntity(
                    botId = alexId,
                    title = "Build Wheat Farm",
                    description = "Construct an irrigated 9x9 crop grid.",
                    category = "Farming",
                    status = "In Progress",
                    progress = 0.45f
                )
            )

            // Seed Chat History for Bots
            companionDao.insertChatMessage(
                ChatMessage(
                    botId = steveId,
                    sender = "PocketBot_Steve",
                    message = "Ready to build or excavate! Provide a task or connect me to your LAN game."
                )
            )
            companionDao.insertChatMessage(
                ChatMessage(
                    botId = alexId,
                    sender = "Companion_Alex",
                    message = "Let's explore! I brought some carrots and wheat seeds."
                )
            )
            companionDao.insertChatMessage(
                ChatMessage(
                    botId = guardId,
                    sender = "IronGuard_Bot",
                    message = "Security mode active. No monsters shall approach."
                )
            )

            // Seed Blueprints
            companionDao.insertBlueprint(
                BlueprintEntity(
                    name = "Survival Oak Cabin",
                    type = "House",
                    difficulty = "Easy",
                    stoneCount = 32,
                    woodCount = 64,
                    ironCount = 0,
                    glassCount = 8,
                    description = "A warm, compact rustic starter cabin perfect for Bedrock survival mode. Includes complete roof trim and safe door entrances.",
                    gridData = "LOG_OAK,PLANKS_OAK,GLASS_PANE,STAIRS_OAK,STONE_COBBLE"
                )
            )

            companionDao.insertBlueprint(
                BlueprintEntity(
                    name = "Cobblestone Arch Bridge",
                    type = "Bridge",
                    difficulty = "Easy",
                    stoneCount = 120,
                    woodCount = 16,
                    ironCount = 0,
                    glassCount = 0,
                    description = "An aesthetic arched span to bridge ravines or rivers. Employs cobblestone stair buttresses and security barriers.",
                    gridData = "STONE_COBBLE,STAIRS_COBBLE,FENCE_OAK"
                )
            )

            companionDao.insertBlueprint(
                BlueprintEntity(
                    name = "Castellated Defense Tower",
                    type = "Castle",
                    difficulty = "Medium",
                    stoneCount = 320,
                    woodCount = 24,
                    ironCount = 8,
                    glassCount = 4,
                    description = "A 4-story high circular fortress watchtower equipped with parapets, arrow slots, and climbing ladders for sniper defense.",
                    gridData = "STONE_BRICK,LADDER,IRON_BARS,SLAB_STONE"
                )
            )

            companionDao.insertBlueprint(
                BlueprintEntity(
                    name = "Semi-Auto Crop Farm",
                    type = "Farm",
                    difficulty = "Medium",
                    stoneCount = 48,
                    woodCount = 32,
                    ironCount = 4,
                    glassCount = 0,
                    description = "An efficient 9x9 farm layout featuring built-in water flow trenches, composter bins, and a double-chest collection bay.",
                    gridData = "DIRT,WATER,CHEST,LOG_OAK,HOE,SEEDS"
                )
            )
        }
    }
}
