package com.example.androidlab1

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.androidlab1.model.*

@Composable
fun SmartMeterApp() {
    var inputText by remember { mutableStateOf("150") }
    var selectedTariff by remember { mutableStateOf(Tariff.DAY) }
    var message by remember { mutableStateOf("") }

    val meter = remember {
        SmartElectricityMeter(
            meterId = "SM-2025-001",
            initialReading = 1250.0,
            maxPower = 8000.0
        )
    }

    LaunchedEffect(selectedTariff) {
        meter.changeTariff(selectedTariff)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Практична 4: Generics, Exceptions, Extensions",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        OutlinedTextField(
            value = inputText,
            onValueChange = { inputText = it },
            label = { Text("Місячне споживання (кВт·год)") },
            supportingText = { Text("Наприклад: 150, 450.5") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                val result = meter.updateConsumptionSafely(inputText)
                result.onSuccess { value ->
                    message = "Успішно оновлено: $value кВт·год"
                }.onFailure { ex ->
                    message = when (ex) {
                        is OverloadException -> "ПЕРЕВАНТАЖЕННЯ! Потужність перевищує 8000 Вт"
                        is InvalidConsumptionException -> ex.message ?: "Невірне значення"
                        else -> "Помилка обробки даних"
                    }
                }
            },
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Оновити споживання")
        }

        if (message.isNotEmpty()) {
            Text(
                text = message,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        ElevatedCard {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Лічильник: ${meter.meterId}", style = MaterialTheme.typography.titleMedium)
                Text("Загальне споживання: ${String.format("%.2f", meter.totalConsumption)} кВт·год")
                Text("Поточний тариф: ${meter.currentTariff.displayName}")
                Text("Вартість: ${String.format("%.2f", meter.calculateCost())} грн")
                if (meter.isOverload()) {
                    Text("УВАГА: ПЕРЕВАНТАЖЕННЯ!", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            }
        }

        TariffSelector(selected = selectedTariff) { selectedTariff = it }

        HistoryCard(
            title = "Історія споживання (останні 5)",
            items = meter.consumptionHistory.getAll().takeLast(5)
        )

        HistoryCard(
            title = "Історія тарифів",
            items = meter.tariffHistory.getAll().takeLast(5)
        )
    }
}

@Composable
fun TariffSelector(selected: Tariff, onSelect: (Tariff) -> Unit) {
    Column {
        Text("Оберіть тариф:", fontWeight = FontWeight.Medium, modifier = Modifier.padding(bottom = 8.dp))
        Tariff.values().forEach { tariff ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                RadioButton(
                    selected = selected == tariff,
                    onClick = { onSelect(tariff) }
                )
                Text("${tariff.displayName} — ${tariff.pricePerKWh} грн/кВт·год")
            }
        }
    }
}

@Composable
fun HistoryCard(title: String, items: List<HistoryItem<*>>) {
    ElevatedCard(modifier = Modifier.padding(vertical = 4.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(8.dp))
            if (items.isEmpty()) {
                Text("Немає записів", color = MaterialTheme.colorScheme.outline)
            } else {
                items.forEach { item ->
                    Text("• $item", fontSize = 12.sp)
                }
            }
        }
    }
}