package com.anthonius.aura.reminder.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reminders")
data class Reminder(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val description: String = "",
    val dateTime: Long, // simpan sebagai timestamp
    val category: String = "Personal",
    val reminderBeforeMinutes: Int = 10, // 5,10,15,30,60
    val isCompleted: Boolean = false,
    val isCancelled: Boolean = false,
    val isAiSuggested: Boolean = false, // hasil saran AI conflict solver
    val priorityLevel: Int = 0, // 0=Normal, 1=Upcoming, 2=Urgent
    val ringtoneUri: String = "", // custom ringtone pilihan user
    val createdAt: Long = System.currentTimeMillis()
)