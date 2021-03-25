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
            val (_, response, _) = Fuel.post("${getString(R.string.host)}/register")
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


    private fun isValid(emailText: String?): Boolean {
        val emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\."+
        "[a-zA-Z0-9_+&*-]+)*@" +
                "(?:[a-zA-Z0-9-]+\\.)+[a-z" +
                "A-Z]{2,7}$"

        val pat = Pattern.compile(emailRegex)
        if (emailText == null)
            return false

        return pat.matcher(emailText).matches()
    }


    private fun makeDialog() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(ctx)
        builder?.setTitle("Account Verification")
        builder?.setMessage("You have received an email to verify your account. Please click the link within the email to verify your account. \nNOTE: You will not be able to log in until you verify your account.")
        builder?.setPositiveButton("OK", DialogInterface.OnClickListener { _, _ ->
            startActivity(Intent(ctx, LoginActivity::class.java))
        })
    }


    // commented out textChangeListener


//    val myIcon =
//    resources.getDrawable(R.drawable.ic_baseline_done_24)
//    myIcon.setBounds(
//    0,
//    0,
//    myIcon.intrinsicWidth,
//    myIcon.intrinsicHeight
//    )
//
//    val bodyJson = Gson().toJson(hashMapOf<Any, Any>())
//    CoroutineScope(Dispatchers.IO).launch {
//        val (request, response, result) = Fuel.post("${getString(R.string.host)}/get_users")
//                .body(bodyJson)
//                .header("Content-Type" to "application/json")
//                .response()
//
//        withContext(Dispatchers.Main) {
//            runOnUiThread {
//                if (response.isSuccessful) {
//                    val status = response.statusCode
//                    if (status == 200) {
//                        val (bytes, _) = result
//                        if (bytes != null) {
//                            val users = Gson().fromJson(String(bytes), HashMap::class.java) as MutableList<HashMap<String, String>>
//                            findViewById<Button>(R.id.su_cancel).setOnClickListener {
//                                cancelOnClick()
//                            }
//
//                            signUpButton.setOnClickListener {
//                                signUpOnClick()
//                            }
//
//                            signUpButton.isEnabled = true
//
//
//                            emailET.setError("Invalid Email")
//
//                            emailET.addTextChangedListener(object : TextWatcher {
//                                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
//
//
//                                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
//                                    if (!isValid(email)) {
//                                        emailET.error = "Invalid Email"
//                                        return
//                                    }
//
//                                    var isRepeat = false
//                                    for (user in users) {
//                                        if (email == user.get("email")) {
//                                            isRepeat = true
//                                            emailET.error = "This email has already been used to create an account"
//                                            break
//                                        }
//
//                                        if (username == user.get("username")) {
//                                            usernameET.error = "This username has already been used to create an account"
//                                        }
//                                    }
//
//                                    var usernameIsValid = false
//                                    var pwIsValid = false
//                                    if (!isRepeat) {
//                                        emailET.setError("", myIcon)
//                                    }
//
//                                    if (usernameET.error.equals(null)) {
//                                        usernameET.setError("", myIcon)
//                                        usernameIsValid = true
//                                    }
//
//                                    if (pw.length < 8) {
//                                        pwET.error = "Enter a password longer than 7 characters"
//                                    }
//
//                                    else {
//                                        pwIsValid = true
//                                    }
//                                }
//
//
//                                override fun afterTextChanged(s: Editable?) {}
//                            })
//
//                            usernameET.addTextChangedListener(object : TextWatcher {
//                                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
//
//
//                                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
//                                }
//
//
//                                override fun afterTextChanged(s: Editable?) {}
//                            })
//
//
//                            pwET.addTextChangedListener(object : TextWatcher {
//                                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
//
//
//                                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
//                                }
//
//
//                                override fun afterTextChanged(s: Editable?) {}
//                            })
//                        }
//
//                        else {
//                            Toast.makeText(ctx, "Network Error", Toast.LENGTH_LONG).show()
//                            println("yes")
//                        }
//                    }
//
//                    else if (status == 404) {
//                        Toast.makeText(ctx, "Sign up Failure", Toast.LENGTH_LONG).show()
//                    }
//                }
//
//                else {
//                    Toast.makeText(ctx, "Network Error", Toast.LENGTH_LONG).show()
//                }
//            }
//        }
}