package com.example.myapplication.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun RulesScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Правила игры «Жуки»",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = "Цель игры",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Уничтожить как можно больше насекомых за отведенное время, набрав максимальное количество очков.",
            style = MaterialTheme.typography.bodyLarge
        )
        
        Text(
            text = "Правила",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "• Нажимайте на насекомых, чтобы уничтожить их и получить очки",
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = "• За каждый промах вы получаете штраф",
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = "• Скорость и количество насекомых зависит от настроек игры",
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = "• Уровень сложности влияет на начисление очков",
            style = MaterialTheme.typography.bodyLarge
        )
        
        Text(
            text = "Бонусы",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "• Каждые 15 секунд появляется специальный бонус",
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = "• При активации бонуса насекомые начинают реагировать на наклон телефона",
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = "• Золотой таракан появляется каждые 20 секунд и дает очки пропорционально курсу золота",
            style = MaterialTheme.typography.bodyLarge
        )
        
        Text(
            text = "Управление",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "• Тапайте по экрану для уничтожения насекомых",
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = "• После активации бонуса наклоняйте телефон, чтобы управлять движением насекомых",
            style = MaterialTheme.typography.bodyLarge
        )
        
        Text(
            text = "Удачи в охоте на жуков!",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}
