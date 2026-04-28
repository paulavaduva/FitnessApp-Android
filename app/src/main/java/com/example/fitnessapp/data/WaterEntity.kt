package com.example.fitnessapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "water_table")
data class WaterEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val glasses: Int,
    val date: Long
)