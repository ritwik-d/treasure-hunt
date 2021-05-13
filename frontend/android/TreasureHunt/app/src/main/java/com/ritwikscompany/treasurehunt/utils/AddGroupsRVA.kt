package com.ritwikscompany.treasurehunt.utils

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.recyclerview.widget.RecyclerView
import com.ritwikscompany.treasurehunt.R

class AddGroupsRVA(
    private val groups: ArrayList<String>,
    var checkedGroups: ArrayList<String> = ArrayList()
): RecyclerView.Adapter<AddGroupsRVA.ViewHolder>() {
    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val checkBox: CheckBox = itemView.findViewById(R.id.group_check_box)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val context = parent.context
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.row_checked_group, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.checkBox.text = this.groups[position]
        if (holder.checkBox.text.toString() in checkedGroups) {
            holder.checkBox.isChecked = true
        }
        holder.checkBox.setOnClickListener {
            if (holder.checkBox.isChecked) {
                this.checkedGroups.add(this.groups[position])
            }
            else {
                this.checkedGroups.remove(this.groups[position])
            }
        }
    }

    override fun getItemCount(): Int {
        return groups.size
    }
}