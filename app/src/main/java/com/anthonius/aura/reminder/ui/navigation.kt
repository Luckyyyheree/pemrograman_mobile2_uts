package com.anthonius.aura.reminder.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.anthonius.aura.reminder.ui.calendar.CalendarScreen
import com.anthonius.aura.reminder.ui.chart.ChartScreen
import com.anthonius.aura.reminder.ui.chatbot.ChatbotScreen
import com.anthonius.aura.reminder.ui.components.BottomNavItem
import com.anthonius.aura.reminder.ui.home.HomeScreen

@Composable
fun AuraNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = BottomNavItem.Home.route,
        modifier = modifier
    ) {
        composable(BottomNavItem.Home.route) {
            HomeScreen()
        }
        composable(BottomNavItem.Calendar.route) {
            CalendarScreen()
        }
        composable(BottomNavItem.Chart.route) {
            ChartScreen()
        }
        composable(BottomNavItem.Chatbot.route) {
            ChatbotScreen()
        }
    }
}