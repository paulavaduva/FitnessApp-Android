package com.example.fitnessapp

sealed class Screen(val route: String, val label: String) {
    object Overview : Screen("overview", "Overview")
    object Diary : Screen("diary", "Diary")
    object Statistics : Screen("statistics", "Statistics")
    object Profile : Screen("profile", "Profile")
}