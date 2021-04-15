package com.ritwikscompany.treasurehunt.utils

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ritwikscompany.treasurehunt.R

class FindChallengeRVA(
        var challenges: ArrayList<String>,
        var startOnClick: (challengeName: String) -> Unit
): RecyclerView.Adapter<FindChallengeRVA.ViewHolder>() {
    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val challengeET: TextView = itemView.findViewById(R.id.row_challenge_name)
        val startChallengeBTN: ImageButton = itemView.findViewById(R.id.row_start_challenge)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FindChallengeRVA.ViewHolder {
        val context = parent.context
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.row_find_challenge, parent, false)
        return ViewHolder(view)
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.challengeET.setText(this.challenges[position])
        holder.startChallengeBTN.setOnClickListener {
            startOnClick(this.challenges[position])
        }
    }


    override fun getItemCount(): Int {
        return this.challenges.size
    }
}