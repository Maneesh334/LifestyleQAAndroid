package com.example.myapplication

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

class SensorHandler(
    private val context: Context,
    private val onSensorDataChanged: (() -> Unit)? = null
) : SensorEventListener {

    private val sensorManager: SensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val sensorList: List<Sensor> = sensorManager.getSensorList(Sensor.TYPE_ALL)
    val sensorDataMap = mutableMapOf<String, FloatArray>()
    val sensorDataList = mutableListOf<SensorData>()  // Added sensorDataList

    fun initializeSensors() {
        for (sensor in sensorList) {
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
            // Initialize sensorDataList with empty values
            sensorDataList.add(SensorData(sensor.name, FloatArray(0)))
        }
    }

    fun unregisterSensors() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        val sensorName = event.sensor.name
        val sensorValues = event.values.clone()

        // Update the map with the latest sensor values
        sensorDataMap[sensorName] = sensorValues

        // Update the corresponding SensorData in sensorDataList
        val sensorData = sensorDataList.find { it.name == sensorName }
        if (sensorData != null) {
            sensorData.values = sensorValues
            // Notify that sensor data has changed
            onSensorDataChanged?.invoke()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Optional: Handle changes in sensor accuracy
    }
}
