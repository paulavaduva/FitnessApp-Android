package com.example.fitnessapp.services

import android.app.*
import android.content.*
import android.content.pm.ServiceInfo
import android.hardware.*
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.fitnessapp.data.MealDatabase
import com.example.fitnessapp.data.StepEntity
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*

class StepCounterService : Service(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var lastSensorValue = -1f
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onCreate() {
        super.onCreate()
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        val prefs = getSharedPreferences("step_prefs", Context.MODE_PRIVATE)
        lastSensorValue = prefs.getFloat("last_sensor_value", -1f)

        sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_UI)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(1, createNotification(), ServiceInfo.FOREGROUND_SERVICE_TYPE_HEALTH)
        } else {
            startForeground(1, createNotification())
        }
    }

    private fun createNotification(): Notification {
        val channelId = "step_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Fitness Tracking", NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("FitnessApp is active")
            .setContentText("We are counting your steps...")
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setOngoing(true)
            .build()
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_STEP_COUNTER) {
            val totalStepsSinceBoot = event.values[0]

            if (lastSensorValue != -1f) {
                val delta = (totalStepsSinceBoot - lastSensorValue).toInt()
                if (delta > 0) {
                    saveStepsToDb(delta)
                }
            }

            lastSensorValue = totalStepsSinceBoot
            getSharedPreferences("step_prefs", Context.MODE_PRIVATE)
                .edit()
                .putFloat("last_sensor_value", totalStepsSinceBoot)
                .apply()
        }
    }

    private fun saveStepsToDb(delta: Int) {
        serviceScope.launch {
            val database = MealDatabase.getDatabase(applicationContext, this)
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

            val currentRecord = database.stepDao().getStepsForDayOnce(today)
            val newTotal = (currentRecord?.count ?: 0) + delta

            database.stepDao().upsertSteps(StepEntity(today, newTotal))
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
        serviceScope.cancel()
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}