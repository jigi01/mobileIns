package com.example.myapplication.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.res.imageResource
import androidx.core.content.ContextCompat
import com.example.myapplication.R
import com.example.myapplication.data.Bug
import com.example.myapplication.data.Bonus
import com.example.myapplication.data.GameState
import com.example.myapplication.database.GameDatabase
import com.example.myapplication.database.ScoreEntity
import com.example.myapplication.utils.SoundManager
import com.example.myapplication.api.CbrApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import androidx.compose.runtime.withFrameNanos
import androidx.compose.runtime.DisposableEffect
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager as AndroidSensorManager

@Composable
fun GameScreen(
    maxBugs: Int = 10,
    gameSpeed: Float = 5f,
    roundDuration: Int = 60
) {
    val context = LocalContext.current
    val database = remember { GameDatabase.getDatabase(context) }
    val coroutineScope = rememberCoroutineScope()
    val soundManager = remember { SoundManager(context) }
    
    val goldenBugBitmap = remember {
        try {
            ContextCompat.getDrawable(context, R.drawable.golden_bug)?.let { drawable ->
                val bitmap = android.graphics.Bitmap.createBitmap(
                    drawable.intrinsicWidth,
                    drawable.intrinsicHeight,
                    android.graphics.Bitmap.Config.ARGB_8888
                )
                val canvas = android.graphics.Canvas(bitmap)
                drawable.setBounds(0, 0, canvas.width, canvas.height)
                drawable.draw(canvas)
                bitmap.asImageBitmap()
            }
        } catch (e: Exception) {
            null
        }
    }
    
    var gameState by remember { mutableStateOf(GameState()) }
    var canvasSize by remember { mutableStateOf(Pair(0f, 0f)) }
    var playerName by remember { mutableStateOf("Игрок") }
    var difficultyLevel by remember { mutableIntStateOf(5) }
    var showNameDialog by remember { mutableStateOf(false) }
    
    var gravityX by remember { mutableFloatStateOf(0f) }
    var gravityY by remember { mutableFloatStateOf(0f) }
    
    val sensorManager = remember { context.getSystemService(Context.SENSOR_SERVICE) as AndroidSensorManager }
    val accelerometer = remember { sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) }
    
    DisposableEffect(gameState.bonusActive) {
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (gameState.bonusActive && event != null) {
                    gravityX = event.values[0] * 0.5f
                    gravityY = event.values[1] * 0.5f
                    
                    if (kotlin.math.abs(gravityX) > 1f || kotlin.math.abs(gravityY) > 1f) {
                        soundManager.playBugScream()
                    }
                }
            }
            
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
        
        if (gameState.bonusActive && accelerometer != null) {
            sensorManager.registerListener(listener, accelerometer, AndroidSensorManager.SENSOR_DELAY_GAME)
        }
        
        onDispose {
            sensorManager.unregisterListener(listener)
            gravityX = 0f
            gravityY = 0f
        }
    }
    
    DisposableEffect(Unit) {
        onDispose {
            soundManager.release()
        }
    }
    
    LaunchedEffect(gameState.isPlaying) {
        if (gameState.isPlaying && !gameState.isPaused) {
            while (isActive && gameState.timeRemaining > 0) {
                delay(1000)
                var newState = gameState.copy(
                    timeRemaining = gameState.timeRemaining - 1,
                    timeSinceLastBonus = gameState.timeSinceLastBonus + 1,
                    timeSinceLastGoldenBug = gameState.timeSinceLastGoldenBug + 1
                )
                
                if (newState.bonusActive && newState.bonusTimeLeft > 0) {
                    newState = newState.copy(bonusTimeLeft = newState.bonusTimeLeft - 1)
                    if (newState.bonusTimeLeft <= 0) {
                        newState = newState.copy(bonusActive = false)
                    }
                }
                
                if (newState.timeSinceLastBonus >= 15 && newState.bonus == null && canvasSize.first > 0) {
                    newState = newState.copy(
                        bonus = Bonus.createRandom(0, canvasSize.first, canvasSize.second),
                        timeSinceLastBonus = 0
                    )
                }
                
                if (newState.timeSinceLastGoldenBug >= 20 && newState.goldenBug == null && canvasSize.first > 0) {
                    val goldenBug = Bug(
                        id = -1,
                        position = androidx.compose.ui.geometry.Offset(
                            x = kotlin.random.Random.nextFloat() * (canvasSize.first - 120f).coerceAtLeast(0f),
                            y = kotlin.random.Random.nextFloat() * (canvasSize.second - 120f).coerceAtLeast(0f)
                        ),
                        velocity = androidx.compose.ui.geometry.Offset(
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
                
                gameState = newState
                
                if (gameState.timeRemaining <= 0) {
                    gameState = gameState.copy(isPlaying = false)
                    
                    coroutineScope.launch {
                        database.gameDao().insertScore(
                            ScoreEntity(
                                playerId = 0,
                                playerName = playerName,
                                score = gameState.score,
                                difficultyLevel = difficultyLevel
                            )
                        )
                    }
                }
            }
        }
    }
    
    LaunchedEffect(gameState.isPlaying) {
        if (gameState.isPlaying && !gameState.isPaused) {
            var lastFrameTime = 0L
            var frameCount = 0
            
            while (isActive && gameState.isPlaying) {
                withFrameNanos { frameTimeNanos ->
                    if (lastFrameTime != 0L) {
                        val deltaTime = (frameTimeNanos - lastFrameTime) / 1_000_000f
                        
                        val updatedBugs = gameState.bugs.map { bug ->
                            val newBug = bug.copy()
                            if (gameState.bonusActive) {
                                newBug.updatePosition(canvasSize.first, canvasSize.second, gameSpeed / 3f, gravityX, gravityY)
                            } else {
                                newBug.updatePosition(canvasSize.first, canvasSize.second, gameSpeed / 3f)
                            }
                            newBug
                        }
                        
                        frameCount++
                        val shouldSpawnNewBug = frameCount % 60 == 0 && 
                                               gameState.bugs.size < maxBugs && 
                                               canvasSize.first > 0
                        
                        if (shouldSpawnNewBug) {
                            val newBug = Bug.createRandom(
                                gameState.nextBugId,
                                canvasSize.first,
                                canvasSize.second
                            )
                            gameState = gameState.copy(
                                bugs = updatedBugs + newBug,
                                nextBugId = gameState.nextBugId + 1
                            )
                        } else {
                            gameState = gameState.copy(bugs = updatedBugs)
                        }
                        
                        gameState.goldenBug?.let { goldenBug ->
                            val newGoldenBug = goldenBug.copy()
                            if (gameState.bonusActive) {
                                newGoldenBug.updatePosition(canvasSize.first, canvasSize.second, gameSpeed / 4f, gravityX, gravityY)
                            } else {
                                newGoldenBug.updatePosition(canvasSize.first, canvasSize.second, gameSpeed / 4f)
                            }
                            gameState = gameState.copy(goldenBug = newGoldenBug)
                        }
                    }
                    lastFrameTime = frameTimeNanos
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5DC))
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Очки: ${gameState.score}", style = MaterialTheme.typography.titleLarge)
                Text("Время: ${gameState.timeRemaining}с", style = MaterialTheme.typography.titleLarge)
                Text("Промахи: ${gameState.missCount}", style = MaterialTheme.typography.titleLarge)
            }
        }

        if (!gameState.isPlaying) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (gameState.score > 0) {
                    Text(
                        "Игра окончена!",
                        style = MaterialTheme.typography.headlineLarge
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Итоговый счёт: ${gameState.score}",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Промахов: ${gameState.missCount}",
                        style = MaterialTheme.typography.titleMedium
                    )
                } else {
                    Text(
                        "Игра «Жуки»",
                        style = MaterialTheme.typography.headlineLarge
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Нажимайте на насекомых, чтобы их уничтожить!",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = playerName,
                    onValueChange = { playerName = it },
                    label = { Text("Ваше имя") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Column {
                    Text("Уровень сложности: $difficultyLevel")
                    Slider(
                        value = difficultyLevel.toFloat(),
                        onValueChange = { difficultyLevel = it.toInt() },
                        valueRange = 1f..10f,
                        steps = 8
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = {
                        coroutineScope.launch {
                            val goldPrice = CbrApi.getGoldPrice().toInt()
                            gameState = GameState(
                                isPlaying = true,
                                timeRemaining = roundDuration,
                                goldPrice = goldPrice
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (gameState.score > 0) "Играть снова" else "Начать игру")
                }
            }
        } else {
            val textMeasurer = rememberTextMeasurer()
            
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            val clickedGoldenBug = gameState.goldenBug?.let { if (it.contains(offset)) it else null }
                            if (clickedGoldenBug != null) {
                                soundManager.playBonusSound()
                                gameState = gameState
                                    .addScore(clickedGoldenBug.points)
                                    .copy(goldenBug = null)
                            } else {
                                val clickedBonus = gameState.bonus?.let { if (it.contains(offset)) it else null }
                                if (clickedBonus != null) {
                                    soundManager.playBonusSound()
                                    gameState = gameState.copy(
                                        bonus = null,
                                        bonusActive = true,
                                        bonusTimeLeft = 10,
                                        score = gameState.score + 50
                                    )
                                } else {
                                    val clickedBug = gameState.bugs.find { it.contains(offset) }
                                    if (clickedBug != null) {
                                        soundManager.playBugHitSound()
                                        gameState = gameState
                                            .addScore(clickedBug.points)
                                            .copy(bugs = gameState.bugs - clickedBug)
                                    } else {
                                        soundManager.playMissSound()
                                        gameState = gameState.addMiss()
                                    }
                                }
                            }
                        }
                    }
            ) {
                canvasSize = Pair(size.width, size.height)
                
                if (gameState.bonusActive) {
                    drawCircle(
                        color = Color.Yellow.copy(alpha = 0.2f),
                        radius = size.minDimension * 0.4f
                    )
                }
                
                gameState.bonus?.let { bonus ->
                    drawText(
                        textMeasurer = textMeasurer,
                        text = bonus.emoji,
                        topLeft = Offset(
                            x = bonus.position.x,
                            y = bonus.position.y
                        ),
                        style = androidx.compose.ui.text.TextStyle(fontSize = 64.sp)
                    )
                }
                
                gameState.goldenBug?.let { goldenBug ->
                    goldenBugBitmap?.let { bitmap ->
                        drawImage(
                            image = bitmap,
                            srcOffset = androidx.compose.ui.unit.IntOffset.Zero,
                            srcSize = androidx.compose.ui.unit.IntSize(bitmap.width, bitmap.height),
                            dstOffset = androidx.compose.ui.unit.IntOffset(
                                x = goldenBug.position.x.toInt(),
                                y = goldenBug.position.y.toInt()
                            ),
                            dstSize = androidx.compose.ui.unit.IntSize(150, 150)
                        )
                    } ?: run {
                        drawText(
                            textMeasurer = textMeasurer,
                            text = goldenBug.emoji,
                            topLeft = Offset(
                                x = goldenBug.position.x,
                                y = goldenBug.position.y
                            ),
                            style = androidx.compose.ui.text.TextStyle(fontSize = 96.sp)
                        )
                    }
                }
                
                gameState.bugs.forEach { bug ->
                    drawText(
                        textMeasurer = textMeasurer,
                        text = bug.emoji,
                        topLeft = Offset(
                            x = bug.position.x + 10f,
                            y = bug.position.y + 10f
                        ),
                        style = androidx.compose.ui.text.TextStyle(fontSize = 72.sp)
                    )
                }
            }
        }
        
        if (gameState.bonusActive && gameState.bonusTimeLeft > 0) {
            Text(
                text = "🎯 ГИРОСКОП АКТИВЕН! ${gameState.bonusTimeLeft}с",
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Yellow.copy(alpha = 0.7f))
                    .padding(8.dp),
                style = MaterialTheme.typography.titleMedium,
                color = Color.Black
            )
        }
    }
}
