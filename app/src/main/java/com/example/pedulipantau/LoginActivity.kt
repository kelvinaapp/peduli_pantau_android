package com.example.pedulipantau

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.pedulipantau.helper.Constant
import com.example.pedulipantau.helper.PrefHelper
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.math.BigInteger
import java.security.MessageDigest

class LoginActivity : AppCompatActivity() {

    private val TAG = "LoginActivityLog"

    lateinit var prefHelper: PrefHelper
    private val db = Firebase.firestore

    private fun moveIntent(){
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        prefHelper = PrefHelper(this)

        val btnLogin = findViewById<Button>(R.id.btn_login)
        btnLogin.setOnClickListener{
            val username = findViewById<EditText>(R.id.login_ktp).text.toString()
            val password = findViewById<EditText>(R.id.login_password).text.toString()

            if(username.isNotEmpty() && password.isNotEmpty()) {

                //check db
                val crypt = MessageDigest.getInstance("MD5")
                crypt.update(password.toByteArray())
                val md5 = BigInteger(1, crypt.digest()).toString(16)

                db.collection("pasien")
                    .whereEqualTo("nik", username)
                    .whereEqualTo("password", md5)
                    .get()
                    .addOnSuccessListener { results ->
                        if(results.size() > 0) {
                            for(document in results) {
//                                Log.d(TAG, "onCreate: ${document.data.getValue("name")}")
                                prefHelper.put(Constant.PREF_USERNAME, username)
                                prefHelper.put(Constant.PREF_PASSWORD, md5)
                                prefHelper.put(Constant.PREF_PERSON_NAME, document.data.getValue("name").toString())
                                prefHelper.put(Constant.PREF_IS_LOGIN, true)
                                prefHelper.put(Constant.PREF_USER_ID, document.id)
                                Toast.makeText(this, "Login Berhasil", Toast.LENGTH_LONG).show()
                                moveIntent()
                            }
                        } else
                            Toast.makeText(this, "NIK atau password anda salah", Toast.LENGTH_LONG).show()

                    }
                    .addOnFailureListener { exception ->
                        Log.w(TAG, "onCreate: Error -> $exception", )
                    }

            }

            Log.d(TAG, "onCreate: $username, $password")

        }
    }
}