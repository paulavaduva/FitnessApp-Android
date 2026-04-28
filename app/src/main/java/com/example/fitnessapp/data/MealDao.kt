package com.example.fitnessapp.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MealDao {

    @Insert
    suspend fun insertMeal(meal: MealEntity)

    @Query("SELECT * FROM meals_table ORDER BY dateAdded DESC")
    fun getAllMeals(): Flow<List<MealEntity>>

    @Delete
    suspend fun deleteMeal(meal: MealEntity)

    @Update
    suspend fun updateMeal(meal: MealEntity)
}