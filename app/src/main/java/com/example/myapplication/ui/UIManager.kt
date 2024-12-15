package com.example.myapplication.ui

import android.app.Activity
import com.google.android.material.textfield.TextInputEditText
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R

class UIManager(private val activity: Activity) {
    // UI elements
    lateinit var recyclerViewSensors: RecyclerView
    lateinit var btnSendData: Button
    lateinit var editTextHeight: TextInputEditText
    lateinit var editTextWeight: TextInputEditText
    lateinit var editTextTargetSteps: TextInputEditText
    lateinit var editTextTargetCalories: TextInputEditText

    // UI elements for health data display
    lateinit var textViewHeartRate: TextInputEditText
    lateinit var textViewBodyTemp: TextInputEditText
    lateinit var textViewSteps: TextInputEditText
    lateinit var textViewCalories: TextInputEditText
    lateinit var textViewBloodOxygen: TextInputEditText
    lateinit var textViewGlucose: TextInputEditText
    lateinit var textViewHRV: TextInputEditText
    lateinit var textViewHypnogram: TextInputEditText
    lateinit var textViewBloodPressure: TextInputEditText

    // UI elements for weather data display
    lateinit var textViewTemperature: TextInputEditText
    lateinit var textViewHumidity: TextInputEditText
    lateinit var textViewRainfall: TextInputEditText
    lateinit var textViewSnowfall: TextInputEditText
    lateinit var textViewAQI: TextInputEditText
    lateinit var textViewUVIndex: TextInputEditText
    lateinit var textViewWindSpeed: TextInputEditText
    lateinit var textViewWindGust: TextInputEditText
    lateinit var textViewPressure: TextInputEditText

    lateinit var editTextQuestion: TextInputEditText
    lateinit var btnAskQuestion: Button
    lateinit var recyclerViewQA: RecyclerView

    fun initializeUIElements() {
        recyclerViewSensors = activity.findViewById(R.id.recyclerViewSensors)
        btnSendData = activity.findViewById(R.id.btnSendData)
        editTextHeight = activity.findViewById(R.id.editTextHeight)
        editTextWeight = activity.findViewById(R.id.editTextWeight)
        editTextTargetSteps = activity.findViewById(R.id.editTextTargetSteps)
        editTextTargetCalories = activity.findViewById(R.id.editTextTargetCalories)

        // Initialize health data UI elements
        textViewHeartRate = activity.findViewById(R.id.textViewHeartRate)
        textViewBodyTemp = activity.findViewById(R.id.textViewBodyTemp)
        textViewSteps = activity.findViewById(R.id.textViewSteps)
        textViewCalories = activity.findViewById(R.id.textViewCalories)
        textViewBloodOxygen = activity.findViewById(R.id.textViewBloodOxygen)
        textViewGlucose = activity.findViewById(R.id.textViewGlucose)
        textViewHRV = activity.findViewById(R.id.textViewHRV)
        textViewHypnogram = activity.findViewById(R.id.textViewHypnogram)
        textViewBloodPressure = activity.findViewById(R.id.textViewBloodPressure)

        // Initialize weather data UI elements
        textViewTemperature = activity.findViewById(R.id.textViewTemperature)
        textViewHumidity = activity.findViewById(R.id.textViewHumidity)
        textViewRainfall = activity.findViewById(R.id.textViewRainfall)
        textViewSnowfall = activity.findViewById(R.id.textViewSnowfall)
        textViewAQI = activity.findViewById(R.id.textViewAQI)
        textViewUVIndex = activity.findViewById(R.id.textViewUVIndex)
        textViewWindSpeed = activity.findViewById(R.id.textViewWindSpeed)
        textViewWindGust = activity.findViewById(R.id.textViewWindGust)
        textViewPressure = activity.findViewById(R.id.textViewPressure)

        // Initialize Q&A section
        editTextQuestion = activity.findViewById(R.id.editTextQuestion)
        btnAskQuestion = activity.findViewById(R.id.btnAskQuestion)
        recyclerViewQA = activity.findViewById(R.id.recyclerViewQA)
        //textViewAnswer = activity.findViewById(R.id.textViewAnswer)
    }
}
