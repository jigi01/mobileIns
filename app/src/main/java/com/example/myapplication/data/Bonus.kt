package com.example.myapplication.data

import androidx.compose.ui.geometry.Offset
import kotlin.random.Random

data class Bonus(
    val id: Int,
    val position: Offset,
    val size: Float = 80f,
    val emoji: String = "⭐",
    val duration: Long = 10000L // 10 секунд действия
) {
    fun contains(offset: Offset): Boolean {
        val padding = 40f
        return offset.x >= position.x - padding && 
               offset.x <= position.x + size + padding &&
               offset.y >= position.y - padding && 
               offset.y <= position.y + size + padding
    }
    
    companion object {
        fun createRandom(id: Int, width: Float, height: Float): Bonus {
            val size = 80f
            return Bonus(
                id = id,
                position = Offset(
                    x = Random.nextFloat() * (width - size).coerceAtLeast(0f),
                    y = Random.nextFloat() * (height - size).coerceAtLeast(0f)
                )
            )
        }
    }
}
