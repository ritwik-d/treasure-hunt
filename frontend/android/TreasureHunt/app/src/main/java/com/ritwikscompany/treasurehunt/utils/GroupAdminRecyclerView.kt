package com.ritwikscompany.treasurehunt.utils

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Bitmap
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ritwikscompany.treasurehunt.R
import de.hdodenhof.circleimageview.CircleImageView
import java.util.*
import kotlin.collections.HashMap

class GroupAdminRecyclerView(var users: ArrayList<String>, var pfps: ArrayList<Bitmap>, val context: Context, var removeMemberOnClick: (member: String, userData2: HashMap<String, Any>, groupData2: HashMap<String, Any>) -> Unit, var userData: HashMap<String, Any>, var groupData: HashMap<String, Any>):
    RecyclerView.Adapter<GroupAdminRecyclerView.ViewHolder>() {
    var rows = ArrayList<View>()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val context = parent.context
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.group_member_row_admin, parent, false)
        Log.d("TAG", "onCreateViewHolder: $rows")
        return ViewHolder(view)
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.memberTV.text = users[position]
        holder.pfpCIV.setImageBitmap(pfps[position])

        holder.removeMemberButton.setOnClickListener {
            removeMember(holder.memberTV.text.toString())
        }

        rows.add(holder.itemView)
    }


    override fun getItemCount(): Int {
        return users.size
    }


    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val pfpCIV: CircleImageView = itemView.findViewById(R.id.gm_pfp)
        val memberTV = itemView.findViewById<TextView>(R.id.gm_username)!!
        val removeMemberButton: ImageButton = itemView.findViewById(R.id.gm_remove)
    }


    private fun removeMember(username: String) {
        val builder = AlertDialog.Builder(this.context)
        builder.setTitle("Are you sure you want to remove $username?")
        builder.setPositiveButton("Yes", DialogInterface.OnClickListener {_, _ ->
            removeMemberOnClick(username, this.userData, this.groupData)
        })
        builder.setNegativeButton("Cancel", DialogInterface.OnClickListener {_, _ -> })
        builder.show()
    }
}