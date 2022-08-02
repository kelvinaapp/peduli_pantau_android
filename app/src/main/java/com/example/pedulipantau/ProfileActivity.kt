package com.example.pedulipantau

import android.content.Intent
import android.opengl.Visibility
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.pedulipantau.helper.Constant
import com.example.pedulipantau.helper.PrefHelper
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import org.w3c.dom.Text
import java.math.BigInteger
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.math.log

class ProfileActivity : AppCompatActivity() {

    private val TAG = "ProfileActivity"
    private lateinit var idPasien : String

    private val db = Firebase.firestore
    private var isEdit = false
    lateinit var prefHelper: PrefHelper

    private fun getProfileData() {

        val nik = findViewById<TextView>(R.id.profile_nik)
        val nama = findViewById<TextView>(R.id.profile_nama)
        val alamat = findViewById<TextView>(R.id.profile_alamat)
        val tglLahir = findViewById<TextView>(R.id.profile_tgl_lahir)
        val tglPositif = findViewById<TextView>(R.id.profile_tgl_positif)
        val password = findViewById<TextView>(R.id.profile_password)


        db.collection("pasien").document(idPasien).get()
            .addOnSuccessListener { result ->
                nik.text = result.data?.getValue("nik").toString()
                nama.text = result.data?.getValue("name").toString()
                alamat.text = result.data?.getValue("alamat").toString()

                val temp1 = result.data?.getValue("tgl_lahir").toString().toLong()
                val temp2 = result.data?.getValue("tgl_positif").toString().toLong()
                var tgl_lahir = Instant.ofEpochMilli(temp1)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime()
                var tgl_positif = Instant.ofEpochMilli(temp2)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime()
                val formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy")

                tglLahir.text = formatter.format(tgl_lahir)
                tglPositif.text = formatter.format(tgl_positif)
                password.text = "********"

                Log.d(TAG, "getProfileData: ${tgl_lahir}")
            }
            .addOnFailureListener{ exception ->
                Log.w(TAG, "getProfileData: Error $exception", )
            }
    }

    private fun updateNewPassword(newPass : String, reNewPass : String) {
        if(newPass.equals(reNewPass) && newPass != "") {
            val crypt = MessageDigest.getInstance("MD5")
            crypt.update(newPass.toByteArray())
            val md5 = BigInteger(1, crypt.digest()).toString(16)

            db.collection("pasien").document(idPasien)
                .update("password", md5)
                .addOnSuccessListener {
                    Toast.makeText(this, "password berhasil diperbaharui", Toast.LENGTH_LONG).show()
                }.addOnFailureListener{ ex ->
                    Toast.makeText(this, "Gagal Memperbaharui password", Toast.LENGTH_LONG).show()
                    Log.w(TAG, "updateNewPassword: Failed $ex" )
                }

        } else {
            Toast.makeText(this, "Password tidak sesuai", Toast.LENGTH_LONG).show()
        }
    }

    private fun logout(){
        prefHelper = PrefHelper(this)

        prefHelper.put(Constant.PREF_USERNAME, "")
        prefHelper.put(Constant.PREF_PASSWORD, "")
        prefHelper.put(Constant.PREF_PERSON_NAME, "")
        prefHelper.put(Constant.PREF_IS_LOGIN, false)
        prefHelper.put(Constant.PREF_USER_ID, "")
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        idPasien = intent.getStringExtra("Id").toString()

        getProfileData()

        val btnBack = findViewById<ImageView>(R.id.btn_back)
        btnBack.setOnClickListener {
            finish()
        }

        val btnChangePass = findViewById<Button>(R.id.btn_ubah_password)
        btnChangePass.setOnClickListener{
            val passContainer = findViewById<ConstraintLayout>(R.id.profile_pass_container)
            val etNewPass = findViewById<EditText>(R.id.profile_new_pass)
            val etReNewPass = findViewById<EditText>(R.id.profile_reenter_pass)

            if(!isEdit) {
                passContainer.visibility = View.GONE
                etNewPass.visibility = View.VISIBLE
                etReNewPass.visibility = View.VISIBLE
                etNewPass.setText("")
                etReNewPass.setText("")
                btnChangePass.text = "Submit"
                isEdit = true

            } else {
                updateNewPassword(etNewPass.text.toString(), etReNewPass.text.toString())
                Log.d(TAG, "onCreate: ${etNewPass.text.toString()} ${ etReNewPass.text.toString()}")

                passContainer.visibility = View.VISIBLE
                etNewPass.visibility = View.GONE
                etReNewPass.visibility = View.GONE
                btnChangePass.text = "Ubah Password"
                isEdit = false

            }

        }

        val btnLogout = findViewById<Button>(R.id.btn_logout)
        btnLogout.setOnClickListener{
            this.logout()
        }

    }
}