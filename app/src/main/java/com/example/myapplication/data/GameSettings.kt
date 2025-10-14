package com.example.myapplication.data

data class GameSettings(
    val gameSpeed: Float = 5f,
    val maxBugs: Int = 10,
    val bonusInterval: Int = 15,
    val roundDuration: Int = 60
)
