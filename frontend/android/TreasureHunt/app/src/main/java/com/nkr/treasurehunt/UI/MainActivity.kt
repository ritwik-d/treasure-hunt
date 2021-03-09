package com.nkr.treasurehunt.UI

import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.nkr.treasurehunt.R
import java.io.File
import java.io.FileReader


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //check if there is already a user logged in
    }

    fun goToSignUpScreen(view: View) {
        startActivity(Intent(this, SignupActivity::class.java))
    }

    fun goToLoginScreen(view: View) {
        startActivity(Intent(this, LoginActivity::class.java))
    }
}