package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val botId: Int,
    val title: String,
    val description: String,
    val category: String, // "Mining", "Building", "Farming", "Combat", "Gathering"
    val status: String = "Pending", // "Pending", "In Progress", "Completed", "Failed"
    val progress: Float = 0.0f, // 0.0 to 1.0
    val timestamp: Long = System.currentTimeMillis()
)
