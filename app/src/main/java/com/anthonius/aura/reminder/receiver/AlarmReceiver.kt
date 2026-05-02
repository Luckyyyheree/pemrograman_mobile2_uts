package com.anthonius.aura.reminder.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.anthonius.aura.reminder.service.AlarmService

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val reminderId = intent.getIntExtra("reminder_id", -1)
        val reminderTitle = intent.getStringExtra("reminder_title") ?: "Reminder"
        val reminderDesc = intent.getStringExtra("reminder_description") ?: ""
        val ringtoneUri = intent.getStringExtra("ringtone_uri") ?: ""

        val serviceIntent = Intent(context, AlarmService::class.java).apply {
            putExtra("reminder_id", reminderId)
            putExtra("reminder_title", reminderTitle)
            putExtra("reminder_description", reminderDesc)
            putExtra("ringtone_uri", ringtoneUri)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }
    }
}