package com.example.pedulipantau

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class DetailEdukasiActivity : AppCompatActivity() {

    private val TAG = "DetailEdukasiActivityLog"
    var id : String = ""

    private lateinit var edukasiTitle : TextView
    private lateinit var image : ImageView
    private lateinit var content : TextView
    private lateinit var updatedAt : TextView
//    private lateinit var sumber : TextView

    private val db = Firebase.firestore

    private fun getEdukasi() {

        db.collection("edukasi").document(id).get()
            .addOnSuccessListener { document ->
                //isi view
                edukasiTitle.text = document.data?.getValue("judul").toString()
                Glide.with(this).load(document.data?.getValue("gambar")).into(image);
                content.text = document.data?.getValue("content").toString()

                val temp = document.data?.getValue("updated_at").toString().toLong()
                var tglUpdate = Instant.ofEpochMilli(temp)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime()
                val formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy")
                updatedAt.text = formatter.format(tglUpdate)
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "getEdukasi: Error -> $exception", )
            }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_edukasi)

        edukasiTitle = findViewById(R.id.detail_edukasi_title)
        image = findViewById(R.id.detail_edukasi_img)
        content = findViewById(R.id.detail_edukasi_content)
        updatedAt = findViewById(R.id.detail_edukasi_updatedAt)

        var title = findViewById<TextView>(R.id.activity_title)
        title.text = "Edukasi & Berita"

        val btnBack = findViewById<ImageView>(R.id.btn_back)
        btnBack.setOnClickListener { finish() }

        id = intent.getStringExtra("id").toString()
        getEdukasi()
    }
}