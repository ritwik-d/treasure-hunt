package com.ritwikscompany.treasurehunt.utils

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ritwikscompany.treasurehunt.R

class RacesRecyclerViewAdapter(var creators: ArrayList<String>, var titles: ArrayList<String>, var groups: ArrayList<String>,
        var onEnterClicked: (raceTitle: String) -> Unit)
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
        holder.creatorTV.text = creators[position]
        holder.raceGroupTV.text = groups[position]
        holder.raceTitleTV.text = titles[position]
        holder.enterBTN.setOnClickListener {
            onEnterClicked(holder.raceTitleTV.text.toString())
        }
    }

    override fun getItemCount(): Int {
        return creators.size + titles.size - groups.size
    }
}