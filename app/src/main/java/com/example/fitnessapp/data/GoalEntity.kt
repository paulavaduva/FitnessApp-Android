package com.example.fitnessapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "goals_table")
data class GoalEntity(
    @PrimaryKey val id: String = "current_goals",
    val stepGoal: Int = 1800,
    val waterGoal: Int = 8,
    val kcalGoal: Double = 2000.0,
    val proteinGoal: Double = 150.0,
    val carbGoal: Double = 200.0,
    val fatGoal: Double = 70.0
)