package com.anthonius.aura.reminder.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anthonius.aura.reminder.ui.theme.*

sealed class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val label: String
) {
    object Home : BottomNavItem("home", Icons.Filled.Home, "Home")
    object Calendar : BottomNavItem("calendar", Icons.Filled.CalendarMonth, "Calendar")
    object Chart : BottomNavItem("chart", Icons.Filled.PieChart, "Stats")
    object Chatbot : BottomNavItem("chatbot", Icons.Filled.SmartToy, "AI Chat")
}

@Composable
fun AuraBottomNavigation(
    currentRoute: String,
    onItemClick: (BottomNavItem) -> Unit
) {
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Calendar,
        BottomNavItem.Chart,
        BottomNavItem.Chatbot
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0x44FFFFFF),
                            Color(0x22FFFFFF)
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0x66FFFFFF),
                            Color(0x22FFFFFF)
                        )
                    ),
                    shape = RoundedCornerShape(28.dp)
                )
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val isSelected = currentRoute == item.route
                BottomNavItemView(
                    item = item,
                    isSelected = isSelected,
                    onClick = { onItemClick(item) }
                )
            }
        }
    }
}

@Composable
fun BottomNavItemView(
    item: BottomNavItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(
                if (isSelected) Brush.linearGradient(
                    colors = listOf(AuraPurple, AuraBlue)
                ) else Brush.linearGradient(
                    colors = listOf(Color.Transparent, Color.Transparent)
                )
            )
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.label,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = item.label,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        } else {
            Icon(
                imageVector = item.icon,
                contentDescription = item.label,
                tint = Color(0x99FFFFFF),
                modifier = Modifier.size(22.dp)
            )
        }
    }
}