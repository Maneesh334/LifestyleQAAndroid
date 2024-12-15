package com.example.myapplication

import android.app.TimePickerDialog
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.Calendar
import com.example.myapplication.notification.NotificationTimeAdapter

class NotificationTimeSetup(private val activity: MainActivity) {
    
    fun setupNotificationTimeSelection() {
        val timePickerButton = activity.findViewById<Button>(R.id.timePickerButton)
        val selectedTimeText = activity.findViewById<TextView>(R.id.selectedTimeText)
        
        // Initialize RecyclerView for notification times
        val recyclerView = activity.findViewById<RecyclerView>(R.id.notificationTimesRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        activity.notificationAdapter = NotificationTimeAdapter(
            activity.notificationManager.getScheduledTimes().toList(),
            onDeleteClick = { time ->
                activity.notificationManager.removeNotification(time.hour, time.minute)
                activity.notificationAdapter.updateTimes(activity.notificationManager.getScheduledTimes().toList())
            },
            onEditClick = { oldTime ->
                val calendar = Calendar.getInstance()
                TimePickerDialog(
                    activity,
                    { _, hourOfDay, minute ->
                        try {
                            // Remove old notification
                            activity.notificationManager.removeNotification(oldTime.hour, oldTime.minute)
                            // Schedule new notification with the same question
                            activity.notificationManager.scheduleNotification(hourOfDay, minute, oldTime.question)
                            activity.notificationAdapter.updateTimes(activity.notificationManager.getScheduledTimes().toList())
                            Toast.makeText(
                                activity,
                                "Notification rescheduled to ${String.format("%02d:%02d", hourOfDay, minute)}",
                                Toast.LENGTH_SHORT
                            ).show()
                        } catch (e: Exception) {
                            Toast.makeText(activity, e.message, Toast.LENGTH_SHORT).show()
                        }
                    },
                    oldTime.hour,
                    oldTime.minute,
                    true
                ).show()
            },
            onQuestionChange = { time, newQuestion ->
                activity.notificationManager.updateNotificationQuestion(time.hour, time.minute, newQuestion)
                activity.notificationAdapter.updateTimes(activity.notificationManager.getScheduledTimes().toList())
                Toast.makeText(
                    activity,
                    "Question updated for ${String.format("%02d:%02d", time.hour, time.minute)}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        )
        recyclerView.adapter = activity.notificationAdapter

        // Update adapter when new notification is added
        timePickerButton.setOnClickListener {
            val calendar = Calendar.getInstance()
            TimePickerDialog(
                activity,
                { _, hourOfDay, minute ->
                    try {
                        activity.notificationManager.scheduleNotification(hourOfDay, minute)
                        activity.notificationAdapter.updateTimes(activity.notificationManager.getScheduledTimes().toList())
                        selectedTimeText.text = String.format("%02d:%02d", hourOfDay, minute)
                        Toast.makeText(
                            activity,
                            "Notification scheduled for ${String.format("%02d:%02d", hourOfDay, minute)}",
                            Toast.LENGTH_SHORT
                        ).show()
                    } catch (e: Exception) {
                        Toast.makeText(activity, e.message, Toast.LENGTH_SHORT).show()
                    }
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            ).show()
        }
    }
}
