package com.example.myapplication

import android.content.Context
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import org.json.JSONObject
import java.io.IOException

class WeatherService(private val context: Context) {
    private val WEATHER_URL = "https://api.open-meteo.com/v1/forecast"
    private val AIR_QUALITY_URL = "https://air-quality-api.open-meteo.com/v1/air-quality"
    private val client = OkHttpClient()

    fun fetchWeatherData(latitude: Double, longitude: Double, callback: (WeatherData?, String?) -> Unit) {
        val weatherUrl = WEATHER_URL.toHttpUrlOrNull()?.newBuilder()
            ?.addQueryParameter("latitude", latitude.toString())
            ?.addQueryParameter("longitude", longitude.toString())
            ?.addQueryParameter("current", "temperature_2m,relative_humidity_2m,precipitation,snowfall,pressure_msl,wind_speed_10m,wind_gusts_10m")
            ?.addQueryParameter("timeformat", "unixtime")
            ?.addQueryParameter("timezone", "auto")
            ?.build()
            ?.toString() ?: return

        val airQualityUrl = AIR_QUALITY_URL.toHttpUrlOrNull()?.newBuilder()
            ?.addQueryParameter("latitude", latitude.toString())
            ?.addQueryParameter("longitude", longitude.toString())
            ?.addQueryParameter("current", "european_aqi,uv_index")
            ?.addQueryParameter("timeformat", "unixtime")
            ?.addQueryParameter("timezone", "auto")
            ?.build()
            ?.toString() ?: return

        var weatherData: JSONObject? = null
        var airQualityData: JSONObject? = null

        // Fetch weather data
        client.newCall(Request.Builder().url(weatherUrl).build()).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(null, "Failed to fetch weather data: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    callback(null, "Weather API error: ${response.code}")
                    return
                }

                try {
                    weatherData = JSONObject(response.body?.string() ?: "")
                    if (airQualityData != null) {
                        processData(weatherData!!, airQualityData!!, callback)
                    }
                } catch (e: Exception) {
                    callback(null, "Error parsing weather data: ${e.message}")
                }
            }
        })

        // Fetch air quality data
        client.newCall(Request.Builder().url(airQualityUrl).build()).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(null, "Failed to fetch air quality data: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    callback(null, "Air quality API error: ${response.code}")
                    return
                }

                try {
                    airQualityData = JSONObject(response.body?.string() ?: "")
                    if (weatherData != null) {
                        processData(weatherData!!, airQualityData!!, callback)
                    }
                } catch (e: Exception) {
                    callback(null, "Error parsing air quality data: ${e.message}")
                }
            }
        })
    }

    private fun processData(weatherJson: JSONObject, airQualityJson: JSONObject, callback: (WeatherData?, String?) -> Unit) {
        try {
            val current = weatherJson.getJSONObject("current")
            val currentAirQuality = airQualityJson.getJSONObject("current")

            val weatherData = WeatherData(
                currentTemperature = current.getDouble("temperature_2m"),
                humidity = current.getInt("relative_humidity_2m"),
                rainfall = current.getDouble("precipitation"),
                snowfall = current.getDouble("snowfall"),
                aqi = currentAirQuality.getInt("european_aqi"),
                uvIndex = currentAirQuality.getDouble("uv_index"),
                windSpeed = current.getDouble("wind_speed_10m"),
                windGust = current.getDouble("wind_gusts_10m"),
                pressure = current.getInt("pressure_msl")
            )
            
            callback(weatherData, null)
        } catch (e: Exception) {
            callback(null, "Error processing data: ${e.message}")
        }
    }
}
