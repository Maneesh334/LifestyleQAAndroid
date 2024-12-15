package com.example.myapplication

/**
 * Data class representing weather information from Open-Meteo API
 * All measurements are in metric units
 */
data class WeatherData(
    val currentTemperature: Double,   // Temperature in Celsius
    val humidity: Int,                // Humidity percentage (0-100)
    val rainfall: Double,             // Precipitation in mm (last hour)
    val snowfall: Double,             // Snowfall in mm (last hour)
    val aqi: Int,                     // European AQI (0-100+)
    val uvIndex: Double,              // UV Index (0-11+)
    val windSpeed: Double,            // Wind speed in meters per second
    val windGust: Double,             // Wind gust in meters per second
    val pressure: Int                 // Atmospheric pressure in hPa
) {
    /**
     * Returns a human-readable description of the Air Quality Index based on European AQI standard
     */
    fun getAQIDescription(): String = when {
        aqi <= 20 -> "Good"
        aqi <= 40 -> "Fair"
        aqi <= 60 -> "Moderate"
        aqi <= 80 -> "Poor"
        aqi <= 100 -> "Very Poor"
        else -> "Extremely Poor"
    }
}
