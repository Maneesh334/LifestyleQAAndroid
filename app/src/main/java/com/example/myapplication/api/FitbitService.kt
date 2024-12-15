package com.example.myapplication.api

import android.content.Context
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FitbitService(private val context: Context) {
    companion object {
        private const val FITBIT_API_BASE_URL = "https://api.fitbit.com/1/user/-/"
        private const val BEARER_TOKEN = "eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiIyM1BaR0IiLCJzdWIiOiJDQktWTFIiLCJpc3MiOiJGaXRiaXQiLCJ0eXAiOiJhY2Nlc3NfdG9rZW4iLCJzY29wZXMiOiJyc29jIHJlY2cgcnNldCByaXJuIHJveHkgcm51dCBycHJvIHJzbGUgcmNmIHJhY3QgcmxvYyBycmVzIHJ3ZWkgcmhyIHJ0ZW0iLCJleHAiOjE3MzM3OTAyNTMsImlhdCI6MTczMzc2MTQ1M30.HKqbk7F4blvkUG2FQhWVITgq_DBmmKiwi849D7jQrew"
        private const val REFRESH_TOKEN = "14005e10221002898642c7c69f4d1caac68b36c0e34ba00a4e1d5a1b80077d6b"
    }

    fun fetchFitbitData(onSuccess: (JSONObject) -> Unit, onError: (String) -> Unit) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val currentDate = dateFormat.format(Date())
        val url = "${FITBIT_API_BASE_URL}activities/date/$currentDate.json"

        val request = object : JsonObjectRequest(
            Request.Method.GET,
            url,
            null,
            { response ->
                onSuccess(response)
            },
            { error ->
                onError(error.message ?: "Unknown error occurred")
            }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["accept"] = "application/x-www-form-urlencoded"
                headers["authorization"] = "Bearer $BEARER_TOKEN"
                return headers
            }
        }

        Volley.newRequestQueue(context).add(request)
    }
}
