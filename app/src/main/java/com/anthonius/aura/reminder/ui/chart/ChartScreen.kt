package com.anthonius.aura.reminder.ui.chart

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.anthonius.aura.reminder.ui.components.GlassCard
import com.anthonius.aura.reminder.ui.theme.*
import com.anthonius.aura.reminder.viewmodel.ReminderViewModel

@Composable
fun ChartScreen(
    viewModel: ReminderViewModel = viewModel()
) {
    val allReminders by viewModel.allReminders.collectAsState()
    var showClearDialog by remember { mutableStateOf(false) }

    val total = allReminders.size
    val completed = allReminders.count { it.isCompleted }
    val cancelled = allReminders.count { it.isCancelled }
    val active = allReminders.count { !it.isCompleted && !it.isCancelled }
    val urgent = allReminders.count { it.priorityLevel == 2 && !it.isCompleted }
    val upcoming = allReminders.count { it.priorityLevel == 1 && !it.isCompleted }
    val normal = allReminders.count { it.priorityLevel == 0 && !it.isCompleted }
    val aiSuggested = allReminders.count { it.isAiSuggested }

    val categoryData = listOf(
        "Personal" to allReminders.count { it.category == "Personal" },
        "Work" to allReminders.count { it.category == "Work" },
        "Health" to allReminders.count { it.category == "Health" },
        "Sport" to allReminders.count { it.category == "Sport" },
        "Shopping" to allReminders.count { it.category == "Shopping" },
        "Other" to allReminders.count { it.category == "Other" }
    ).filter { it.second > 0 }

    val categoryColors = listOf(
        CategoryPersonal, CategoryWork, CategoryHealth,
        CategorySport, CategoryShopping, CategoryOther
    )

    // Clear Completed Dialog
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            containerColor = Color(0xFF1A1040),
            title = {
                Text(
                    text = "Hapus Reminder Selesai?",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Semua reminder yang sudah selesai ($completed reminder) akan dihapus permanen.",
                    color = Color(0x99FFFFFF)
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearCompletedReminders()
                    showClearDialog = false
                }) {
                    Text("Hapus", color = PriorityUrgent, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Batal", color = Color(0x99FFFFFF))
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = "Statistics",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = "Your reminder insights",
            fontSize = 14.sp,
            color = Color(0x99FFFFFF)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Overall Progress Card
        GlassCard(cornerRadius = 20.dp) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Overall Progress",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(140.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        DonutChart(
                            completed = completed,
                            active = active,
                            cancelled = cancelled,
                            total = total
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$total",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Total",
                                fontSize = 12.sp,
                                color = Color(0x99FFFFFF)
                            )
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        LegendItem(color = PriorityNormal, label = "Active", count = active)
                        LegendItem(color = AuraBlue, label = "Done", count = completed)
                        LegendItem(color = PriorityUrgent, label = "Cancelled", count = cancelled)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Priority Breakdown
        GlassCard(cornerRadius = 20.dp) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Priority Breakdown",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(16.dp))

                PriorityBar(
                    label = "🔴 Urgent",
                    count = urgent,
                    total = active.coerceAtLeast(1),
                    color = PriorityUrgent
                )
                Spacer(modifier = Modifier.height(10.dp))
                PriorityBar(
                    label = "🟡 Upcoming",
                    count = upcoming,
                    total = active.coerceAtLeast(1),
                    color = PriorityUpcoming
                )
                Spacer(modifier = Modifier.height(10.dp))
                PriorityBar(
                    label = "🟢 Normal",
                    count = normal,
                    total = active.coerceAtLeast(1),
                    color = PriorityNormal
                )
                Spacer(modifier = Modifier.height(10.dp))
                PriorityBar(
                    label = "🔵 AI Suggested",
                    count = aiSuggested,
                    total = total.coerceAtLeast(1),
                    color = PriorityAiChoice
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Category Breakdown
        if (categoryData.isNotEmpty()) {
            GlassCard(cornerRadius = 20.dp) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "By Category",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    categoryData.forEachIndexed { index, (category, count) ->
                        val color = categoryColors.getOrElse(index) { CategoryOther }
                        PriorityBar(
                            label = category,
                            count = count,
                            total = total.coerceAtLeast(1),
                            color = color
                        )
                        if (index < categoryData.size - 1) {
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Quick Stats
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickStatCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Filled.AutoAwesome,
                label = "AI Assisted",
                value = "$aiSuggested",
                color = AuraCyan
            )
            QuickStatCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Filled.CheckCircle,
                label = "Completed",
                value = if (total > 0) "${(completed * 100 / total)}%" else "0%",
                color = PriorityNormal
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Clear Completed Button
        if (completed > 0) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0x22FFFFFF))
                    .border(1.dp, PriorityUrgent.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    .clickable { showClearDialog = true }
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.DeleteSweep,
                        contentDescription = null,
                        tint = PriorityUrgent,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Clear $completed Completed Reminder",
                        color = PriorityUrgent,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
fun DonutChart(
    completed: Int,
    active: Int,
    cancelled: Int,
    total: Int
) {
    Canvas(modifier = Modifier.size(140.dp)) {
        val strokeWidth = 20f
        val radius = size.minDimension / 2 - strokeWidth
        val center = Offset(size.width / 2, size.height / 2)

        if (total == 0) {
            drawArc(
                color = Color(0x33FFFFFF),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(strokeWidth, cap = StrokeCap.Round)
            )
            return@Canvas
        }

        val activeAngle = (active.toFloat() / total) * 360f
        val completedAngle = (completed.toFloat() / total) * 360f
        val cancelledAngle = (cancelled.toFloat() / total) * 360f
        var startAngle = -90f

        if (activeAngle > 0) {
            drawArc(
                color = PriorityNormal,
                startAngle = startAngle,
                sweepAngle = activeAngle,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(strokeWidth, cap = StrokeCap.Round)
            )
            startAngle += activeAngle
        }

        if (completedAngle > 0) {
            drawArc(
                color = AuraBlue,
                startAngle = startAngle,
                sweepAngle = completedAngle,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(strokeWidth, cap = StrokeCap.Round)
            )
            startAngle += completedAngle
        }

        if (cancelledAngle > 0) {
            drawArc(
                color = PriorityUrgent,
                startAngle = startAngle,
                sweepAngle = cancelledAngle,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(strokeWidth, cap = StrokeCap.Round)
            )
        }
    }
}

@Composable
fun LegendItem(color: Color, label: String, count: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "$label ($count)",
            fontSize = 13.sp,
            color = Color(0xCCFFFFFF)
        )
    }
}

@Composable
fun PriorityBar(
    label: String,
    count: Int,
    total: Int,
    color: Color
) {
    val fraction = if (total > 0) count.toFloat() / total else 0f

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, fontSize = 13.sp, color = Color(0xCCFFFFFF))
            Text(
                text = "$count",
                fontSize = 13.sp,
                color = color,
                fontWeight = FontWeight.SemiBold
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0x22FFFFFF))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(4.dp))
                    .background(color)
            )
        }
    }
}

@Composable
fun QuickStatCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    value: String,
    color: Color
) {
    GlassCard(modifier = modifier, cornerRadius = 16.dp) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = label,
                fontSize = 12.sp,
                color = Color(0x99FFFFFF)
            )
        }
    }
}