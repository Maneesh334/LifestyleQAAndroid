package com.example.myapplication.notification

object QuestionManager {
    data class Question(
        val fullQuestion: String,
        val shortPhrase: String
    )

    val healthQuestions = listOf(
        Question(
            "How am I feeling today?",
            "Mood Check"
        ),
        Question(
            "Did I achieve my step goal today?",
            "Step Goal"
        ),
        Question(
            "How was my sleep quality last night?",
            "Sleep Check"
        ),
        Question(
            "Should I be drinking more water?",
            "Water Intake"
        ),
        Question(
            "How would you rate my stress level right now?",
            "Stress Level"
        ),
        Question(
            "Should I exercise now?",
            "Exercise Check"
        ),
        Question(
            "How is my energy level right now?",
            "Energy Level"
        ),
        Question(
            "How should I be dressed according to the weather conditions outside?",
            "Fit Check"
        )
    )

    fun getShortPhrase(fullQuestion: String): String {
        return healthQuestions.find { it.fullQuestion == fullQuestion }?.shortPhrase ?: "Health Check"
    }
}
