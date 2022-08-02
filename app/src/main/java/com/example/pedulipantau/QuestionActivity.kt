package com.example.pedulipantau

import kotlinx.serialization.*
import kotlinx.serialization.json.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.JsonWriter
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.LocalDateTime
import java.time.ZoneId

class QuestionActivity : AppCompatActivity() {

    private val TAG = "LogHarianActivity"
    private val PASIEN = "EiDoLWlIXYBwx1EaqpU4"

    private val db = Firebase.firestore
    private val questions = arrayListOf<Question>()
    private val answers = arrayListOf<String>()

    private lateinit var newRecyclerView : RecyclerView

    private var questionAdapter = QuestionAdapter(questions) { answer ->

        var json = Json.encodeToString(answer)

        try {
            if(answer.answer == "Iya" || answer.answer == "Tidak")
                answers[answer.position] = json
        } catch (ex : IndexOutOfBoundsException) {
            answers.add(json)
        }

    }

    private fun getQuestions() {
        db.collection("formlog")
            .orderBy("nomor", Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
//                    Log.d(TAG, "${document.id} => ${document.data.getValue("question")}")
                    var question = Question(document.data.getValue("nomor").toString(),
                        document.data.getValue("question").toString())

                    questions.add(question)

                    newRecyclerView = findViewById(R.id.question_list)
                    newRecyclerView.layoutManager = LinearLayoutManager(this)
                    newRecyclerView.adapter = questionAdapter
                }
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents.", exception)
            }
    }

    private fun submitAnswer() {

        if(answers.size == questions.size) {

            val data = hashMapOf(
                "created_at" to "${LocalDateTime.now(ZoneId.of("GMT+7"))}",
                "answer" to answers,
            )

            db.collection("pasien").document(PASIEN).collection("log")
                .add(data)
                .addOnSuccessListener {
                    Log.d(TAG, "Answer successfully written!")
                    Toast.makeText(this, "Laporan harian berhasil diinputkan", Toast.LENGTH_LONG).show()
                    finish()
                }
                .addOnFailureListener { e -> Log.w(TAG, "Error writing document", e) }

            //data success and finish

        } else {
            Toast.makeText(this, "Tolong jawab semua pertanyaan diatas", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log_harian)

        getQuestions()

        //change title
        val title = findViewById<TextView>(R.id.activity_title)
        title.setText("Laporan Harian")

        //back to main menu
        val btnBack = findViewById<ImageView>(R.id.btn_back)
        btnBack.setOnClickListener{
            finish()
        }

        //submit form
        val btnSubmit = findViewById<Button>(R.id.btn_submit)
        btnSubmit.setOnClickListener{
            submitAnswer()
        }
    }

}