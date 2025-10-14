package com.example.myapplication.data

data class GameState(
    val isPlaying: Boolean = false,
    val isPaused: Boolean = false,
    val score: Int = 0,
    val timeRemaining: Int = 60,
    val bugs: List<Bug> = emptyList(),
    val nextBugId: Int = 0,
    val missCount: Int = 0,
    val bonus: Bonus? = null,
    val bonusActive: Boolean = false,
    val bonusTimeLeft: Int = 0,
    val timeSinceLastBonus: Int = 0,
    val goldenBug: Bug? = null,
    val timeSinceLastGoldenBug: Int = 0,
    val goldPrice: Int = 8000
) {
    fun addScore(points: Int): GameState {
        return copy(score = score + points)
    }
    
    fun addMiss(): GameState {
        return copy(score = (score - 5).coerceAtLeast(0), missCount = missCount + 1)
    }
}
