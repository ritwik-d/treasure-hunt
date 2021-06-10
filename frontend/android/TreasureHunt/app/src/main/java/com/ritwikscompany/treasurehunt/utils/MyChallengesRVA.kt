package com.ritwikscompany.treasurehunt.utils

import android.animation.ObjectAnimator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.ritwikscompany.treasurehunt.R


class MyChallengesRVA(
        var challenges: ArrayList<String>,
        var deleteOnClick: (challengeName: String) -> Unit,
        var editOnClick: (challengeName: String) -> Unit,
        private var minusButton: FloatingActionButton,
        var checkedChallenges: ArrayList<String> = arrayListOf()
): RecyclerView.Adapter<MyChallengesRVA.ViewHolder>() {
    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val checkBox: CheckBox = itemView.findViewById(R.id.rowCheckBox)
        val trashButton: ImageView = itemView.findViewById(R.id.row_delete_challenge)
        val editButton: ImageView = itemView.findViewById(R.id.row_edit_challenge)
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
            checkBoxOnClick(holder.checkBox)
        }
        holder.trashButton.setOnClickListener {
            deleteOnClick(this.challenges[position])
            this.checkedChallenges.remove(this.challenges[position])
            if (this.checkedChallenges.size == 0) {
                this.minusButton.visibility = View.INVISIBLE
            }
        }
        holder.editButton.setOnClickListener {
            editOnClick(this.challenges[position])
        }
    }


    override fun getItemCount(): Int {
        return challenges.size
    }


    private fun checkBoxOnClick(checkBox: CheckBox) {
        if (checkBox.isChecked) {
            this.checkedChallenges.add(checkBox.text.toString())
            this.minusButton.visibility = View.VISIBLE
            val animation = ObjectAnimator.ofFloat(this.minusButton, "translationY", -175f)
            animation.duration = 1000
            animation.start()
        }
        else {
            this.checkedChallenges.remove(checkBox.text.toString())
            if (this.checkedChallenges.size == 0) {
                val animation = ObjectAnimator.ofFloat(this.minusButton, "translationY", 175f)
                animation.duration = 1000
                animation.start()
                this.minusButton.visibility = View.INVISIBLE
            }
        }
    }
}