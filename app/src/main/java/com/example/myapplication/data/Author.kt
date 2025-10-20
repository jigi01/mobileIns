package com.example.myapplication.data

data class Author(
    val name: String,
    val emoji: String
)

val authorsList = listOf(
    Author("Бурдинский Андрей", "👨‍💻"),
    Author("Жиглов Глеб", "👩‍💻"),
    Author("Игошин Матвей", "👨‍🎓")
)
