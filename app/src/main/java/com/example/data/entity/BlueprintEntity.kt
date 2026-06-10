package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "blueprints")
data class BlueprintEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val type: String, // "House", "Castle", "Bridge", "Farm"
    val difficulty: String, // "Easy", "Medium", "Hard"
    val stoneCount: Int,
    val woodCount: Int,
    val ironCount: Int,
    val glassCount: Int,
    val description: String,
    val gridData: String // e.g. CSV or JSON of block layouts
)
