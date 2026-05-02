package com.anthonius.aura.reminder.data.repository

import com.anthonius.aura.reminder.data.local.ReminderDao
import com.anthonius.aura.reminder.data.model.Reminder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class ReminderRepository(private val reminderDao: ReminderDao) {

    fun getAllReminders(): Flow<List<Reminder>> =
        reminderDao.getAllReminders()

    fun getActiveReminders(): Flow<List<Reminder>> =
        reminderDao.getActiveReminders()

    fun getRemindersByDate(startOfDay: Long, endOfDay: Long): Flow<List<Reminder>> =
        reminderDao.getRemindersByDate(startOfDay, endOfDay)

    fun getRemindersBetween(start: Long, end: Long): Flow<List<Reminder>> =
        reminderDao.getRemindersBetween(start, end)

    suspend fun getReminderById(id: Int): Reminder? =
        reminderDao.getReminderById(id)

    suspend fun insertReminder(reminder: Reminder): Long =
        reminderDao.insertReminder(reminder)

    suspend fun updateReminder(reminder: Reminder) =
        reminderDao.updateReminder(reminder)

    suspend fun deleteReminder(reminder: Reminder) =
        reminderDao.deleteReminder(reminder)

    suspend fun deleteAllReminders() =
        reminderDao.deleteAllReminders()

    suspend fun deleteRemindersByDate(startOfDay: Long, endOfDay: Long) =
        reminderDao.deleteRemindersByDate(startOfDay, endOfDay)

    suspend fun updatePriorityLevel(id: Int, level: Int) =
        reminderDao.updatePriorityLevel(id, level)

    suspend fun markAsCompleted(id: Int) =
        reminderDao.markAsCompleted(id)

    suspend fun getConflictingReminders(
        dateTime: Long,
        gapMinutes: Int = 60
    ): List<Reminder> {
        val start = dateTime - (gapMinutes * 60 * 1000)
        val end = dateTime + (gapMinutes * 60 * 1000)
        return reminderDao.getRemindersBetween(start, end)
            .first()
            .filter { !it.isCompleted && !it.isCancelled }
    }
}