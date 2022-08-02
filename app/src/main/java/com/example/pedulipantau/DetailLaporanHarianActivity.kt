package com.example.pedulipantau

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.core.OrderBy
import com.google.firebase.firestore.core.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement

class DetailLaporanHarianActivity : AppCompatActivity() {

    private val db = Firebase.firestore
    private val TAG = "DetailLaporanHarianActivity"
    private val PASIEN = "EiDoLWlIXYBwx1EaqpU4"

    private lateinit var listLaporan: ListLaporan
    private var detailLaporanHarian = arrayListOf<DetailLaporanHarian>()
    private lateinit var newRecyclerView : RecyclerView
    private var detailLaporanHarianAdapter = DetailLaporanHarianAdapter(detailLaporanHarian)

    private fun getDetailLaporan() {

        db.collection("pasien").document(PASIEN).collection("log").document(listLaporan.id).get()
            .addOnSuccessListener { result ->
                var answers = result.data?.getValue("answer") as ArrayList<String>

                for(answer in answers) {
                    var json = Json.decodeFromString(Answer.serializer(), answer)

                    db.collection("formlog").whereEqualTo("nomor", json.position).get()
                        .addOnSuccessListener { questions ->
                            for (question in questions) {
//                                Log.d(TAG, "getDetailLaporan: ${question.data.getValue("question")}")
                                var detail = DetailLaporanHarian(
                                    question.data.getValue("question").toString(),
                                    json.answer.toString()
                                )

                                detailLaporanHarian.add(detail)

                                newRecyclerView = findViewById(R.id.detail_laporan_list)
                                newRecyclerView.layoutManager = LinearLayoutManager(this)
                                newRecyclerView.adapter = detailLaporanHarianAdapter
                            }
                        }
                    Log.d(TAG, "getDetailLaporan: ${json.position}")
                }

                Log.d(TAG, "getDetailLaporan: ${listLaporan.id}")

            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents.", exception)
            }


    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_laporan_harian)

        var extra = intent.getStringExtra("listLaporan")
        listLaporan = Json.decodeFromString(ListLaporan.serializer(), extra.toString())
//        Log.d(TAG, "onCreate: ID $listLaporan")

        getDetailLaporan()

        val title = findViewById<TextView>(R.id.activity_title)
        title.setText("${listLaporan.date}")

        val btnBack = findViewById<ImageView>(R.id.btn_back)
        btnBack.setOnClickListener {
            finish()
        }
    }
}