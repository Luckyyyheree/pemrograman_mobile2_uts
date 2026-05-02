package com.anthonius.aura.reminder.ui.components

import android.content.Context
import android.media.RingtoneManager
import android.net.Uri
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import android.media.Ringtone
import com.anthonius.aura.reminder.ui.theme.*

data class RingtoneItem(
    val title: String,
    val uri: Uri
)

fun getRingtones(context: Context): List<RingtoneItem> {
    val ringtones = mutableListOf<RingtoneItem>()

    // Default/Silent option
    ringtones.add(RingtoneItem("Default Alarm", RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)))
    ringtones.add(RingtoneItem("Default Notification", RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)))

    try {
        val manager = RingtoneManager(context).apply {
            setType(RingtoneManager.TYPE_ALL)
        }
        val cursor = manager.cursor
        while (cursor.moveToNext()) {
            val title = cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX)
            val uri = manager.getRingtoneUri(cursor.position)
            ringtones.add(RingtoneItem(title, uri))
        }
        cursor.close()
    } catch (e: Exception) {
        e.printStackTrace()
    }

    return ringtones
}

@Composable
fun RingtonePickerDialog(
    currentRingtoneUri: String,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    val context = LocalContext.current
    val ringtones = remember { getRingtones(context) }
    var selectedUri by remember { mutableStateOf(currentRingtoneUri) }
    var currentlyPlaying by remember { mutableStateOf<Ringtone?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            currentlyPlaying?.stop()
        }
    }

    Dialog(
        onDismissRequest = {
            currentlyPlaying?.stop()
            onDismiss()
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f)
                .padding(16.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(0xEE0D0B1E), Color(0xEE0A0E1A))
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
            Column(modifier = Modifier.padding(24.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.MusicNote,
                            contentDescription = null,
                            tint = AuraCyan,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Pilih Nada Dering",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    IconButton(onClick = {
                        currentlyPlaying?.stop()
                        onDismiss()
                    }) {
                        Icon(
                            Icons.Filled.Close,
                            contentDescription = "Close",
                            tint = Color(0x99FFFFFF)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Ringtone List
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(ringtones) { ringtone ->
                        val isSelected = selectedUri == ringtone.uri.toString()

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isSelected)
                                        Brush.linearGradient(
                                            colors = listOf(
                                                AuraPurple.copy(alpha = 0.3f),
                                                AuraBlue.copy(alpha = 0.3f)
                                            )
                                        )
                                    else
                                        Brush.linearGradient(
                                            colors = listOf(
                                                Color(0x22FFFFFF),
                                                Color(0x22FFFFFF)
                                            )
                                        )
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) AuraPurple else Color(0x22FFFFFF),
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable {
                                    selectedUri = ringtone.uri.toString()
                                    currentlyPlaying?.stop()
                                    try {
                                        val r = RingtoneManager.getRingtone(
                                            context,
                                            ringtone.uri
                                        )
                                        r.play()
                                        currentlyPlaying = r
                                        // Auto stop preview after 3 seconds
                                        android.os.Handler(
                                            android.os.Looper.getMainLooper()
                                        ).postDelayed({
                                            r.stop()
                                        }, 3000)
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isSelected)
                                                Brush.linearGradient(
                                                    colors = listOf(AuraPurple, AuraBlue)
                                                )
                                            else
                                                Brush.linearGradient(
                                                    colors = listOf(
                                                        Color(0x33FFFFFF),
                                                        Color(0x33FFFFFF)
                                                    )
                                                )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        if (isSelected) Icons.Filled.MusicNote
                                        else Icons.Filled.MusicNote,
                                        contentDescription = null,
                                        tint = if (isSelected) Color.White
                                        else Color(0x99FFFFFF),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = ringtone.title,
                                    fontSize = 14.sp,
                                    color = if (isSelected) Color.White else Color(0xCCFFFFFF),
                                    fontWeight = if (isSelected) FontWeight.SemiBold
                                    else FontWeight.Normal
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    Icons.Filled.CheckCircle,
                                    contentDescription = null,
                                    tint = AuraPurple,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Confirm Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(AuraPurple, AuraBlue)
                            )
                        )
                        .clickable {
                            currentlyPlaying?.stop()
                            onSelect(selectedUri)
                            onDismiss()
                        }
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Pilih Nada Ini",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}