package com.example.myapplication

import kotlin.random.Random

class HealthDataGenerator {

    fun generateDynamicHealthData(): HealthData {
        val random = Random(System.currentTimeMillis())

        val heartRate = random.nextInt(60, 100).toDouble() // 60 - 100 bpm
        val bodyTemperature = String.format("%.1f", random.nextDouble(36.0, 37.5)).toDouble() // 36.0 - 37.5°C
        val steps = random.nextInt(5000, 15000) // 5,000 - 15,000 steps
        val caloriesBurned = String.format("%.1f", random.nextDouble(1800.0, 3500.0)).toDouble() // 1,800 - 3,500 kcal
        val bloodOxygen = String.format("%.1f", random.nextDouble(95.0, 100.0)).toDouble() // 95% - 100%
        val glucoseLevels = String.format("%.1f", random.nextDouble(70.0, 140.0)).toDouble() // 70 mg/dL - 140 mg/dL
        val hrv = String.format("%.1f", random.nextDouble(20.0, 120.0)).toDouble() // 20 ms - 120 ms
        val hypnogram = generateHypnogram(random)
        val bloodPressure = generateBloodPressure(random)

        return HealthData(
            heartRate = heartRate,
            bodyTemperature = bodyTemperature,
            stepsLast24h = steps,
            caloriesBurnedLast24h = caloriesBurned,
            bloodOxygen = bloodOxygen,
            glucoseLevels = glucoseLevels,
            hrv = hrv,
            hypnogram = hypnogram,
            bloodPressure = bloodPressure
        )
    }

    private fun generateHypnogram(random: Random): String {
        val stages = listOf("Awake", "Light", "Deep", "REM")
        val hypnogram = mutableListOf<String>()
        for (i in 1..8) { // Assume 8 hours of sleep
            hypnogram.add(stages[random.nextInt(stages.size)])
        }
        return hypnogram.joinToString(" -> ")
    }

    private fun generateBloodPressure(random: Random): String {
        val systolic = random.nextInt(90, 140) // 90 - 140 mmHg
        val diastolic = random.nextInt(60, 90) // 60 - 90 mmHg
        return "$systolic/$diastolic mmHg"
    }
}
