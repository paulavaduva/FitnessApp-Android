package com.example.fitnessapp.viewmodel

import androidx.lifecycle.*
import com.example.fitnessapp.data.*
import kotlinx.coroutines.flow.*
import java.text.SimpleDateFormat
import java.util.*

class StatisticsViewModel(
    private val stepDao: StepDao,
    private val mealDao: MealDao,
    private val goalDao: GoalDao
) : ViewModel() {

    private val goalsFlow: Flow<GoalEntity?> = goalDao.getGoals()

    val stepStatistics: Flow<Triple<Map<String, List<Float>>, Map<String, List<String>>, Map<String, List<String>>>> = combine(
        stepDao.getLast30DaysSteps(),
        goalsFlow
    ) { steps, goals ->
        val target = goals?.stepGoal?.toFloat() ?: 10000f

        // WEEK
        val weekSteps = steps.take(7).sortedBy { it.date }
        val weekData = weekSteps.map { (it.count.toFloat() / target).coerceIn(0f, 1.2f) }
        val weekLabels = weekSteps.map { formatDateToDayName(it.date) }
        val weekRaw = weekSteps.map { "${it.count}" }

        // DAY
        val todayCount = steps.find { it.date == getTodayDate() }?.count ?: 0
        val dayData = listOf(0.2f, 0.5f, 0.8f, 1.0f).map { (todayCount * it / target).coerceIn(0f, 1f) }
        val dayLabels = listOf("08h", "12h", "16h", "20h")
        //val dayRaw = dayLabels.map { "$todayCount" }
        val dayRaw = listOf(0.2f, 0.5f, 0.8f, 1.0f).map {
            "${(todayCount * it).toInt()}"
        }

        // MONTH
        val monthlyGrouped = steps.groupBy { it.date.substring(0, 7) }
        val monthData = monthlyGrouped.map { (_, days) -> (days.sumOf { it.count }.toFloat() / (target * 30)) }
        val monthLabels = monthlyGrouped.keys.map { formatDateToMonthName(it) }
        val monthRaw = monthlyGrouped.map { (_, days) -> "${days.sumOf { it.count }}" }

        Triple(
            mapOf("Day" to dayData, "Week" to weekData, "Month" to monthData),
            mapOf("Day" to dayLabels, "Week" to weekLabels, "Month" to monthLabels),
            mapOf("Day" to dayRaw, "Week" to weekRaw, "Month" to monthRaw)
        )
    }

    val calorieStatistics: Flow<Triple<Map<String, List<Float>>, Map<String, List<String>>, Map<String, List<String>>>> = combine(
        mealDao.getAllMeals(),
        goalsFlow
    ) { meals, goals ->
        val target = goals?.kcalGoal?.toFloat() ?: 2000f
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val groupedByDay = meals.groupBy { sdf.format(Date(it.dateAdded)) }

        // WEEK
        val last7Days = groupedByDay.keys.sorted().takeLast(7)
        val weekData = last7Days.map { (groupedByDay[it]?.sumOf { m -> m.calories * m.quantity }?.toFloat() ?: 0f) / target }
        val weekLabels = last7Days.map { formatDateToDayName(it) }
        val weekRaw = last7Days.map { "${groupedByDay[it]?.sumOf { m -> (m.calories * m.quantity).toInt() } ?: 0}" }

        // DAY
        val todayMeals = groupedByDay[getTodayDate()] ?: emptyList()
        val mealSections = mapOf(
            "BRK" to todayMeals.filter { getHourFromTimestamp(it.dateAdded) in 5..10 },
            "LUN" to todayMeals.filter { getHourFromTimestamp(it.dateAdded) in 11..16 },
            "DIN" to todayMeals.filter { getHourFromTimestamp(it.dateAdded) >= 17 || getHourFromTimestamp(it.dateAdded) < 23 }
        )
        val dayData = mealSections.values.map { section -> (section.sumOf { it.calories * it.quantity }.toFloat() / target) }
        val dayLabels = mealSections.keys.toList()
        val dayRaw = mealSections.values.map { section -> "${section.sumOf { (it.calories * it.quantity).toInt() }}" }

        // MONTH
        val monthGrouped = meals.groupBy { SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date(it.dateAdded)) }
        val monthData = monthGrouped.map { (_, mList) -> (mList.sumOf { it.calories * it.quantity }.toFloat() / (target * 30)) }
        val monthLabels = monthGrouped.keys.map { formatDateToMonthName(it) }
        val monthRaw = monthGrouped.map { (_, mList) -> "${mList.sumOf { (it.calories * it.quantity).toInt() }}" }

        Triple(
            mapOf("Day" to dayData, "Week" to weekData, "Month" to monthData),
            mapOf("Day" to dayLabels, "Week" to weekLabels, "Month" to monthLabels),
            mapOf("Day" to dayRaw, "Week" to weekRaw, "Month" to monthRaw)
        )
    }


    private fun getTodayDate() = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    private fun getHourFromTimestamp(timestamp: Long): Int {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        return calendar.get(Calendar.HOUR_OF_DAY)
    }

    private fun formatDateToDayName(dateString: String): String {
        return try {
            val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateString)
            SimpleDateFormat("E", Locale.ENGLISH).format(date!!).uppercase()
        } catch (e: Exception) { "?" }
    }

    private fun formatDateToMonthName(monthString: String): String {
        return try {
            val date = SimpleDateFormat("yyyy-MM", Locale.getDefault()).parse(monthString)
            SimpleDateFormat("MMM", Locale.ENGLISH).format(date!!).uppercase()
        } catch (e: Exception) { "?" }
    }
}

class StatisticsViewModelFactory(
    private val stepDao: StepDao,
    private val mealDao: MealDao,
    private val goalDao: GoalDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StatisticsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return StatisticsViewModel(stepDao, mealDao, goalDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}