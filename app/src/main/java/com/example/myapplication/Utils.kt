package com.example.myapplication

fun getAQIDescription(aqi: Int): String {
    return when (aqi) {
        1 -> "Good"
        2 -> "Fair"
        3 -> "Moderate"
        4 -> "Poor"
        5 -> "Very Poor"
        else -> "Unknown"
    }
}
