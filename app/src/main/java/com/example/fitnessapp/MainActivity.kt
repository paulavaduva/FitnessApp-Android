package com.example.fitnessapp

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.work.*
import java.util.concurrent.TimeUnit
import android.content.Intent
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

import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fitnessapp.data.MealDatabase
import com.example.fitnessapp.services.StepCounterService
import com.example.fitnessapp.viewmodel.MealViewModel
import com.example.fitnessapp.viewmodel.MealViewModelFactory
import com.example.fitnessapp.ui.theme.*
import com.example.fitnessapp.viewmodel.DailyActivityViewModel
import com.example.fitnessapp.viewmodel.StatisticsViewModel
import com.example.fitnessapp.viewmodel.StatisticsViewModelFactory
import kotlinx.coroutines.CoroutineScope

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createNotificationChannel()
        setupBackgroundWork()

        enableEdgeToEdge()
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACTIVITY_RECOGNITION)
            == PackageManager.PERMISSION_GRANTED) {
            startStepCounterService()
        }
        val appScope = lifecycleScope
        setContent {
            FitnessAppTheme {
                RequestActivityPermission(onPermissionGranted = { startStepCounterService() })
                RequestNotificationPermission()
                MainScreen(externalScope = appScope)
            }
        }
    }
    private fun startStepCounterService() {
        val serviceIntent = Intent(this, StepCounterService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }
    }

    private fun setupBackgroundWork() {
        val workManager = WorkManager.getInstance(this)

        val waterData = Data.Builder()
            .putString("TYPE", "WATER")
            .build()

        val waterRequest = PeriodicWorkRequestBuilder<FitnessReminderWorker>(3, TimeUnit.HOURS)
            .setInputData(waterData)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                    .build()
            )
            .build()

        workManager.enqueueUniquePeriodicWork(
            "WaterReminderWork",
            ExistingPeriodicWorkPolicy.KEEP,
            waterRequest
        )

        val stepData = Data.Builder().putString("TYPE", "STEPS").build()
        val stepRequest = PeriodicWorkRequestBuilder<FitnessReminderWorker>(4, TimeUnit.HOURS)
            .setInputData(stepData)
            .build()

        workManager.enqueueUniquePeriodicWork(
            "StepWork",
            ExistingPeriodicWorkPolicy.KEEP,
            stepRequest
        )
    }
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Fitness Reminders"
            val descriptionText = "Notificări pentru obiective"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("FITNESS_NOTIF_CH", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}

@Composable
fun RequestActivityPermission(onPermissionGranted: () -> Unit) {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            onPermissionGranted()
        }
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACTIVITY_RECOGNITION)
                != PackageManager.PERMISSION_GRANTED) {
                launcher.launch(android.Manifest.permission.ACTIVITY_RECOGNITION)
            }
        } else {
            onPermissionGranted()
        }
    }
}

@Composable
fun RequestNotificationPermission() {
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            println("Notification Permission Granted")
        } else {
            println("Notification Permission Denied")
        }
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permissionCheck = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            )

            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}

//@Preview
@Composable
fun MainScreen(externalScope: CoroutineScope) {
    val navController = rememberNavController()

    val context = LocalContext.current
    //val scope = rememberCoroutineScope()
    val database = remember {
        MealDatabase.getDatabase(context, externalScope)
    }

    val dailyVM: DailyActivityViewModel = viewModel(
        factory = DailyActivityViewModel.DailyActivityViewModelFactory(
            goalDao = database.goalDao(),
            stepDao = database.stepDao(),
            waterDao = database.waterDao(),
            context = context
        )
    )

    val statsVM: StatisticsViewModel = viewModel(
        factory = StatisticsViewModelFactory(
            stepDao = database.stepDao(),
            mealDao = database.mealDao(),
            goalDao = database.goalDao()
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
            composable(Screen.Statistics.route) { StatisticsScreen(statsVM = statsVM) }
            composable(Screen.Profile.route) { ProfileScreen() }
        }
    }
}