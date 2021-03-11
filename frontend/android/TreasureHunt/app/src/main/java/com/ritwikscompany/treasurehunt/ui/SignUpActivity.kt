package com.ritwikscompany.treasurehunt.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.github.kittinunf.fuel.Fuel
import com.google.gson.Gson
import com.ritwikscompany.treasurehunt.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SignUpActivity : AppCompatActivity() {

    private val ctx = this@SignUpActivity
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        findViewById<Button>(R.id.su_cancel).setOnClickListener {
            cancelOnClick()
        }

        findViewById<Button>(R.id.su_sign_up).setOnClickListener {
            signUpOnClick()
        }
    }


    private fun cancelOnClick() {
        startActivity(Intent(ctx, MainActivity::class.java))
    }


    private fun signUpOnClick() {
        // get ui objects

        val emailET = findViewById<EditText>(R.id.su_email)
        val pwET = findViewById<EditText>(R.id.su_pw)
        val usernameET = findViewById<EditText>(R.id.su_username)

        val email = emailET.text.toString()
        val pw = pwET.text.toString()
        val username = usernameET.text.toString()

        // checks if user is entering valid info

        if (pw.length < 8) {
            pwET.error = "Password must have more than 7 characters"
            pwET.requestFocus()
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailET.error = "Enter a valid email"
            emailET.requestFocus()
            return
        }

        if (username.length < 4) {
            usernameET.error = "Username must have more than 3 characters"
            usernameET.requestFocus()
            return
        }

        // makes api call

        val bodyJson = Gson().toJson(hashMapOf(
            "email" to email,
            "pw" to pw,
            "username" to username
        ))

        CoroutineScope(Dispatchers.IO).launch {
            val (request, response, result) = Fuel.post("http://192.168.1.56:8000/register")
                .body(bodyJson)
                .response()

            withContext(Dispatchers.Main) {
                runOnUiThread {
                    val status = response.statusCode
                    if (status == 201) {
                        startActivity(Intent(ctx, MainActivity::class.java))
                    }

                    else if (status == 400) {
                        Toast.makeText(ctx, "An account has already been created with this email", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }
}