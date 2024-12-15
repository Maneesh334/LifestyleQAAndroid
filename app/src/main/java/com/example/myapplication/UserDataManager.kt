package com.example.myapplication

import android.content.Context

class UserDataManager(private val context: Context) {

    companion object {
        private const val PREFS_NAME = "UserPrefs"
        private const val KEY_HEIGHT = "height"
        private const val KEY_WEIGHT = "weight"
        private const val KEY_TARGET_STEPS = "target_steps"
        private const val KEY_TARGET_CALORIES = "target_calories"
    }

    fun saveUserData(height: Double?, weight: Double?, targetSteps: Int?, targetCalories: Double?) {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString(KEY_HEIGHT, height?.toString())
            putString(KEY_WEIGHT, weight?.toString())
            putInt(KEY_TARGET_STEPS, targetSteps ?: 0)
            putString(KEY_TARGET_CALORIES, targetCalories?.toString())
            apply()
        }
    }

    fun loadUserData(): UserData {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val height = sharedPreferences.getString(KEY_HEIGHT, null)?.toDoubleOrNull()
        val weight = sharedPreferences.getString(KEY_WEIGHT, null)?.toDoubleOrNull()
        val targetSteps = sharedPreferences.getInt(KEY_TARGET_STEPS, 0)
        val targetCalories = sharedPreferences.getString(KEY_TARGET_CALORIES, null)?.toDoubleOrNull()
        return UserData(height, weight, targetSteps, targetCalories)
    }
}

data class UserData(
    var height: Double?,
    var weight: Double?,
    var targetSteps: Int?,
    var targetCalories: Double?
)
