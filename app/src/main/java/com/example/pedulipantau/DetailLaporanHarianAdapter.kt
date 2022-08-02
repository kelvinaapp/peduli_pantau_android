package com.example.pedulipantau

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class DetailLaporanHarianAdapter(private val detailList: List<DetailLaporanHarian>)
    : RecyclerView.Adapter<DetailLaporanHarianAdapter.ViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DetailLaporanHarianAdapter.ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.list_detail_laporan, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: DetailLaporanHarianAdapter.ViewHolder, position: Int) {
        var currentItem = detailList[position]
        holder.question.text = currentItem.question
        holder.answer.text = currentItem.answer
    }

    override fun getItemCount(): Int {
        return detailList.size
    }

    inner class ViewHolder(itemView: View) :RecyclerView.ViewHolder(itemView) {

        var question: TextView
        var answer: TextView

        init {
            question = itemView.findViewById(R.id.detail_question)
            answer = itemView.findViewById(R.id.detail_answer)
        }
    }

}