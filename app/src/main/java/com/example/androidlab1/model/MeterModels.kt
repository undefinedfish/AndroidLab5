package com.example.androidlab1.model

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

enum class Tariff(val displayName: String, val pricePerKWh: Double) {
    DAY("Денний (07:00–23:00)", 2.64),
    NIGHT("Нічний (23:00–07:00)", 1.32),
    WEEKEND("Вихідні та свята", 1.85)
}

interface ElectricityMeter {
    val meterId: String
    var totalConsumption: Double
    fun getCurrentReading(): Double
    fun reset()
}

interface SmartFeatures {
    val isConnected: Boolean
    val maxPower: Double
    fun getCurrentPower(): Double
    fun isOverload(): Boolean
}

abstract class BaseMeter(
    override val meterId: String,
    private val initialReading: Double = 0.0
) : ElectricityMeter {

    override var totalConsumption: Double = initialReading
        set

    override fun getCurrentReading(): Double = totalConsumption

    override fun reset() {
        totalConsumption = 0.0
    }

    abstract fun getMeterType(): String
}

class HistoryContainer<T>(private val maxSize: Int = 50) {
    private val items = mutableListOf<HistoryItem<T>>()

    fun add(value: T, description: String = "Зміна") {
        if (items.size >= maxSize) items.removeAt(0)
        items.add(HistoryItem(value, description, LocalDateTime.now()))
    }

    fun getAll(): List<HistoryItem<T>> = items.toList()
    fun getLast(): HistoryItem<T>? = items.lastOrNull()
}

data class HistoryItem<T>(
    val value: T,
    val description: String,
    val timestamp: LocalDateTime
) {
    private val formatter = DateTimeFormatter.ofPattern("dd.MM HH:mm")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun toString(): String = "${formatter.format(timestamp)} — $description: $value"
}

class InvalidConsumptionException(message: String) : Exception(message)
class OverloadException(val currentPower: Double, val limit: Double) : Exception(
    "ПЕРЕВАНТАЖЕННЯ! $currentPower Вт > $limit Вт"
)

class SmartElectricityMeter(
    override val meterId: String,
    private val initialReading: Double = 0.0,
    override val maxPower: Double
) : BaseMeter(meterId, initialReading), SmartFeatures {

    override var isConnected: Boolean = true
    var currentTariff: Tariff = Tariff.DAY
    private var currentPowerUsage: Double = 0.0

    val consumptionHistory: HistoryContainer<Double> by lazy { HistoryContainer(30) }
    val tariffHistory: HistoryContainer<Tariff> by lazy { HistoryContainer(20) }

    override fun getCurrentPower() = currentPowerUsage
    override fun isOverload() = currentPowerUsage > maxPower

    override fun getMeterType() = "Розумний лічильник"

    fun updateConsumptionSafely(input: String?): Result<Double> = runCatching {
        val text = input?.trim() ?: throw InvalidConsumptionException("Поле порожнє")

        val value = text.toSafeDouble()
            ?: throw InvalidConsumptionException("Введіть коректне число")

        if (value < 0) throw InvalidConsumptionException("Значення не може бути від'ємним")
        if (value > 1000) throw InvalidConsumptionException("Занадто велике значення")

        currentPowerUsage = (value * 1000) / (30 * 24)
        totalConsumption = initialReading + value

        if (isOverload()) throw OverloadException(currentPowerUsage, maxPower)

        consumptionHistory.add(value, "Оновлено споживання")
        value
    }

    fun changeTariff(newTariff: Tariff) {
        currentTariff = newTariff
        tariffHistory.add(newTariff, "Зміна тарифу")
    }

    fun calculateCost(): Double = totalConsumption * currentTariff.pricePerKWh
}

fun String.toSafeDouble(): Double? =
    this.replace(",", ".").trim().toDoubleOrNull()