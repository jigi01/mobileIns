package com.example.myapplication.ui.screens

import android.util.Log
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
import androidx.core.content.ContextCompat
import com.example.myapplication.R
import com.example.myapplication.viewmodel.GameViewModel
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager as AndroidSensorManager
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel

@Composable
fun GameScreen(
    maxBugs: Int = 10,
    gameSpeed: Float = 5f,
    roundDuration: Int = 60,
    viewModel: GameViewModel = koinViewModel()
) {
    Log.d(TAG, "GameScreen composition started")
    
    val context = LocalContext.current
    val applicationContext = remember { 
        Log.d(TAG, "Remembering applicationContext")
        context.applicationContext 
    }
    
    val gameState by viewModel.gameState.collectAsStateWithLifecycle()
    val canvasSize by viewModel.canvasSize.collectAsStateWithLifecycle()
    val playerName by viewModel.playerName.collectAsStateWithLifecycle()
    val difficultyLevel by viewModel.difficultyLevel.collectAsStateWithLifecycle()
    val gravityX by viewModel.gravityX.collectAsStateWithLifecycle()
    val gravityY by viewModel.gravityY.collectAsStateWithLifecycle()
    
    val goldenBugBitmap = remember {
        Log.d(TAG, "Loading golden bug bitmap")
        try {
            ContextCompat.getDrawable(applicationContext, R.drawable.golden_bug)?.let { drawable ->
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
    
    val sensorManager = remember { 
        Log.d(TAG, "Getting SensorManager")
        applicationContext.getSystemService(Context.SENSOR_SERVICE) as AndroidSensorManager 
    }
    val accelerometer = remember { 
        Log.d(TAG, "Getting accelerometer")
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) 
    }
    
    DisposableEffect(gameState.bonusActive) {
        Log.d(TAG, "DisposableEffect: bonusActive=${gameState.bonusActive}")
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (gameState.bonusActive && event != null) {
                    val newGravityX = event.values[0] * 0.5f
                    val newGravityY = event.values[1] * 0.5f
                    viewModel.setGravity(newGravityX, newGravityY)
                    
                    if (kotlin.math.abs(newGravityX) > 1f || kotlin.math.abs(newGravityY) > 1f) {
                        viewModel.soundManager.playBugScream()
                    }
                }
            }
            
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
        
        if (gameState.bonusActive && accelerometer != null) {
            Log.d(TAG, "Registering sensor listener")
            sensorManager.registerListener(listener, accelerometer, AndroidSensorManager.SENSOR_DELAY_GAME)
        }
        
        onDispose {
            Log.d(TAG, "DisposableEffect onDispose")
            sensorManager.unregisterListener(listener)
            viewModel.setGravity(0f, 0f)
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
                    onValueChange = { viewModel.setPlayerName(it) },
                    label = { Text("Ваше имя") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Column {
                    Text("Уровень сложности: $difficultyLevel")
                    Slider(
                        value = difficultyLevel.toFloat(),
                        onValueChange = { viewModel.setDifficultyLevel(it.toInt()) },
                        valueRange = 1f..10f,
                        steps = 8
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = {
                        viewModel.startGame(maxBugs, gameSpeed, roundDuration)
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
                            viewModel.onTap(offset)
                        }
                    }
            ) {
                viewModel.setCanvasSize(size.width, size.height)
                
                if (gameState.bonusActive) {
                    drawCircle(
                        color = Color.Yellow.copy(alpha = 0.2f),
                        radius = size.minDimension * 0.4f
                    )
                }
                
                gameState.bonus?.let { bonus ->
                    if (bonus.position.x >= 0 && bonus.position.x < size.width &&
                        bonus.position.y >= 0 && bonus.position.y < size.height) {
                        drawText(
                            textMeasurer = textMeasurer,
                            text = bonus.emoji,
                            topLeft = Offset(
                                x = bonus.position.x.coerceIn(0f, size.width - 100f),
                                y = bonus.position.y.coerceIn(0f, size.height - 100f)
                            ),
                            style = androidx.compose.ui.text.TextStyle(fontSize = 64.sp)
                        )
                    }
                }
                
                gameState.goldenBug?.let { goldenBug ->
                    if (goldenBug.position.x >= 0 && goldenBug.position.x < size.width &&
                        goldenBug.position.y >= 0 && goldenBug.position.y < size.height) {
                        goldenBugBitmap?.let { bitmap ->
                            drawImage(
                                image = bitmap,
                                srcOffset = androidx.compose.ui.unit.IntOffset.Zero,
                                srcSize = androidx.compose.ui.unit.IntSize(bitmap.width, bitmap.height),
                                dstOffset = androidx.compose.ui.unit.IntOffset(
                                    x = goldenBug.position.x.coerceIn(0f, size.width - 150f).toInt(),
                                    y = goldenBug.position.y.coerceIn(0f, size.height - 150f).toInt()
                                ),
                                dstSize = androidx.compose.ui.unit.IntSize(150, 150)
                            )
                        } ?: run {
                            drawText(
                                textMeasurer = textMeasurer,
                                text = goldenBug.emoji,
                                topLeft = Offset(
                                    x = goldenBug.position.x.coerceIn(0f, size.width - 150f),
                                    y = goldenBug.position.y.coerceIn(0f, size.height - 150f)
                                ),
                                style = androidx.compose.ui.text.TextStyle(fontSize = 96.sp)
                            )
                        }
                    }
                }
                
                gameState.bugs.forEach { bug ->
                    if (bug.position.x >= 0 && bug.position.x < size.width &&
                        bug.position.y >= 0 && bug.position.y < size.height) {
                        drawText(
                            textMeasurer = textMeasurer,
                            text = bug.emoji,
                            topLeft = Offset(
                                x = (bug.position.x + 10f).coerceIn(0f, size.width - 100f),
                                y = (bug.position.y + 10f).coerceIn(0f, size.height - 100f)
                            ),
                            style = androidx.compose.ui.text.TextStyle(fontSize = 72.sp)
                        )
                    }
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

private const val TAG = "GameScreen"
