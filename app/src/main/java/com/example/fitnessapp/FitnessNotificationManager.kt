package com.example.fitnessapp

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat

object FitnessNotificationManager {

    private const val CHANNEL_ID = "FITNESS_NOTIF_CH"

    private fun sendBasicNotification(context: Context, id: Int, title: String, message: String, icon: Int) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(icon)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(id, builder.build())
    }

    fun sendStepGoalReached(context: Context, steps: Int) {
        sendBasicNotification(
            context,
            1001,
            "Goal Reached! 🏃‍♂️",
            "Great job! You've reached your goal of $steps steps. Keep it up!",
            R.drawable.baseline_fitness_center_24
        )
    }

    fun sendStepReminder(context: Context, currentSteps: Int, goal: Int) {
        val remaining = goal - currentSteps
        sendBasicNotification(
            context,
            1002,
            "Keep Moving! 🏃‍♂️",
            "Only $remaining steps left to reach your daily goal. You can do it!",
            R.drawable.baseline_fitness_center_24
        )
    }

    fun sendWaterReminder(context: Context) {
        sendBasicNotification(
            context,
            1003,
            "Stay Hydrated! 💧",
            "You haven't logged water in a while. Take a break and hydrate!",
            R.drawable.baseline_fitness_center_24
        )
    }

    fun sendWaterGoalReached(context: Context, goal: Int) {
        sendBasicNotification(
            context,
            1004,
            "Goal Reached! 💧",
            "Congratulations! You've reached your daily goal of $goal glasses of water.",
            R.drawable.baseline_fitness_center_24
        )
    }
}