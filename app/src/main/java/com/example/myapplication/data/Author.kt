package com.example.myapplication.data

data class Author(
    val name: String,
    val emoji: String
)

val authorsList = listOf(
    Author("Иванов Иван Иванович", "👨‍💻"),
    Author("Петрова Мария Сергеевна", "👩‍💻"),
    Author("Сидоров Алексей Петрович", "👨‍🎓")
)
