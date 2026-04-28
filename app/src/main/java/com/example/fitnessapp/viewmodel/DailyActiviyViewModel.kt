package com.example.fitnessapp.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class DailyActivityViewModel : ViewModel() {
    var currentSteps by mutableStateOf(1250)
    var stepGoal by mutableStateOf(1800)

    var waterGlasses by mutableStateOf(6)
    var waterGoal by mutableStateOf(8)

    var userName by mutableStateOf("Paula")

    fun addWater() {
        if (waterGlasses < 20) {
            waterGlasses++
        }
    }

    fun updateActivityGoals(newSteps: String, newWater: String) {
        newSteps.toIntOrNull()?.let { stepGoal = it }
        newWater.toIntOrNull()?.let { waterGoal = it }
    }
}