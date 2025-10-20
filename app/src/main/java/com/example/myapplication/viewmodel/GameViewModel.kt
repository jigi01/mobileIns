package com.example.myapplication.viewmodel

import android.content.Context
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.api.CbrApi
import com.example.myapplication.data.Bonus
import com.example.myapplication.data.Bug
import com.example.myapplication.data.GameState
import com.example.myapplication.database.GameDatabase
import com.example.myapplication.database.ScoreEntity
import com.example.myapplication.utils.SoundManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class GameViewModel(
    private val context: Context,
    private val database: GameDatabase
) : ViewModel() {

    private val _gameState = MutableStateFlow(GameState())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private val _canvasSize = MutableStateFlow(Pair(0f, 0f))
    val canvasSize: StateFlow<Pair<Float, Float>> = _canvasSize.asStateFlow()

    private val _playerName = MutableStateFlow("Игрок")
    val playerName: StateFlow<String> = _playerName.asStateFlow()

    private val _difficultyLevel = MutableStateFlow(5)
    val difficultyLevel: StateFlow<Int> = _difficultyLevel.asStateFlow()

    private val _gravityX = MutableStateFlow(0f)
    val gravityX: StateFlow<Float> = _gravityX.asStateFlow()

    private val _gravityY = MutableStateFlow(0f)
    val gravityY: StateFlow<Float> = _gravityY.asStateFlow()

    val soundManager = SoundManager(context)

    private var gameTimerJob: Job? = null
    private var gameLoopJob: Job? = null

    fun setCanvasSize(width: Float, height: Float) {
        _canvasSize.value = Pair(width, height)
    }

    fun setPlayerName(name: String) {
        _playerName.value = name
    }

    fun setDifficultyLevel(level: Int) {
        _difficultyLevel.value = level
    }

    fun setGravity(x: Float, y: Float) {
        _gravityX.value = x
        _gravityY.value = y
    }

    fun startGame(maxBugs: Int, gameSpeed: Float, roundDuration: Int) {
        viewModelScope.launch {
            val goldPrice = try {
                CbrApi.getGoldPrice().toInt()
            } catch (e: Exception) {
                8000
            }
            
            _gameState.value = GameState(
                isPlaying = true,
                timeRemaining = roundDuration,
                goldPrice = goldPrice
            )

            startGameTimer(maxBugs, gameSpeed)
            startGameLoop(maxBugs, gameSpeed)
        }
    }

    private fun startGameTimer(maxBugs: Int, gameSpeed: Float) {
        gameTimerJob?.cancel()
        gameTimerJob = viewModelScope.launch {
            while (isActive && _gameState.value.timeRemaining > 0) {
                delay(1000)
                var newState = _gameState.value.copy(
                    timeRemaining = _gameState.value.timeRemaining - 1,
                    timeSinceLastBonus = _gameState.value.timeSinceLastBonus + 1,
                    timeSinceLastGoldenBug = _gameState.value.timeSinceLastGoldenBug + 1
                )

                if (newState.bonusActive && newState.bonusTimeLeft > 0) {
                    newState = newState.copy(bonusTimeLeft = newState.bonusTimeLeft - 1)
                    if (newState.bonusTimeLeft <= 0) {
                        newState = newState.copy(bonusActive = false)
                        _gravityX.value = 0f
                        _gravityY.value = 0f
                    }
                }

                if (newState.timeSinceLastBonus >= 15 && newState.bonus == null && _canvasSize.value.first > 0) {
                    newState = newState.copy(
                        bonus = Bonus.createRandom(0, _canvasSize.value.first, _canvasSize.value.second),
                        timeSinceLastBonus = 0
                    )
                }

                if (newState.timeSinceLastGoldenBug >= 20 && newState.goldenBug == null && _canvasSize.value.first > 0) {
                    val goldenBug = Bug(
                        id = -1,
                        position = Offset(
                            x = kotlin.random.Random.nextFloat() * (_canvasSize.value.first - 120f).coerceAtLeast(0f),
                            y = kotlin.random.Random.nextFloat() * (_canvasSize.value.second - 120f).coerceAtLeast(0f)
                        ),
                        velocity = Offset(
                            x = (kotlin.random.Random.nextFloat() - 0.5f) * 3f,
                            y = (kotlin.random.Random.nextFloat() - 0.5f) * 3f
                        ),
                        emoji = "🪳",
                        points = newState.goldPrice,
                        size = 150f
                    )
                    newState = newState.copy(
                        goldenBug = goldenBug,
                        timeSinceLastGoldenBug = 0
                    )
                }

                _gameState.value = newState

                if (_gameState.value.timeRemaining <= 0) {
                    endGame()
                }
            }
        }
    }

    private fun startGameLoop(maxBugs: Int, gameSpeed: Float) {
        gameLoopJob?.cancel()
        gameLoopJob = viewModelScope.launch {
            var frameCount = 0
            while (isActive && _gameState.value.isPlaying) {
                delay(16)
                
                val currentState = _gameState.value
                val canvas = _canvasSize.value
                val gx = _gravityX.value
                val gy = _gravityY.value

                val updatedBugs = currentState.bugs.map { bug ->
                    val newBug = bug.copy()
                    if (currentState.bonusActive) {
                        newBug.updatePosition(canvas.first, canvas.second, gameSpeed / 3f, gx, gy)
                    } else {
                        newBug.updatePosition(canvas.first, canvas.second, gameSpeed / 3f)
                    }
                    newBug
                }

                frameCount++
                val shouldSpawnNewBug = frameCount % 60 == 0 &&
                        currentState.bugs.size < maxBugs &&
                        canvas.first > 0

                val newBugs = if (shouldSpawnNewBug) {
                    val newBug = Bug.createRandom(
                        currentState.nextBugId,
                        canvas.first,
                        canvas.second
                    )
                    updatedBugs + newBug
                } else {
                    updatedBugs
                }

                val newGoldenBug = currentState.goldenBug?.let { goldenBug ->
                    val newBug = goldenBug.copy()
                    if (currentState.bonusActive) {
                        newBug.updatePosition(canvas.first, canvas.second, gameSpeed / 4f, gx, gy)
                    } else {
                        newBug.updatePosition(canvas.first, canvas.second, gameSpeed / 4f)
                    }
                    newBug
                }

                _gameState.value = currentState.copy(
                    bugs = newBugs,
                    nextBugId = if (shouldSpawnNewBug) currentState.nextBugId + 1 else currentState.nextBugId,
                    goldenBug = newGoldenBug
                )
            }
        }
    }

    private fun endGame() {
        _gameState.value = _gameState.value.copy(isPlaying = false)
        gameTimerJob?.cancel()
        gameLoopJob?.cancel()

        viewModelScope.launch {
            database.gameDao().insertScore(
                ScoreEntity(
                    playerId = 0,
                    playerName = _playerName.value,
                    score = _gameState.value.score,
                    difficultyLevel = _difficultyLevel.value
                )
            )
        }
    }

    fun onTap(offset: Offset) {
        val currentState = _gameState.value

        val clickedGoldenBug = currentState.goldenBug?.let { 
            if (it.contains(offset)) it else null 
        }
        
        if (clickedGoldenBug != null) {
            soundManager.playBonusSound()
            _gameState.value = currentState
                .addScore(clickedGoldenBug.points)
                .copy(goldenBug = null)
        } else {
            val clickedBonus = currentState.bonus?.let { 
                if (it.contains(offset)) it else null 
            }
            
            if (clickedBonus != null) {
                soundManager.playBonusSound()
                _gameState.value = currentState.copy(
                    bonus = null,
                    bonusActive = true,
                    bonusTimeLeft = 10,
                    score = currentState.score + 50
                )
            } else {
                val clickedBug = currentState.bugs.find { it.contains(offset) }
                
                if (clickedBug != null) {
                    soundManager.playBugHitSound()
                    _gameState.value = currentState
                        .addScore(clickedBug.points)
                        .copy(bugs = currentState.bugs - clickedBug)
                } else {
                    soundManager.playMissSound()
                    _gameState.value = currentState.addMiss()
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        gameTimerJob?.cancel()
        gameLoopJob?.cancel()
        soundManager.release()
    }
}
