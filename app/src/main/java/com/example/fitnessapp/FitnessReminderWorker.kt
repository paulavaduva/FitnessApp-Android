package com.example.fitnessapp

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.fitnessapp.data.MealDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import java.util.Calendar

class FitnessReminderWorker(context: Context, workerParams: WorkerParameters) :
    CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val reminderType = inputData.getString("TYPE") ?: "WATER"

        val database = MealDatabase.getDatabase(applicationContext, CoroutineScope(Dispatchers.IO))

        when (reminderType) {
            "WATER" -> checkWaterAndNotify(database)
            "STEPS" -> checkStepsAndNotify(database)
        }

        return Result.success()
    }

    private suspend fun checkWaterAndNotify(database: MealDatabase) {
        val startOfDay = getStartOfDay()
        val currentWater = database.waterDao().getTodayRecord(startOfDay)?.glasses ?: 0

        val goals = database.goalDao().getGoals().firstOrNull()

        val userWaterGoal = goals?.waterGoal ?: 8

        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

        if (currentWater < userWaterGoal && currentHour in 8..22) {
            FitnessNotificationManager.sendWaterReminder(applicationContext)
        }
    }

    private suspend fun checkStepsAndNotify(database: MealDatabase) {
        val todayDate = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())

        val currentSteps = database.stepDao().getStepsForDay(todayDate).firstOrNull()?.count ?: 0

        val goals = database.goalDao().getGoals().firstOrNull()
        val userStepGoal = goals?.stepGoal ?: 1800

        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

        if (currentHour in 9..21 && currentSteps < userStepGoal) {
            FitnessNotificationManager.sendStepReminder(
                applicationContext,
                currentSteps,
                userStepGoal
            )
        }
    }

    private fun getStartOfDay(): Long {
        return Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
}