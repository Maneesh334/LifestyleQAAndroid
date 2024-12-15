package com.example.myapplication.api

import android.content.Context
import android.util.Log
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import java.util.Calendar

class ApiService(private val context: Context) {
    companion object {
        private const val TAG = "ApiService"
        private const val API_URL = "https://7549-34-134-86-113.ngrok-free.app/api2"
    }

    fun sendQuestionToServer(
        question: String,
        data: JSONObject,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        val requestData = JSONObject()
        try {
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
            
            requestData.put("question", question + "Answer the question in not more than one sentence.")
            requestData.put("data", data)
            requestData.put("timestamp", timestamp)
            requestData.put("dayOfWeek", dayOfWeek)
        } catch (e: Exception) {
            Log.e(TAG, "Error preparing request data", e)
            onError("Error preparing request: ${e.message}")
            return
        }

        val request = JsonObjectRequest(
            Request.Method.POST,
            API_URL,
            requestData,
            { response ->
                val answer = response.optString("answer")
                onSuccess(answer)
            },
            { error ->
                Log.e(TAG, "Error sending question to server", error)
                onError("Error: ${error.message}")
            }
        )

        request.retryPolicy = DefaultRetryPolicy(
            8000, // 8 seconds timeout
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        Volley.newRequestQueue(context).add(request)
    }
}
