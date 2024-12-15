package com.example.myapplication.notification

import android.app.Activity
import android.content.Intent
import android.view.View
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Button
import android.widget.EditText
import com.example.myapplication.R

class NotificationDataHandler(private val activity: Activity) {
    fun handleNotificationData(intent: Intent) {
        val question = intent.getStringExtra("notification_question")
        val answer = intent.getStringExtra("notification_answer")
        
        if (question != null && answer != null) {
            // Find or create a TextView to display the response
            val responseContainer = activity.findViewById<LinearLayout>(R.id.responseContainer)
            val scrollView = activity.findViewById<ScrollView>(R.id.mainScrollView)
            responseContainer.visibility = View.VISIBLE
            
            val questionTextView = activity.findViewById<TextView>(R.id.questionTextView)
            questionTextView.text = QuestionManager.getShortPhrase(question)
            
            val answerTextView = activity.findViewById<TextView>(R.id.answerTextView)
            answerTextView.text = answer

            // Set the question in the input field
            val editTextQuestion = activity.findViewById<EditText>(R.id.editTextQuestion)
            val btnAskQuestion = activity.findViewById<Button>(R.id.btnAskQuestion)

            editTextQuestion.setText(question)

            btnAskQuestion.postDelayed({
                btnAskQuestion.performClick()
            }, 300)

            scrollView.post {
                scrollView.smoothScrollTo(0, responseContainer.top)


                responseContainer.setBackgroundResource(R.drawable.highlighted_background)
                responseContainer.postDelayed({
                    responseContainer.setBackgroundResource(R.drawable.rounded_background)
                }, 1000)
            }
        }
    }
}
