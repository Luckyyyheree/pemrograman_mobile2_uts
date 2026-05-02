package com.anthonius.aura.reminder.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.anthonius.aura.reminder.AuraApplication
import com.anthonius.aura.reminder.data.model.Reminder
import com.anthonius.aura.reminder.util.AlarmScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import androidx.compose.runtime.mutableStateListOf
import com.anthonius.aura.reminder.ui.chatbot.ChatMessage


class ReminderViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as AuraApplication).repository
    private val context = application.applicationContext

    val allReminders: StateFlow<List<Reminder>> = repository
        .getAllReminders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeReminders: StateFlow<List<Reminder>> = repository
        .getActiveReminders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _conflictingReminders = MutableStateFlow<List<Reminder>>(emptyList())

    val conflictingReminders: StateFlow<List<Reminder>> = _conflictingReminders
    // Chat messages - persistent across tabs
    val chatMessages = mutableStateListOf(
        ChatMessage(
            text = "Halo! Saya Aura AI 👋\nSaya bisa membantu kamu mengecek jadwal, menghapus reminder, dan menjawab pertanyaan seputar jadwalmu.\n\nMau tanya apa?",
            isUser = false
        )
    )

    fun addChatMessage(message: ChatMessage) {
        chatMessages.add(message)
    }
    fun insertReminder(reminder: Reminder) {
        viewModelScope.launch {
            val id = repository.insertReminder(reminder)
            val savedReminder = reminder.copy(id = id.toInt())
            AlarmScheduler.scheduleAlarm(context, savedReminder)
        }
    }

    fun updateReminder(reminder: Reminder) {
        viewModelScope.launch {
            repository.updateReminder(reminder)
            AlarmScheduler.cancelAlarm(context, reminder.id)
            AlarmScheduler.scheduleAlarm(context, reminder)
        }
    }

    fun deleteReminder(reminder: Reminder) {
        viewModelScope.launch {
            repository.deleteReminder(reminder)
            AlarmScheduler.cancelAlarm(context, reminder.id)
        }
    }

    fun deleteAllReminders() {
        viewModelScope.launch {
            val reminders = allReminders.value
            reminders.forEach { AlarmScheduler.cancelAlarm(context, it.id) }
            repository.deleteAllReminders()
        }
    }
    fun clearCompletedReminders() {
        viewModelScope.launch {
            val completedReminders = allReminders.value.filter { it.isCompleted }
            completedReminders.forEach { reminder ->
                repository.deleteReminder(reminder)
                AlarmScheduler.cancelAlarm(context, reminder.id)
            }
        }
    }
    fun deleteRemindersByDate(startOfDay: Long, endOfDay: Long) {
        viewModelScope.launch {
            repository.deleteRemindersByDate(startOfDay, endOfDay)
        }
    }

    fun markAsCompleted(id: Int) {
        viewModelScope.launch {
            repository.markAsCompleted(id)
            AlarmScheduler.cancelAlarm(context, id)
        }
    }

    fun checkConflicts(dateTime: Long) {
        viewModelScope.launch {
            val conflicts = repository.getConflictingReminders(dateTime)
            _conflictingReminders.value = conflicts
        }
    }
    suspend fun getConflicts(dateTime: Long): List<Reminder> {
        return repository.getConflictingReminders(dateTime, gapMinutes = 60)
    }
    fun clearConflicts() {
        _conflictingReminders.value = emptyList()
    }

    fun updatePriorityLevels() {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val reminders = activeReminders.value
            reminders.forEach { reminder ->
                val diff = reminder.dateTime - now
                val newPriority = when {
                    diff < 0 -> 2
                    diff < 2 * 60 * 60 * 1000 -> 2
                    diff < 24 * 60 * 60 * 1000 -> 1
                    else -> 0
                }
                if (reminder.priorityLevel != newPriority) {
                    repository.updatePriorityLevel(reminder.id, newPriority)
                }
            }
        }
    }
}