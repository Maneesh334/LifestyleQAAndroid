package com.example.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SensorAdapter(private val sensorList: List<SensorData>) :
    RecyclerView.Adapter<SensorAdapter.SensorViewHolder>() {

    class SensorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val sensorNameTextView: TextView = itemView.findViewById(R.id.textViewSensorName)
        val sensorValuesTextView: TextView = itemView.findViewById(R.id.textViewSensorValues)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SensorViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_sensor, parent, false)
        return SensorViewHolder(view)
    }

    override fun onBindViewHolder(holder: SensorViewHolder, position: Int) {
        val sensorData = sensorList[position]
        holder.sensorNameTextView.text = sensorData.name
        holder.sensorValuesTextView.text = sensorData.values.joinToString(", ")
    }

    override fun getItemCount(): Int = sensorList.size
}
