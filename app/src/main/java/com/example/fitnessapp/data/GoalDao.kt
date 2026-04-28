package com.example.fitnessapp.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalDao {
    @Query("SELECT * FROM goals_table WHERE id = 'current_goals' LIMIT 1")
    fun getGoals(): Flow<GoalEntity?>

    @Upsert
    suspend fun upsertGoals(goals: GoalEntity)
}