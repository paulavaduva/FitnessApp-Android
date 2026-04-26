package com.example.fitnessapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.fitnessapp.ui.theme.FitnessAppTheme

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.compose.material3.*
import androidx.compose.runtime.getValue
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.fitnessapp.ui.theme.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FitnessAppTheme {
                MainScreen()
            }
        }
    }
}

@Preview
@Composable
fun MainScreen() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = BackgroundDark,
                contentColor = EmeraldGreen
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                // Buton Overview
                NavigationBarItem(
                    selected = currentRoute == Screen.Overview.route,
                    onClick = { navController.navigate(Screen.Overview.route) },
                    label = { Text(Screen.Overview.label) },
                    icon = { Icon(Icons.Default.Home, contentDescription = null) }
                )
                // Buton Diary
                NavigationBarItem(
                    selected = currentRoute == Screen.Diary.route,
                    onClick = { navController.navigate(Screen.Diary.route) },
                    label = { Text(Screen.Diary.label) },
                    icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = null) }
                )
                // Buton Statistics
                NavigationBarItem(
                    selected = currentRoute == Screen.Statistics.route,
                    onClick = { navController.navigate(Screen.Statistics.route) },
                    label = { Text(Screen.Statistics.label) },
                    icon = { Icon(Icons.Default.Info, contentDescription = null) }
                )
                // Buton Profile
                NavigationBarItem(
                    selected = currentRoute == Screen.Profile.route,
                    onClick = { navController.navigate(Screen.Profile.route) },
                    label = { Text(Screen.Profile.label) },
                    icon = { Icon(Icons.Default.Person, contentDescription = null) }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Overview.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Overview.route) { OverviewScreen() }
            composable(Screen.Diary.route) { DiaryScreen() }
            composable(Screen.Statistics.route) { StatisticsScreen() }
            composable(Screen.Profile.route) { ProfileScreen() }
        }
    }
}