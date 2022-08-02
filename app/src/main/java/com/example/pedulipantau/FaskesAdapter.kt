package com.example.pedulipantau

import android.bluetooth.le.ScanResult
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.serialization.descriptors.PrimitiveKind
import java.util.*
import kotlin.collections.ArrayList

class FaskesAdapter (private val faskesList: List<Faskes>,
                     private val onClickListener: ((faskes : Faskes, clicked: String) -> Unit)
) : RecyclerView.Adapter<FaskesAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FaskesAdapter.ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.list_faskes, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: FaskesAdapter.ViewHolder, position: Int) {
        val currentItem = faskesList[position]
        holder.name.text = currentItem.nama
        holder.address.text = currentItem.alamat
        holder.phoneNumber.text = currentItem.phone

        holder.btnMaps.setOnClickListener {onClickListener.invoke(currentItem, "maps")}
        holder.btnPhone.setOnClickListener {onClickListener.invoke(currentItem, "phone")}
    }

    override fun getItemCount(): Int {
        return faskesList.size
    }

    inner class ViewHolder(itemView: View) :RecyclerView.ViewHolder(itemView) {

        var name: TextView
        var address: TextView
        var phoneNumber: TextView
        var btnMaps: Button
        var btnPhone: Button

        init {
            name = itemView.findViewById(R.id.faskes_name)
            address = itemView.findViewById(R.id.faskes_address)
            phoneNumber = itemView.findViewById(R.id.faskes_phone)
            btnMaps = itemView.findViewById(R.id.btn_faskes_maps)
            btnPhone = itemView.findViewById(R.id.btn_faskes_phone)
        }
    }
}