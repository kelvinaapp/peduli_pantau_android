package com.example.pedulipantau

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

class ListEdukasiActivity : AppCompatActivity() {

    private val TAG = "EdukasiActivityLog"
    private val db = Firebase.firestore

    private var listEdukasi = arrayListOf<ListEdukasi>()
    private var fulllistEdukasi = arrayListOf<ListEdukasi>()
    private lateinit var newRecyclerView : RecyclerView

    private var edukasiAdapter = ListEdukasiAdapter(listEdukasi) { id ->
        val intent = Intent(this, DetailEdukasiActivity::class.java)
        intent.putExtra("id", id)
        startActivity(intent)
    }

    private fun getListEdukasi() {

        db.collection("edukasi").orderBy("updated_at").get()
            .addOnSuccessListener { results ->
                for (document in results) {
                    var edukasi = ListEdukasi(
                        document.id,
                        document.data.getValue("judul").toString(),
                        document.data.getValue("gambar").toString(),
                        document.data.getValue("updated_at").toString(),
                    )
                    listEdukasi.add(edukasi)
//                    Log.d(TAG, "$faskes")

                    newRecyclerView = findViewById(R.id.edukasi_list)
                    newRecyclerView.layoutManager = LinearLayoutManager(this)
                    newRecyclerView.adapter = edukasiAdapter

//                    Log.d(TAG, "getListEdukasi: $edukasi")
                }
                fulllistEdukasi.addAll(listEdukasi)
            }

    }

    private fun getListEdukasiByKeyword(keyword : String) {

        var temp = fulllistEdukasi.filter{it.judul.lowercase().contains(keyword.lowercase())} as ArrayList
        listEdukasi.clear()
        listEdukasi.addAll(temp)
        edukasiAdapter.notifyDataSetChanged()

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_edukasi)

        var title = findViewById<TextView>(R.id.activity_title)
        title.text = "Edukasi & Berita"

        val btnBack = findViewById<ImageView>(R.id.btn_back)
        btnBack.setOnClickListener { finish() }

        getListEdukasi()

        val searchBox = findViewById<EditText>(R.id.edukasi_search)
        searchBox.doOnTextChanged { text, start, before, count ->

            getListEdukasiByKeyword(text.toString())

//            Log.d(TAG, "onCreate: search text $text , $before , $start, $count")
        }
    }
}