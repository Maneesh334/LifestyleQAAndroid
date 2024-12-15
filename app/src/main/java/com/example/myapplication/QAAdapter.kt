package com.example.myapplication

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.View
import android.widget.TextView

class QAAdapter(private val qaList: List<QAPair>) : RecyclerView.Adapter<QAAdapter.QAViewHolder>() {

    class QAViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewQuestion: TextView = itemView.findViewById(R.id.textViewQuestion)
        val textViewAnswer: TextView = itemView.findViewById(R.id.textViewAnswer)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QAViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_qa, parent, false)
        return QAViewHolder(view)
    }

    override fun onBindViewHolder(holder: QAViewHolder, position: Int) {
        val qaPair = qaList[position]
        holder.textViewQuestion.text = "Q: ${qaPair.question}"
        holder.textViewAnswer.text = "A: ${qaPair.answer}"
    }

    override fun getItemCount(): Int {
        return qaList.size
    }
}
