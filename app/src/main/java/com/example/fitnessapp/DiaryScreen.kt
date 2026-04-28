package com.example.fitnessapp

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fitnessapp.data.MealEntity
import com.example.fitnessapp.viewmodel.MealViewModel
import java.text.SimpleDateFormat
import androidx.compose.material.icons.filled.Delete
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiaryScreen(viewModel: MealViewModel) {
    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedMealType by remember { mutableStateOf("Breakfast") }

    var selectedDate by remember { mutableStateOf(Calendar.getInstance()) }

    var newMealName by remember { mutableStateOf("") }
    var newCalories by remember { mutableStateOf("") }
    var newProteins by remember { mutableStateOf("") }
    var newCarbs by remember { mutableStateOf("") }
    var newFats by remember { mutableStateOf("") }

    var editingMeal by remember { mutableStateOf<MealEntity?>(null) }
    var quantity by remember { mutableStateOf("1.0") }

    val databaseMeals by viewModel.allMeals.collectAsState()
    val mealCategories = listOf("Breakfast", "Lunch", "Dinner", "Snack")

    val mealsInSelectedDate = databaseMeals.filter { meal ->
        val mealCal = Calendar.getInstance().apply { timeInMillis = meal.dateAdded }
        isSameDay(mealCal, selectedDate)
    }

    val catalogResults by viewModel.searchFoodInLibrary(searchQuery).collectAsState(initial = emptyList())
    val historyResults = databaseMeals
        .filter { it.name.contains(searchQuery, ignoreCase = true) }
        .distinctBy { it.name.lowercase().trim() to it.calories }

    val backgroundColor = Color(0xFF13151A)
    val surfaceColor = Color(0xFF1E2026)
    val accentGreen = Color(0xFF4ADE80)
    val textColor = Color.White
    val textSecondary = Color.Gray

    Scaffold(
        containerColor = backgroundColor,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    newMealName = ""; newCalories = ""; newProteins = ""; newCarbs = ""; newFats = ""
                    showAddDialog = true
                },
                containerColor = accentGreen,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = backgroundColor)
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    val newCal = selectedDate.clone() as Calendar
                    newCal.add(Calendar.DATE, -1)
                    selectedDate = newCal
                }) {
                    Icon(Icons.Default.KeyboardArrowLeft, contentDescription = null, tint = accentGreen)
                }

                Text(
                    text = getDisplayDate(selectedDate),
                    color = textColor,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                IconButton(onClick = {
                    val newCal = selectedDate.clone() as Calendar
                    newCal.add(Calendar.DATE, 1)
                    selectedDate = newCal
                }) {
                    Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = accentGreen)
                }
            }

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search...", color = textSecondary) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = textSecondary) },
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                shape = RoundedCornerShape(24.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = surfaceColor,
                    unfocusedContainerColor = surfaceColor,
                    focusedBorderColor = accentGreen,
                    unfocusedBorderColor = Color.Transparent,
                    focusedTextColor = textColor,
                    unfocusedTextColor = textColor
                )
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                if (searchQuery.isNotEmpty()) {
                    if (catalogResults.isNotEmpty()) {
                        item { SectionHeader("Suggestions", accentGreen) }
                        items(catalogResults) { item ->
                            CatalogItemRow(item, surfaceColor, accentGreen, textColor) {
                                newMealName = item.name
                                newCalories = item.calories.toString()
                                newProteins = item.protein.toString()
                                newCarbs = item.carbs.toString()
                                newFats = item.fat.toString()
                                showAddDialog = true
                            }
                        }
                    }
                    if (historyResults.isNotEmpty()) {
                        item { SectionHeader("From History", textSecondary) }
                        items(historyResults) { meal ->
                            MealItemRow(meal, surfaceColor, accentGreen, textColor, textSecondary,
                                onDelete = { viewModel.deleteMeal(meal) },
                                onEdit = {
                                    editingMeal = meal
                                    newMealName = meal.name
                                    newCalories = meal.calories.toString()
                                    newProteins = meal.protein.toString()
                                    newCarbs = meal.carbs.toString()
                                    newFats = meal.fat.toString()
                                    selectedMealType = meal.mealType
                                    showAddDialog = true
                                })
                        }
                    }
                } else {
                    mealCategories.forEach { category ->
                        val mealsInCategory = mealsInSelectedDate.filter { it.mealType == category }
                        item { SectionHeader(category, accentGreen) }

                        if (mealsInCategory.isEmpty()) {
                            item { Text("No meals for $category", color = textSecondary, fontSize = 13.sp, modifier = Modifier.padding(start = 8.dp)) }
                        } else {
                            items(mealsInCategory) { meal ->
                                MealItemRow(meal = meal,
                                    surfaceColor = surfaceColor,
                                    accentGreen = accentGreen,
                                    textColor = textColor,
                                    textSecondary = textSecondary,
                                    onDelete = { viewModel.deleteMeal(meal) },
                                    onEdit = {
                                        editingMeal = meal
                                        newMealName = meal.name
                                        newCalories = meal.calories.toString()
                                        newProteins = meal.protein.toString()
                                        newCarbs = meal.carbs.toString()
                                        newFats = meal.fat.toString()
                                        selectedMealType = meal.mealType
                                        showAddDialog = true
                                    })
                            }
                        }
                    }
                }
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }

        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                containerColor = surfaceColor,
                title = { Text("Add to ${getDisplayDate(selectedDate)}", color = textColor, fontSize = 18.sp) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            mealCategories.forEach { type ->
                                FilterChip(
                                    selected = selectedMealType == type,
                                    onClick = { selectedMealType = type },
                                    label = { Text(type, fontSize = 10.sp) },
                                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = accentGreen, selectedLabelColor = backgroundColor)
                                )
                            }
                        }
                        CustomInputField("Meal name", newMealName, { newMealName = it }, textColor, accentGreen)
                        CustomInputField("Calories", newCalories, { newCalories = it }, textColor, accentGreen)
                        CustomInputField("Quantity (pcs/grams)", quantity, { quantity = it }, textColor, accentGreen)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(modifier = Modifier.weight(1f)) { CustomInputField("Prot", newProteins, { newProteins = it }, textColor, accentGreen) }
                            Box(modifier = Modifier.weight(1f)) { CustomInputField("Carbs", newCarbs, { newCarbs = it }, textColor, accentGreen) }
                            Box(modifier = Modifier.weight(1f)) { CustomInputField("Fat", newFats, { newFats = it }, textColor, accentGreen) }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val q = quantity.toDoubleOrNull() ?: 1.0
                            val name = newMealName
                            val cal = newCalories.toDoubleOrNull() ?: 0.0
                            val prot = newProteins.toDoubleOrNull() ?: 0.0
                            val carb = newCarbs.toDoubleOrNull() ?: 0.0
                            val fat = newFats.toDoubleOrNull() ?: 0.0

                            if (editingMeal != null) {
                                val updatedMeal = editingMeal!!.copy(
                                    name = name,
                                    calories = cal,
                                    protein = prot,
                                    carbs = carb,
                                    fat = fat,
                                    mealType = selectedMealType,
                                    quantity = q
                                )
                                viewModel.updateMeal(updatedMeal)
                            } else {
                                viewModel.addMeal(
                                    name = name,
                                    calories = cal,
                                    protein = prot,
                                    carbs = carb,
                                    fat = fat,
                                    type = selectedMealType,
                                    date = selectedDate.timeInMillis,
                                    quantity = q
                                )
                            }

                            showAddDialog = false
                            editingMeal = null
                            quantity = "1.0"
                            searchQuery = ""
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = accentGreen)
                    ) {
                        Text(
                            text = if (editingMeal != null) "Update" else "Save",
                            color = backgroundColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            )
        }
    }
}


fun getDisplayDate(calendar: Calendar): String {
    val today = Calendar.getInstance()
    val yesterday = Calendar.getInstance().apply { add(Calendar.DATE, -1) }

    return when {
        isSameDay(calendar, today) -> "Today"
        isSameDay(calendar, yesterday) -> "Yesterday"
        else -> SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(calendar.time)
    }
}

fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}

@Composable
fun SectionHeader(title: String, color: Color) {
    Text(title, color = color, fontSize = 17.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 12.dp))
}

@Composable
fun CatalogItemRow(item: com.example.fitnessapp.data.FoodEntity, surfaceColor: Color, accentGreen: Color, textColor: Color, onClick: () -> Unit) {
    Surface(onClick = onClick, color = surfaceColor.copy(0.5f), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Add, null, tint = accentGreen, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(12.dp))
            Column {
                Text(item.name, color = textColor, fontSize = 15.sp)
                Text("${item.calories} kcal", color = Color.Gray, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun MealItemRow(meal: MealEntity, surfaceColor: Color, accentGreen: Color, textColor: Color, textSecondary: Color, onDelete: () -> Unit, onEdit: () -> Unit) {
    val totalCalories = meal.calories * meal.quantity
    Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(surfaceColor).clickable { onEdit() }.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(40.dp).clip(CircleShape).background(Color(0xFF2A2D36)), Alignment.Center) {
            Text(if (meal.name.isNotEmpty()) meal.name.take(1) else "?", color = accentGreen, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = "${meal.name} ${if(meal.quantity > 1) "x${meal.quantity}" else ""}",
                color = textColor,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "${totalCalories} kcal • ${SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(meal.dateAdded))}",
                color = textSecondary,
                fontSize = 12.sp
            )
        }
        IconButton(onClick = onDelete) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = null,
                tint = Color.Red.copy(alpha = 0.6f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun CustomInputField(label: String, value: String, onValueChange: (String) -> Unit, textColor: Color, accentGreen: Color) {
    OutlinedTextField(
        value = value, onValueChange = onValueChange,
        label = { Text(label, fontSize = 10.sp, color = Color.Gray) },
        singleLine = true, modifier = Modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = textColor, unfocusedTextColor = textColor, focusedBorderColor = accentGreen, unfocusedBorderColor = Color.Gray)
    )
}