package com.example.fitnessapp.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.fitnessapp.data.FoodDao
import com.example.fitnessapp.data.MealDao
import com.example.fitnessapp.data.MealEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

class MealViewModel(private val mealDao: MealDao, private val foodDao: FoodDao) : ViewModel() {

    val allMeals: StateFlow<List<MealEntity>> = mealDao.getAllMeals()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    fun searchFoodInLibrary(query: String) = foodDao.searchFoods(query)

    var kcalGoal by mutableStateOf(2000.0)
    var proteinGoal by mutableStateOf(150.0)
    var carbGoal by mutableStateOf(200.0)
    var fatGoal by mutableStateOf(70.0)
    var stepGoal by mutableStateOf(10000)
    var waterGoal by mutableStateOf(8)

    fun updateGoals(kcal: Double, protein: Double, carbs: Double, fat: Double, steps: Int, water: Int) {
        kcalGoal = kcal
        proteinGoal = protein
        carbGoal = carbs
        fatGoal = fat
        stepGoal = steps
        waterGoal = water
    }

    fun getTodayMeals(meals: List<MealEntity>): List<MealEntity> {
        val today = Calendar.getInstance()
        return meals.filter {
            val mealDate = Calendar.getInstance().apply { timeInMillis = it.dateAdded }
            mealDate.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                    mealDate.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)
        }
    }

    fun addMeal(name: String, calories: Double, protein: Double, carbs: Double, fat: Double, type: String, date: Long, quantity: Double) {
        viewModelScope.launch {
            val newMeal = MealEntity(
                name = name,
                calories = calories,
                protein = protein,
                carbs = carbs,
                fat = fat,
                mealType = type,
                dateAdded = date,
                quantity = quantity
            )
            mealDao.insertMeal(newMeal)
        }
    }

    fun deleteMeal(meal: MealEntity) {
        viewModelScope.launch {
            mealDao.deleteMeal(meal)
        }
    }

    fun updateMeal(meal: MealEntity) {
        viewModelScope.launch {
            mealDao.updateMeal(meal)
        }
    }
}

class MealViewModelFactory(private val mealDao: MealDao, private val foodDao: FoodDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MealViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MealViewModel(mealDao, foodDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}