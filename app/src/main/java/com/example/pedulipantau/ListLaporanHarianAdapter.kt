package com.example.pedulipantau

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView

class ListLaporanHarianAdapter(private val laporanList : List<ListLaporan>,
                               private val onClickListener: ((constraint : ConstraintLayout, idLaporan : ListLaporan) -> Unit)) :
    RecyclerView.Adapter<ListLaporanHarianAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListLaporanHarianAdapter.ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.list_laporan, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var currentItem = laporanList[position]
        holder.tanggal.text = "Laporan ${currentItem.date}"
        holder.item.setOnClickListener { onClickListener.invoke(holder.item, currentItem) }
    }

    override fun getItemCount(): Int {
        return laporanList.size
    }

    inner class ViewHolder(itemView: View) :RecyclerView.ViewHolder(itemView) {

        var tanggal: TextView
        var item: ConstraintLayout

        init {
            tanggal = itemView.findViewById(R.id.tanggal_list_laporan)
            item = itemView.findViewById(R.id.item_history_laporan)
        }
    }
}