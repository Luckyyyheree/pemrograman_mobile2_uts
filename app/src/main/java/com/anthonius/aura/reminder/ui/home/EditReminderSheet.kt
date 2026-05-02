package com.anthonius.aura.reminder.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.anthonius.aura.reminder.data.model.Reminder
import com.anthonius.aura.reminder.ui.theme.*
import com.anthonius.aura.reminder.viewmodel.ReminderViewModel
import kotlinx.coroutines.launch
import java.util.*
import com.anthonius.aura.reminder.ui.components.RingtonePickerDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditReminderSheet(
    reminder: Reminder,
    onDismiss: () -> Unit,
    onSave: (Reminder) -> Unit,
    viewModel: ReminderViewModel
) {
    var title by remember { mutableStateOf(reminder.title) }
    var description by remember { mutableStateOf(reminder.description) }
    var selectedCategory by remember { mutableStateOf(reminder.category) }
    var selectedReminderBefore by remember { mutableIntStateOf(reminder.reminderBeforeMinutes) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val initialCal = Calendar.getInstance().apply { timeInMillis = reminder.dateTime }
    var selectedDateMillis by remember { mutableStateOf(reminder.dateTime) }
    var selectedHour by remember { mutableIntStateOf(initialCal.get(Calendar.HOUR_OF_DAY)) }
    var selectedMinute by remember { mutableIntStateOf(initialCal.get(Calendar.MINUTE)) }
    var showConflictDialog by remember { mutableStateOf(false) }
    var pendingReminder by remember { mutableStateOf<Reminder?>(null) }
    var conflictList by remember { mutableStateOf<List<Reminder>>(emptyList()) }
    val coroutineScope = rememberCoroutineScope()
    var selectedRingtoneUri by remember { mutableStateOf(reminder.ringtoneUri) }
    var showRingtonePicker by remember { mutableStateOf(false) }
    val categories = listOf("Personal", "Work", "Health", "Sport", "Shopping", "Other")
    val reminderBeforeOptions = listOf(5, 10, 15, 30, 60)

    val calendar = Calendar.getInstance().apply {
        timeInMillis = selectedDateMillis
        set(Calendar.HOUR_OF_DAY, selectedHour)
        set(Calendar.MINUTE, selectedMinute)
        set(Calendar.SECOND, 0)
    }
    val finalDateTime = calendar.timeInMillis

    if (showConflictDialog && pendingReminder != null) {
        ConflictDialog(
            conflictingReminders = conflictList,
            pendingReminder = pendingReminder!!,
            onDismiss = {
                showConflictDialog = false
                pendingReminder = null
                conflictList = emptyList()
            },
            onConfirm = { updatedReminder ->
                onSave(updatedReminder)
                showConflictDialog = false
                pendingReminder = null
                conflictList = emptyList()
            }
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(0xCC0D0B1E), Color(0xCC0A0E1A))
                    )
                )
                .border(
                    1.dp,
                    Brush.linearGradient(
                        colors = listOf(Color(0x66FFFFFF), Color(0x22FFFFFF))
                    ),
                    RoundedCornerShape(28.dp)
                )
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.Edit,
                            contentDescription = null,
                            tint = AuraCyan,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Edit Reminder",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Close",
                            tint = Color(0x99FFFFFF)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Title
                GlassTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = "Title",
                    icon = Icons.Filled.Title
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Description
                GlassTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = "Description (optional)",
                    icon = Icons.Filled.Notes
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Date & Time
                Text(
                    text = "Date & Time",
                    fontSize = 14.sp,
                    color = Color(0x99FFFFFF),
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0x22FFFFFF))
                            .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(12.dp))
                            .clickable { showDatePicker = true }
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.CalendarMonth,
                                contentDescription = null,
                                tint = AuraCyan,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = java.text.SimpleDateFormat(
                                    "dd MMM yyyy", Locale.getDefault()
                                ).format(java.util.Date(selectedDateMillis)),
                                color = Color.White,
                                fontSize = 13.sp
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0x22FFFFFF))
                            .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(12.dp))
                            .clickable { showTimePicker = true }
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.AccessTime,
                                contentDescription = null,
                                tint = AuraCyan,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = String.format("%02d:%02d", selectedHour, selectedMinute),
                                color = Color.White,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Category
                Text(
                    text = "Category",
                    fontSize = 14.sp,
                    color = Color(0x99FFFFFF),
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.take(3).forEach { category ->
                        CategoryChip(
                            modifier = Modifier.weight(1f),
                            label = category,
                            isSelected = selectedCategory == category,
                            onClick = { selectedCategory = category }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.drop(3).forEach { category ->
                        CategoryChip(
                            modifier = Modifier.weight(1f),
                            label = category,
                            isSelected = selectedCategory == category,
                            onClick = { selectedCategory = category }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Remind Before
                Text(
                    text = "Remind me before",
                    fontSize = 14.sp,
                    color = Color(0x99FFFFFF),
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    reminderBeforeOptions.forEach { minutes ->
                        ReminderBeforeChip(
                            modifier = Modifier.weight(1f),
                            minutes = minutes,
                            isSelected = selectedReminderBefore == minutes,
                            onClick = { selectedReminderBefore = minutes }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                Spacer(modifier = Modifier.height(16.dp))

// Ringtone Picker
                Text(
                    text = "Nada Alarm",
                    fontSize = 14.sp,
                    color = Color(0x99FFFFFF),
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0x22FFFFFF))
                        .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(12.dp))
                        .clickable { showRingtonePicker = true }
                        .padding(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.MusicNote,
                                contentDescription = null,
                                tint = AuraCyan,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (selectedRingtoneUri.isEmpty()) "Default Alarm"
                                else "Custom Ringtone",
                                color = Color.White,
                                fontSize = 13.sp
                            )
                        }
                        Icon(
                            Icons.Filled.ChevronRight,
                            contentDescription = null,
                            tint = Color(0x66FFFFFF),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                if (showRingtonePicker) {
                    RingtonePickerDialog(
                        currentRingtoneUri = selectedRingtoneUri,
                        onDismiss = { showRingtonePicker = false },
                        onSelect = { uri ->
                            selectedRingtoneUri = uri
                        }
                    )
                }
                // Save Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            if (title.isNotEmpty()) Brush.linearGradient(
                                colors = listOf(AuraCyan, AuraBlue)
                            ) else Brush.linearGradient(
                                colors = listOf(Color(0x44FFFFFF), Color(0x44FFFFFF))
                            )
                        )
                        .clickable(enabled = title.isNotEmpty()) {
                            val updatedReminder = reminder.copy(
                                title = title,
                                description = description,
                                dateTime = finalDateTime,
                                category = selectedCategory,
                                reminderBeforeMinutes = selectedReminderBefore,
                                ringtoneUri = selectedRingtoneUri
                            )
                            coroutineScope.launch {
                                val conflicts = viewModel.getConflicts(finalDateTime)
                                    .filter { it.id != reminder.id }
                                if (conflicts.isNotEmpty()) {
                                    conflictList = conflicts
                                    pendingReminder = updatedReminder
                                    showConflictDialog = true
                                } else {
                                    onSave(updatedReminder)
                                }
                            }
                        }
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Update Reminder",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDateMillis
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        selectedDateMillis = it
                    }
                    showDatePicker = false
                }) {
                    Text("OK", color = AuraPurple)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel", color = Color(0x99FFFFFF))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = selectedHour,
            initialMinute = selectedMinute
        )
        Dialog(onDismissRequest = { showTimePicker = false }) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFF1A1040))
                    .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(24.dp))
                    .padding(24.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "Select Time",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    TimePicker(state = timePickerState)
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showTimePicker = false }) {
                            Text("Cancel", color = Color(0x99FFFFFF))
                        }
                        TextButton(onClick = {
                            selectedHour = timePickerState.hour
                            selectedMinute = timePickerState.minute
                            showTimePicker = false
                        }) {
                            Text("OK", color = AuraPurple)
                        }
                    }
                }
            }
        }
    }
}