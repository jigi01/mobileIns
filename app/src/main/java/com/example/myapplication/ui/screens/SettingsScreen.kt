package com.example.myapplication.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.GameSettings

@Composable
fun SettingsScreen() {
    var gameSpeed by remember { mutableFloatStateOf(5f) }
    var maxBugs by remember { mutableFloatStateOf(10f) }
    var bonusInterval by remember { mutableFloatStateOf(15f) }
    var roundDuration by remember { mutableFloatStateOf(60f) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            text = "Настройки игры",
            style = MaterialTheme.typography.headlineMedium
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column {
                    Text(
                        text = "Скорость игры: ${gameSpeed.toInt()}",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Slider(
                        value = gameSpeed,
                        onValueChange = { gameSpeed = it },
                        valueRange = 1f..10f,
                        steps = 8
                    )
                    Text(
                        text = "Управляет скоростью движения насекомых",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Divider()

                Column {
                    Text(
                        text = "Максимальное количество тараканов: ${maxBugs.toInt()}",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Slider(
                        value = maxBugs,
                        onValueChange = { maxBugs = it },
                        valueRange = 5f..30f,
                        steps = 24
                    )
                    Text(
                        text = "Максимальное количество насекомых на экране одновременно",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Divider()

                Column {
                    Text(
                        text = "Интервал появления бонусов: ${bonusInterval.toInt()} сек",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Slider(
                        value = bonusInterval,
                        onValueChange = { bonusInterval = it },
                        valueRange = 10f..30f,
                        steps = 19
                    )
                    Text(
                        text = "Частота появления специальных бонусов",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Divider()

                Column {
                    Text(
                        text = "Длительность раунда: ${roundDuration.toInt()} сек",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Slider(
                        value = roundDuration,
                        onValueChange = { roundDuration = it },
                        valueRange = 30f..300f,
                        steps = 26
                    )
                    Text(
                        text = "Продолжительность одного игрового раунда",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Button(
            onClick = { /* TODO: Сохранить настройки */ },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Сохранить настройки")
        }
    }
}
