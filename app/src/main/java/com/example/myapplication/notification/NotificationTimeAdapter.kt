package com.example.myapplication.notification

import android.app.AlertDialog
import android.app.TimePickerDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import java.util.*

class NotificationTimeAdapter(
    private var notificationTimes: List<NotificationManager.NotificationTime>,
    private val onDeleteClick: (NotificationManager.NotificationTime) -> Unit,
    private val onEditClick: (NotificationManager.NotificationTime) -> Unit,
    private val onQuestionChange: (NotificationManager.NotificationTime, String) -> Unit
) : RecyclerView.Adapter<NotificationTimeAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val timeText: TextView = view.findViewById(R.id.timeText)
        val questionText: TextView = view.findViewById(R.id.questionText)
        val editButton: ImageButton = view.findViewById(R.id.editButton)
        val deleteButton: ImageButton = view.findViewById(R.id.deleteButton)
        val editQuestionButton: ImageButton = view.findViewById(R.id.editQuestionButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification_time, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val notificationTime = notificationTimes[position]
        holder.timeText.text = String.format("%02d:%02d", notificationTime.hour, notificationTime.minute)
        holder.questionText.text = QuestionManager.getShortPhrase(notificationTime.question)
        
        holder.editButton.setOnClickListener { onEditClick(notificationTime) }
        holder.deleteButton.setOnClickListener { onDeleteClick(notificationTime) }
        
        holder.editQuestionButton.setOnClickListener { view ->
            showQuestionSelectionDialog(view, notificationTime)
        }
    }

    private fun showQuestionSelectionDialog(view: View, notificationTime: NotificationManager.NotificationTime) {
        val context = view.context
        val questions = QuestionManager.healthQuestions
        val currentIndex = questions.indexOfFirst { it.fullQuestion == notificationTime.question }
        
        AlertDialog.Builder(context)
            .setTitle("Select Question")
            .setSingleChoiceItems(
                questions.map { it.shortPhrase }.toTypedArray(),
                currentIndex
            ) { dialog, which ->
                val selectedQuestion = questions[which].fullQuestion
                onQuestionChange(notificationTime, selectedQuestion)
                updateTimes(notificationTimes)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    override fun getItemCount() = notificationTimes.size

    fun updateTimes(newTimes: List<NotificationManager.NotificationTime>) {
        notificationTimes = newTimes
        notifyDataSetChanged()
    }

    fun getCurrentTimes(): List<NotificationManager.NotificationTime> {
        return notificationTimes
    }
}
