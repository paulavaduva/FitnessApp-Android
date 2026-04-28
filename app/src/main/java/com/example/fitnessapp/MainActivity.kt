package com.example.fitnessapp

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fitnessapp.data.MealDatabase
import com.example.fitnessapp.viewmodel.MealViewModel
import com.example.fitnessapp.viewmodel.MealViewModelFactory
import com.example.fitnessapp.ui.theme.*
import com.example.fitnessapp.viewmodel.DailyActivityViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FitnessAppTheme {
                RequestActivityPermission()
                MainScreen()
            }
        }
    }
}

@Composable
fun RequestActivityPermission() {
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            println("Permission Granted")
        } else {
            println("Permission Denied")
        }
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val permissionCheck = ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACTIVITY_RECOGNITION
            )

            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                launcher.launch(android.Manifest.permission.ACTIVITY_RECOGNITION)
            }
        }
    }
}

//@Preview
@Composable
fun MainScreen() {
    val navController = rememberNavController()

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val database = remember {
        MealDatabase.getDatabase(context, scope)
    }

    val dailyVM: DailyActivityViewModel = viewModel(
        factory = DailyActivityViewModel.DailyActivityViewModelFactory(
            goalDao = database.goalDao(),
            stepDao = database.stepDao(),
            context = context
        )
    )

    val mealViewModel: MealViewModel = viewModel(
        factory = MealViewModelFactory(
            database.mealDao(),
            database.foodDao()
        )
    )

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
            composable(Screen.Overview.route) { OverviewScreen(dailyVM = dailyVM, viewModel = mealViewModel) }
            composable(Screen.Diary.route) { DiaryScreen(viewModel = mealViewModel) }
            composable(Screen.Statistics.route) { StatisticsScreen() }
            composable(Screen.Profile.route) { ProfileScreen() }
        }
    }
}