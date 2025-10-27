package com.example.myapplication.utils

import android.content.Context
import android.media.MediaPlayer
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log

class SoundManager(private val context: Context) {
    
    init {
        Log.d(TAG, "SoundManager created, context: ${context::class.simpleName}")
    }
    
    private val vibrator: Vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }
    
    fun playBugHitSound() {
        Log.d(TAG, "playBugHitSound")
        vibrate(50)
    }
    
    fun playBonusSound() {
        Log.d(TAG, "playBonusSound")
        vibrate(100)
    }
    
    fun playMissSound() {
        vibrate(30)
    }
    
    fun playBugScream() {
        val pattern = longArrayOf(0, 100, 50, 100)
        vibratePattern(pattern)
    }
    
    private fun vibrate(duration: Long) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(duration)
        }
    }
    
    private fun vibratePattern(pattern: LongArray) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(pattern, -1)
        }
    }
    
    fun release() {
        Log.d(TAG, "release called")
        // Cleanup if needed
    }
    
    companion object {
        private const val TAG = "SoundManager"
    }
}
