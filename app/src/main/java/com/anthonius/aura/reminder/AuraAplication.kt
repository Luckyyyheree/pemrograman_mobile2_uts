package com.anthonius.aura.reminder

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import com.anthonius.aura.reminder.data.local.ReminderDatabase
import com.anthonius.aura.reminder.data.repository.ReminderRepository

class AuraApplication : Application() {

    val database by lazy { ReminderDatabase.getDatabase(this) }
    val repository by lazy { ReminderRepository(database.reminderDao()) }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        val manager = getSystemService(NotificationManager::class.java)

        // Channel untuk reminder biasa
        val reminderChannel = NotificationChannel(
            "aura_reminder_channel",
            "Aura Reminders",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notifikasi reminder Aura"
            enableVibration(true)
        }

        // Channel untuk alarm (urgent)
        val alarmChannel = NotificationChannel(
            "aura_alarm_channel",
            "Aura Alarm",
            NotificationManager.IMPORTANCE_MAX
        ).apply {
            description = "Alarm reminder Aura"
            enableVibration(true)
        }

        manager.createNotificationChannel(reminderChannel)
        manager.createNotificationChannel(alarmChannel)
    }
}