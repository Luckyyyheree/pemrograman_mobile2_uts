package com.anthonius.aura.reminder.ui.home

import android.media.RingtoneManager
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.anthonius.aura.reminder.data.model.Reminder
import com.anthonius.aura.reminder.ui.components.RingtonePickerDialog
import com.anthonius.aura.reminder.ui.theme.*
import com.anthonius.aura.reminder.viewmodel.ReminderViewModel
import kotlinx.coroutines.launch
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddReminderSheet(
    onDismiss: () -> Unit,
    onSave: (Reminder) -> Unit,
    viewModel: ReminderViewModel
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Personal") }
    var selectedReminderBefore by remember { mutableIntStateOf(10) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var selectedDateMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    var selectedHour by remember { mutableIntStateOf(Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) }
    var selectedMinute by remember { mutableIntStateOf(Calendar.getInstance().get(Calendar.MINUTE)) }
    var showConflictDialog by remember { mutableStateOf(false) }
    var pendingReminder by remember { mutableStateOf<Reminder?>(null) }
    var conflictList by remember { mutableStateOf<List<Reminder>>(emptyList()) }
    var selectedRingtoneUri by remember { mutableStateOf("") }
    var showRingtonePicker by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val categories = listOf("Personal", "Work", "Health", "Sport", "Shopping", "Other")
    val reminderBeforeOptions = listOf(5, 10, 15, 30, 60)

    val calendar = Calendar.getInstance().apply {
        timeInMillis = selectedDateMillis
        set(Calendar.HOUR_OF_DAY, selectedHour)
        set(Calendar.MINUTE, selectedMinute)
        set(Calendar.SECOND, 0)
    }
    val finalDateTime = calendar.timeInMillis

    val ringtoneName = remember(selectedRingtoneUri) {
        if (selectedRingtoneUri.isEmpty()) "Default Alarm"
        else {
            try {
                val uri = android.net.Uri.parse(selectedRingtoneUri)
                RingtoneManager.getRingtone(context, uri)
                    ?.getTitle(context) ?: "Custom Ringtone"
            } catch (e: Exception) {
                "Custom Ringtone"
            }
        }
    }

    if (showConflictDialog && pendingReminder != null) {
        ConflictDialog(
            conflictingReminders = conflictList,
            pendingReminder = pendingReminder!!,
            onDismiss = {
                showConflictDialog = false
                pendingReminder = null
                conflictList = emptyList()
            },
            onConfirm = { reminder ->
                onSave(reminder)
                showConflictDialog = false
                pendingReminder = null
                conflictList = emptyList()
            }
        )
    }

    if (showRingtonePicker) {
        RingtonePickerDialog(
            currentRingtoneUri = selectedRingtoneUri,
            onDismiss = { showRingtonePicker = false },
            onSelect = { uri -> selectedRingtoneUri = uri }
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
                    Text(
                        text = "New Reminder",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
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
                                text = ringtoneName,
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

                Spacer(modifier = Modifier.height(24.dp))

                // Save Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            if (title.isNotEmpty()) Brush.linearGradient(
                                colors = listOf(AuraPurple, AuraBlue)
                            ) else Brush.linearGradient(
                                colors = listOf(Color(0x44FFFFFF), Color(0x44FFFFFF))
                            )
                        )
                        .clickable(enabled = title.isNotEmpty()) {
                            val reminder = Reminder(
                                title = title,
                                description = description,
                                dateTime = finalDateTime,
                                category = selectedCategory,
                                reminderBeforeMinutes = selectedReminderBefore,
                                ringtoneUri = selectedRingtoneUri
                            )
                            coroutineScope.launch {
                                val conflicts = viewModel.getConflicts(finalDateTime)
                                if (conflicts.isNotEmpty()) {
                                    conflictList = conflicts
                                    pendingReminder = reminder
                                    showConflictDialog = true
                                } else {
                                    onSave(reminder)
                                }
                            }
                        }
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Save Reminder",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }

    // Date Picker
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

    // Time Picker
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

@Composable
fun GlassTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = Color(0x99FFFFFF)) },
        leadingIcon = {
            Icon(icon, contentDescription = null, tint = AuraCyan)
        },
        modifier = Modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedBorderColor = AuraPurple,
            unfocusedBorderColor = Color(0x33FFFFFF),
            cursorColor = AuraPurple,
            focusedContainerColor = Color(0x22FFFFFF),
            unfocusedContainerColor = Color(0x11FFFFFF)
        ),
        shape = RoundedCornerShape(12.dp)
    )
}

@Composable
fun CategoryChip(
    modifier: Modifier = Modifier,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(
                if (isSelected) Brush.linearGradient(
                    colors = listOf(AuraPurple.copy(alpha = 0.5f), AuraBlue.copy(alpha = 0.5f))
                ) else Brush.linearGradient(
                    colors = listOf(Color(0x22FFFFFF), Color(0x22FFFFFF))
                )
            )
            .border(
                1.dp,
                if (isSelected) AuraPurple else Color(0x33FFFFFF),
                RoundedCornerShape(10.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = if (isSelected) Color.White else Color(0x99FFFFFF),
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

@Composable
fun ReminderBeforeChip(
    modifier: Modifier = Modifier,
    minutes: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(
                if (isSelected) Brush.linearGradient(
                    colors = listOf(AuraCyan.copy(alpha = 0.3f), AuraBlue.copy(alpha = 0.3f))
                ) else Brush.linearGradient(
                    colors = listOf(Color(0x22FFFFFF), Color(0x22FFFFFF))
                )
            )
            .border(
                1.dp,
                if (isSelected) AuraCyan else Color(0x33FFFFFF),
                RoundedCornerShape(10.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "${minutes}m",
            fontSize = 11.sp,
            color = if (isSelected) AuraCyan else Color(0x99FFFFFF),
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}