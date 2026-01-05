package com.example.weather

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.delay

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun WeatherScreen(
    modifier: Modifier = Modifier,
    predefinedCities: List<CityWeather> = PredefinedCities.cities,
    allCityNames: Set<String> = PredefinedCities.allCities
) {
    // ✅ FIXED: Start with only first 6 cities
    var availableCities by remember {
        mutableStateOf(predefinedCities.take(6).toMutableList())
    }
    var selectedCityIndex by remember { mutableStateOf(0) }
    var selectedMetricIndex by remember { mutableStateOf(0) }
    var showDialog by remember { mutableStateOf(false) }
    var selectedAddCity by remember { mutableStateOf<String?>(null) }

    // 🔁 Auto-switch logic
    val metricCount = 5
    val autoSwitchInterval = 2000L
    val pauseDuration = 3000L
    var lastManualTime by remember { mutableStateOf(0L) }
    val isAutoSwitchEnabled = (System.currentTimeMillis() - lastManualTime) > pauseDuration
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(selectedCityIndex, isAutoSwitchEnabled, lifecycleOwner.lifecycle) {
        if (!isAutoSwitchEnabled) return@LaunchedEffect
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            while (isAutoSwitchEnabled) {
                delay(autoSwitchInterval)
                selectedMetricIndex = (selectedMetricIndex + 1) % metricCount
            }
        }
    }

    fun onMetricClick(index: Int) {
        selectedMetricIndex = index
        lastManualTime = System.currentTimeMillis()
    }

    val currentCity = availableCities[selectedCityIndex]
    val metrics = listOf("温度", "湿度", "气压", "天气", "风速")
    val metricValues = listOf(
        "${currentCity.data.temperature}°C",
        "${currentCity.data.humidity}%",
        "${currentCity.data.pressure} hPa",
        currentCity.data.condition,
        "${currentCity.data.windSpeed} m/s"
    )

    // City addition dialog
    if (showDialog) {
        // ✅ FIXED: Compute addable cities correctly
        val addableCities = allCityNames - availableCities.map { it.cityName }.toSet()
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Add City") },
            text = {
                Column {
                    Text("Choose a city to add:", modifier = Modifier.padding(bottom = 8.dp))
                    if (addableCities.isEmpty()) {
                        Text("No more cities to add.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    } else {
                        LazyColumn {
                            items(addableCities.toList()) { city ->
                                Text(
                                    text = city,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { selectedAddCity = city }
                                        .background(
                                            if (selectedAddCity == city) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                                        )
                                        .padding(12.dp),
                                    color = if (selectedAddCity == city) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        selectedAddCity?.let { city ->
                            predefinedCities.firstOrNull { it.cityName == city }?.also { newCity ->
                                availableCities = availableCities.toMutableList().apply { add(newCity) }
                                // Auto-select the newly added city
                                selectedCityIndex = availableCities.lastIndex
                                selectedMetricIndex = 0
                            }
                        }
                        showDialog = false
                        selectedAddCity = null
                    },
                    enabled = selectedAddCity != null
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Top: City Buttons + Add Button
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "城市",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add city")
            }
        }

        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(availableCities, key = { it.cityName }) { city ->
                val isSelected = city.cityName == currentCity.cityName
                Card(
                    modifier = Modifier
                        .height(48.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .clickable {
                            selectedCityIndex = availableCities.indexOf(city)
                            selectedMetricIndex = 0
                        },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                    ),
                    elevation = CardDefaults.cardElevation(if (isSelected) 4.dp else 1.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(
                            text = city.cityName,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Main Metric Display
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            when (currentCity.data.condition.lowercase()) {
                                "sunny" -> com.example.weather.ui.theme.WarmYellow
                                "rainy", "thunderstorm" -> Color(0xFF4A6FA5)
                                "cloudy", "overcast", "foggy" -> Color(0xFFA9A9A9)
                                "windy" -> Color(0xFF87CEEB)
                                else -> com.example.weather.ui.theme.SkyBlue
                            }.copy(alpha = 0.3f),
                            Color.Transparent
                        )
                    )
                )
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AnimatedContent(
                    targetState = selectedMetricIndex,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(300)) with fadeOut(animationSpec = tween(300))
                    }
                ) { index ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = metrics[index],
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = metricValues[index],
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Metric selector dots
                Row(
                    horizontalArrangement = Arrangement.Center
                ) {
                    metrics.forEachIndexed { i, _ ->
                        Box(
                            modifier = Modifier
                                .size(if (i == selectedMetricIndex) 12.dp else 8.dp)
                                .clip(RoundedCornerShape(50))
                                .background(
                                    if (i == selectedMetricIndex) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                )
                                .clickable { onMetricClick(i) }
                                .padding(2.dp)
                        )
                        if (i < metrics.size - 1) Spacer(modifier = Modifier.width(6.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Bottom: Full Weather Summary
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "${currentCity.cityName}天气状况",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(12.dp))
                WeatherDetailRow("🌡️ 温度", "${currentCity.data.temperature}°C")
                WeatherDetailRow("💧 湿度", "${currentCity.data.humidity}%")
                WeatherDetailRow("🔽 气压", "${currentCity.data.pressure} hPa")
                WeatherDetailRow("☁️ 天气", currentCity.data.condition)
                WeatherDetailRow("💨 风速", "${currentCity.data.windSpeed} m/s")
            }
        }
    }
}

@Composable
fun WeatherDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = value,
            textAlign = TextAlign.End,
            fontWeight = FontWeight.Normal,
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
    Spacer(modifier = Modifier.height(6.dp))
}