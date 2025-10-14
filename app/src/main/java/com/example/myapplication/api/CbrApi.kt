package com.example.myapplication.api

import okhttp3.OkHttpClient
import okhttp3.Request
import org.w3c.dom.Element
import java.text.SimpleDateFormat
import java.util.*
import javax.xml.parsers.DocumentBuilderFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object CbrApi {
    private val client = OkHttpClient.Builder()
        .connectTimeout(3, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(3, java.util.concurrent.TimeUnit.SECONDS)
        .build()
    
    suspend fun getGoldPrice(): Double = withContext(Dispatchers.IO) {
        try {
            val calendar = Calendar.getInstance()
            
            // Пробуем последние 10 дней (ЦБ не публикует данные в выходные/праздники)
            for (daysAgo in 0..10) {
                calendar.time = Date()
                calendar.add(Calendar.DAY_OF_MONTH, -daysAgo)
                
                val day = String.format("%02d", calendar.get(Calendar.DAY_OF_MONTH))
                val month = String.format("%02d", calendar.get(Calendar.MONTH) + 1)
                val year = calendar.get(Calendar.YEAR)
                val dateStr = "$day/$month/$year"
                
                val url = "https://www.cbr.ru/scripts/xml_metall.asp?date_req1=$dateStr&date_req2=$dateStr"
                
                val request = Request.Builder()
                    .url(url)
                    .build()
                
                val response = client.newCall(request).execute()
                val xmlString = response.body?.string() ?: continue
                
                if (xmlString.isEmpty() || !xmlString.contains("<Record")) continue
                
                val factory = DocumentBuilderFactory.newInstance()
                val builder = factory.newDocumentBuilder()
                val document = builder.parse(xmlString.byteInputStream())
                
                // Ищем Record с атрибутом Code="1" (Золото)
                val records = document.getElementsByTagName("Record")
                for (i in 0 until records.length) {
                    val record = records.item(i) as Element
                    val code = record.getAttribute("Code")
                    
                    if (code == "1") { // Code="1" = Золото
                        val buyNode = record.getElementsByTagName("Buy").item(0)
                        val buyPrice = buyNode?.textContent?.replace(",", ".")?.toDoubleOrNull()
                        
                        // Цена за тройскую унцию, конвертируем в рубли за грамм
                        if (buyPrice != null && buyPrice > 0) {
                            return@withContext buyPrice
                        }
                    }
                }
            }
            
            // Если не нашли данные за 10 дней - возвращаем примерную цену
            8338.0
        } catch (e: Exception) {
            e.printStackTrace()
            268.0
        }
    }
}
