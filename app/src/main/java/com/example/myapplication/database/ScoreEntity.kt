package com.example.myapplication.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scores")
data class ScoreEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val playerId: Long,
    val playerName: String,
    val score: Int,
    val difficultyLevel: Int,
    val timestamp: Long = System.currentTimeMillis()
)
