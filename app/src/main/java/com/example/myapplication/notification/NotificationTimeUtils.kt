package com.example.myapplication.notification

import android.app.TimePickerDialog
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import java.util.Calendar

class NotificationTimeUtils(
    private val activity: AppCompatActivity,
    private val notificationManager: NotificationManager
) {
    private lateinit var notificationAdapter: NotificationTimeAdapter

    fun setupNotificationTimeSelection() {
        val timePickerButton = activity.findViewById<Button>(R.id.timePickerButton)
        val selectedTimeText = activity.findViewById<TextView>(R.id.selectedTimeText)
        
        // Initialize RecyclerView for notification times
        val recyclerView = activity.findViewById<RecyclerView>(R.id.notificationTimesRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        
        notificationAdapter = NotificationTimeAdapter(
            notificationManager.getScheduledTimes().toList(),
            onDeleteClick = { time ->
                notificationManager.removeNotification(time.hour, time.minute)
                notificationAdapter.updateTimes(notificationManager.getScheduledTimes().toList())
            },
            onEditClick = { time ->
                showTimePickerDialog(time)
            },
            onQuestionChange = { time, newQuestion ->
                if (notificationManager.updateNotificationQuestion(time.hour, time.minute, newQuestion)) {
                    notificationAdapter.updateTimes(notificationManager.getScheduledTimes().toList())
                    Toast.makeText(
                        activity,
                        "Question updated successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        )
        recyclerView.adapter = notificationAdapter

        timePickerButton.setOnClickListener {
            val calendar = Calendar.getInstance()
            TimePickerDialog(
                activity,
                { _, hourOfDay, minute ->
                    notificationManager.scheduleNotification(hourOfDay, minute)
                    notificationAdapter.updateTimes(notificationManager.getScheduledTimes().toList())
                    selectedTimeText.text = String.format("%02d:%02d", hourOfDay, minute)
                    Toast.makeText(
                        activity,
                        "Notification scheduled for ${String.format("%02d:%02d", hourOfDay, minute)}",
                        Toast.LENGTH_SHORT
                    ).show()
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            ).show()
        }
    }

    fun showTimePickerDialog(time: NotificationManager.NotificationTime) {
        val calendar = Calendar.getInstance()
        TimePickerDialog(
            activity,
            { _, hourOfDay, minute ->
                notificationManager.removeNotification(time.hour, time.minute)
                notificationManager.scheduleNotification(hourOfDay, minute, time.question)
                notificationAdapter.updateTimes(notificationManager.getScheduledTimes().toList())
                Toast.makeText(
                    activity,
                    "Notification rescheduled to ${String.format("%02d:%02d", hourOfDay, minute)}",
                    Toast.LENGTH_SHORT
                ).show()
            },
            time.hour,
            time.minute,
            true
        ).show()
    }
}
