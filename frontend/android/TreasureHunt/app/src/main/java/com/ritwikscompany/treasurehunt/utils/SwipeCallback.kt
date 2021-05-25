package com.ritwikscompany.treasurehunt.utils

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

//class SwipeCallback(private var adapter: MyChallengesRVA?) :
//    ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
//    override fun onMove(
//        recyclerView: RecyclerView,
//        viewHolder: RecyclerView.ViewHolder,
//        target: RecyclerView.ViewHolder
//    ): Boolean {
//
//    }
//
//
//    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
//        val position = viewHolder.adapterPosition
//        adapter.deleteItem(position)
//    }
//
//
//    private var icon: Drawable? = null
//    private var background: ColorDrawable? = null
//
//    constructor(adapter: MyChallengesRVA): super(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
//        this.adapter = adapter
//        icon = ContextCompat.getDrawable(
//            this.adapter.context,
//            R.drawable.ic_delete_white_36
//        )
//        background = ColorDrawable(Color.RED)
//    }
//}