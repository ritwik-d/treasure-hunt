package com.ritwikscompany.treasurehunt.utils

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ritwikscompany.treasurehunt.R

class RacesRecyclerViewAdapter(var races: ArrayList<Race>,
        var onEnterClicked: (race: Race) -> Unit)
    : RecyclerView.Adapter<RacesRecyclerViewAdapter.ViewHolder>() {
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val creatorTV = itemView.findViewById<TextView>(R.id.row_race_creator)!!
        val raceTitleTV = itemView.findViewById<TextView>(R.id.row_race_title)!!
        val raceGroupTV = itemView.findViewById<TextView>(R.id.row_race_group)!!
        val enterBTN = itemView.findViewById<Button>(R.id.row_race_enter)!!
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val context = parent.context
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.row_race, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.creatorTV.text = races[position].creatorName
        holder.raceGroupTV.text = races[position].groupName
        holder.raceTitleTV.text = races[position].title
        holder.enterBTN.setOnClickListener {
            onEnterClicked(races[position])
        }
    }

    override fun getItemCount(): Int {
        return this.races.size
    }
}