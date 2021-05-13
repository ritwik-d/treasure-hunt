package com.ritwikscompany.treasurehunt.ui

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.github.kittinunf.fuel.Fuel
import com.google.gson.Gson
import com.ritwikscompany.treasurehunt.R
import com.ritwikscompany.treasurehunt.utils.Utils.Utils.getCheckMark
import com.ritwikscompany.treasurehunt.utils.Utils.Utils.isValid
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class SignUpActivity : AppCompatActivity() {

    private val ctx = this@SignUpActivity
    private lateinit var emailET: EditText
    private lateinit var pwET: EditText
    private lateinit var usernameET: EditText
    private lateinit var button: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        emailET = findViewById(R.id.su_email)
        pwET = findViewById(R.id.su_pw)
        usernameET = findViewById(R.id.su_username)
        button = findViewById(R.id.su_sign_up)

        button.isEnabled = false

        setUp()

        findViewById<Button>(R.id.su_cancel).setOnClickListener {
            cancelOnClick()
        }

        findViewById<Button>(R.id.su_sign_up).setOnClickListener {
            signUpOnClick()
        }
    }


    override fun onBackPressed() {
        startActivity(Intent(ctx, MainActivity::class.java))
    }


    private fun setUp() {
        emailET.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                val text = p0.toString()

                if ((!isValid(text)) or (!Patterns.EMAIL_ADDRESS.matcher(text).matches())) {
                    emailET.error = "Enter a valid email"
                    emailET.requestFocus()
                    button.isEnabled = false
                    return
                }
                else {
                    emailET.setError("Good", getCheckMark(ctx))
                    if (usernameET.error == "Good" && pwET.error == "Good" && emailET.error == "Good") {
                        button.isEnabled = true
                    }
                }
            }

            override fun afterTextChanged(p0: Editable?) {}
        })

        pwET.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                val text = p0.toString()

                if (text.length < 8) {
                    pwET.error = "Password length must be 8 or more characters"
                    pwET.requestFocus()
                    button.isEnabled = false

                    return
                }
                else {
                    pwET.setError("Good", getCheckMark(ctx))
                    if (usernameET.error == "Good" && pwET.error == "Good" && emailET.error == "Good") {
                        button.isEnabled = true
                    }
                }
            }

            override fun afterTextChanged(p0: Editable?) {}
        })

        usernameET.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                val text = p0.toString()

                if (text.length < 2) {
                    usernameET.error = "Username length must be 3 or more characters"
                    usernameET.requestFocus()
                    button.isEnabled = false

                    return
                }
                else {
                    usernameET.setError("Good", getCheckMark(ctx))
                    if (usernameET.error == "Good" && pwET.error == "Good" && emailET.error == "Good") {
                        button.isEnabled = true
                    }
                }
            }

            override fun afterTextChanged(p0: Editable?) {}
        })
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
                            usernameET.error =
                                "An account has already been created with this username"
                            usernameET.requestFocus()
                        }
                    }
                }
            }
        }
    }


    private fun signUpOnClick() {
        val email = emailET.text.toString()
        val pw = pwET.text.toString()
        val username = usernameET.text.toString()

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