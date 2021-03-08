package com.nkr.treasurehunt.UI

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.nkr.treasurehunt.R
import com.nkr.treasurehunt.UI.LoginActivity.Companion.generateJsonAccountFile

class SignupActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        val button = findViewById<Button>(R.id.Signup)

        button.setOnClickListener{view ->
            generateJsonAccountFile(null, null)
        }
    }
}