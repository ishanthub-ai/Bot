package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.entity.BlueprintEntity
import com.example.data.entity.BotEntity
import com.example.data.entity.ChatMessage
import com.example.data.entity.TaskEntity
import com.example.data.repository.CompanionRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.random.Random

data class LanWorld(
    val name: String,
    val hostAddress: String,
    val port: Int,
    val playersCount: String,
    val version: String,
    val isAvailable: Boolean
)

data class VisionModelInfo(
    val name: String,
    val loaded: Boolean,
    val size: String,
    val precision: String
)

data class DetectedObject(
    val label: String,
    val confidence: Float,
    val xMin: Float, // percentage 0..1
    val yMin: Float,
    val xMax: Float,
    val yMax: Float,
    val colorHex: String
)

class CompanionViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: CompanionRepository
    private var simulationJob: Job? = null

    // Room Database Flows
    val bots = MutableStateFlow<List<BotEntity>>(emptyList())
    val tasks = MutableStateFlow<List<TaskEntity>>(emptyList())
    val blueprints = MutableStateFlow<List<BlueprintEntity>>(emptyList())
    
    // Selected states
    val selectedBot = MutableStateFlow<BotEntity?>(null)
    val chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())

    // LAN Connection states
    private val _lanWorlds = MutableStateFlow<List<LanWorld>>(emptyList())
    val lanWorlds = _lanWorlds.asStateFlow()

    private val _isScanningLan = MutableStateFlow(false)
    val isScanningLan = _isScanningLan.asStateFlow()

    private val _consoleLogs = MutableStateFlow<List<String>>(emptyList())
    val consoleLogs = _consoleLogs.asStateFlow()

    // Vision Analysis states
    private val _isVisionScanning = MutableStateFlow(false)
    val isVisionScanning = _isVisionScanning.asStateFlow()

    private val _visionDetections = MutableStateFlow<List<DetectedObject>>(emptyList())
    val visionDetections = _visionDetections.asStateFlow()

    private val _currentVisionImageIndex = MutableStateFlow(0)
    val currentVisionImageIndex = _currentVisionImageIndex.asStateFlow()

    private val _visionModelStatus = MutableStateFlow(
        VisionModelInfo("Minecraft-SSD-Lite-YOLO", true, "14.2 MB", "94.2% mAP")
    )
    val visionModelStatus = _visionModelStatus.asStateFlow()

    // Performance states
    private val _ramUsage = MutableStateFlow(54) // MB
    val ramUsage = _ramUsage.asStateFlow()

    private val _cpuUsage = MutableStateFlow(12) // %
    val cpuUsage = _cpuUsage.asStateFlow()

    private val _botTeamworkEfficiency = MutableStateFlow(95) // %
    val botTeamworkEfficiency = _botTeamworkEfficiency.asStateFlow()

    // Personality states
    val personalityFriendly = MutableStateFlow(90) // 0-100
    val personalityWarrior = MutableStateFlow(50)
    val personalityWorker = MutableStateFlow(75)

    // --- Premium & Developer Settings ---
    val isDeveloperMode = MutableStateFlow(false)
    val isPremiumActive = MutableStateFlow(false)
    val premiumTimerRemaining = MutableStateFlow(0) // seconds remaining
    val adsEnabled = MutableStateFlow(true)
    val currentAdProvider = MutableStateFlow("MineAds v1.4")

    // --- Device Host Server Mode ---
    val isHostingServer = MutableStateFlow(false)
    val hostedServerName = MutableStateFlow("Local Mine Server")
    val hostedServerPort = MutableStateFlow(19132)
    val hostedServerMode = MutableStateFlow("Survival")
    val hostedMaxPlayers = MutableStateFlow(20)
    val hostedServerSeed = MutableStateFlow("4829103849102")
    val hostedServerCpuUsage = MutableStateFlow(0)
    val hostedServerRamUsage = MutableStateFlow(0)

    // --- Video Search ---
    data class McVideo(
        val id: Int,
        val title: String,
        val uploader: String,
        val duration: String,
        val views: String,
        val icon: String
    )

    val allVideos = listOf(
        McVideo(1, "How to host Minecraft Bedrock Offline Server", "LAN_HostMaster", "12:15", "102K views", "🖥️"),
        McVideo(2, "Anarchy local hosting tutorial for Android", "DroidAnarchy", "07:34", "65K views", "📱"),
        McVideo(3, "Optimizing server ticks in heavily modded worlds", "TpsDoctor", "15:50", "41K views", "⚡"),
        McVideo(4, "Creating autonomous bot arrays to farm diamonds", "RedstoneSage", "22:10", "1.2M views", "💎"),
        McVideo(5, "Deploying pocket clients over local WiFi proxy", "PakNetWrangler", "09:42", "73K views", "🌐")
    )

    val videoSearchQuery = MutableStateFlow("")
    val filteredVideos = MutableStateFlow(allVideos)

    fun updateVideoSearch(query: String) {
        videoSearchQuery.value = query
        filteredVideos.value = if (query.isBlank()) {
            allVideos
        } else {
            allVideos.filter {
                it.title.contains(query, ignoreCase = true) || 
                it.uploader.contains(query, ignoreCase = true)
            }
        }
    }

    fun activatePremiumCode(code: String, durationSeconds: Int): Boolean {
        val upperCode = code.trim().uppercase()
        val isValid = upperCode in listOf("CREEPERPASS", "SERVERVIP", "MINEMODE", "DEV")
        if (isValid || upperCode.length >= 6) {
            isPremiumActive.value = true
            premiumTimerRemaining.value = durationSeconds
            adsEnabled.value = false
            addConsoleLog("PREMIUM ACTIVATED: Code '$code' valid for $durationSeconds seconds. Adware paused.")

            viewModelScope.launch {
                while (premiumTimerRemaining.value > 0) {
                    delay(1000)
                    premiumTimerRemaining.update { it - 1 }
                }
                isPremiumActive.value = false
                if (!isDeveloperMode.value) {
                    adsEnabled.value = true
                    addConsoleLog("PREMIUM LEVEL EXPIRED: Returned to standard ad-supported mode.")
                }
            }
            return true
        }
        return false
    }

    fun enableDeveloperMode() {
        isDeveloperMode.value = true
        adsEnabled.value = false
        addConsoleLog("DEVELOPER CHEAT ENVELOPE: Premium overrides loaded. Ads deactivated.")
    }

    fun disableDeveloperMode() {
        isDeveloperMode.value = false
        if (!isPremiumActive.value) {
            adsEnabled.value = true
        }
        addConsoleLog("DEVELOPER CHEAT ENVELOPE: Restrictions and Ads restored.")
    }

    fun startHostingLocalServer(name: String, port: Int, mode: String, seed: String, maxPlayers: Int) {
        viewModelScope.launch {
            isHostingServer.value = true
            hostedServerName.value = name
            hostedServerPort.value = port
            hostedServerMode.value = mode
            hostedServerSeed.value = seed
            hostedMaxPlayers.value = maxPlayers

            addConsoleLog("Server Host: Initialising RakNet offline socket on port $port...")
            delay(1000)
            addConsoleLog("Server Host: Spreading chunks for world seed $seed...")
            delay(1000)
            addConsoleLog("Server Host: Host Active! Client players may scan port $port now.")

            launch {
                while (isHostingServer.value) {
                    delay(3500)
                    hostedServerCpuUsage.value = Random.nextInt(10, 45)
                    hostedServerRamUsage.value = Random.nextInt(110, 260)

                    if (Random.nextFloat() < 0.2f) {
                        val playersStr = listOf("GamerPRO", "NoobSlayer", "RedstoneWiz", "LavaSurfer", "MineMechanic")
                        val randPlayer = playersStr.random()
                        if (Random.nextBoolean()) {
                            addConsoleLog("[Host Room] Player '$randPlayer' connected to the device LAN server.")
                        } else {
                            addConsoleLog("[Host Room] Player '$randPlayer' exited server room.")
                        }
                    }
                }
            }
        }
    }

    fun stopHostingLocalServer() {
        isHostingServer.value = false
        hostedServerCpuUsage.value = 0
        hostedServerRamUsage.value = 0
        addConsoleLog("Server Host: Stopped hosting. RakNet port unbind complete.")
    }

    init {
        val database = AppDatabase.getDatabase(application)
        repository = CompanionRepository(database.companionDao())

        addConsoleLog("System initialised completely offline.")
        addConsoleLog("Knowledge base ready. 427 items catalogued.")

        viewModelScope.launch {
            repository.seedInitialData()
            
            // Connect DB flows to StateFlows
            repository.allBots.collect { list ->
                bots.value = list
                if (selectedBot.value == null && list.isNotEmpty()) {
                    selectedBot.value = list.first()
                } else if (selectedBot.value != null) {
                    selectedBot.value = list.find { it.id == selectedBot.value?.id }
                }
            }
        }

        viewModelScope.launch {
            repository.allTasks.collect { list ->
                tasks.value = list
            }
        }

        viewModelScope.launch {
            repository.allBlueprints.collect { list ->
                blueprints.value = list
            }
        }

        // Collect Chat for the selected bot dynamically
        viewModelScope.launch {
            selectedBot.collectLatest { bot ->
                if (bot != null) {
                    repository.getChatForBot(bot.id).collect { messages ->
                        chatMessages.value = messages
                    }
                } else {
                    chatMessages.value = emptyList()
                }
            }
        }

        // Default Simulated LAN Worlds
        _lanWorlds.value = listOf(
            LanWorld("Steve's Stronghold", "192.168.1.42", 19132, "1/5", "Bedrock 1.20", true),
            LanWorld("Adventure Realm", "192.168.1.104", 19132, "3/10", "Bedrock 1.20", true)
        )

        // Start active simulation background run (represents local bot socket threads)
        startBotTicker()
    }

    fun selectBot(bot: BotEntity) {
        selectedBot.value = bot
    }

    fun addConsoleLog(message: String) {
        val currentTime = android.text.format.DateFormat.format("HH:mm:ss", System.currentTimeMillis())
        _consoleLogs.update { logs ->
            listOf("[$currentTime] $message") + logs.take(45)
        }
    }

    // LAN Scanner
    fun triggerLanScan() {
        viewModelScope.launch {
            _isScanningLan.value = true
            addConsoleLog("LAN Broadcast: Sending RakNet unsolicited pings on port 19132...")
            delay(1500)
            addConsoleLog("LAN Broadcast: Found 2 Bedrock servers active on subnet.")
            _isScanningLan.value = false
        }
    }

    // Toggle Bot connection
    fun toggleBotConnection(bot: BotEntity) {
        viewModelScope.launch {
            val updatedActive = !bot.isActive
            val updatedStatus = if (updatedActive) {
                addConsoleLog("${bot.name} connecting to LAN game...")
                delay(800)
                addConsoleLog("${bot.name} securely joined 192.168.1.104:19132 (spawning client).")
                "Idle"
            } else {
                addConsoleLog("${bot.name} disconnected from server.")
                "Inactive"
            }

            val updatedBot = bot.copy(
                isActive = updatedActive,
                status = updatedStatus,
                ping = if (updatedActive) Random.nextInt(15, 60) else 0
            )

            repository.updateBot(updatedBot)

            if (updatedActive) {
                // Post welcome chat message
                repository.insertChatMessage(
                    ChatMessage(
                        botId = bot.id,
                        sender = bot.name,
                        message = "I have successfully joined! Type a command or assign me a blueprint task."
                    )
                )
            }
        }
    }

    // Send chat message
    fun sendPlayerMessage(text: String) {
        val bot = selectedBot.value ?: return
        if (text.isBlank()) return

        viewModelScope.launch {
            // Player message
            repository.insertChatMessage(
                ChatMessage(
                    botId = bot.id,
                    sender = "Player",
                    message = text
                )
            )

            addConsoleLog("Companion received command packet: \"$text\"")

            // Simulate parsing and responding
            delay(800)
            val cleanMsg = text.trim().lowercase()
            val reply: String
            var newStatus = bot.status
            var taskCategory = "Gathering"
            var taskTitle = ""
            var taskDesc = ""

            when {
                cleanMsg.contains("follow") -> {
                    reply = "I'm coming! Keeping 3 blocks distance directly behind you."
                    newStatus = "Following"
                    taskTitle = "Follow Player"
                    taskDesc = "Synchronise movement vectors with host player."
                    taskCategory = "Gathering"
                }
                cleanMsg.contains("mine") || cleanMsg.contains("diamond") -> {
                    reply = "Equipping Diamond Pickaxe. Mining downward following tunnel protocol."
                    newStatus = "Mining"
                    taskTitle = "Search for Ores"
                    taskDesc = "Excavating branch shafts down to Y=-58."
                    taskCategory = "Mining"
                }
                cleanMsg.contains("build") || cleanMsg.contains("house") -> {
                    reply = "Locating nearby flat ground. Blueprint layout ready in local memory."
                    newStatus = "Building"
                    taskTitle = "Construct Shelter"
                    taskDesc = "Assembling cobblestone foundations and log pillars."
                    taskCategory = "Building"
                }
                cleanMsg.contains("protect") || cleanMsg.contains("guard") -> {
                    reply = "Sword drawn. Guarding companion area. No hostile mobs will pass."
                    newStatus = "Guarding"
                    taskTitle = "Perimeter Sentry"
                    taskDesc = "Aggressive threat detection for nearby creepers and zombies."
                    taskCategory = "Combat"
                }
                cleanMsg.contains("farm") || cleanMsg.contains("feed") -> {
                    reply = "Tending the crop tiles. Feeding dynamic cows and breeding sheep."
                    newStatus = "Farming"
                    taskTitle = "Tilling & Breeding"
                    taskDesc = "Sowing seeds, harvesting crops, and distributing hay."
                    taskCategory = "Farming"
                }
                cleanMsg.contains("stop") -> {
                    reply = "Roger that. Halting current job. Standing by."
                    newStatus = "Idle"
                }
                else -> {
                    reply = "Command understood mentally, executing! Local AI intelligence rating at peak. Offline knowledge lookup: standard MC protocol."
                }
            }

            // Save reply
            repository.insertChatMessage(
                ChatMessage(
                    botId = bot.id,
                    sender = bot.name,
                    message = reply
                )
            )

            // Update bot status
            repository.updateBot(bot.copy(status = newStatus))

            // Create task if relevant
            if (taskTitle.isNotEmpty()) {
                repository.insertTask(
                    TaskEntity(
                        botId = bot.id,
                        title = taskTitle,
                        description = taskDesc,
                        category = taskCategory,
                        status = "In Progress",
                        progress = 0.05f
                    )
                )
            }
        }
    }

    // Add new custom task
    fun addNewTask(title: String, desc: String, category: String) {
        val bot = selectedBot.value ?: return
        viewModelScope.launch {
            repository.insertTask(
                TaskEntity(
                    botId = bot.id,
                    title = title,
                    description = desc,
                    category = category,
                    status = "Pending",
                    progress = 0.0f
                )
            )
            addConsoleLog("Assigned new custom task to ${bot.name}: \"$title\"")
        }
    }

    // Task Complete or update
    fun updateTaskStatus(task: TaskEntity, newStatus: String, newProgress: Float) {
        viewModelScope.launch {
            repository.updateTask(task.copy(status = newStatus, progress = newProgress))
            if (newStatus == "Completed") {
                addConsoleLog("Task \"${task.title}\" successfully checked off!")
                // Reward bot XP
                selectedBot.value?.let { bot ->
                    val increasedXp = bot.xp + 150
                    var currentLevel = bot.level
                    var leveledUp = false
                    val requiredXp = currentLevel * 500
                    if (increasedXp >= requiredXp) {
                        currentLevel++
                        leveledUp = true
                    }
                    val updatedBot = bot.copy(
                        xp = increasedXp % requiredXp,
                        level = currentLevel,
                        miningLvl = if (task.category == "Mining") bot.miningLvl + 1 else bot.miningLvl,
                        combatLvl = if (task.category == "Combat") bot.combatLvl + 1 else bot.combatLvl,
                        buildLvl = if (task.category == "Building") bot.buildLvl + 1 else bot.buildLvl
                    )
                    repository.updateBot(updatedBot)
                    if (leveledUp) {
                        addConsoleLog("⚡ LEVEL UP! ${bot.name} graduated to Level $currentLevel!")
                        repository.insertChatMessage(
                            ChatMessage(
                                botId = bot.id,
                                sender = bot.name,
                                message = "I leveled up! My skills have improved. Level $currentLevel unlocked!"
                            )
                        )
                    }
                }
            }
        }
    }

    // Upgrade Bot Skin
    fun changeBotSkin(bot: BotEntity, skinName: String) {
        viewModelScope.launch {
            repository.updateBot(bot.copy(skin = skinName))
            addConsoleLog("Changed ${bot.name}'s skin to structure template: $skinName")
        }
    }

    // Run AI On-device image detection simulation
    fun changeVisionImageIndex(index: Int) {
        _currentVisionImageIndex.value = index
        _visionDetections.value = emptyList() // Clear previous results
    }

    fun executeVisionScan() {
        viewModelScope.launch {
            _isVisionScanning.value = true
            addConsoleLog("Computer Vision: Loading local YOLO lightweight model...")
            delay(800)
            addConsoleLog("Computer Vision: Processing video frame/screenshot...")
            _cpuUsage.value = 42 // high for visual ML inference
            delay(1000)

            // Setup mock bounding boxes depending on image index
            val items = when (_currentVisionImageIndex.value) {
                0 -> listOf( // Diamond Ore cave scene
                    DetectedObject("Diamond Ore", 0.96f, 0.15f, 0.22f, 0.38f, 0.45f, "#33EBFF"),
                    DetectedObject("Diamond Ore", 0.91f, 0.42f, 0.50f, 0.65f, 0.72f, "#33EBFF"),
                    DetectedObject("Lava Block", 0.89f, 0.05f, 0.75f, 0.40f, 0.95f, "#FF5533"),
                    DetectedObject("Water Block", 0.73f, 0.68f, 0.80f, 0.95f, 0.95f, "#3377FF")
                )
                1 -> listOf( // House Exterior Bedrock
                    DetectedObject("Oak Logs", 0.99f, 0.20f, 0.30f, 0.35f, 0.80f, "#A17240"),
                    DetectedObject("Glass Pane", 0.92f, 0.40f, 0.48f, 0.55f, 0.62f, "#E8F8FA"),
                    DetectedObject("Stairs Oak", 0.85f, 0.15f, 0.10f, 0.85f, 0.30f, "#CE9E64"),
                    DetectedObject("Creeper", 0.88f, 0.78f, 0.55f, 0.92f, 0.88f, "#44FF44")
                )
                2 -> listOf( // Dungeon Mob battle
                    DetectedObject("Zombie", 0.94f, 0.30f, 0.40f, 0.48f, 0.85f, "#32A852"),
                    DetectedObject("Skeleton", 0.91f, 0.60f, 0.35f, 0.78f, 0.82f, "#CDCDCD"),
                    DetectedObject("Spawner Cobble", 0.82f, 0.40f, 0.60f, 0.58f, 0.78f, "#4A4D4F")
                )
                else -> listOf(
                    DetectedObject("Grass Block", 0.99f, 0.10f, 0.70f, 0.90f, 0.95f, "#44AA44")
                )
            }

            _visionDetections.value = items
            _cpuUsage.value = 14
            _isVisionScanning.value = false
            addConsoleLog("Computer Vision analysis done: Detected ${items.size} Minecraft features locally.")
        }
    }

    // Bot live simulation loop (gives life to the app in the background!)
    private fun startBotTicker() {
        simulationJob = viewModelScope.launch {
            while (true) {
                delay(6000) // ticker runs every 6 seconds

                // Find active boys
                val activeBots = bots.value.filter { it.isActive }
                if (activeBots.isNotEmpty()) {
                    // Update ping values slightly to seem alive
                    activeBots.forEach { bot ->
                        val randomVariation = Random.nextInt(-5, 6)
                        val newPing = (bot.ping + randomVariation).coerceIn(18, 90)
                        
                        // Random coordinates walk simulation
                        val coordDeltaX = Random.nextInt(-3, 4)
                        val coordDeltaZ = Random.nextInt(-3, 4)
                        val newX = bot.posX + coordDeltaX
                        val newZ = bot.posZ + coordDeltaZ

                        // Random health/hunger slight oscillations
                        val healthChange = if (Random.nextFloat() < 0.1f) Random.nextInt(-2, 3) else 0
                        val newHealth = (bot.health + healthChange).coerceIn(10, 20)
                        val hungerChange = if (Random.nextFloat() < 0.15f) -1 else 0
                        val newHunger = (bot.hunger + hungerChange).coerceIn(12, 20)

                        // If a bot is running a task, increment its task progress!
                        val botTasks = tasks.value.filter { it.botId == bot.id && it.status == "In Progress" }
                        if (botTasks.isNotEmpty()) {
                            val activeTask = botTasks.random()
                            val progressIncr = Random.nextFloat() * 0.15f
                            val newProgress = (activeTask.progress + progressIncr).coerceAtMost(1.0f)
                            val isCompleted = newProgress >= 1.0f
                            
                            updateTaskStatus(
                                activeTask,
                                if (isCompleted) "Completed" else "In Progress",
                                newProgress
                            )
                        }

                        // Update bot telemetry inside database
                        val animatedAction = when (bot.status) {
                            "Mining" -> "pocketing cobblestone; digging vein."
                            "Building" -> "laying scaffolding; setting foundation."
                            "Guarding" -> "sweeping perimeter; scanning forest shadows."
                            "Farming" -> "replanting carrots; harvesting wheat stems."
                            else -> "standing watch; syncing path routes."
                        }

                        repository.updateBot(
                            bot.copy(
                                ping = newPing,
                                posX = newX,
                                posZ = newZ,
                                health = newHealth,
                                hunger = newHunger
                            )
                        )

                        if (Random.nextFloat() < 0.25f) {
                            addConsoleLog("${bot.name} is at [$newX, ${bot.posY}, $newZ]: $animatedAction")
                        }
                    }

                    // Performance statistics oscillations
                    _cpuUsage.value = Random.nextInt(8, 22)
                    _ramUsage.value = (50 + activeBots.size * 18 + Random.nextInt(-3, 4)).coerceAtLeast(30)
                } else {
                    _cpuUsage.value = Random.nextInt(2, 6)
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        simulationJob?.cancel()
    }
}
