package com.anthonius.aura.reminder.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import kotlinx.coroutines.launch
import org.json.JSONObject
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.abs

@Composable
fun ConflictDialog(
    conflictingReminders: List<Reminder>,
    pendingReminder: Reminder,
    onDismiss: () -> Unit,
    onConfirm: (Reminder) -> Unit
) {
    var aiSuggestion by remember { mutableStateOf("") }
    var isLoadingAi by remember { mutableStateOf(true) }
    var suggestedDateTime by remember { mutableStateOf<Long?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())

    LaunchedEffect(Unit) {
        isLoadingAi = true
        try {
            val conflictInfo = conflictingReminders.joinToString("\n") {
                "- ${it.title} pada ${dateFormat.format(Date(it.dateTime))}"
            }
            val prompt = "User ingin membuat reminder berjudul '${pendingReminder.title}' pada ${dateFormat.format(Date(pendingReminder.dateTime))}. Tapi ada konflik dengan jadwal berikut:\\n$conflictInfo\\n\\nBerikan saran singkat dalam Bahasa Indonesia: 1. Jelaskan konflik yang terjadi 2. Sarankan waktu alternatif yang kosong dalam format HH:mm 3. Alasan singkat. Jawab maksimal 3 kalimat."

            val result = callGeminiApi(prompt)
            aiSuggestion = result

            // Cari semua pola waktu HH:mm di response AI
            val timePattern = Regex("""(\d{1,2}):(\d{2})""")
            val timeMatches = timePattern.findAll(result).toList()

// Ambil waktu yang BERBEDA dari waktu bentrok
            val conflictTimes = conflictingReminders.map { reminder ->
                val cal = Calendar.getInstance().apply { timeInMillis = reminder.dateTime }
                cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
            }

            for (match in timeMatches) {
                val hour = match.groupValues[1].toIntOrNull() ?: continue
                val minute = match.groupValues[2].toIntOrNull() ?: continue
                if (hour > 23 || minute > 59) continue

                val timeInMinutes = hour * 60 + minute
                val isDifferentFromConflicts = conflictTimes.all {
                    kotlin.math.abs(timeInMinutes - it) >= 30
                }

                if (isDifferentFromConflicts) {
                    val cal = Calendar.getInstance().apply {
                        timeInMillis = pendingReminder.dateTime
                        set(Calendar.HOUR_OF_DAY, hour)
                        set(Calendar.MINUTE, minute)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    suggestedDateTime = cal.timeInMillis
                    break
                }
            }
        } catch (e: Exception) {
            aiSuggestion = "Tidak dapat memuat saran AI: ${e.message}"
        }
        isLoadingAi = false
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
                        colors = listOf(Color(0xEE1A0B2E), Color(0xEE0A0E1A))
                    )
                )
                .border(
                    1.dp,
                    Brush.linearGradient(
                        colors = listOf(PriorityUrgent.copy(alpha = 0.5f), Color(0x22FFFFFF))
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Warning,
                        contentDescription = null,
                        tint = PriorityUrgent,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Jadwal Bentrok!",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = PriorityUrgent
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Pending reminder info
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0x22FFFFFF))
                        .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Text(
                            text = "Reminder baru:",
                            fontSize = 12.sp,
                            color = Color(0x99FFFFFF)
                        )
                        Text(
                            text = pendingReminder.title,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                        Text(
                            text = dateFormat.format(Date(pendingReminder.dateTime)),
                            fontSize = 13.sp,
                            color = AuraCyan
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Conflicting reminders
                Text(
                    text = "Bentrok dengan:",
                    fontSize = 14.sp,
                    color = Color(0x99FFFFFF),
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))

                conflictingReminders.forEach { conflict ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(PriorityUrgent.copy(alpha = 0.1f))
                            .border(
                                1.dp,
                                PriorityUrgent.copy(alpha = 0.3f),
                                RoundedCornerShape(10.dp)
                            )
                            .padding(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.Event,
                                contentDescription = null,
                                tint = PriorityUrgent,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = conflict.title,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                                Text(
                                    text = dateFormat.format(Date(conflict.dateTime)),
                                    fontSize = 12.sp,
                                    color = PriorityUrgent
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // AI Suggestion Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(AuraPurple.copy(alpha = 0.1f))
                        .border(
                            1.dp,
                            AuraPurple.copy(alpha = 0.3f),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(12.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.AutoAwesome,
                                contentDescription = null,
                                tint = AuraPurple,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Saran AI",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AuraPurple
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        if (isLoadingAi) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = AuraPurple,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "AI sedang menganalisis...",
                                    fontSize = 13.sp,
                                    color = Color(0x99FFFFFF)
                                )
                            }
                        } else {
                            Text(
                                text = aiSuggestion,
                                fontSize = 13.sp,
                                color = Color(0xCCFFFFFF),
                                lineHeight = 20.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Batalkan Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0x22FFFFFF))
                        .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(12.dp))
                        .padding(vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(
                            text = "Batalkan Reminder Baru",
                            color = Color(0x99FFFFFF),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Pakai Saran AI Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(AuraPurple, AuraBlue)
                            )
                        )
                        .padding(vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    TextButton(
                        onClick = {
                            val finalReminder = if (suggestedDateTime != null) {
                                pendingReminder.copy(
                                    dateTime = suggestedDateTime!!,
                                    isAiSuggested = true
                                )
                            } else {
                                pendingReminder.copy(isAiSuggested = true)
                            }
                            onConfirm(finalReminder)
                        }
                    ) {
                        Text(
                            text = "Pakai Saran AI",
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Tetap di Waktu Ini Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(PriorityUrgent.copy(alpha = 0.15f))
                        .border(
                            1.dp,
                            PriorityUrgent.copy(alpha = 0.3f),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    TextButton(onClick = { onConfirm(pendingReminder) }) {
                        Text(
                            text = "Tetap di Waktu Ini",
                            color = PriorityUrgent,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

suspend fun callGeminiApi(prompt: String): String {
    return withContext(Dispatchers.IO) {
        try {
            val apiKey = ""
            val url = URL("https://api.groq.com/openai/v1/chat/completions")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("Authorization", "Bearer $apiKey")
            connection.connectTimeout = 15000
            connection.readTimeout = 15000
            connection.doOutput = true

            val requestBody = """
                {
                    "model": "llama-3.3-70b-versatile",
                    "messages": [
                        {
                            "role": "user",
                            "content": "${prompt.replace("\"", "'").replace("\n", "\\n")}"
                        }
                    ],
                    "max_tokens": 500,
                    "temperature": 0.7
                }
            """.trimIndent()

            connection.outputStream.use { os ->
                os.write(requestBody.toByteArray(Charsets.UTF_8))
            }

            val responseCode = connection.responseCode
            if (responseCode != HttpURLConnection.HTTP_OK) {
                val errorStream = connection.errorStream?.bufferedReader()?.readText()
                return@withContext "Error $responseCode: $errorStream"
            }

            val response = connection.inputStream.bufferedReader().readText()
            val jsonResponse = JSONObject(response)
            jsonResponse
                .getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content")
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }
}