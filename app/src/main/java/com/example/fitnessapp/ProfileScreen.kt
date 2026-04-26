package com.example.fitnessapp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.pow
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen() {
    val backgroundColor = Color(0xFF13151A)
    val surfaceColor = Color(0xFF1E2026)
    val accentGreen = Color(0xFF4ADE80)
    val textColor = Color.White
    val textSecondary = Color.Gray

    var heightCm by remember { mutableStateOf("174") }
    var weightKg by remember { mutableStateOf("65") }
    var age by remember { mutableStateOf("22") }
    var gender by remember { mutableStateOf("Female") }
    var showEditDialog by remember { mutableStateOf(false) }

    val h = heightCm.toFloatOrNull() ?: 0f
    val w = weightKg.toFloatOrNull() ?: 0f
    val a = age.toIntOrNull() ?: 0

    val bmiValue = if (h > 0 && w > 0) {
        val bmi = w / (h / 100f).pow(2)
        (bmi * 10).roundToInt() / 10.0
    } else 0.0

    val bmiCategory = when {
        bmiValue < 18.5 -> "Underweight"
        bmiValue in 18.5..24.9 -> "Healthy Weight"
        bmiValue in 25.0..29.9 -> "Overweight"
        else -> "Obese"
    }

    val bmrValue = if (h > 0 && w > 0 && a > 0) {
        (10 * w + 6.25 * h - 5 * a - 161).roundToInt()
    } else 0

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Profile",
                color = textColor,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 24.dp)
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SmallStatCard(
                    Modifier.weight(1f), "Height", "$heightCm cm",
                    Icons.Default.Height, surfaceColor, textColor, textSecondary, accentGreen
                )
                SmallStatCard(
                    Modifier.weight(1f), "Weight", "$weightKg kg",
                    Icons.Default.MonitorWeight, surfaceColor, textColor, textSecondary, accentGreen
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SmallStatCard(
                    Modifier.weight(1f), "Age", "$age yo",
                    Icons.Default.CalendarToday, surfaceColor, textColor, textSecondary, accentGreen
                )
                SmallStatCard(
                    Modifier.weight(1f), "Gender", gender,
                    Icons.Default.Person, surfaceColor, textColor, textSecondary, accentGreen
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            LargeStatCard(
                title = "Your BMI",
                value = bmiValue.toString(),
                subtitle = bmiCategory,
                icon = Icons.Default.Favorite,
                accentColor = accentGreen,
                surfaceColor = surfaceColor,
                textColor = textColor,
                textSecondary = textSecondary
            )

            Spacer(modifier = Modifier.height(16.dp))

            LargeStatCard(
                title = "Your BMR",
                value = bmrValue.toString(),
                subtitle = "Calories / day",
                icon = Icons.Default.LocalFireDepartment,
                accentColor = accentGreen,
                surfaceColor = surfaceColor,
                textColor = textColor,
                textSecondary = textSecondary
            )

            Spacer(modifier = Modifier.height(100.dp))
        }

        FloatingActionButton(
            onClick = { showEditDialog = true },
            containerColor = accentGreen,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Edit, contentDescription = "Edit Profile", tint = backgroundColor)
        }
    }

    if (showEditDialog) {
        var tempHeight by remember { mutableStateOf(heightCm) }
        var tempWeight by remember { mutableStateOf(weightKg) }
        var tempAge by remember { mutableStateOf(age) }

        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            containerColor = surfaceColor,
            title = { Text("Update Profile Info", color = textColor) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ProfileTextField("Height (cm)", tempHeight) { tempHeight = it }
                    ProfileTextField("Weight (kg)", tempWeight) { tempWeight = it }
                    ProfileTextField("Age", tempAge) { tempAge = it }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        heightCm = tempHeight
                        weightKg = tempWeight
                        age = tempAge
                        showEditDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = accentGreen)
                ) {
                    Text("Save", color = backgroundColor, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
fun SmallStatCard(modifier: Modifier, title: String, value: String, icon: ImageVector, bgColor: Color, txtColor: Color, secColor: Color, accentColor: Color) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(28.dp))
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = title, color = secColor, fontSize = 13.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = value, color = txtColor, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun LargeStatCard(title: String, value: String, subtitle: String, icon: ImageVector, accentColor: Color, surfaceColor: Color, textColor: Color, textSecondary: Color) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(surfaceColor)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(36.dp))
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = title, color = textSecondary, fontSize = 14.sp)
        Text(text = value, color = textColor, fontSize = 36.sp, fontWeight = FontWeight.ExtraBold)
        Text(text = subtitle, color = accentColor, fontSize = 16.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun ProfileTextField(label: String, value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedBorderColor = Color(0xFF4ADE80)
        )
    )
}

@Preview
@Composable
fun PreviewProfile() {
    ProfileScreen()
}