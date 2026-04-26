package com.example.fitnessapp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class Meal(
    val name: String,
    val calories: Int,
    val protein: String = "0",
    val carbs: String = "0",
    val fat: String = "0"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiaryScreen() {
    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }

    var newMealName by remember { mutableStateOf("") }
    var newCalories by remember { mutableStateOf("") }
    var newProteins by remember { mutableStateOf("") }
    var newCarbs by remember { mutableStateOf("") }
    var newFats by remember { mutableStateOf("") }

    val predefinedMeals = listOf(
        Meal("Banana", 89),
        Meal("Apple", 95),
        Meal("Chicken Breast", 165)
    )

    val filteredMeals = predefinedMeals.filter {
        it.name.contains(searchQuery, ignoreCase = true)
    }

    val backgroundColor = Color(0xFF13151A)
    val surfaceColor = Color(0xFF1E2026)
    val accentGreen = Color(0xFF4ADE80)
    val textColor = Color.White
    val textSecondary = Color.Gray

    Scaffold(
        containerColor = backgroundColor,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
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
            Text(
                text = "Meals",
                color = textColor,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(vertical = 24.dp)
            )

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search meal", color = textSecondary) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = textSecondary) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
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

            if (filteredMeals.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No meals found", color = textSecondary)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(filteredMeals) { meal ->
                        MealItemRow(meal, surfaceColor, accentGreen, textColor, textSecondary)
                    }
                }
            }
        }

        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                containerColor = surfaceColor,
                title = { Text(text = "Add Custom Meal", color = textColor, fontSize = 18.sp) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        CustomInputField("Meal name", newMealName, { newMealName = it }, textColor, accentGreen)
                        CustomInputField("Calories (kcal)", newCalories, { newCalories = it }, textColor, accentGreen)

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(modifier = Modifier.weight(1f)) {
                                CustomInputField("Prot (g)", newProteins, { newProteins = it }, textColor, accentGreen)
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                CustomInputField("Carb (g)", newCarbs, { newCarbs = it }, textColor, accentGreen)
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                CustomInputField("Fat (g)", newFats, { newFats = it }, textColor, accentGreen)
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showAddDialog = false
                            newMealName = ""; newCalories = ""; newProteins = ""; newCarbs = ""; newFats = ""
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = accentGreen)
                    ) {
                        Text("Save", color = backgroundColor, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddDialog = false }) {
                        Text("Cancel", color = accentGreen)
                    }
                }
            )
        }
    }
}

@Composable
fun CustomInputField(label: String, value: String, onValueChange: (String) -> Unit, textColor: Color, accentGreen: Color) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontSize = 11.sp, color = Color.Gray) },
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = textColor,
            unfocusedTextColor = textColor,
            focusedBorderColor = accentGreen,
            unfocusedBorderColor = Color.Gray
        )
    )
}

@Composable
fun MealItemRow(meal: Meal, surfaceColor: Color, accentGreen: Color, textColor: Color, textSecondary: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(surfaceColor)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Color(0xFF2A2D36)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = meal.name.take(1), color = accentGreen, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(text = meal.name, color = textColor, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Text(text = "${meal.calories} kcal", color = textSecondary, fontSize = 14.sp)
        }
    }
}

@Preview
@Composable
fun PreviewDiary() {
    DiaryScreen()
}