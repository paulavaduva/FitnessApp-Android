package com.example.fitnessapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "steps_table")
data class StepEntity(
    @PrimaryKey val date: String,
    val count: Int
)