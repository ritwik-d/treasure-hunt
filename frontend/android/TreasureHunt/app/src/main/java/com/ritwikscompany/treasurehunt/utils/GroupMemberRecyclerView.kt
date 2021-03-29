package com.ritwikscompany.treasurehunt.utils

import android.graphics.Bitmap
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ritwikscompany.treasurehunt.R
import de.hdodenhof.circleimageview.CircleImageView
import java.util.ArrayList

class GroupMemberRecyclerView(var users: ArrayList<String>, var pfps: ArrayList<Bitmap>):
    RecyclerView.Adapter<GroupMemberRecyclerView.ViewHolder>() {
    var rows = ArrayList<View>()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val context = parent.context
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.group_member_row_member, parent, false)
        Log.d("TAG", "onCreateViewHolder: $rows")
        return ViewHolder(view)
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.memberTV.text = users[position]
        holder.pfpCIV.setImageBitmap(pfps[position])
        rows.add(holder.itemView)
    }


    override fun getItemCount(): Int {
        return users.size
    }


    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        var pfpCIV: CircleImageView = itemView.findViewById(R.id.gm_pfp)
        var memberTV = itemView.findViewById<TextView>(R.id.gm_username)!!
    }
}