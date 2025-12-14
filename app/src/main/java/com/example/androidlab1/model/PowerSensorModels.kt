package com.example.androidlab1.model

import kotlinx.coroutines.*
import kotlin.random.Random

enum class EnergySensorType(val displayName: String, val unit: String) {
    VOLTAGE("Напруга", "В"),
    CURRENT("Струм", "А"),
    POWER("Потужність", "Вт"),
    ENERGY("Енергія", "кВт·год"),
    TEMPERATURE("Температура обладнання", "°C")
}

data class SensorData(
    val sensorId: String,
    val type: EnergySensorType,
    val value: Double,
    val timestamp: Long = System.currentTimeMillis(),
    val isAnomaly: Boolean = false
)

class EnergySensor(
    val id: String,
    val type: EnergySensorType,
    private val scope: CoroutineScope
) {
    private var job: Job? = null
    private val subscribers = mutableSetOf<(SensorData) -> Unit>()

    private val normalRanges: Map<EnergySensorType, ClosedFloatingPointRange<Double>> = mapOf(
        EnergySensorType.VOLTAGE to 210.0..240.0,
        EnergySensorType.CURRENT to 0.0..40.0,
        EnergySensorType.POWER to 0.0..9000.0,
        EnergySensorType.ENERGY to 0.0..500.0,
        EnergySensorType.TEMPERATURE to 20.0..60.0
    )

    fun startGenerating(delayMs: Long = 1500) {
        job = scope.launch {
            while (isActive) {
                val value = generateRandomValue(type)
                val range = normalRanges[type] ?: (0.0..100.0)
                val isAnomaly = value !in range
                val data = SensorData(id, type, value, isAnomaly = isAnomaly)

                subscribers.forEach { it(data) }

                delay(delayMs)
            }
        }
    }

    fun stop() {
        job?.cancel()
    }

    fun subscribe(listener: (SensorData) -> Unit) {
        subscribers.add(listener)
    }

    fun unsubscribe(listener: (SensorData) -> Unit) {
        subscribers.remove(listener)
    }

    private fun generateRandomValue(type: EnergySensorType): Double = when (type) {
        EnergySensorType.VOLTAGE -> 220.0 + Random.nextDouble(-20.0, 20.0)
        EnergySensorType.CURRENT -> Random.nextDouble(5.0, 35.0)
        EnergySensorType.POWER -> Random.nextDouble(1000.0, 8000.0)
        EnergySensorType.ENERGY -> Random.nextDouble(50.0, 450.0)
        EnergySensorType.TEMPERATURE -> 30.0 + Random.nextDouble(-15.0, 30.0)
    }
}