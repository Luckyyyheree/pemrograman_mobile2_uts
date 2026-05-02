package com.anthonius.aura.reminder.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.anthonius.aura.reminder.data.model.Reminder
import com.anthonius.aura.reminder.ui.components.GlassCard
import com.anthonius.aura.reminder.ui.home.ReminderCard
import com.anthonius.aura.reminder.ui.theme.*
import com.anthonius.aura.reminder.viewmodel.ReminderViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CalendarScreen(
    viewModel: ReminderViewModel = viewModel()
) {
    val allReminders by viewModel.allReminders.collectAsState()
    var currentMonth by remember { mutableStateOf(Calendar.getInstance()) }
    var selectedDate by remember { mutableStateOf<Calendar?>(null) }

    val selectedDayReminders = remember(selectedDate, allReminders) {
        selectedDate?.let { date ->
            allReminders.filter { reminder ->
                val reminderCal = Calendar.getInstance().apply { timeInMillis = reminder.dateTime }
                reminderCal.get(Calendar.YEAR) == date.get(Calendar.YEAR) &&
                        reminderCal.get(Calendar.DAY_OF_YEAR) == date.get(Calendar.DAY_OF_YEAR)
            }
        } ?: emptyList()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = "Calendar",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = "Your schedule overview",
            fontSize = 14.sp,
            color = Color(0x99FFFFFF)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Calendar Card
        GlassCard(cornerRadius = 20.dp) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Month Navigation
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        currentMonth = (currentMonth.clone() as Calendar).apply {
                            add(Calendar.MONTH, -1)
                        }
                        selectedDate = null
                    }) {
                        Icon(
                            Icons.Filled.ChevronLeft,
                            contentDescription = "Previous",
                            tint = Color.White
                        )
                    }

                    val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
                    Text(
                        text = monthFormat.format(currentMonth.time),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )

                    IconButton(onClick = {
                        currentMonth = (currentMonth.clone() as Calendar).apply {
                            add(Calendar.MONTH, 1)
                        }
                        selectedDate = null
                    }) {
                        Icon(
                            Icons.Filled.ChevronRight,
                            contentDescription = "Next",
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Day headers
                Row(modifier = Modifier.fillMaxWidth()) {
                    listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach { day ->
                        Text(
                            text = day,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (day == "Sun") AuraPink else Color(0x99FFFFFF)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Calendar Grid
                val daysInMonth = getDaysInMonth(currentMonth, allReminders)
                val weeks = daysInMonth.chunked(7)

                weeks.forEach { week ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        week.forEach { dayInfo ->
                            DayCell(
                                modifier = Modifier.weight(1f),
                                dayInfo = dayInfo,
                                isSelected = selectedDate?.let { sel ->
                                    dayInfo.date?.let { date ->
                                        sel.get(Calendar.YEAR) == date.get(Calendar.YEAR) &&
                                                sel.get(Calendar.DAY_OF_YEAR) == date.get(Calendar.DAY_OF_YEAR)
                                    }
                                } ?: false,
                                onClick = {
                                    dayInfo.date?.let {
                                        selectedDate = if (selectedDate?.get(Calendar.DAY_OF_YEAR) ==
                                            it.get(Calendar.DAY_OF_YEAR)) null else it
                                    }
                                }
                            )
                        }
                        // Fill remaining cells
                        repeat(7 - week.size) {
                            Box(modifier = Modifier.weight(1f))
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Selected day reminders
        if (selectedDate != null) {
            val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
            Text(
                text = if (selectedDayReminders.isEmpty())
                    "No reminders on ${dateFormat.format(selectedDate!!.time)}"
                else
                    "Reminders on ${dateFormat.format(selectedDate!!.time)}",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (selectedDayReminders.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Tap + on Home to add a reminder",
                        color = Color(0x66FFFFFF),
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 100.dp)
                ) {
                    items(selectedDayReminders) { reminder ->
                        ReminderCard(
                            reminder = reminder,
                            onComplete = { viewModel.markAsCompleted(reminder.id) },
                            onDelete = { viewModel.deleteReminder(reminder) }
                        )
                    }
                }
            }
        }
    }
}

data class DayInfo(
    val date: Calendar?,
    val dayNumber: Int,
    val hasReminder: Boolean,
    val isToday: Boolean,
    val reminderCount: Int = 0
)

fun getDaysInMonth(month: Calendar, reminders: List<Reminder>): List<DayInfo> {
    val result = mutableListOf<DayInfo>()
    val cal = month.clone() as Calendar
    cal.set(Calendar.DAY_OF_MONTH, 1)

    val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1
    val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    val today = Calendar.getInstance()

    repeat(firstDayOfWeek) {
        result.add(DayInfo(null, 0, false, false))
    }

    for (day in 1..daysInMonth) {
        val dayCal = cal.clone() as Calendar
        dayCal.set(Calendar.DAY_OF_MONTH, day)

        val dayReminders = reminders.filter { reminder ->
            val reminderCal = Calendar.getInstance().apply { timeInMillis = reminder.dateTime }
            reminderCal.get(Calendar.YEAR) == dayCal.get(Calendar.YEAR) &&
                    reminderCal.get(Calendar.DAY_OF_YEAR) == dayCal.get(Calendar.DAY_OF_YEAR)
        }

        val isToday = today.get(Calendar.YEAR) == dayCal.get(Calendar.YEAR) &&
                today.get(Calendar.DAY_OF_YEAR) == dayCal.get(Calendar.DAY_OF_YEAR)

        result.add(
            DayInfo(
                date = dayCal,
                dayNumber = day,
                hasReminder = dayReminders.isNotEmpty(),
                isToday = isToday,
                reminderCount = dayReminders.size
            )
        )
    }

    return result
}

@Composable
fun DayCell(
    modifier: Modifier = Modifier,
    dayInfo: DayInfo,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    if (dayInfo.date == null) {
        Box(modifier = modifier.padding(4.dp))
        return
    }

    val isToday = dayInfo.isToday
    val hasReminder = dayInfo.hasReminder

    Box(
        modifier = modifier
            .padding(3.dp)
            .aspectRatio(1f)
            .clip(CircleShape)
            .background(
                when {
                    isSelected -> Brush.linearGradient(
                        colors = listOf(AuraPurple, AuraBlue)
                    )
                    isToday -> Brush.linearGradient(
                        colors = listOf(AuraCyan.copy(alpha = 0.3f), AuraBlue.copy(alpha = 0.3f))
                    )
                    else -> Brush.linearGradient(
                        colors = listOf(Color.Transparent, Color.Transparent)
                    )
                }
            )
            .border(
                width = if (isToday && !isSelected) 1.dp else 0.dp,
                color = if (isToday && !isSelected) AuraCyan else Color.Transparent,
                shape = CircleShape
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${dayInfo.dayNumber}",
                fontSize = 13.sp,
                fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal,
                color = when {
                    isSelected -> Color.White
                    isToday -> AuraCyan
                    else -> Color(0xCCFFFFFF)
                }
            )
            if (hasReminder) {
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) Color.White else AuraPurple
                        )
                )
            }
        }
    }
}