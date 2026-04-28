package com.example.fitnessapp.viewmodel

import android.app.Application
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnessapp.data.MealDatabase
import com.example.fitnessapp.data.WaterEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.util.Calendar
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.fitnessapp.data.GoalDao
import com.example.fitnessapp.data.GoalEntity
import com.example.fitnessapp.data.StepDao
import com.example.fitnessapp.data.StepEntity
import com.example.fitnessapp.data.WaterDao
import kotlinx.coroutines.launch

class DailyActivityViewModel(
    private val goalDao: GoalDao,
    private val stepDao: StepDao,
    private val waterDao: WaterDao,
    context: Context
) : ViewModel(), SensorEventListener {
    var currentSteps by mutableStateOf(0)
    var stepGoal by mutableStateOf(1800)
    var waterGlasses by mutableStateOf(6)
    var waterGoal by mutableStateOf(8)
    //var userName by mutableStateOf("Paula")
    var kcalGoal by mutableStateOf(2000.0)
    var proteinGoal by mutableStateOf(150.0)
    var carbGoal by mutableStateOf(200.0)
    var fatGoal by mutableStateOf(70.0)

    private var sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
    private var lastSensorValue = -1f

    init {
        loadStepsFromDb()
        loadGoalsFromDb()

        if (stepCounterSensor == null) {
            android.util.Log.e("STEP_DEBUG", "EROARE")
        } else {
            val isRegistered = sensorManager.registerListener(this, stepCounterSensor, SensorManager.SENSOR_DELAY_UI)
            android.util.Log.d("STEP_DEBUG", "Senzorul gasit: $isRegistered")
        }
    }

    private fun loadGoalsFromDb() {
        viewModelScope.launch {
            goalDao.getGoals().collect { goals ->
                goals?.let {
                    stepGoal = it.stepGoal
                    waterGoal = it.waterGoal
                    kcalGoal = it.kcalGoal
                    proteinGoal = it.proteinGoal
                    carbGoal = it.carbGoal
                    fatGoal = it.fatGoal
                }
            }
        }
    }

    fun updateActivityGoals(steps: String,
                            water: String,
                            kcal: String,
                            carbs: String,
                            protein: String,
                            fat: String
    ) {
        viewModelScope.launch {
            val newGoals = GoalEntity(
                stepGoal = steps.toIntOrNull() ?: stepGoal,
                waterGoal = water.toIntOrNull() ?: waterGoal,
                kcalGoal = kcal.toDoubleOrNull() ?: kcalGoal,
                carbGoal = carbs.toDoubleOrNull() ?: carbGoal,
                proteinGoal = protein.toDoubleOrNull() ?: proteinGoal,
                fatGoal = fat.toDoubleOrNull() ?: fatGoal
            )
            goalDao.upsertGoals(newGoals)
        }
    }

    private fun loadStepsFromDb() {
        viewModelScope.launch {
            val today = getTodayDate()
            stepDao.getStepsForDay(today).collect { entity ->
                if (entity != null) {
                    currentSteps = entity.count
                }
            }
        }
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
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    override fun onCleared() {
        super.onCleared()
        sensorManager.unregisterListener(this)
    }

    private fun saveStepsToDb(delta: Int) {
        viewModelScope.launch {
            val today = getTodayDate()
            val newCount = currentSteps + delta
            stepDao.upsertSteps(StepEntity(today, newCount))
            currentSteps = newCount
        }
    }

    private fun getTodayDate(): String {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        return sdf.format(java.util.Date())
    }

    private val startOfDay: Long
        get() = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

    val todayWater: Flow<Int?> = waterDao.getTodayWater(startOfDay)
    fun addWater() {
        viewModelScope.launch(Dispatchers.IO) {
            val todayRecord = waterDao.getTodayRecord(startOfDay)

            if (todayRecord != null) {
                val updatedRecord = todayRecord.copy(
                    glasses = todayRecord.glasses + 1
                )
                waterDao.updateWater(updatedRecord)
            } else {
                waterDao.insertWater(
                    WaterEntity(
                        glasses = 1,
                        date = System.currentTimeMillis()
                    )
                )
            }
        }
    }

    class DailyActivityViewModelFactory(
        private val goalDao: GoalDao,
        private val stepDao: StepDao,
        private val waterDao: WaterDao,
        private val context: Context
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(DailyActivityViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return DailyActivityViewModel(goalDao, stepDao, waterDao, context) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}