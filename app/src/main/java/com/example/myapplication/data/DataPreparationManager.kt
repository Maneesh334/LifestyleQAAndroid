package com.example.myapplication.data

import com.example.myapplication.SensorHandler
import com.example.myapplication.WeatherData
import com.example.myapplication.HealthData
import com.example.myapplication.QAPair
import org.json.JSONArray
import org.json.JSONObject

class DataPreparationManager {
    fun prepareDataToSend(
        height: Double?,
        weight: Double?,
        targetSteps: Int?,
        targetCalories: Double?,
        healthData: HealthData,
        weatherData: WeatherData,
        sensorHandler: SensorHandler,
        qaHistory: List<QAPair>,
        fitbitData: JSONObject
    ): JSONObject {
        return JSONObject().apply {
            // User profile data
            put("user_profile", JSONObject().apply {
                put("height_cm", height)
                put("weight_kg", weight)
                put("target_steps", targetSteps)
                put("target_calories", targetCalories)
            })

            // Health metrics
            put("health_metrics", JSONObject().apply {
                put("heart_rate_bpm", healthData.heartRate)
                put("body_temperature_celsius", healthData.bodyTemperature)
                put("steps_count", healthData.stepsLast24h)
                put("calories_burned", healthData.caloriesBurnedLast24h)
                put("blood_oxygen_percentage", healthData.bloodOxygen)
                put("glucose_mg_dl", healthData.glucoseLevels)
                put("heart_rate_variability_ms", healthData.hrv)
                put("sleep_stages", healthData.hypnogram)
                put("blood_pressure", healthData.bloodPressure)
            })

            // Weather data
            put("weather_data", JSONObject().apply {
                put("temperature_celsius", weatherData.currentTemperature)
                put("humidity_percentage", weatherData.humidity)
                put("precipitation_mm", weatherData.rainfall)
                put("snowfall_mm", weatherData.snowfall)
                put("air_quality_index", weatherData.aqi)
                put("air_quality_description", weatherData.getAQIDescription())
                put("uv_index", weatherData.uvIndex)
                put("wind_speed_ms", weatherData.windSpeed)
                put("wind_gust_ms", weatherData.windGust)
                put("pressure_hpa", weatherData.pressure)
            })

            // Fitbit data
            put("fitbit_data", fitbitData)

            // Android sensor data
            put("sensor_data", JSONObject().apply {
                for ((sensorName, values) in sensorHandler.sensorDataMap) {
                    val valuesArray = JSONArray()
                    values.forEach { valuesArray.put(it) }
                    put(sensorName, valuesArray)
                }
            })

            // Location data
            put("location", JSONObject().apply {
                put("latitude", 40.7128)  // Currently hardcoded, should be replaced with actual GPS data
                put("longitude", -74.0060)
            })

            // Q&A History
            put("qa_history", JSONArray().apply {
                qaHistory.forEach { qaPair ->
                    put(JSONObject().apply {
                        put("question", qaPair.question)
                        put("answer", qaPair.answer)
                    })
                }
            })
        }
    }
}
