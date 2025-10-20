package com.example.myapplication.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.*
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.text.FontWeight
import androidx.glance.unit.ColorProvider
import com.example.myapplication.api.CbrApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GameWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val goldPrice = try {
            withContext(Dispatchers.IO) {
                CbrApi.getGoldPrice().toInt()
            }
        } catch (e: Exception) {
            268 
        }
        
        provideContent {
            GameWidgetContent(goldPrice)
        }
    }
}

@Composable
fun GameWidgetContent(goldPrice: Int) {
    Column(
        modifier = GlanceModifier.fillMaxSize().padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "🪙",
            style = TextStyle(fontSize = 72.sp)
        )
        Text(
            text = "$goldPrice ₽/г",
            style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = ColorProvider(Color.White))
        )
    }
}

class GameWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = GameWidget()
}
