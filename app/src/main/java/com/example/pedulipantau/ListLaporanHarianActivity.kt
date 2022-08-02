package com.example.pedulipantau

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ListLaporanHarianActivity : AppCompatActivity() {

    private val TAG = "RiwayatLaporanHarian"
    private val PASIEN = "EiDoLWlIXYBwx1EaqpU4"

    private val db = Firebase.firestore
    private var arrayLaporan = arrayListOf<ListLaporan>()

    private lateinit var newRecyclerView : RecyclerView

    private val listLaporanHarianAdapter = ListLaporanHarianAdapter(arrayLaporan) { item, listLaporan ->
        Log.d(TAG, "listLaporanHarianAdapter: ${listLaporan.id}")

        val intent = Intent(this, DetailLaporanHarianActivity::class.java)
        var json = Json.encodeToString(ListLaporan.serializer(), listLaporan)
        intent.putExtra("listLaporan", json)
        startActivity(intent)

    }

    private fun getListLaporanHarian() {
        db.collection("pasien").document(PASIEN).collection("log").
        orderBy("created_at", Query.Direction.DESCENDING).get()
            .addOnSuccessListener { results ->
                for(result in results) {
                    var temp = result.data.getValue("created_at").toString()
                    var date = LocalDateTime.parse(temp)
                    var formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy")
                    var formattedDate = formatter.format(date)
//                    Log.d(TAG, "getListLaporanHarian: ${formattedDate}.")

                    arrayLaporan.add(ListLaporan(result.id, formattedDate))

                    newRecyclerView = findViewById(R.id.laporan_list)
                    newRecyclerView.layoutManager = LinearLayoutManager(this)
                    newRecyclerView.adapter = listLaporanHarianAdapter
                }
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents.", exception)
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_riwayat_laporan_harian)

        getListLaporanHarian()

        val title = findViewById<TextView>(R.id.activity_title)
        title.setText("Riwayat Laporan Harian")

        val btnBack = findViewById<ImageView>(R.id.btn_back)
        btnBack.setOnClickListener { finish() }
    }
}