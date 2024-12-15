package com.example.myapplication

import android.widget.Toast
import com.example.myapplication.ui.UIManager

class InputValidator {

    fun validateInputs(activity: MainActivity, uiManager: UIManager): Boolean {
        val heightInput = uiManager.editTextHeight.text.toString()
        val weightInput = uiManager.editTextWeight.text.toString()
        val targetStepsInput = uiManager.editTextTargetSteps.text.toString()
        val targetCaloriesInput = uiManager.editTextTargetCalories.text.toString()

        var valid = true

        // Validate Height
        if (heightInput.isBlank()) {
            uiManager.editTextHeight.error = "Please enter your height in centimeters"
            valid = false
        }

        // Validate Weight
        if (weightInput.isBlank()) {
            uiManager.editTextWeight.error = "Please enter your weight in kilograms"
            valid = false
        }

        // Validate Target Steps
        if (targetStepsInput.isBlank()) {
            uiManager.editTextTargetSteps.error = "Please enter your target daily steps"
            valid = false
        }

        // Validate Target Calories
        if (targetCaloriesInput.isBlank()) {
            uiManager.editTextTargetCalories.error = "Please enter your target daily calories burned"
            valid = false
        }

        if (!valid) {
            return false
        }

        // Parse and validate numerical inputs
        return try {
            activity.height = heightInput.toDouble()
            activity.weight = weightInput.toDouble()
            activity.targetSteps = targetStepsInput.toInt()
            activity.targetCalories = targetCaloriesInput.toDouble()

            // Additional validations
            if (activity.height!! <= 0) {
                uiManager.editTextHeight.error = "Height must be a positive number"
                valid = false
            }
            if (activity.weight!! <= 0) {
                uiManager.editTextWeight.error = "Weight must be a positive number"
                valid = false
            }
            if (activity.targetSteps!! <= 0) {
                uiManager.editTextTargetSteps.error = "Target steps must be a positive number"
                valid = false
            }
            if (activity.targetCalories!! <= 0) {
                uiManager.editTextTargetCalories.error = "Target calories must be a positive number"
                valid = false
            }

            valid
        } catch (e: NumberFormatException) {
            Toast.makeText(activity, "Please enter valid numerical values.", Toast.LENGTH_SHORT).show()
            false
        }
    }
}
