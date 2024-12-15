package com.example.myapplication

// Define a data class for health data
data class HealthData(
    var heartRate: Double,               // in bpm
    var bodyTemperature: Double,        // in Celsius
    var stepsLast24h: Int,
    var caloriesBurnedLast24h: Double,  // in kcal
    var bloodOxygen: Double,            // in %
    var glucoseLevels: Double,          // in mg/dL
    var hrv: Double,                    // in ms
    var hypnogram: String,              // Sleep stages representation
    var bloodPressure: String           // e.g., "120/80 mmHg"
)
