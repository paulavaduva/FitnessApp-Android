package com.example.fitnessapp.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface StepDao {
    @Query("SELECT * FROM steps_table WHERE date = :todayDate")
    fun getStepsForDay(todayDate: String): Flow<StepEntity?>

    @Upsert
    suspend fun upsertSteps(steps: StepEntity)

    @Query("SELECT * FROM steps_table WHERE date = :date LIMIT 1")
    suspend fun getStepsForDayOnce(date: String): StepEntity?

    @Query("SELECT * FROM steps_table ORDER BY date DESC LIMIT 7")
    fun getLast7DaysSteps(): Flow<List<StepEntity>>

    @Query("SELECT * FROM steps_table ORDER BY date DESC LIMIT 30")
    fun getLast30DaysSteps(): Flow<List<StepEntity>>
}