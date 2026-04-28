package com.example.fitnessapp.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface WaterDao {
    @Insert
    suspend fun insertWater(water: WaterEntity)

    @Update
    suspend fun updateWater(water: WaterEntity)

    @Query("SELECT * FROM water_table WHERE date >= :startOfDay LIMIT 1")
    suspend fun getTodayRecord(startOfDay: Long): WaterEntity?

    @Query("SELECT SUM(glasses) FROM water_table WHERE date >= :startOfDay")
    fun getTodayWater(startOfDay: Long): Flow<Int?>
}