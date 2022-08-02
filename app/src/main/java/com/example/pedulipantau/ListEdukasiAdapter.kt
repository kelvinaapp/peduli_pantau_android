package com.example.pedulipantau

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class ListEdukasiAdapter (private val edukasiList: List<ListEdukasi>,
                          private val onClickListener: ((id : String) -> Unit)
) : RecyclerView.Adapter<ListEdukasiAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListEdukasiAdapter.ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.list_edukasi, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ListEdukasiAdapter.ViewHolder, position: Int) {
        val currentItem = edukasiList[position]
        var editedTitle = ""
//        Log.d("Adapter", "onBindViewHolder: ${currentItem.judul.length}")
        if(currentItem.judul.length > 70) editedTitle = currentItem.judul.substring(0, 69) + "..."
        else editedTitle = currentItem.judul

        holder.judul.text = editedTitle
        Glide.with(holder.image.context).load(currentItem.gambar).into(holder.image);
        holder.item.setOnClickListener { onClickListener.invoke(currentItem.id) }

        val temp = currentItem.updatedAt.toLong()
        var tglUpdate = Instant.ofEpochMilli(temp)
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime()
        val formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy")
        holder.updatedAt.text = formatter.format(tglUpdate)

    }

    override fun getItemCount(): Int {
        return edukasiList.size
    }

    inner class ViewHolder(itemView: View) :RecyclerView.ViewHolder(itemView) {

        var judul : TextView
        var image : ImageView
        var item : ConstraintLayout
        var updatedAt : TextView

        init {
            judul = itemView.findViewById(R.id.edukasi_title)
            image = itemView.findViewById(R.id.edukasi_img)
            item = itemView.findViewById(R.id.edukasi_item)
            updatedAt = itemView.findViewById(R.id.edukasi_updatedAt)
        }
    }
}