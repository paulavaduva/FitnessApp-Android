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
import com.example.fitnessapp.viewmodel.StatisticsViewModel

@Composable
fun StatisticsScreen(statsVM: StatisticsViewModel) {
    val stepStats by statsVM.stepStatistics.collectAsState(initial = Triple(emptyMap(), emptyMap(), emptyMap()))
    val calorieStats by statsVM.calorieStatistics.collectAsState(initial = Triple(emptyMap(), emptyMap(), emptyMap()))

    val backgroundColor = Color(0xFF13151A)
    val surfaceColor = Color(0xFF1E2026)

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
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(vertical = 24.dp)
            )

            CustomBarChartCard(
                title = "Calorie Trends",
                dataMap = calorieStats.first,
                labelsMap = calorieStats.second,
                rawValuesMap = calorieStats.third,
                yAxisLabels = listOf("2500", "2000", "1500", "1000", "500", "0"),
                barColor = Color(0xFF4ADE80),
                surfaceColor = Color(0xFF1E2026)
            )

            Spacer(modifier = Modifier.height(24.dp))

            CustomBarChartCard(
                title = "Steps Trends",
                dataMap = stepStats.first,
                labelsMap = stepStats.second,
                rawValuesMap = stepStats.third,
                yAxisLabels = listOf("10k", "7.5k", "5k", "2.5k", "0"),
                barColor = Color(0xFF2DD4BF),
                surfaceColor = Color(0xFF1E2026)
            )
        }
    }
}

@Composable
fun CustomBarChartCard(
    title: String,
    dataMap: Map<String, List<Float>>,
    labelsMap: Map<String, List<String>>,
    rawValuesMap: Map<String, List<String>>,
    yAxisLabels: List<String>,
    barColor: Color,
    surfaceColor: Color
) {
    val barBackgroundColor = Color(0xFF2A2D36)
    val textSecondary = Color.Gray
    var selectedTab by remember { mutableStateOf("Week") }

    var selectedBarIndex by remember { mutableStateOf(-1) }

    val currentData = dataMap[selectedTab] ?: emptyList()
    val currentLabels = labelsMap[selectedTab] ?: emptyList()
    val currentRawValues = rawValuesMap[selectedTab] ?: emptyList()

    LaunchedEffect(selectedTab) { selectedBarIndex = -1 }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(surfaceColor)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(text = title, color = Color.White, fontWeight = FontWeight.SemiBold)
            Row(modifier = Modifier.clip(RoundedCornerShape(12.dp)).background(barBackgroundColor).padding(4.dp)) {
                listOf("Day", "Week", "Month").forEach { tab ->
                    val isSelected = selectedTab == tab
                    Text(
                        text = tab,
                        color = if (isSelected) barBackgroundColor else textSecondary,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) barColor else Color.Transparent)
                            .clickable { selectedTab = tab }
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        fontSize = 12.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(modifier = Modifier.fillMaxWidth().height(220.dp)) {
            Column(modifier = Modifier.fillMaxHeight().padding(end = 8.dp, bottom = 24.dp), verticalArrangement = Arrangement.SpaceBetween, horizontalAlignment = Alignment.End) {
                yAxisLabels.forEach { Text(it, color = textSecondary, fontSize = 10.sp) }
            }

            Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                currentData.forEachIndexed { index, fillPercentage ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedBarIndex = if (selectedBarIndex == index) -1 else index } // Toggle selecție
                    ) {
                        if (selectedBarIndex == index) {
                            Text(
                                text = currentRawValues.getOrElse(index) { "" },
                                color = barColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            Spacer(modifier = Modifier.height(14.dp))
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.5f)
                                .weight(1f)
                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                .background(if(selectedBarIndex == index) barColor.copy(alpha = 0.1f) else barBackgroundColor),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight(fillPercentage.coerceIn(0f, 1f))
                                    .background(barColor)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = currentLabels.getOrElse(index) { "" }, color = if(selectedBarIndex == index) Color.White else textSecondary, fontSize = 10.sp)
                    }
                }
            }
        }
    }
}
