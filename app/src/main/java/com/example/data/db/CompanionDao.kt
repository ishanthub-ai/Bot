package com.example.data.db

import androidx.room.*
import com.example.data.entity.BlueprintEntity
import com.example.data.entity.BotEntity
import com.example.data.entity.ChatMessage
import com.example.data.entity.TaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CompanionDao {

    // Bots
    @Query("SELECT * FROM bots ORDER BY id ASC")
    fun getAllBots(): Flow<List<BotEntity>>

    @Query("SELECT * FROM bots WHERE id = :id LIMIT 1")
    suspend fun getBotById(id: Int): BotEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBot(bot: BotEntity): Long

    @Update
    suspend fun updateBot(bot: BotEntity)

    @Delete
    suspend fun deleteBot(bot: BotEntity)

    // Tasks
    @Query("SELECT * FROM tasks WHERE botId = :botId ORDER BY timestamp DESC")
    fun getTasksForBot(botId: Int): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks ORDER BY timestamp DESC")
    fun getAllTasks(): Flow<List<TaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity)

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Delete
    suspend fun deleteTask(task: TaskEntity)

    // Chat History
    @Query("SELECT * FROM chat_messages WHERE botId = :botId ORDER BY timestamp ASC")
    fun getChatForBot(botId: Int): Flow<List<ChatMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatMessage(message: ChatMessage)

    // Blueprints
    @Query("SELECT * FROM blueprints ORDER BY id ASC")
    fun getAllBlueprints(): Flow<List<BlueprintEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBlueprint(blueprint: BlueprintEntity)
}
