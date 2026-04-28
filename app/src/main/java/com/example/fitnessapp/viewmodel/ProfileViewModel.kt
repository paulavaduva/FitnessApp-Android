package com.example.fitnessapp.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnessapp.data.MealDatabase
import com.example.fitnessapp.data.UserEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.pow
import kotlin.math.roundToInt

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val userDao = MealDatabase.getDatabase(application, viewModelScope).userDao()
    val userProfile = userDao.getUserProfile()
    fun saveProfile() {
        viewModelScope.launch(Dispatchers.IO) {
            val updatedUser = UserEntity(
                id = 1,
                name = userName,
                height = heightCm,
                weight = weightKg,
                age = age,
                gender = gender
            )
            userDao.insertProfile(updatedUser)
        }
    }
    var heightCm by mutableStateOf("")
    var weightKg by mutableStateOf("")
    var age by mutableStateOf("")

    var gender by mutableStateOf("")
    var userName by mutableStateOf("")

    val bmiValue: Double
        get() {
            val h = heightCm.toFloatOrNull() ?: 0f
            val w = weightKg.toFloatOrNull() ?: 0f
            return if (h > 0 && w > 0) {
                val bmi = w / (h / 100f).pow(2)
                (bmi * 10).roundToInt() / 10.0
            } else 0.0
        }

    val bmiCategory: String
        get() = when {
            bmiValue < 18.5 -> "Underweight"
            bmiValue in 18.5..24.9 -> "Healthy Weight"
            bmiValue in 25.0..29.9 -> "Overweight"
            else -> "Obese"
        }

    val bmrValue: Int
        get() {
            val h = heightCm.toFloatOrNull() ?: 0f
            val w = weightKg.toFloatOrNull() ?: 0f
            val a = age.toIntOrNull() ?: 0
            return if (h > 0 && w > 0 && a > 0) {
                (10 * w + 6.25 * h - 5 * a - 161).roundToInt()
            } else 0
        }

}