package com.anthonius.aura.reminder.ui.chatbot

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.anthonius.aura.reminder.ui.home.callGeminiApi
import com.anthonius.aura.reminder.ui.theme.*
import com.anthonius.aura.reminder.viewmodel.ReminderViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

@Composable
fun ChatbotScreen(
    viewModel: ReminderViewModel = viewModel()
) {
    val allReminders by viewModel.allReminders.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val messages = viewModel.chatMessages
    var inputText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val quickActions = listOf(
        "📅 Jadwal hari ini",
        "📆 Jadwal minggu ini",
        "🔴 Jadwal urgent",
        "➕ Berapa total jadwal?",
        "📊 Ringkasan jadwal",
        "🗑️ Cara hapus jadwal"
    )

    fun sendMessage(text: String) {
        if (text.isBlank() || isLoading) return

        viewModel.addChatMessage(ChatMessage(text = text, isUser = true))
        inputText = ""
        isLoading = true

        coroutineScope.launch {
            try {
                val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
                val today = Calendar.getInstance()

                val todayReminders = allReminders.filter { reminder ->
                    val cal = Calendar.getInstance().apply { timeInMillis = reminder.dateTime }
                    cal.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                            cal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)
                }

                val weekReminders = allReminders.filter { reminder ->
                    val diff = reminder.dateTime - today.timeInMillis
                    diff in 0..(7 * 24 * 60 * 60 * 1000)
                }

                val prompt = """
                    Kamu adalah Aura AI, asisten reminder yang helpful dan friendly.
                    
                    Data reminder user saat ini:
                    - Total reminder: ${allReminders.size}
                    - Reminder hari ini (${todayReminders.size} jadwal):
                    ${if (todayReminders.isEmpty()) "  Tidak ada jadwal hari ini" else todayReminders.joinToString("\n") { "  • ${it.title} - jam ${dateFormat.format(Date(it.dateTime))} [${it.category}] [${if (it.isCompleted) "Selesai" else "Aktif"}]" }}
                    
                    - Reminder minggu ini (${weekReminders.size} jadwal):
                    ${if (weekReminders.isEmpty()) "  Tidak ada jadwal minggu ini" else weekReminders.joinToString("\n") { "  • ${it.title} - ${dateFormat.format(Date(it.dateTime))} [${it.category}]" }}
                    
                    - Semua reminder aktif:
                    ${if (allReminders.filter { !it.isCompleted && !it.isCancelled }.isEmpty()) "  Tidak ada reminder aktif" else allReminders.filter { !it.isCompleted && !it.isCancelled }.joinToString("\n") { "  • ${it.title} - ${dateFormat.format(Date(it.dateTime))} [${it.category}] [Priority: ${when(it.priorityLevel) { 2 -> "Urgent" 1 -> "Upcoming" else -> "Normal" }}]" }}
                    
                    Pertanyaan user: $text
                    
                    Instruksi:
                    - Jawab dalam Bahasa Indonesia dengan ramah dan jelas
                    - Selalu sebutkan NAMA jadwal beserta waktunya ketika menampilkan daftar jadwal
                    - Format jadwal: nama jadwal - tanggal/jam - kategori
                    - Jika tidak ada jadwal, beritahu dengan jelas
                    - Jika user minta hapus jadwal, beritahu bahwa penghapusan harus dilakukan manual di Home screen dengan swipe atau tekan icon hapus
                    - Maksimal 10 kalimat
                """.trimIndent()

                val response = callGeminiApi(prompt)
                viewModel.addChatMessage(ChatMessage(text = response, isUser = false))
            } catch (e: Exception) {
                viewModel.addChatMessage(ChatMessage(
                    text = "Maaf, terjadi kesalahan. Coba lagi ya! 🙏",
                    isUser = false
                ))
            }
            isLoading = false
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Header
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Spacer(modifier = Modifier.height(48.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(AuraPurple, AuraBlue)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.AutoAwesome,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Aura AI",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Smart reminder assistant",
                        fontSize = 12.sp,
                        color = Color(0x99FFFFFF)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Messages
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(messages) { message ->
                ChatBubble(message = message)
            }
            if (isLoading) {
                item {
                    TypingIndicator()
                }
            }
        }

        // Quick Actions
        LazyRow(
            modifier = Modifier.padding(vertical = 8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(quickActions) { action ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0x22FFFFFF))
                        .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(20.dp))
                        .clickable { sendMessage(action) }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = action,
                        fontSize = 12.sp,
                        color = Color(0xCCFFFFFF)
                    )
                }
            }
        }

        // Input Area
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                modifier = Modifier.weight(1f),
                placeholder = {
                    Text("Tanya sesuatu...", color = Color(0x66FFFFFF))
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = AuraPurple,
                    unfocusedBorderColor = Color(0x33FFFFFF),
                    cursorColor = AuraPurple,
                    focusedContainerColor = Color(0x22FFFFFF),
                    unfocusedContainerColor = Color(0x11FFFFFF)
                ),
                shape = RoundedCornerShape(20.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { sendMessage(inputText) }),
                maxLines = 3
            )

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        if (!isLoading && inputText.isNotBlank())
                            Brush.linearGradient(colors = listOf(AuraPurple, AuraBlue))
                        else
                            Brush.linearGradient(
                                colors = listOf(Color(0x44FFFFFF), Color(0x44FFFFFF))
                            )
                    )
                    .clickable(enabled = !isLoading && inputText.isNotBlank()) {
                        sendMessage(inputText)
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.Send,
                    contentDescription = "Send",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage) {
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!message.isUser) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(colors = listOf(AuraPurple, AuraBlue))
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.AutoAwesome,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(
            horizontalAlignment = if (message.isUser) Alignment.End else Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .widthIn(max = 280.dp)
                    .clip(
                        RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (message.isUser) 16.dp else 4.dp,
                            bottomEnd = if (message.isUser) 4.dp else 16.dp
                        )
                    )
                    .background(
                        if (message.isUser)
                            Brush.linearGradient(colors = listOf(AuraPurple, AuraBlue))
                        else
                            Brush.linearGradient(
                                colors = listOf(Color(0x33FFFFFF), Color(0x22FFFFFF))
                            )
                    )
                    .border(
                        width = if (message.isUser) 0.dp else 1.dp,
                        color = if (message.isUser) Color.Transparent else Color(0x33FFFFFF),
                        shape = RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (message.isUser) 16.dp else 4.dp,
                            bottomEnd = if (message.isUser) 4.dp else 16.dp
                        )
                    )
                    .padding(12.dp)
            ) {
                Text(
                    text = message.text,
                    color = Color.White,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = timeFormat.format(Date(message.timestamp)),
                fontSize = 10.sp,
                color = Color(0x66FFFFFF)
            )
        }

        if (message.isUser) {
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color(0x33FFFFFF))
                    .border(1.dp, Color(0x33FFFFFF), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.Person,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun TypingIndicator() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(colors = listOf(AuraPurple, AuraBlue))
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Filled.AutoAwesome,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0x33FFFFFF))
                .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(16.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                repeat(3) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(Color(0x99FFFFFF))
                    )
                }
            }
        }
    }
}