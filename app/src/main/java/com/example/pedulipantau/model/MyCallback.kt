package com.example.pedulipantau.model

import com.google.firebase.firestore.QueryDocumentSnapshot

interface MyCallback {
    fun onCallback(value: QueryDocumentSnapshot)
}