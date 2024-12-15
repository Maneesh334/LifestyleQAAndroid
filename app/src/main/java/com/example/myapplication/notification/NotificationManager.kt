package com.example.myapplication.notification

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import androidx.core.app.NotificationCompat
import android.app.TimePickerDialog
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.example.myapplication.R
import java.util.Calendar
import android.util.Log

class NotificationManager(private val context: Context) {
    private val alarmManager: AlarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val notificationManager: NotificationManager = 
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    companion object {
        private const val TAG = "NotificationManager"
        const val CHANNEL_ID = "health_app_channel"
        const val NOTIFICATION_REQUEST_CODE = 123
        private const val PREFS_NAME = "notification_prefs"
        private const val KEY_NOTIFICATION_TIMES = "notification_times"
        private const val KEY_NOTIFICATION_QUESTIONS = "notification_questions"
        private const val MAX_NOTIFICATIONS = 5
    }

    data class NotificationTime(
        val hour: Int,
        val minute: Int,
        var question: String = QuestionManager.healthQuestions[0].fullQuestion
    ) {
        val id: Int
            get() = hour * 100 + minute

        override fun toString(): String = "$hour:$minute:${question.hashCode()}"
    }

    init {
        Log.d(TAG, "Initializing NotificationManager")
        createNotificationChannel()
        if (hasRequiredPermissions()) {
            Log.d(TAG, "Has required permissions, restoring scheduled notification")
            restoreScheduledNotification()
        } else {
            Log.w(TAG, "Missing required permissions")
        }
    }

    private fun hasRequiredPermissions(): Boolean {
        // Check for notification permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasNotificationPermission = context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) == android.content.pm.PackageManager.PERMISSION_GRANTED
            Log.d(TAG, "Notification permission granted: $hasNotificationPermission")
            if (!hasNotificationPermission) {
                return false
            }
        }
        
        // Check for exact alarm permission on Android 12+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val canScheduleAlarms = alarmManager.canScheduleExactAlarms()
            Log.d(TAG, "Can schedule exact alarms: $canScheduleAlarms")
            if (!canScheduleAlarms) {
                return false
            }
        }
        
        Log.d(TAG, "All required permissions granted")
        return true
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Health App Notifications"
            val descriptionText = "Channel for Health App notifications"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableVibration(true)
                enableLights(true)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun scheduleNotification(hour: Int, minute: Int, question: String = QuestionManager.healthQuestions[0].fullQuestion) {
        if (!hasRequiredPermissions()) {
            throw SecurityException("Required permissions are not granted")
        }

        val newTime = NotificationTime(hour, minute, question)
        val times = getScheduledTimes().toMutableSet()
        
        // If this is a reschedule, don't check the max notifications limit
        val isReschedule = times.any { it.hour == hour && it.minute == minute }
        if (!isReschedule && times.size >= MAX_NOTIFICATIONS) {
            throw IllegalStateException("Maximum number of notifications ($MAX_NOTIFICATIONS) already scheduled")
        }
        
        // Update or add the notification time
        times.removeIf { it.hour == hour && it.minute == minute }
        times.add(newTime)
        saveNotificationTimes(times)
        scheduleAlarm(newTime)
    }

    fun removeNotification(hour: Int, minute: Int) {
        val timeToRemove = NotificationTime(hour, minute)
        val times = getScheduledTimes().toMutableSet()
        
        // Find and remove the notification with matching hour and minute
        val notificationToRemove = times.find { it.hour == hour && it.minute == minute }
        if (notificationToRemove != null) {
            times.remove(notificationToRemove)
            saveNotificationTimes(times)
            cancelAlarm(notificationToRemove)
            Log.d(TAG, "Removed notification for $hour:$minute")
        } else {
            Log.d(TAG, "No notification found for $hour:$minute")
        }
    }

    fun getScheduledTimes(): Set<NotificationTime> {
        val timesJson = prefs.getStringSet(KEY_NOTIFICATION_TIMES, setOf()) ?: setOf()
        val questionsMap = prefs.getStringSet(KEY_NOTIFICATION_QUESTIONS, setOf())
            ?.associate { 
                val parts = it.split(":")
                "${parts[0]}:${parts[1]}" to QuestionManager.healthQuestions[parts[2].toIntOrNull() ?: 0].fullQuestion
            } ?: mapOf()

        return timesJson.map { 
            val parts = it.split(":")
            val timeKey = "${parts[0]}:${parts[1]}"
            NotificationTime(
                parts[0].toInt(), 
                parts[1].toInt(),
                questionsMap[timeKey] ?: QuestionManager.healthQuestions[0].fullQuestion
            )
        }.toSet()
    }

    private fun saveNotificationTimes(times: Set<NotificationTime>) {
        val timesJson = times.map { "${it.hour}:${it.minute}" }.toSet()
        val questionsJson = times.map { time ->
            val index = QuestionManager.healthQuestions.indexOfFirst { q -> q.fullQuestion == time.question }
            "${time.hour}:${time.minute}:${if (index >= 0) index else 0}"
        }.toSet()

        prefs.edit()
            .putStringSet(KEY_NOTIFICATION_TIMES, timesJson)
            .putStringSet(KEY_NOTIFICATION_QUESTIONS, questionsJson)
            .apply()
    }

    fun updateNotificationQuestion(hour: Int, minute: Int, newQuestion: String): Boolean {
        val times = getScheduledTimes().toMutableSet()
        return times.find { it.hour == hour && it.minute == minute }?.let { time ->
            times.remove(time)
            times.add(NotificationTime(hour, minute, newQuestion))
            saveNotificationTimes(times)
            // Reschedule the notification with the new question
            cancelAlarm(time)
            scheduleAlarm(NotificationTime(hour, minute, newQuestion))
            true
        } ?: false
    }

    private fun scheduleAlarm(notificationTime: NotificationTime) {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, notificationTime.hour)
            set(Calendar.MINUTE, notificationTime.minute)
            set(Calendar.SECOND, 0)
            
            // If the time has already passed today, schedule for tomorrow
            if (before(Calendar.getInstance())) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("notification_id", notificationTime.id)
            putExtra("question", notificationTime.question)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            notificationTime.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )

        Log.d(TAG, "Scheduled notification for ${notificationTime.hour}:${notificationTime.minute} with question: ${notificationTime.question}")
    }

    private fun cancelAlarm(notificationTime: NotificationTime) {
        val intent = Intent(context, NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            notificationTime.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
        Log.d(TAG, "Cancelled notification for ${notificationTime.hour}:${notificationTime.minute}")
    }

    private fun restoreScheduledNotification() {
        val times = getScheduledTimes()
        times.forEach { time ->
            scheduleAlarm(time)
        }
    }

    fun setupNotificationTimeSelection(timePickerButton: Button, selectedTimeText: TextView) {
        // Restore saved time if exists
        getScheduledTimes().forEach { time ->
            val timeString = String.format("%02d:%02d", time.hour, time.minute)
            selectedTimeText.text = timeString
        }

        timePickerButton.setOnClickListener {
            val calendar = Calendar.getInstance()
            
            TimePickerDialog(
                context,
                { _, hourOfDay, minute ->
                    // Update the text view with selected time
                    val timeString = String.format("%02d:%02d", hourOfDay, minute)
                    selectedTimeText.text = timeString
                    
                    // Schedule notification
                    scheduleNotification(hourOfDay, minute)
                    
                    Toast.makeText(
                        context,
                        "Notification scheduled for $timeString",
                        Toast.LENGTH_SHORT
                    ).show()
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true // 24-hour format
            ).show()
        }
    }
}
