package com.example.myapplication.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

enum class Tab(val title: String) {
    GAME("Игра"),
    RECORDS("Рекорды"),
    REGISTRATION("Регистрация"),
    RULES("Правила"),
    AUTHORS("Авторы"),
    SETTINGS("Настройки")
}

@Composable
fun MainScreen() {
    var selectedTab by remember { mutableStateOf(Tab.GAME) }

    Column(modifier = Modifier.fillMaxSize()) {
        ScrollableTabRow(
            selectedTabIndex = selectedTab.ordinal,
            modifier = Modifier.fillMaxWidth()
        ) {
            Tab.entries.forEach { tab ->
                androidx.compose.material3.Tab(
                    selected = selectedTab == tab,
                    onClick = { selectedTab = tab },
                    text = { Text(tab.title) }
                )
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            when (selectedTab) {
                Tab.GAME -> GameScreen()
                Tab.RECORDS -> RecordsScreen()
                Tab.REGISTRATION -> RegistrationScreen()
                Tab.RULES -> RulesScreen()
                Tab.AUTHORS -> AuthorsScreen()
                Tab.SETTINGS -> SettingsScreen()
            }
        }
    }
}
