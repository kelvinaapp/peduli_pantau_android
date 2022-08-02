package com.example.pedulipantau

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.*
import kotlin.collections.ArrayList


class FaskesActivity : AppCompatActivity() {

    private val TAG = "FaskesActivityLog"

    private val db = Firebase.firestore
    private var listFaskes = arrayListOf<Faskes>()
    private lateinit var newRecyclerView : RecyclerView
    private var fullListFaskes = arrayListOf<Faskes>()

    private var faskesAdapter = FaskesAdapter(listFaskes) { faskes, clicked ->

        if(clicked == "maps") {
//            val uri: String = String.format(Locale.ENGLISH, "geo:0,0?q=${faskes.latitude},${faskes.longitude}(${faskes.nama})")
            val uri: String = String.format(Locale.ENGLISH, "geo:0,0?q=${faskes.alamat}(${faskes.nama})")
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
            startActivity(intent)
        } else {
            val uri: String = String.format(Locale.ENGLISH, "tel:${faskes.phone}")
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse(uri))
            startActivity(intent)
        }

    }

    private fun getListFaskes() {

        db.collection("faskes").get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    var faskes = Faskes(
                        document.data.getValue("nama").toString(),
                        document.data.getValue("alamat").toString(),
                        document.data.getValue("phone").toString(),
                        document.data.getValue("latitude").toString(),
                        document.data.getValue("longitude").toString(),
                    )
                    listFaskes.add(faskes)
//                    Log.d(TAG, "$faskes")

                    newRecyclerView = findViewById(R.id.faskes_list)
                    newRecyclerView.layoutManager = LinearLayoutManager(this)
                    newRecyclerView.adapter = faskesAdapter
                }
                fullListFaskes.addAll(listFaskes)
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "getListFaskes: Failed $exception")
            }

        Log.d(TAG, "getListFaskes: ONCREATE")

    }

    private fun getListFaskesByKeyword(keyword : String) {

        var temp = fullListFaskes.filter{it.nama.lowercase().contains(keyword.lowercase())
                || it.alamat.lowercase().contains(keyword.lowercase())} as ArrayList
        listFaskes.clear()
        listFaskes.addAll(temp)
        faskesAdapter.notifyDataSetChanged()

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_faskes)

        val title = findViewById<TextView>(R.id.activity_title)
        title.text = "Fasilitas Kesehatan"

        val btnBack = findViewById<ImageView>(R.id.btn_back)
        btnBack.setOnClickListener { finish() }

        getListFaskes()

        val searchBox = findViewById<EditText>(R.id.faskes_search)
        searchBox.doOnTextChanged { text, start, before, count ->

            getListFaskesByKeyword(text.toString())

//            Log.d(TAG, "onCreate: search text $text , $before , $start, $count")
        }
    }
}