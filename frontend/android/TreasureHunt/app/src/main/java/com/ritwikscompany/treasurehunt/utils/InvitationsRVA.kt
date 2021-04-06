package com.ritwikscompany.treasurehunt.utils

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ritwikscompany.treasurehunt.R

class InvitationsRVA(
    var invitations: ArrayList<HashMap<String, String>>,
    var acceptOnClick: (groupName: String) -> Unit,
    var declineOnClick: (groupName: String) -> Unit
): RecyclerView.Adapter<InvitationsRVA.ViewHolder>() {
    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val groupName: TextView = itemView.findViewById(R.id.row_invite_group)
        val fromName: TextView = itemView.findViewById(R.id.row_invite_from)
        val acceptButton: ImageButton = itemView.findViewById(R.id.row_invite_accept)
        val declineButton: ImageButton = itemView.findViewById(R.id.row_invite_decline)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val context = parent.context
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.row_invitation, parent, false)
        return ViewHolder(view)
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.groupName.text = this.invitations[position]["group_name"]
        holder.fromName.text = this.invitations[position]["from_name"]
        holder.acceptButton.setOnClickListener {
            acceptOnClick(this.invitations[position]["group_name"] as String)
        }
        holder.declineButton.setOnClickListener {
            declineOnClick(this.invitations[position]["group_name"] as String)
        }
    }


    override fun getItemCount(): Int {
        return invitations.size
    }
}