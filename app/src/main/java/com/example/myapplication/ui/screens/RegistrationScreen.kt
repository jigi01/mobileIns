package com.example.myapplication.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.data.Gender
import com.example.myapplication.data.PlayerData
import com.example.myapplication.data.ZodiacSign
import com.example.myapplication.utils.ZodiacCalculator
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrationScreen() {
    var fullName by remember { mutableStateOf("") }
    var selectedGender by remember { mutableStateOf(Gender.MALE) }
    var selectedCourse by remember { mutableStateOf(1) }
    var difficultyLevel by remember { mutableFloatStateOf(1f) }
    var selectedDate by remember { mutableStateOf(Calendar.getInstance().timeInMillis) }
    var zodiacSign by remember { mutableStateOf(ZodiacSign.ARIES) }
    var showData by remember { mutableStateOf(false) }
    var expandedCourse by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    val dateFormat = remember { SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Регистрация игрока",
            style = MaterialTheme.typography.headlineMedium
        )

        OutlinedTextField(
            value = fullName,
            onValueChange = { fullName = it },
            label = { Text("ФИО") },
            modifier = Modifier.fillMaxWidth()
        )

        Text("Пол:", style = MaterialTheme.typography.titleMedium)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Gender.entries.forEach { gender ->
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedGender == gender,
                        onClick = { selectedGender = gender }
                    )
                    Text(gender.displayName)
                }
            }
        }

        ExposedDropdownMenuBox(
            expanded = expandedCourse,
            onExpandedChange = { expandedCourse = it }
        ) {
            OutlinedTextField(
                value = "$selectedCourse курс",
                onValueChange = {},
                readOnly = true,
                label = { Text("Курс") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCourse) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = expandedCourse,
                onDismissRequest = { expandedCourse = false }
            ) {
                (1..6).forEach { course ->
                    DropdownMenuItem(
                        text = { Text("$course курс") },
                        onClick = {
                            selectedCourse = course
                            expandedCourse = false
                        }
                    )
                }
            }
        }

        Column {
            Text("Уровень сложности: ${difficultyLevel.toInt()}", style = MaterialTheme.typography.titleMedium)
            Slider(
                value = difficultyLevel,
                onValueChange = { difficultyLevel = it },
                valueRange = 1f..10f,
                steps = 8,
                modifier = Modifier.fillMaxWidth()
            )
        }

        OutlinedButton(
            onClick = { showDatePicker = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Дата рождения: ${dateFormat.format(Date(selectedDate))}")
        }

        if (showDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text("Отмена")
                    }
                }
            ) {
                val datePickerState = rememberDatePickerState(
                    initialSelectedDateMillis = selectedDate
                )
                DatePicker(
                    state = datePickerState,
                    showModeToggle = false
                )
                LaunchedEffect(datePickerState.selectedDateMillis) {
                    datePickerState.selectedDateMillis?.let {
                        selectedDate = it
                        zodiacSign = ZodiacCalculator.calculateZodiacSign(it)
                    }
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Знак зодиака:", style = MaterialTheme.typography.titleMedium)
                    Text(zodiacSign.displayName)
                    Text(zodiacSign.dateRange, style = MaterialTheme.typography.bodySmall)
                }
                Text(zodiacSign.emoji, fontSize = 48.sp)
            }
        }

        Button(
            onClick = {
                zodiacSign = ZodiacCalculator.calculateZodiacSign(selectedDate)
                showData = true
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Зарегистрировать")
        }

        if (showData) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Данные игрока:", style = MaterialTheme.typography.titleLarge)
                    Text("ФИО: $fullName")
                    Text("Пол: ${selectedGender.displayName}")
                    Text("Курс: $selectedCourse")
                    Text("Уровень сложности: ${difficultyLevel.toInt()}")
                    Text("Дата рождения: ${dateFormat.format(Date(selectedDate))}")
                    Text("Знак зодиака: ${zodiacSign.displayName} ${zodiacSign.emoji}")
                }
            }
        }
    }
}
