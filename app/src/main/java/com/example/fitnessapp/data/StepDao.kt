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
}