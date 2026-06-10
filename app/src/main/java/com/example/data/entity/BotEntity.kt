package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bots")
data class BotEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val skin: String, // e.g. "Steve", "Alex", "Creeper Hunter", "Nether Knight"
    val level: Int = 1,
    val xp: Int = 0,
    val health: Int = 20, // max 20 standard MC
    val hunger: Int = 20, // max 20 standard MC
    val miningLvl: Int = 1,
    val combatLvl: Int = 1,
    val buildLvl: Int = 1,
    val status: String = "Inactive", // "Inactive", "Idle", "Mining", "Building", "Guarding"
    val posX: Int = 0,
    val posY: Int = 64,
    val posZ: Int = 0,
    val dimension: String = "Overworld",
    val ping: Int = 0,
    val isActive: Boolean = false
)
