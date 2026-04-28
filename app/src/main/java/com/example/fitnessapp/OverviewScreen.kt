package com.example.fitnessapp

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fitnessapp.ui.theme.*
import androidx.compose.runtime.*
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fitnessapp.viewmodel.DailyActivityViewModel
import com.example.fitnessapp.viewmodel.MealViewModel
import com.example.fitnessapp.viewmodel.ProfileViewModel
import java.util.Calendar


@Composable
fun OverviewScreen(dailyVM: DailyActivityViewModel = viewModel(), viewModel: MealViewModel, profileViewModel: ProfileViewModel = viewModel()) {
    val scrollState = rememberScrollState()
    var showDialog by remember { mutableStateOf(false) }
    
    //    var stepGoal by remember { mutableStateOf("1800") }
    var kcalGoal by remember { mutableStateOf("1800") }
    var carbGoal by remember { mutableStateOf("180") }
    var proteinGoal by remember { mutableStateOf("135") }
    var fatGoal by remember { mutableStateOf("60") }
//    var waterGoal by remember { mutableStateOf("8") }
    val consumedWater by dailyVM.todayWater.collectAsState(initial = 0)
    val userProfile by profileViewModel.userProfile.collectAsState(initial = null)

    val allMeals by viewModel.allMeals.collectAsState()

    val todayMeals = remember(allMeals) {
        val today = Calendar.getInstance()
        allMeals.filter {
            val mealDate = Calendar.getInstance().apply { timeInMillis = it.dateAdded }
            mealDate.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                    mealDate.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)
        }
    }

    val consumedKcal = todayMeals.sumOf { it.calories * it.quantity }.toFloat()
    val consumedCarbs = todayMeals.sumOf { it.carbs * it.quantity }.toFloat()
    val consumedProtein = todayMeals.sumOf { it.protein * it.quantity }.toFloat()
    val consumedFat = todayMeals.sumOf { it.fat * it.quantity }.toFloat()

    Scaffold(
        containerColor = BackgroundDark
    ) { innerPadding ->
        if (showDialog) {
            EditGoalsDialog(
                initialSteps = dailyVM.stepGoal.toString(),
                initialWater = dailyVM.waterGoal.toString(),
                onDismiss = { showDialog = false },
                onSave = { steps, kcal, carbs, protein, fat, water ->
                    dailyVM.updateActivityGoals(steps, water)

//                    if (steps.isNotEmpty()) stepGoal = steps
                    if (kcal.isNotEmpty()) kcalGoal = kcal
                    if (carbs.isNotEmpty()) carbGoal = carbs
                    if (protein.isNotEmpty()) proteinGoal = protein
                    if (fat.isNotEmpty()) fatGoal = fat
//                    if(water.isNotEmpty()) waterGoal = water

                    viewModel.updateGoals(
                        kcal = kcal.toDoubleOrNull() ?: viewModel.kcalGoal,
                        protein = protein.toDoubleOrNull() ?: viewModel.proteinGoal,
                        carbs = carbs.toDoubleOrNull() ?: viewModel.carbGoal,
                        fat = fat.toDoubleOrNull() ?: viewModel.fatGoal,
//                        steps = steps.toIntOrNull() ?: viewModel.stepGoal,
//                        water = water.toIntOrNull() ?: viewModel.waterGoal
                    )
                    showDialog = false
                }
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .background(BackgroundDark)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val displayName = userProfile?.name ?: "User"
            HeaderSection(name = if(displayName.isEmpty()) "User" else displayName)

            Spacer(modifier = Modifier.height(40.dp))

            ProgressCircle(
                label = "steps",
                current = dailyVM.currentSteps.toString(),
                goal = dailyVM.stepGoal.toString(),
                progress = dailyVM.currentSteps.toFloat() / dailyVM.stepGoal,
//                current = "1250",
//                goal = viewModel.stepGoal.toString(),
//                progress = 1250f / viewModel.stepGoal.toFloat(),
                size = 190.dp,
                strokeWidth = 16.dp
            )

            Spacer(modifier = Modifier.height(30.dp))

            CaloriesCard(
                consumed = consumedKcal.toInt(),
                goal = viewModel.kcalGoal.toInt()
            )

            Spacer(modifier = Modifier.height(30.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                NutrientCard(
                    modifier = Modifier.weight(1f),
                    name = "Carb",
                    value = "${consumedCarbs.toInt()}/${viewModel.carbGoal.toInt()}g",
                    progress = if (viewModel.carbGoal > 0) (consumedCarbs / viewModel.carbGoal.toFloat()) else 0f
                )
                NutrientCard(
                    modifier = Modifier.weight(1f),
                    name = "Protein",
                    value = "${consumedProtein.toInt()}/${viewModel.proteinGoal.toInt()}g",
                    progress = if (viewModel.proteinGoal > 0) (consumedProtein / viewModel.proteinGoal.toFloat()) else 0f
                )
                NutrientCard(
                    modifier = Modifier.weight(1f),
                    name = "Fat",
                    value = "${consumedFat.toInt()}/${viewModel.fatGoal.toInt()}g",
                    progress = if (viewModel.fatGoal > 0) (consumedFat / viewModel.fatGoal.toFloat()) else 0f
                )
            }

            Spacer(modifier = Modifier.height(15.dp))

            WaterTrackerCard(
                current = consumedWater ?: 0,
                goal = dailyVM.waterGoal,
                onAddClick = { dailyVM.addWater() }
            )

            Spacer(modifier = Modifier.height(15.dp))

            SetGoalButton(onClick = { showDialog = true })
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun HeaderSection(name: String){
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Hello, $name!",
            color = TextWhite,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Monday, June 25",
            color = TextLightGrey,
            fontSize = 16.sp
        )
    }
}

@Composable
fun ProgressCircle(
    label: String,
    current: String,
    goal: String,
    progress: Float,
    size: Dp,
    strokeWidth: Dp
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(contentAlignment = Alignment.Center) {
            CircularProgressIndicator(
            progress = { 1f },
            modifier = Modifier.size(size),
            color = CardGrey,
            strokeWidth = strokeWidth,
            trackColor = ProgressIndicatorDefaults.circularIndeterminateTrackColor,
            strokeCap = ProgressIndicatorDefaults.CircularDeterminateStrokeCap,
            )
            CircularProgressIndicator(
//            progress = { progress },
            progress = { progress.coerceIn(0f, 1f) },
            modifier = Modifier.size(size),
            color = EmeraldGreen,
            strokeWidth = strokeWidth,
            trackColor = Color.Transparent,
            strokeCap = ProgressIndicatorDefaults.CircularDeterminateStrokeCap,
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = current,
                    color = TextWhite,
                    fontSize = (size.value / 6).sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "of $goal",
                    color = TextLightGrey,
                    fontSize = (size.value / 10).sp
                )

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.DirectionsRun,
                    contentDescription = null,
                    tint = EmeraldGreen,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = label, color = TextWhite, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun CaloriesCard(consumed: Int, goal: Int) {
    val remaining = goal - consumed

    Card(
        modifier = Modifier.fillMaxWidth().height(80.dp),
        colors = CardDefaults.cardColors(containerColor = CardGrey),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Calories", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(imageVector = Icons.Default.LocalFireDepartment, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(22.dp))
                }
                Text(
                    text = if (remaining >= 0) "Remaining: $remaining kcal" else "Over limit: ${-remaining} kcal",
                    color = if (remaining >= 0) TextLightGrey else Color.Red,
                    fontSize = 14.sp
                )
            }
            Text(
                text = "$consumed / $goal kcal",
                color = EmeraldGreen,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }
    }
}

@Composable
fun NutrientCard(modifier: Modifier, name: String, value: String, progress: Float) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = CardGrey),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = name, color = TextWhite, fontSize = 18.sp)

            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .background(BackgroundDark, CircleShape)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .fillMaxHeight()
                        .background(EmeraldGreen, CircleShape)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = value, color = TextLightGrey, fontSize = 14.sp)
        }
    }
}

@Composable
fun WaterTrackerCard(current: Int, goal: Int, onAddClick: () -> Unit) {
//    var waterGlasses by remember { mutableStateOf(0) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        colors = CardDefaults.cardColors(containerColor = CardGrey),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp, vertical = 16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                )
                {
                    Icon(
                        imageVector = Icons.Default.WaterDrop,
                        contentDescription = null,
                        tint = EmeraldGreen,
                        modifier = Modifier.size(22.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Water",
                        color = TextWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }

                Text(
                    text = "$current / $goal glasses",
                    color = TextLightGrey,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(start = 30.dp)
                )
            }

            IconButton(
                onClick = onAddClick,
                modifier = Modifier
                    .background(EmeraldGreen, CircleShape)
                    .size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Water",
                    tint = Color.Black
                )
            }
        }
    }
}

@Composable
fun SetGoalButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp)
            .height(56.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = EmeraldGreen,
            contentColor = Color.Black
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
    ) {
        Text(
            text = "SET NEW GOAL",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun EditGoalsDialog(
    initialSteps: String,
    initialWater: String,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String, String, String) -> Unit
) {
    var stepsInput by remember { mutableStateOf(initialSteps) }
    var kcalInput by remember { mutableStateOf("") }
    var carbsInput by remember { mutableStateOf("") }
    var proteinInput by remember { mutableStateOf("") }
    var fatInput by remember { mutableStateOf("") }
    var waterInput by remember { mutableStateOf(initialWater) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardGrey,
        title = {
            Text("Set New Goals", color = TextWhite, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Enter your daily targets:", color = TextLightGrey, fontSize = 14.sp)

                GoalInputField("Daily Steps", stepsInput) { stepsInput = it }
                GoalInputField("Daily Calories", kcalInput) { kcalInput = it }
                GoalInputField("Carbs Goal (g)", carbsInput) { carbsInput = it }
                GoalInputField("Protein Goal (g)", proteinInput) { proteinInput = it }
                GoalInputField("Fat Goal (g)", fatInput) { fatInput = it }
                GoalInputField("Water Goal (glasses)", waterInput) { waterInput = it }
            }
        },
        confirmButton = {
            TextButton(
                {
                    onSave(stepsInput, kcalInput, carbsInput, proteinInput, fatInput, waterInput)
                }
            ) {
                Text("SAVE", color = EmeraldGreen, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL", color = TextLightGrey)
            }
        }
    )
}

@Composable
fun GoalInputField(label: String, value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = TextWhite,
            unfocusedTextColor = TextWhite,
            focusedBorderColor = EmeraldGreen,
            unfocusedBorderColor = TextLightGrey,
            focusedLabelColor = EmeraldGreen,
            unfocusedLabelColor = TextLightGrey
        )
    )
}
