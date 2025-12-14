package com.example.androidlab1

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.androidlab1.model.*
import kotlinx.coroutines.launch

@Composable
fun EnergyMonitorApp() {
    val scope = rememberCoroutineScope()

    val sensors = remember {
        listOf(
            EnergySensor("SENS-V01", EnergySensorType.VOLTAGE, scope),
            EnergySensor("SENS-I01", EnergySensorType.CURRENT, scope),
            EnergySensor("SENS-P01", EnergySensorType.POWER, scope),
            EnergySensor("SENS-E01", EnergySensorType.ENERGY, scope),
            EnergySensor("SENS-T01", EnergySensorType.TEMPERATURE, scope)
        )
    }

    var allReadings by remember { mutableStateOf<List<SensorData>>(emptyList()) }

    val anomalyCount by remember(allReadings) {
        derivedStateOf { allReadings.count { it.isAnomaly } }
    }

    LaunchedEffect(Unit) {
        sensors.forEach { sensor ->
            sensor.subscribe { data ->
                scope.launch {
                    allReadings = (allReadings + data)
                        .sortedByDescending { it.timestamp }
                        .takeLast(30)
                }
            }
            sensor.startGenerating(1500)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            sensors.forEach { it.stop() }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A2A0A))
            .padding(16.dp)
    ) {
        Text(
            text = "Система моніторингу енерговитрат",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF4CAF50)
        )

        Spacer(Modifier.height(12.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1B5E20)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Активні сенсори: ${sensors.size}", color = Color.White)
                if (anomalyCount > 0) {
                    Text("Аномалії: $anomalyCount", color = Color(0xFFFF5252), fontWeight = FontWeight.Bold)
                } else {
                    Text("Усі параметри в нормі", color = Color(0xFF8BC34A))
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(allReadings) { reading ->
                EnergySensorCard(reading)
            }
        }
    }
}

@Composable
fun EnergySensorCard(reading: SensorData) {
    val backgroundColor = if (reading.isAnomaly) Color(0xFFB71C1C) else Color(0xFF2E7D32)

    Card(
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${reading.sensorId} — ${reading.type.displayName}",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${String.format("%.2f", reading.value)} ${reading.type.unit}",
                    color = Color(0xFFB2FAB4),
                    fontSize = 16.sp
                )
            }
            Text(
                text = if (reading.isAnomaly) "АНОМАЛІЯ" else "Норма",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
}