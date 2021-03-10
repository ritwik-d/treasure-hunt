package com.nkr.treasurehunt.UI

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.nkr.treasurehunt.R
import com.parse.ParseException
import com.parse.ParseUser

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val login: Button? = findViewById(R.id.Login)

        val emailOBJ = findViewById<EditText>(R.id.Email)
        val pwdOBJ = findViewById<EditText>(R.id.Password)

        login?.setOnClickListener {
            login(emailOBJ.text.toString(), pwdOBJ.text.toString())
        }
    }

    fun login(username: String, password: String) {
        ParseUser.logInInBackground(username,password) { parseUser: ParseUser?, parseException: ParseException? ->
            if (parseUser != null) {
                startActivity(Intent(this, HomeActivity::class.java).apply {})
            } else {
                ParseUser.logOut()
                if (parseException != null) {
                    Toast.makeText(this, parseException.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}