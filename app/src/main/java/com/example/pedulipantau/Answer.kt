package com.example.pedulipantau
import kotlinx.serialization.*

@Serializable
data class Answer(var position : Int, var answer : String ?= null)
