package com.nkr.treasurehunt.UI

import android.os.Bundle
import android.os.Environment
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.nkr.treasurehunt.R
import java.io.File
import java.io.FileWriter
import java.io.IOException


class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val login: Button? = findViewById(R.id.Login)

        val emailOBJ = findViewById<EditText>(R.id.Email)
        val pwdOBJ = findViewById<EditText>(R.id.Password)

        login?.setOnClickListener {}
    }
}