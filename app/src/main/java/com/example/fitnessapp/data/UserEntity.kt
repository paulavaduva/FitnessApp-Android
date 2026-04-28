package com.example.fitnessapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile_table")
data class UserEntity(
    @PrimaryKey val id: Int = 1,
    val name: String = "",
    val height: String = "",
    val weight: String = "",
    val age: String = "",
    val gender: String = ""
)