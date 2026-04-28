package com.example.fitnessapp.viewmodel

import android.app.Application
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

class DailyActivityViewModel(application: Application) : AndroidViewModel(application) {
    private val waterDao = MealDatabase.getDatabase(application, viewModelScope).waterDao()
    var currentSteps by mutableStateOf(1250)
    var stepGoal by mutableStateOf(1800)

//    var waterGlasses by mutableStateOf(6)
    var waterGoal by mutableStateOf(8)


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

    fun updateActivityGoals(newSteps: String, newWater: String) {
        newSteps.toIntOrNull()?.let { stepGoal = it }
        newWater.toIntOrNull()?.let { waterGoal = it }
    }
}