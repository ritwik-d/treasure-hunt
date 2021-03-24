package com.ritwikscompany.treasurehunt.utils

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import com.ritwikscompany.treasurehunt.R
import javax.crypto.Cipher
import javax.crypto.KeyGenerator

class Utils {
    companion object Utils {
        fun getCheckMark(context: Context): Drawable? {
            val myIcon = ContextCompat.getDrawable(context, R.drawable.ic_baseline_done_24)
            myIcon!!.setBounds(
                0,
                0,
                myIcon.intrinsicWidth,
                myIcon.intrinsicHeight
            )
            return myIcon
        }
    }
}