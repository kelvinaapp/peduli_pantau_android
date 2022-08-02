package com.example.pedulipantau

import android.annotation.SuppressLint
import android.bluetooth.le.ScanResult
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView

class ScanResultAdapter(private val deviceList: List<ScanResult>,
                        private val onClickListener: ((device: ScanResult) -> Unit)
) : RecyclerView.Adapter<ScanResultAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScanResultAdapter.ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.list_device, parent, false)
        return ViewHolder(v)
    }

    @SuppressLint("MissingPermission")
    override fun onBindViewHolder(holder: ScanResultAdapter.ViewHolder, position: Int) {
        val currentItem = deviceList[position]
        holder.deviceName.text = currentItem.device.name ?: "Unnamed"
        holder.deviceContainer.setOnClickListener {onClickListener.invoke(currentItem)}
    }

    override fun getItemCount(): Int {
        return deviceList.size
    }

    inner class ViewHolder(itemView: View) :RecyclerView.ViewHolder(itemView) {

        var deviceName: TextView
        var deviceContainer: ConstraintLayout

        init {
            deviceName = itemView.findViewById(R.id.device_name)
            deviceContainer = itemView.findViewById(R.id.device_container)
        }
    }


}