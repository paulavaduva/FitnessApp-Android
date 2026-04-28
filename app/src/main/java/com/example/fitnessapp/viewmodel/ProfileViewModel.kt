package com.example.fitnessapp.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import kotlin.math.pow
import kotlin.math.roundToInt

class ProfileViewModel : ViewModel() {
    var heightCm by mutableStateOf("174")
    var weightKg by mutableStateOf("65")
    var age by mutableStateOf("22")

    var gender by mutableStateOf("Female")
    var userName by mutableStateOf("Paula")

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

    fun updateProfile(newHeight: String, newWeight: String, newAge: String, newGender: String) {
        heightCm = newHeight
        weightKg = newWeight
        age = newAge
        gender = newGender
    }
}