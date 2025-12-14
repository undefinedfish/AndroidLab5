package com.example.androidlab1.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun ConsumptionControl(
    consumption: Double,
    onChange: (Double) -> Unit
) {
    Column {
        Text("Місячне споживання: ${String.format("%.0f", consumption)} кВт·год")
        Slider(
            value = consumption.toFloat(),
            onValueChange = { onChange(it.toDouble()) },
            valueRange = 0f..500f,
            steps = 99
        )
    }
}