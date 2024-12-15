package com.example.myapplication.notification

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.myapplication.*
import com.example.myapplication.api.ApiService
import com.example.myapplication.api.FitbitService
import com.example.myapplication.data.DataPreparationManager
import com.example.myapplication.HealthDataGenerator
import com.example.myapplication.MainActivity
import com.example.myapplication.R
import com.example.myapplication.SensorHandler
import com.example.myapplication.UserDataManager
import com.example.myapplication.WeatherService
import com.example.myapplication.notification.NotificationManager
import org.json.JSONObject

class NotificationReceiver : BroadcastReceiver() {
    companion object {
        private const val TAG = "NotificationReceiver"
        const val ACTION_ANSWER_QUESTION = "com.example.myapplication.ACTION_ANSWER_QUESTION"
        const val ACTION_NOTIFICATION_CANCELLED = "com.example.myapplication.ACTION_NOTIFICATION_CANCELLED"
        const val EXTRA_QUESTION = "extra_question"
        const val EXTRA_NOTIFICATION_HOUR = "extra_notification_hour"
        const val EXTRA_NOTIFICATION_MINUTE = "extra_notification_minute"
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "onReceive called: ${intent.action}")
        
        try {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
            val notificationId = intent.getIntExtra("notification_id", 0)
            val question = intent.getStringExtra("question") ?: QuestionManager.healthQuestions[0]
            
            // Extract hour and minute from notification ID (id = hour * 100 + minute)
            val hour = notificationId / 100
            val minute = notificationId % 100
            
            // Reschedule notification for next day
            val notificationManagerInstance = NotificationManager(context)
            notificationManagerInstance.scheduleNotification(hour, minute, question.toString())
            
            // Create an intent to open the app when notification is tapped
            val contentIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("notification_question", question.toString())
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                contentIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Show initial notification
            val initialNotification = NotificationCompat.Builder(context, NotificationManager.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(QuestionManager.getShortPhrase(question.toString()))
                .setContentText("Preparing your health check...")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setProgress(0, 0, true)
                .build()

            notificationManager.notify(notificationId, initialNotification)

            // Initialize required services
            val dataPreparationManager = DataPreparationManager()
            val weatherService = WeatherService(context)
            val healthDataGenerator = HealthDataGenerator()
            val apiService = ApiService(context)
            val userDataManager = UserDataManager(context)
            val sensorHandler = SensorHandler(context)
            val fitbitService = FitbitService(context)

            // Load user data
            val userData = userDataManager.loadUserData()

            // Initialize sensors
            sensorHandler.initializeSensors()

            // First fetch Fitbit data
            fitbitService.fetchFitbitData(
                onSuccess = { fitbitData ->
                    // Then fetch weather data and continue with the process
                    weatherService.fetchWeatherData(40.7128, -74.0060) { weatherData, error ->
                        if (weatherData != null) {
                            val healthData = healthDataGenerator.generateDynamicHealthData()
                            
                            // Prepare data using DataPreparationManager
                            val jsonData = dataPreparationManager.prepareDataToSend(
                                height = userData.height,
                                weight = userData.weight,
                                targetSteps = userData.targetSteps,
                                targetCalories = userData.targetCalories,
                                healthData = healthData,
                                weatherData = weatherData,
                                sensorHandler = sensorHandler,
                                qaHistory = listOf(),
                                fitbitData = fitbitData
                            )

                            // Send question with prepared data to server
                            apiService.sendQuestionToServer(
                                question.toString(),
                                jsonData,
                                onSuccess = { answer ->
                                    // Clean up sensors
                                    sensorHandler.unregisterSensors()
                                    
                                    // Update notification with answer
                                    val updatedIntent = Intent(context, MainActivity::class.java).apply {
                                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                                        putExtra("notification_question", question.toString())
                                        putExtra("notification_answer", answer.toString())
                                    }
                                    
                                    val updatedPendingIntent = PendingIntent.getActivity(
                                        context,
                                        notificationId,
                                        updatedIntent,
                                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                                    )

                                    val updatedNotification = NotificationCompat.Builder(context, NotificationManager.CHANNEL_ID)
                                        .setSmallIcon(R.drawable.ic_launcher_foreground)
                                        .setContentTitle(QuestionManager.getShortPhrase(question.toString()))
                                        .setContentText(answer.toString())
                                        .setStyle(NotificationCompat.BigTextStyle().bigText(answer.toString()))
                                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                                        .setAutoCancel(true)
                                        .setContentIntent(updatedPendingIntent)
                                        .build()

                                    notificationManager.notify(notificationId, updatedNotification)
                                },
                                onError = { error ->
                                    Log.e(TAG, "Error sending question to server: $error")
                                    sensorHandler.unregisterSensors()
                                }
                            )
                        } else {
                            Log.e(TAG, "Error fetching weather data: $error")
                            sensorHandler.unregisterSensors()
                        }
                    }
                },
                onError = { error ->
                    Log.e(TAG, "Error fetching Fitbit data: $error")
                    sensorHandler.unregisterSensors()
                }
            )

        } catch (e: Exception) {
            Log.e(TAG, "Error in NotificationReceiver", e)
        }
    }
}
