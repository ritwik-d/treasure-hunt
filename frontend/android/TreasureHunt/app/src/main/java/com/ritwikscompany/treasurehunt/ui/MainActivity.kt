package com.ritwikscompany.treasurehunt.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.ritwikscompany.treasurehunt.R

class MainActivity : AppCompatActivity() {

    private val ctx = this@MainActivity
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.main_login).setOnClickListener {
            logInOnClick()
        }

        findViewById<Button>(R.id.main_signup).setOnClickListener {
            signUpOnClick()
        }
    }


    private fun logInOnClick() {
        startActivity(Intent(ctx, LoginActivity::class.java))
    }


    private fun signUpOnClick() {
        startActivity(Intent(ctx, SignUpActivity::class.java))
    }
}