package com.example.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.data.SovereignDataStore
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.flow.first
import java.util.Calendar

class NotificationWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val dataStore = SovereignDataStore(applicationContext)
        val isAr = dataStore.isArabic.first()
        val apiKey = dataStore.geminiApiKey.first().ifBlank { 
            com.asyria.v4.BuildConfig.GEMINI_API_KEY 
        }

        // 1. Prayer Alert Check (Simplified)
        checkAndNotifyPrayer(isAr)

        // 2. Gemini Security Alert
        if (apiKey.isNotEmpty() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                val model = GenerativeModel(modelName = "gemini-3.5-flash", apiKey = apiKey)
                val promptLocale = if (isAr) "Arabic" else "English"
                val prompt = "Provide a very brief (10 words max) high-priority cybersecurity alert about a modern hazard in $promptLocale. No intro."
                val res = model.generateContent(prompt).text ?: ""
                if (res.isNotBlank()) {
                    showNotification(
                        title = if (isAr) "تنبيه أمني سيادي" else "Sovereign Intelligence",
                        message = res.trim(),
                        id = 1002
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        return Result.success()
    }

    private fun checkAndNotifyPrayer(isAr: Boolean) {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        
        // Simple logic: if within 5 mins of a major prayer time, notify
        // This is a crude approximation for the task
        val prayers = mapOf(
            "FAJR" to (4 to 30),
            "DHUHR" to (12 to 45),
            "ASR" to (16 to 15),
            "MAGHRIB" to (19 to 30),
            "ISHA" to (21 to 0)
        )

        for ((name, time) in prayers) {
            if (hour == time.first && minute >= time.second && minute <= time.second + 4) {
                showNotification(
                    title = if (isAr) "نداء صلاة الأولوية" else "PRIORITY PRAYER LINK",
                    message = if (isAr) "تحذير: حان وقت $name. ابدأ بروتوكول الصلاة فوراً." else "ALERT: Time for $name. Initiate prayer protocol immediately.",
                    id = 1001
                )
                break
            }
        }
    }

    private fun showNotification(title: String, message: String, id: Int) {
        val channelId = "sovereign_alerts"
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "SOVEREIGN TACTICAL ALERTS", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Critical system and spiritual intercepts"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(android.R.drawable.stat_sys_warning)
            .setContentTitle("SYS_ALERT | $title")
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(id, notification)
    }
}
