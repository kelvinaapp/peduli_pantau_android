package com.example.pedulipantau.model

import android.content.Context
import android.util.Log
import com.example.pedulipantau.helper.PrefHelper
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.*
import javax.security.auth.callback.Callback
import kotlin.math.log

class LaporanHarian(context : Context) {

    private val TAG = "Laporan Harian Model"
    private val db = Firebase.firestore
    private var ph : PrefHelper = PrefHelper(context)
    private var userId: String = ph.getString("PREF_USER_ID").toString()

    fun getLatestLaporan(callback: MyCallback){
         db.collection("pasien").document(userId).collection("log").limit(1)
            .orderBy("created_at", Query.Direction.DESCENDING).get()
            .addOnCompleteListener { task ->
                if(task.isSuccessful) {
                    var latestLaporan = task.result?.first()
                    if (latestLaporan != null) {
                        callback.onCallback(latestLaporan)
                    }
                }
            }
    }

    fun getLaporanById(id: String){
        db.collection("pasien").document(userId).collection("log").document(id).get()
            .addOnSuccessListener{ result ->
                Log.d(TAG, "getLaporanById: "+result.data)
            }
    }

}