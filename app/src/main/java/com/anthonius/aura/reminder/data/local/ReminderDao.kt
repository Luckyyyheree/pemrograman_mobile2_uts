package com.anthonius.aura.reminder.data.local

import androidx.room.*
import com.anthonius.aura.reminder.data.model.Reminder
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {

    @Query("SELECT * FROM reminders ORDER BY dateTime ASC")
    fun getAllReminders(): Flow<List<Reminder>>

    @Query("SELECT * FROM reminders WHERE id = :id")
    suspend fun getReminderById(id: Int): Reminder?

    @Query("SELECT * FROM reminders WHERE dateTime BETWEEN :startOfDay AND :endOfDay ORDER BY dateTime ASC")
    fun getRemindersByDate(startOfDay: Long, endOfDay: Long): Flow<List<Reminder>>

    @Query("SELECT * FROM reminders WHERE isCompleted = 0 AND isCancelled = 0 ORDER BY dateTime ASC")
    fun getActiveReminders(): Flow<List<Reminder>>

    @Query("SELECT * FROM reminders WHERE dateTime BETWEEN :start AND :end ORDER BY dateTime ASC")
    fun getRemindersBetween(start: Long, end: Long): Flow<List<Reminder>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: Reminder): Long

    @Update
    suspend fun updateReminder(reminder: Reminder)

    @Delete
    suspend fun deleteReminder(reminder: Reminder)

    @Query("DELETE FROM reminders")
    suspend fun deleteAllReminders()

    @Query("DELETE FROM reminders WHERE dateTime BETWEEN :startOfDay AND :endOfDay")
    suspend fun deleteRemindersByDate(startOfDay: Long, endOfDay: Long)

    @Query("UPDATE reminders SET priorityLevel = :level WHERE id = :id")
    suspend fun updatePriorityLevel(id: Int, level: Int)

    @Query("UPDATE reminders SET isCompleted = 1 WHERE id = :id")
    suspend fun markAsCompleted(id: Int)
}