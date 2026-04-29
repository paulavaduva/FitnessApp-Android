
package com.example.fitnessapp.viewmodel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.fitnessapp.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.util.Calendar
import com.example.fitnessapp.FitnessNotificationManager
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
    private val context: Context
) : ViewModel() {

    var currentSteps by mutableStateOf(0)
    var stepGoal by mutableStateOf(1800)
    var waterGlasses by mutableStateOf(0)
    var waterGoal by mutableStateOf(8)
    //var userName by mutableStateOf("Paula")
    var kcalGoal by mutableStateOf(2000.0)
    var proteinGoal by mutableStateOf(150.0)
    var carbGoal by mutableStateOf(200.0)
    var fatGoal by mutableStateOf(70.0)


    init {
        loadStepsFromDb()
        loadGoalsFromDb()
        observeWater()
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

    fun updateActivityGoals(steps: String, water: String, kcal: String, carbs: String, protein: String, fat: String) {
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
                val newSteps = entity?.count ?: 0

                if (stepGoal in (currentSteps + 1)..newSteps && currentSteps > 0) {
                    FitnessNotificationManager.sendStepGoalReached(context, stepGoal)
                }
                currentSteps = newSteps
            }
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
    private fun observeWater() {
        viewModelScope.launch {
            waterDao.getTodayWater(startOfDay).collect { glasses ->
                waterGlasses = glasses ?: 0
            }
        }
    }

    fun addWater() {
        viewModelScope.launch(Dispatchers.IO) {
            val todayRecord = waterDao.getTodayRecord(startOfDay)

            val newGlassesCount = (todayRecord?.glasses ?: 0) + 1

            if (todayRecord != null) {
                val updatedRecord = todayRecord.copy(
                    glasses = newGlassesCount
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
            if (newGlassesCount == waterGoal) {
                FitnessNotificationManager.sendWaterGoalReached(context, waterGoal)
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