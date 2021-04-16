package com.ritwikscompany.treasurehunt.ui

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.isSuccessful
import com.google.gson.Gson
import com.ritwikscompany.treasurehunt.R
import com.ritwikscompany.treasurehunt.utils.Utils.Utils.isValid
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.regex.Pattern


class SignUpActivity : AppCompatActivity() {

    private val ctx = this@SignUpActivity
    private lateinit var emailET: EditText
    private lateinit var pwET: EditText
    private lateinit var usernameET: EditText

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


    private fun httpCall(email: String, pw: String, username: String) {
        val bodyJson = Gson().toJson(
            hashMapOf(
                "email" to email,
                "pw" to pw,
                "username" to username
            )
        )
        CoroutineScope(Dispatchers.IO).launch {
            val (_, response, _) = Fuel.post("${getString(R.string.host)}/api/register")
                .body(bodyJson)
                .header("Content-Type" to "application/json")
                .response()

            withContext(Dispatchers.Main) {
                runOnUiThread {
                    when (response.statusCode) {
                        201 -> {
                            makeDialog()
                        }
                        400 -> {
                            emailET.error = "An account has already been created with this email"
                            emailET.requestFocus()
                        }
                        402 -> {
                            emailET.error = "Enter a valid email"
                            emailET.requestFocus()
                        }
                        401 -> {
                            usernameET.error = "An account has already been created with this username"
                            usernameET.requestFocus()
                        }
                    }
                }
            }
        }
    }


    private fun signUpOnClick() {
        // get ui objects

        emailET = findViewById(R.id.su_email)
        pwET = findViewById(R.id.su_pw)
        usernameET = findViewById(R.id.su_username)

        val email = emailET.text.toString()
        val pw = pwET.text.toString()
        val username = usernameET.text.toString()

        // verifies info

        if ((!isValid(email)) or (!Patterns.EMAIL_ADDRESS.matcher(email).matches())) {
            emailET.error = "Enter a valid email"
            emailET.requestFocus()
            return
        }

        if (pw.length < 8) {
            pwET.error = "Password length must be 8 or more characters"
            pwET.requestFocus()
            return
        }

        if (username.length < 2) {
            usernameET.error = "Username length must be 3 or more characters"
            usernameET.requestFocus()
            return
        }

        // makes api call

        httpCall(email, pw, username)
    }


    private fun makeDialog() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(ctx)
        builder.setTitle("Account Verification")
        builder.setMessage("You have received an email to verify your account. Please click the link within the email to verify your account. \n\nNOTE: You will not be able to log in until you verify your account.")
        builder.setPositiveButton("OK") { _, _ ->
            startActivity(Intent(ctx, LoginActivity::class.java))
        }
        builder.show()
    }
}