package com.example.fitnessapp

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun StatisticsScreen() {
    val backgroundColor = Color(0xFF13151A)
    val surfaceColor = Color(0xFF1E2026)
    val textColor = Color.White

    val calorieColor = Color(0xFF4ADE80)
    val stepsColor = Color(0xFF2DD4BF)

    val calorieDataMap = mapOf(
        "Day" to listOf(0.2f, 0.4f, 0.7f, 0.3f, 0.5f, 0.6f),
        "Week" to listOf(0.4f, 0.5f, 0.4f, 0.55f, 0.45f, 0.65f, 0.55f),
        "Month" to listOf(0.4f, 0.6f, 0.5f, 0.8f, 0.7f, 0.9f)
    )

    val stepDataMap = mapOf(
        "Day" to listOf(0.1f, 0.3f, 0.5f, 0.8f, 0.6f, 0.4f),
        "Week" to listOf(0.5f, 0.35f, 0.75f, 0.25f, 0.4f, 0.35f, 0.6f),
        "Month" to listOf(0.3f, 0.5f, 0.7f, 0.6f, 0.8f, 0.75f)
    )

    val labelsMap = mapOf(
        "Day" to listOf("8h", "11h", "14h", "17h", "20h", "23h"),
        "Week" to listOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN"),
        "Month" to listOf("JAN", "FEB", "MAR", "APR", "MAY", "JUN")
    )

    val yAxisLabels = listOf("3000", "2500", "2000", "1500", "1000", "500", "0")

    Scaffold(
        containerColor = backgroundColor
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = "Statistics",
                color = textColor,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(vertical = 24.dp)
            )

            CustomBarChartCard(
                title = "Calorie Trends",
                dataMap = calorieDataMap,
                labelsMap = labelsMap,
                yAxisLabels = yAxisLabels,
                barColor = calorieColor,
                surfaceColor = surfaceColor
            )

            Spacer(modifier = Modifier.height(24.dp))

            CustomBarChartCard(
                title = "Steps Trends",
                dataMap = stepDataMap,
                labelsMap = labelsMap,
                yAxisLabels = yAxisLabels,
                barColor = stepsColor,
                surfaceColor = surfaceColor
            )
        }
    }
}

@Composable
fun CustomBarChartCard(
    title: String,
    dataMap: Map<String, List<Float>>,
    labelsMap: Map<String, List<String>>,
    yAxisLabels: List<String>,
    barColor: Color,
    surfaceColor: Color
) {
    val textSecondary = Color.Gray
    val barBackgroundColor = Color(0xFF2A2D36)

    var selectedTab by remember { mutableStateOf("Week") }

    val currentData = dataMap[selectedTab] ?: emptyList()
    val currentLabels = labelsMap[selectedTab] ?: emptyList()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(surfaceColor)
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(barBackgroundColor)
                    .padding(4.dp)
            ) {
                val tabs = listOf("Day", "Week", "Month")
                tabs.forEach { tab ->
                    val isSelected = selectedTab == tab
                    Text(
                        text = tab,
                        color = if (isSelected) barBackgroundColor else textSecondary,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) barColor else Color.Transparent)
                            .clickable { selectedTab = tab }
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            // Axa Y
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(end = 8.dp, bottom = 20.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.End
            ) {
                yAxisLabels.forEach { label ->
                    Text(text = label, color = textSecondary, fontSize = 10.sp)
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                currentData.forEachIndexed { index, fillPercentage ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.5f)
                                .weight(1f)
                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                .background(barBackgroundColor),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight(fillPercentage)
                                    .background(barColor)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = currentLabels.getOrElse(index) { "" },
                            color = textSecondary,
                            fontSize = 10.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun PreviewStatistics() {
    StatisticsScreen()
}