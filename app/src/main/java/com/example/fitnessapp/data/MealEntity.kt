package com.example.fitnessapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "meals_table")
data class MealEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val name: String,
    val calories: Double,
    val protein: Double,
    val carbs: Double,
    val fat: Double,
    val mealType: String,
    val quantity: Double = 1.0,
    val dateAdded: Long = System.currentTimeMillis()
)

@Entity(tableName = "food_library")
data class FoodEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val calories: Double,
    val protein: Double,
    val carbs: Double,
    val fat: Double
)
