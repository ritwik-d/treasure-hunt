package com.nkr.treasurehunt.UI

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.nkr.treasurehunt.R
import com.parse.ParseUser


class SignupActivity : AppCompatActivity() {
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        val button = findViewById<Button>(R.id.Signup)
        val usernameOBJ = findViewById<EditText>(R.id.Username)

        val myIcon = resources.getDrawable(R.drawable.ic_baseline_done_24, resources.newTheme())
        myIcon.setBounds(0, 0, myIcon.intrinsicWidth, myIcon.intrinsicHeight)

        usernameOBJ.setError("Username is available", myIcon)

        button.setOnClickListener{ view ->
            val user = ParseUser()
        }
    }
}