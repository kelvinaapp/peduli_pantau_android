package com.example.pedulipantau

import android.bluetooth.le.ScanResult
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.serialization.descriptors.PrimitiveKind
import java.util.*
import kotlin.collections.ArrayList

class QuestionAdapter (private val questionList: List<Question>,
                       private val onClickListener: ((answerList : Answer) -> Unit)
) : RecyclerView.Adapter<QuestionAdapter.ViewHolder>() {

    var rowIndex = 0
    var answerArr = arrayListOf<Answer>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuestionAdapter.ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.list_question, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: QuestionAdapter.ViewHolder, position: Int) {
        val currentItem = questionList[position]
        holder.question.text = currentItem.question
        var answer = Answer(Integer.parseInt(currentItem.number), null)
        answerArr.add(answer)
//        Log.d("OnBindViewHolder", "onBindViewHolder: ${answerArr.toString()}")

        holder.answerNo.setOnClickListener {
            answerOnClick("Tidak", position)
        }
//
        holder.answerYes.setOnClickListener {
            answerOnClick("Iya", position)
        }
//
        if(rowIndex == position) {
            when(answerArr[position].answer) {
                "Iya"-> {
                    holder.answerYes.setImageResource(R.drawable.form_answer_clicked)
                    holder.answerNo.setImageResource(R.drawable.rectangle_rounded)
                }
                "Tidak"-> {
                    holder.answerYes.setImageResource(R.drawable.rectangle_rounded)
                    holder.answerNo.setImageResource(R.drawable.form_answer_clicked)
                }
                null -> {
                    holder.answerYes.setImageResource(R.drawable.rectangle_rounded)
                    holder.answerNo.setImageResource(R.drawable.rectangle_rounded)
                }
            }
        }

//        holder.deviceContainer.setOnClickListener {onClickListener.invoke(currentItem)}
    }

    override fun getItemCount(): Int {
        return questionList.size
    }

    inner class ViewHolder(itemView: View) :RecyclerView.ViewHolder(itemView) {

        var question: TextView
        var answerNo: ImageView
        var answerYes: ImageView

        init {
            question = itemView.findViewById(R.id.item_question)
            answerNo = itemView.findViewById(R.id.answer_no)
            answerYes = itemView.findViewById(R.id.answer_yes)
        }
    }

    private fun answerOnClick(answer : String ?= null, position: Int) {
        rowIndex = position

        //press yes again
        if(answerArr[position].answer == answer)
            answerArr[position].answer = null
        //press yes after null or no
        else
            answerArr[position].answer = answer

        onClickListener.invoke(answerArr[position])

        notifyItemChanged(position)
        Log.d("QuestionAdapter", "onBindViewHolder: clicked yes $answer")
    }
}