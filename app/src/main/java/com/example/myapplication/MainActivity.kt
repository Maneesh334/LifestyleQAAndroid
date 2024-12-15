package com.example.myapplication

import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.myapplication.data.DataPreparationManager
import com.example.myapplication.permissions.PermissionManager
import com.example.myapplication.ui.DataDisplayManager
import com.example.myapplication.ui.UIManager
import org.json.JSONObject
import android.app.TimePickerDialog
import android.widget.Button
import android.widget.TextView
import java.util.Calendar
import com.example.myapplication.notification.NotificationManager
import com.example.myapplication.notification.NotificationTimeAdapter
import com.example.myapplication.notification.NotificationTimeUtils
import com.example.myapplication.notification.NotificationDataHandler
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import com.example.myapplication.notification.QuestionManager
import android.widget.ScrollView
import com.example.myapplication.api.FitbitService

class MainActivity : AppCompatActivity() {

    private lateinit var sensorHandler: SensorHandler
    private lateinit var weatherService: WeatherService
    private lateinit var userDataManager: UserDataManager
    private lateinit var networkManager: NetworkManager
    private lateinit var healthDataGenerator: HealthDataGenerator
    private lateinit var inputValidator: InputValidator
    private lateinit var uiManager: UIManager
    private lateinit var dataDisplayManager: DataDisplayManager
    private lateinit var permissionManager: PermissionManager
    private lateinit var dataPreparationManager: DataPreparationManager
    private lateinit var sensorAdapter: SensorAdapter
    private lateinit var recyclerViewQA: RecyclerView
    private lateinit var qaAdapter: QAAdapter
    lateinit var notificationManager: NotificationManager
    lateinit var notificationAdapter: NotificationTimeAdapter
    private lateinit var notificationTimeUtils: NotificationTimeUtils
    private lateinit var notificationDataHandler: NotificationDataHandler
    private lateinit var fitbitService: FitbitService
    private var qaList: MutableList<QAPair> = mutableListOf()


    var height: Double? = null
    var weight: Double? = null
    var targetSteps: Int? = null
    var targetCalories: Double? = null

    private var isDataSent = false

    companion object {
        private const val PERMISSION_REQUEST_CODE = 123
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        notificationDataHandler = NotificationDataHandler(this)
        fitbitService = FitbitService(this)
        // Handle notification click data if present
        notificationDataHandler.handleNotificationData(intent)

        // Request notifications permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) 
                != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    PERMISSION_REQUEST_CODE
                )
            }
        }

        // For Android 12+, prompt user to enable exact alarms if needed
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                Intent().also { intent ->
                    intent.action = android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
                    startActivity(intent)
                }
            }
        }

        initializeManagers()
        setupUI()
        loadUserData()
        setupClickListeners()
        notificationTimeUtils.setupNotificationTimeSelection()

        if (!permissionManager.hasPermissions()) {
            permissionManager.requestPermissions()
        } else {
            sensorHandler.initializeSensors()
        }
    }

    private fun initializeManagers() {
        uiManager = UIManager(this)
        dataDisplayManager = DataDisplayManager(uiManager)
        permissionManager = PermissionManager(this)
        dataPreparationManager = DataPreparationManager()
        sensorHandler = SensorHandler(this)
        weatherService = WeatherService(this)
        userDataManager = UserDataManager(this)
        networkManager = NetworkManager()
        healthDataGenerator = HealthDataGenerator()
        inputValidator = InputValidator()
        notificationManager = NotificationManager(this)
        notificationTimeUtils = NotificationTimeUtils(this, notificationManager)
    }

    private fun setupUI() {
        uiManager.initializeUIElements()
        sensorAdapter = SensorAdapter(sensorHandler.sensorDataList)
        uiManager.recyclerViewSensors.layoutManager = LinearLayoutManager(this)
        uiManager.recyclerViewSensors.adapter = sensorAdapter

        // Initialize RecyclerView for Q&A
        recyclerViewQA = findViewById(R.id.recyclerViewQA)
        qaAdapter = QAAdapter(qaList)
        recyclerViewQA.layoutManager = LinearLayoutManager(this).apply {
            reverseLayout = true
            stackFromEnd = true
        }
        recyclerViewQA.adapter = qaAdapter
    }

    private fun loadUserData() {
        val userData = userDataManager.loadUserData()
        height = userData.height
        weight = userData.weight
        targetSteps = userData.targetSteps
        targetCalories = userData.targetCalories
        dataDisplayManager.updateUIWithUserData(height, weight, targetSteps, targetCalories)
    }

    private fun setupClickListeners() {
        uiManager.btnSendData.setOnClickListener {
            handleSendDataClick()
        }

        uiManager.btnAskQuestion.setOnClickListener {
            handleAskQuestionClick()
        }
    }

    private fun handleSendDataClick() {
        if (inputValidator.validateInputs(this, uiManager)) {
            userDataManager.saveUserData(height, weight, targetSteps, targetCalories)
            fetchAndProcessData()
        }
    }

    private fun handleAskQuestionClick() {
        userDataManager.saveUserData(height, weight, targetSteps, targetCalories)
        val question = uiManager.editTextQuestion.text.toString()
        fetchAndProcessData(question)
    }

    private fun fetchAndProcessData(question: String? = null) {
        val latitude = 40.7128
        val longitude = -74.0060

        // First fetch Fitbit data
        fitbitService.fetchFitbitData(
            onSuccess = { fitbitData ->
                // Then fetch weather data
                weatherService.fetchWeatherData(latitude, longitude) { weatherData, error ->
                    if (weatherData != null) {
                        runOnUiThread {
                            dataDisplayManager.displayWeatherData(weatherData)
                            val healthData = healthDataGenerator.generateDynamicHealthData()
                            dataDisplayManager.displayHealthData(healthData)

                            val jsonObject = dataPreparationManager.prepareDataToSend(
                                height, weight, targetSteps, targetCalories,
                                healthData, weatherData, sensorHandler, qaList,
                                fitbitData
                            )

                            if (question != null) {
                                sendQuestionToServer(question, jsonObject)
                            } else {
                                sendDataToServer(jsonObject)
                            }
                        }
                    } else {
                        runOnUiThread {
                            Toast.makeText(this, "Failed to fetch weather data: $error", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            },
            onError = { error ->
                runOnUiThread {
                    Toast.makeText(this, "Failed to fetch Fitbit data: $error", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    private fun sendDataToServer(jsonObject: JSONObject) {
        networkManager.sendDataToServer("https://abcdf.free.beeceptor.com", jsonObject) { success, message ->
            runOnUiThread {
                if (success) {
                    Toast.makeText(this, "Data sent successfully", Toast.LENGTH_SHORT).show()
                    isDataSent = true
                } else {
                    Toast.makeText(this, "Failed to send data: $message", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun sendQuestionToServer(question: String, data: JSONObject) {
        val requestData = JSONObject()
        try {
            var qcontext = "Below are examples of how to respond to user queries. Each example includes a hidden reasoning step. The hidden reasoning is never to be revealed to the user. It is shown here only for demonstration. Follow the same pattern for the new query at the end.\n" +
                    "\n" +
                    "[BEGIN EXAMPLE 1]\n" +
                    "User: \"How am I doing on my step goal today?\"\n" +
                    "[THOUGHT PROCESS]:\n" +
                    "- Check step goal: 10,000 steps/day.\n" +
                    "- Current steps from sensor: 6,200 steps.\n" +
                    "- 6,200/10,000 = 62% of goal reached.\n" +
                    "- Still have time to walk more to hit the target.\n" +
                    "[END THOUGHT PROCESS]\n" +
                    "Assistant: \"You’ve hit about 6,200 steps so far, which puts you a little over halfway to your 10,000-step goal. Keep up the good work—another short walk later could help you reach it!\"\n" +
                    "[END EXAMPLE 1]\n" +
                    "\n" +
                    "[BEGIN EXAMPLE 2]\n" +
                    "User: \"What should I wear today?\"\n" +
                    "[THOUGHT PROCESS]:\n" +
            "- Check sensor data:\n" +
            "- temperature_celsius: 1.3°C (quite cold)\n" +
            "- wind_speed_ms: 26.3 m/s (extremely windy)\n" +
            "- precipitation: 0 mm, snowfall: 0 mm\n" +
            "- user health: body_temperature_celsius: 37.1°C (normal), heart_rate_bpm: 69 (normal), no fever, steps_count high (user active).\n" +
            "- Conditions: Very cold and extremely windy. Need strong wind protection and warmth.\n" +
            "- User is healthy but conditions are harsh. Suggest thermal base layers, insulated jacket, windproof shell, warm pants, gloves, and a hat.\n" +
            "[END THOUGHT PROCESS]\n" +
            "Assistant: \"It’s very cold and extremely windy outside. Wear multiple layers, starting with a thermal base layer and adding an insulated jacket. Choose windproof outerwear to protect against the gusts, and consider warm pants, gloves, and a hat. These layers will help keep you comfortable and safe in the harsh conditions.\"\n" +
                    "[END EXAMPLE 2]\n"
            "Do not reveal the thought process at any cost! Thought process is the text that comes after [THOUGHT PROCESS] \n"
            requestData.put("question", question)
            requestData.put("data", data)
            val calendar = Calendar.getInstance()
            val timestamp = calendar.timeInMillis
            val dayOfWeek = when (calendar.get(Calendar.DAY_OF_WEEK)) {
                Calendar.SUNDAY -> "Sunday"
                Calendar.MONDAY -> "Monday"
                Calendar.TUESDAY -> "Tuesday"
                Calendar.WEDNESDAY -> "Wednesday"
                Calendar.THURSDAY -> "Thursday"
                Calendar.FRIDAY -> "Friday"
                Calendar.SATURDAY -> "Saturday"
                else -> "Unknown"
            }
            requestData.put("timestamp",timestamp)
            requestData.put("dayOfWeek", dayOfWeek)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val url = "https://7549-34-134-86-113.ngrok-free.app/api2"
        val request = JsonObjectRequest(
            Request.Method.POST, url, requestData,
            { response ->
                val answer = response.optString("answer")
                Log.d("W", response.toString())
                addQAPair(question, answer)
            },
            { error ->
                Log.e("W", error.toString())
                addQAPair(question, "Error: ${error.message}")
            }
        ).apply {
            retryPolicy = DefaultRetryPolicy(
                8000, // 8 seconds timeout
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
            )
        }

        val requestQueue = Volley.newRequestQueue(this)
        requestQueue.add(request)
    }

    private fun addQAPair(question: String, answer: String) {
        if (qaList.size >= 5) {
            qaList.removeAt(0) // Remove the oldest item to keep the list size at 5
        }
        qaList.add(0, QAPair(question, answer)) // Add new item to the beginning of the list
        qaAdapter.notifyDataSetChanged()
    }

    override fun onResume() {
        super.onResume()
        sensorHandler.initializeSensors()
    }

    override fun onPause() {
        super.onPause()
        sensorHandler.unregisterSensors()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PermissionManager.PERMISSION_REQUEST_CODE) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                sensorHandler.initializeSensors()
            } else {
                Toast.makeText(this, "All permissions are required to run this application.", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }
}
