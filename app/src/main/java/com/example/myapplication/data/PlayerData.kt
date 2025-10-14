package com.example.myapplication.data

data class PlayerData(
    val fullName: String = "",
    val gender: Gender = Gender.MALE,
    val course: Int = 1,
    val difficultyLevel: Int = 1,
    val birthDate: Long = System.currentTimeMillis(),
    val zodiacSign: ZodiacSign = ZodiacSign.ARIES
)

enum class Gender(val displayName: String) {
    MALE("Мужской"),
    FEMALE("Женский")
}

enum class ZodiacSign(val displayName: String, val emoji: String, val dateRange: String) {
    ARIES("Овен", "♈", "21.03 - 20.04"),
    TAURUS("Телец", "♉", "21.04 - 21.05"),
    GEMINI("Близнецы", "♊", "22.05 - 21.06"),
    CANCER("Рак", "♋", "22.06 - 22.07"),
    LEO("Лев", "♌", "23.07 - 23.08"),
    VIRGO("Дева", "♍", "24.08 - 23.09"),
    LIBRA("Весы", "♎", "24.09 - 23.10"),
    SCORPIO("Скорпион", "♏", "24.10 - 22.11"),
    SAGITTARIUS("Стрелец", "♐", "23.11 - 21.12"),
    CAPRICORN("Козерог", "♑", "22.12 - 20.01"),
    AQUARIUS("Водолей", "♒", "21.01 - 19.02"),
    PISCES("Рыбы", "♓", "20.02 - 20.03")
}
