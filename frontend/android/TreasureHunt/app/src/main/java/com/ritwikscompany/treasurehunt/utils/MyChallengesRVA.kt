package com.ritwikscompany.treasurehunt.utils

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.recyclerview.widget.RecyclerView
import com.ritwikscompany.treasurehunt.R

class MyChallengesRVA(var challenges: ArrayList<String>, var checkClickedListener: CheckClickedListener): RecyclerView.Adapter<MyChallengesRVA.ViewHolder>() {
    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val checkBox = itemView.findViewById<CheckBox>(R.id.rowCheckBox)
    }


    interface CheckClickedListener {
        fun onChecked(checkBox: CheckBox, isChecked: Boolean)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val context = parent.context
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.row_my_challenge, parent, false)
        return ViewHolder(view)
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.checkBox.text = this.challenges[position]
        holder.checkBox.setOnClickListener {
            checkClickedListener.onChecked(holder.checkBox, holder.checkBox.isChecked)
        }
    }


    override fun getItemCount(): Int {
        return challenges.size
    }
}