package com.example.core.navigation

sealed class Screen(val route: String) {
    // Auth Graph Destinations (L0)
    object Splash : Screen("splash")
    object Onboarding : Screen("onboarding")
    object Login : Screen("login")
    object Register : Screen("register")

    // Main Graph Destinations (L1 Tabs)
    object Home : Screen("home")
    object Sleep : Screen("sleep")
    object Relaxation : Screen("relaxation")
    object Ikigai : Screen("ikigai")
    object Profile : Screen("profile")

    // Secondary Destinations (L2 Spokes)
    object Journal : Screen("journal")
    object AiJournal : Screen("ai_journal")
    object MoodTracking : Screen("mood_tracking")
    object SleepTracking : Screen("sleep_tracking")
    object Lifestyle : Screen("lifestyle")
    object Notifications : Screen("notifications")
    object Reminder : Screen("reminder")
    object Statistics : Screen("statistics")
    object Settings : Screen("settings")
    object Achievements : Screen("achievements")
}
