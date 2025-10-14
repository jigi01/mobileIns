package com.example.myapplication.data

import androidx.compose.ui.geometry.Offset
import kotlin.random.Random

data class Bug(
    val id: Int,
    var position: Offset,
    var velocity: Offset,
    val emoji: String,
    val points: Int,
    val size: Float = 120f
) {
    fun updatePosition(width: Float, height: Float, speed: Float = 1f, gravityX: Float = 0f, gravityY: Float = 0f) {
        if (width <= 0 || height <= 0) return
        
        var newX = position.x + (velocity.x + gravityX) * speed
        var newY = position.y + (velocity.y + gravityY) * speed
        var newVelX = velocity.x
        var newVelY = velocity.y
        
        if (newX <= 0) {
            newX = 0f
            newVelX = kotlin.math.abs(velocity.x)
        } else if (newX >= width - size) {
            newX = width - size
            newVelX = -kotlin.math.abs(velocity.x)
        }
        
        if (newY <= 0) {
            newY = 0f
            newVelY = kotlin.math.abs(velocity.y)
        } else if (newY >= height - size) {
            newY = height - size
            newVelY = -kotlin.math.abs(velocity.y)
        }
        
        position = Offset(newX, newY)
        velocity = Offset(newVelX, newVelY)
    }
    
    fun contains(offset: Offset): Boolean {
        val padding = 80f
        val bottomPadding = 150f
        return offset.x >= position.x - padding && 
               offset.x <= position.x + size + padding &&
               offset.y >= position.y - padding && 
               offset.y <= position.y + size + bottomPadding
    }
    
    companion object {
        fun createRandom(id: Int, width: Float, height: Float): Bug {
            val bugTypes = listOf(
                "🪲" to 10,
                "🐛" to 15,
                "🦗" to 20,
                "🕷️" to 25,
                "🦟" to 30
            )
            val (emoji, points) = bugTypes.random()
            
            val size = 120f
            return Bug(
                id = id,
                position = Offset(
                    x = Random.nextFloat() * (width - size).coerceAtLeast(0f),
                    y = Random.nextFloat() * (height - size).coerceAtLeast(0f)
                ),
                velocity = Offset(
                    x = (Random.nextFloat() - 0.5f) * 5f,
                    y = (Random.nextFloat() - 0.5f) * 5f
                ),
                emoji = emoji,
                points = points
            )
        }
    }
}
