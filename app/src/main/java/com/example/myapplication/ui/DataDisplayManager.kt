package com.example.myapplication.ui

import com.example.myapplication.HealthData
import com.example.myapplication.WeatherData

class DataDisplayManager(private val uiManager: UIManager) {

    fun displayWeatherData(weatherData: WeatherData) {
        with(uiManager) {
            // Format and display weather data with proper units
            textViewTemperature.setText(String.format("%.1f °C", weatherData.currentTemperature))
            textViewHumidity.setText("${weatherData.humidity}%")
            textViewRainfall.setText(String.format("%.1f mm/h", weatherData.rainfall))
            textViewSnowfall.setText(String.format("%.1f mm/h", weatherData.snowfall))
            textViewAQI.setText("${weatherData.aqi} - ${weatherData.getAQIDescription()}")
            textViewUVIndex.setText(String.format("%.1f", weatherData.uvIndex))
            textViewWindSpeed.setText(String.format("%.1f m/s", weatherData.windSpeed))
            textViewWindGust.setText(String.format("%.1f m/s", weatherData.windGust))
            textViewPressure.setText("${weatherData.pressure} hPa")

            // Disable all weather TextInputEditText fields
            textViewTemperature.isEnabled = false
            textViewHumidity.isEnabled = false
            textViewRainfall.isEnabled = false
            textViewSnowfall.isEnabled = false
            textViewAQI.isEnabled = false
            textViewUVIndex.isEnabled = false
            textViewWindSpeed.isEnabled = false
            textViewWindGust.isEnabled = false
            textViewPressure.isEnabled = false
        }
    }

    fun displayHealthData(healthData: HealthData) {
        with(uiManager) {
            textViewHeartRate.setText(healthData.heartRate.toString())
            textViewBodyTemp.setText(healthData.bodyTemperature.toString())
            textViewSteps.setText(healthData.stepsLast24h.toString())
            textViewCalories.setText(healthData.caloriesBurnedLast24h.toString())
            textViewBloodOxygen.setText(healthData.bloodOxygen.toString())
            textViewGlucose.setText(healthData.glucoseLevels.toString())
            textViewHRV.setText(healthData.hrv.toString())
            textViewHypnogram.setText(healthData.hypnogram)
            textViewBloodPressure.setText(healthData.bloodPressure)
        }
    }

    fun updateUIWithUserData(height: Double?, weight: Double?, targetSteps: Int?, targetCalories: Double?) {
        with(uiManager) {
            editTextHeight.setText(height?.toString())
            editTextWeight.setText(weight?.toString())
            editTextTargetSteps.setText(targetSteps?.toString())
            editTextTargetCalories.setText(targetCalories?.toString())
        }
    }
}
