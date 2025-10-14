package com.example.myapplication.utils

import com.example.myapplication.data.ZodiacSign
import java.util.Calendar

object ZodiacCalculator {
    fun calculateZodiacSign(birthDateMillis: Long): ZodiacSign {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = birthDateMillis
        
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        
        return when {
            (month == 3 && day >= 21) || (month == 4 && day <= 20) -> ZodiacSign.ARIES
            (month == 4 && day >= 21) || (month == 5 && day <= 21) -> ZodiacSign.TAURUS
            (month == 5 && day >= 22) || (month == 6 && day <= 21) -> ZodiacSign.GEMINI
            (month == 6 && day >= 22) || (month == 7 && day <= 22) -> ZodiacSign.CANCER
            (month == 7 && day >= 23) || (month == 8 && day <= 23) -> ZodiacSign.LEO
            (month == 8 && day >= 24) || (month == 9 && day <= 23) -> ZodiacSign.VIRGO
            (month == 9 && day >= 24) || (month == 10 && day <= 23) -> ZodiacSign.LIBRA
            (month == 10 && day >= 24) || (month == 11 && day <= 22) -> ZodiacSign.SCORPIO
            (month == 11 && day >= 23) || (month == 12 && day <= 21) -> ZodiacSign.SAGITTARIUS
            (month == 12 && day >= 22) || (month == 1 && day <= 20) -> ZodiacSign.CAPRICORN
            (month == 1 && day >= 21) || (month == 2 && day <= 19) -> ZodiacSign.AQUARIUS
            else -> ZodiacSign.PISCES
        }
    }
}
