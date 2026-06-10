package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val botId: Int,
    val sender: String, // "Player" or Bot Name
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)
