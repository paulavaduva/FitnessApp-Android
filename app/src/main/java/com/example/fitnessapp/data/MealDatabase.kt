package com.example.fitnessapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.launch

@Database(entities = [MealEntity::class, FoodEntity::class, WaterEntity::class, UserEntity::class], version = 2, exportSchema = false)
abstract class MealDatabase : RoomDatabase() {

    abstract fun mealDao(): MealDao
    abstract fun foodDao(): FoodDao
    abstract fun waterDao(): WaterDao
    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var Instance: MealDatabase? = null

        fun getDatabase(context: Context, scope: kotlinx.coroutines.CoroutineScope): MealDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    MealDatabase::class.java,
                    "fitness_database"
                )
                    .addCallback(FoodDatabaseCallback(scope))
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { Instance = it }
            }
        }
    }

    private class FoodDatabaseCallback(
        private val scope: kotlinx.coroutines.CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
            super.onCreate(db)
            Instance?.let { database ->
                scope.launch {
                    val foodDao = database.foodDao()
                    foodDao.insertAll(listOf(
                        FoodEntity(name = "Apple", calories = 52.0, protein = 0.3, carbs = 14.0, fat = 0.2),
                        FoodEntity(name = "Banana", calories = 89.0, protein = 1.1, carbs = 23.0, fat = 0.3),
                        FoodEntity(name = "Chicken Breast (100g)", calories = 165.0, protein = 31.0, carbs = 0.0, fat = 3.6),
                        FoodEntity(name = "Egg (1 piece)", calories = 70.0, protein = 6.0, carbs = 0.0, fat = 5.0),
                        FoodEntity(name = "Rice (100g)", calories = 130.0, protein = 2.7, carbs = 28.0, fat = 0.3)
                    ))
                }
            }
        }
    }
}