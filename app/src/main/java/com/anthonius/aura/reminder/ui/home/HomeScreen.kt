package com.anthonius.aura.reminder.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.anthonius.aura.reminder.data.model.Reminder
import com.anthonius.aura.reminder.ui.components.GlassCard
import com.anthonius.aura.reminder.ui.theme.*
import com.anthonius.aura.reminder.viewmodel.ReminderViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(
    viewModel: ReminderViewModel = viewModel()
) {
    val reminders by viewModel.activeReminders.collectAsState()
    var showAddReminder by remember { mutableStateOf(false) }
    var editingReminder by remember { mutableStateOf<Reminder?>(null) }

    LaunchedEffect(Unit) {
        viewModel.updatePriorityLevels()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(48.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Aura",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Your smart reminder",
                        fontSize = 14.sp,
                        color = Color(0x99FFFFFF)
                    )
                }
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(AuraPurple, AuraBlue)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${reminders.size}",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SummaryCard(
                    modifier = Modifier.weight(1f),
                    title = "Today",
                    count = reminders.count { isSameDay(it.dateTime, System.currentTimeMillis()) },
                    color = AuraBlue
                )
                SummaryCard(
                    modifier = Modifier.weight(1f),
                    title = "Urgent",
                    count = reminders.count { it.priorityLevel == 2 },
                    color = PriorityUrgent
                )
                SummaryCard(
                    modifier = Modifier.weight(1f),
                    title = "Upcoming",
                    count = reminders.count { it.priorityLevel == 1 },
                    color = PriorityUpcoming
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Reminders",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (reminders.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Filled.NotificationsNone,
                            contentDescription = null,
                            tint = Color(0x66FFFFFF),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No reminders yet",
                            color = Color(0x66FFFFFF),
                            fontSize = 16.sp
                        )
                        Text(
                            text = "Tap + to add one",
                            color = Color(0x44FFFFFF),
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 100.dp)
                ) {
                    items(reminders) { reminder ->
                        ReminderCard(
                            reminder = reminder,
                            onComplete = { viewModel.markAsCompleted(reminder.id) },
                            onDelete = { viewModel.deleteReminder(reminder) },
                            onEdit = { editingReminder = reminder }
                        )
                    }
                }
            }
        }

        // FAB
        FloatingActionButton(
            onClick = { showAddReminder = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 24.dp, bottom = 90.dp),
            containerColor = Color.Transparent,
            elevation = FloatingActionButtonDefaults.elevation(0.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(AuraPurple, AuraBlue)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Add Reminder",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }

    // Add Reminder Sheet
    if (showAddReminder) {
        AddReminderSheet(
            onDismiss = { showAddReminder = false },
            onSave = { reminder ->
                viewModel.insertReminder(reminder)
                showAddReminder = false
            },
            viewModel = viewModel
        )
    }

    // Edit Reminder Sheet
    editingReminder?.let { reminder ->
        EditReminderSheet(
            reminder = reminder,
            onDismiss = { editingReminder = null },
            onSave = { updatedReminder ->
                viewModel.updateReminder(updatedReminder)
                editingReminder = null
            },
            viewModel = viewModel
        )
    }
}

@Composable
fun SummaryCard(
    modifier: Modifier = Modifier,
    title: String,
    count: Int,
    color: Color
) {
    GlassCard(modifier = modifier, cornerRadius = 16.dp) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$count",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = title,
                fontSize = 12.sp,
                color = Color(0x99FFFFFF)
            )
        }
    }
}

@Composable
fun ReminderCard(
    reminder: Reminder,
    onComplete: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit = {}
) {
    val priorityColor = when (reminder.priorityLevel) {
        2 -> PriorityUrgent
        1 -> PriorityUpcoming
        else -> PriorityNormal
    }

    val priorityLabel = when (reminder.priorityLevel) {
        2 -> "Urgent"
        1 -> "Upcoming"
        else -> "Normal"
    }

    val categoryColor = when (reminder.category) {
        "Work" -> CategoryWork
        "Health" -> CategoryHealth
        "Sport" -> CategorySport
        "Shopping" -> CategoryShopping
        else -> CategoryPersonal
    }

    val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())

    GlassCard(cornerRadius = 16.dp) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(60.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(priorityColor)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = reminder.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(categoryColor.copy(alpha = 0.2f))
                            .border(
                                1.dp,
                                categoryColor.copy(alpha = 0.5f),
                                RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = reminder.category,
                            fontSize = 10.sp,
                            color = categoryColor
                        )
                    }
                    // AI Suggested badge
                    if (reminder.isAiSuggested) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(PriorityAiChoice.copy(alpha = 0.2f))
                                .border(
                                    1.dp,
                                    PriorityAiChoice.copy(alpha = 0.5f),
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "AI",
                                fontSize = 10.sp,
                                color = PriorityAiChoice
                            )
                        }
                    }
                }

                if (reminder.description.isNotEmpty()) {
                    Text(
                        text = reminder.description,
                        fontSize = 13.sp,
                        color = Color(0x99FFFFFF),
                        maxLines = 1
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.AccessTime,
                        contentDescription = null,
                        tint = Color(0x99FFFFFF),
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = dateFormat.format(Date(reminder.dateTime)),
                        fontSize = 12.sp,
                        color = Color(0x99FFFFFF)
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(priorityColor.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = priorityLabel,
                            fontSize = 10.sp,
                            color = priorityColor
                        )
                    }
                }
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = "Edit",
                        tint = AuraCyan,
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(onClick = onComplete, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = "Complete",
                        tint = PriorityNormal,
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Delete",
                        tint = PriorityUrgent,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

fun isSameDay(timestamp1: Long, timestamp2: Long): Boolean {
    val cal1 = Calendar.getInstance().apply { timeInMillis = timestamp1 }
    val cal2 = Calendar.getInstance().apply { timeInMillis = timestamp2 }
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}