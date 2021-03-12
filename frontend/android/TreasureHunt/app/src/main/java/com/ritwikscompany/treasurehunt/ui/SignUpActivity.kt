package com.ritwikscompany.treasurehunt.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.isSuccessful
import com.google.gson.Gson
import com.ritwikscompany.treasurehunt.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.regex.Pattern


@Suppress("DEPRECATION")
class SignUpActivity : AppCompatActivity() {

    private val ctx = this@SignUpActivity
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        val myIcon =
        resources.getDrawable(R.drawable.ic_baseline_done_24)
        myIcon.setBounds(
            0,
            0,
            myIcon.intrinsicWidth,
            myIcon.intrinsicHeight
        )

        val log_in_button = findViewById<Button>(R.id.su_sign_up)

        findViewById<Button>(R.id.su_cancel).setOnClickListener {
            cancelOnClick()
        }

        log_in_button.setOnClickListener {
            signUpOnClick()
        }

        log_in_button.isEnabled = true

        val emailET = findViewById<EditText>(R.id.su_email)
        val pwET = findViewById<EditText>(R.id.su_pw)
        val usernameET = findViewById<EditText>(R.id.su_username)

        emailET.setError("Invalid Email")

        emailET.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            @SuppressLint("UseCompatLoadingForDrawables")
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val email = emailET.text.toString()

                if (isValid(email)) {
                    emailET.setError("Invalid Email")
                    return
                }

                findViewById<Button>(R.id.LogIn).visibility = View.INVISIBLE
                findViewById<Button>(R.id.LogIn).setOnClickListener {}
                CoroutineScope(Dispatchers.IO).launch {
                    val (request, response, result) =
                        Fuel.post(
                            "${getString(R.string.host)}/" +
                                    "verify_email"
                        )
                            .header("Content-Type" to "application/json")
                            .response()

                    withContext(Dispatchers.Main) {
                        runOnUiThread {
                            if (response.isSuccessful) {
                                val status = response.statusCode
                                if (status == 200) {
                                    emailET.setError("", myIcon)

                                    if (usernameET.error == "") {
                                        log_in_button.isEnabled = true
                                    }
                                } else if (status == 400) {
                                    emailET.setError(
                                        "This email is already being used  for" +
                                                " another account."
                                    )
                                    Toast.makeText(
                                        ctx,
                                        "Please press the log in button if you " +
                                                "wish to login to this account.",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    findViewById<Button>(R.id.LogIn).visibility = View.VISIBLE
                                    findViewById<Button>(R.id.LogIn).setOnClickListener {
                                        startActivity(Intent(ctx, LoginActivity::class.java)
                                            .apply {
                                                putExtra("email", email)
                                            })
                                    }
                                }
                            } else {
                                Toast.makeText(ctx, "Network Error", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        usernameET.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                CoroutineScope(Dispatchers.IO).launch {
                    val (request, response, result) =
                        Fuel.post(
                            "${getString(R.string.host)}/" +
                                    "verify_username"
                        )
                            .header("Content-Type" to "application/json")
                            .response()

                    withContext(Dispatchers.Main) {
                        runOnUiThread {
                            if (response.isSuccessful) {
                                val status = response.statusCode
                                if (status == 200) {
                                    val myIcon =
                                        resources.getDrawable(R.drawable.ic_baseline_done_24)
                                    myIcon.setBounds(
                                        0,
                                        0,
                                        myIcon.intrinsicWidth,
                                        myIcon.intrinsicHeight
                                    )
                                    usernameET.setError("", myIcon)

                                    if (emailET.error == "") {
                                        log_in_button.isEnabled = true
                                    }
                                } else if (status == 400) {
                                    usernameET.error = "This username is already being used  for" +
                                            " another account."
                                }
                            } else {
                                Toast.makeText(ctx, "Network Error", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {}
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
            val (request, response, result) = Fuel.post("${getString(R.string.host)}/register")
                .body(bodyJson)
                .header("Content-Type" to "application/json")
                .response()

            withContext(Dispatchers.Main) {
                runOnUiThread {
                    if (response.isSuccessful) {
                        val status = response.statusCode
                        if (status == 201) {
                            startActivity(Intent(ctx, LoginActivity::class.java))
                        } else if (status == 400) {
                            Toast.makeText(
                                ctx,
                                "An account has already been created with this email",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }

                    else {
                        Toast.makeText(ctx, "Network Error", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }


    private fun signUpOnClick() {
        // get ui objects

        val emailET = findViewById<EditText>(R.id.su_email)
        val pwET = findViewById<EditText>(R.id.su_pw)
        val usernameET = findViewById<EditText>(R.id.su_username)

        val email = emailET.text.toString()
        val pw = pwET.text.toString()
        val username = usernameET.text.toString()

        // makes api call

        httpCall(email, pw, username)
    }

    private fun isValid(email: String?) : Boolean
    {
        val emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\."+
        "[a-zA-Z0-9_+&*-]+)*@" +
                "(?:[a-zA-Z0-9-]+\\.)+[a-z" +
                "A-Z]{2,7}$"

        val pat = Pattern.compile(emailRegex)
        if (email == null)
            return false

        return pat.matcher(email).matches()
    }
}