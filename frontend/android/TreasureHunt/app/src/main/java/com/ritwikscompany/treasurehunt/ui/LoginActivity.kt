package com.ritwikscompany.treasurehunt.ui

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.isSuccessful
import com.google.gson.Gson
import com.ritwikscompany.treasurehunt.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginActivity : AppCompatActivity() {

    private val ctx = this@LoginActivity
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        findViewById<Button>(R.id.log_cancel).setOnClickListener {
            cancelOnClick()
        }

        findViewById<Button>(R.id.log_log_in).setOnClickListener {
            logInOnClick()
        }

        findViewById<TextView>(R.id.log_forgot_pw).setOnClickListener {
            forgotOnClick()
        }
    }


    private fun cancelOnClick() {
        startActivity(Intent(ctx, MainActivity::class.java))
    }


    private fun forgotOnClick() {
        val emailET = EditText(ctx)
        emailET.hint = getString(R.string.email)

        val builder = AlertDialog.Builder(ctx)
        builder.setTitle("Treasure Hunt Reset Password")
        builder.setMessage("To reset your password, we need to send you an email to verify that you are not faking and identity. Please enter your email below.")
        builder.setView(emailET)
        builder.setPositiveButton("Send", DialogInterface.OnClickListener { _, _ ->
            val bodyJson = Gson().toJson(hashMapOf(
                "email" to emailET.text.toString(),
            ))
            CoroutineScope(Dispatchers.IO).launch {
                val (request, response, result) = Fuel.post("${getString(R.string.host)}/api/send_email_reset_password")
                    .body(bodyJson)
                    .header("Content-Type" to "application/json")
                    .response()

                withContext(Dispatchers.Main) {
                    runOnUiThread {
                        val status = response.statusCode
                        if (status == 200) {
                            val (bytes, _) = result
                            if (bytes != null) {
                                val body: HashMap<String, String> = Gson().fromJson(String(bytes), HashMap::class.java) as HashMap<String, String>
                                val vcode = body["vcode"]

                                val vcodeET = EditText(ctx)
                                vcodeET.hint = "Verification Code"
                                val builder2 = AlertDialog.Builder(ctx)
                                builder2.setTitle("Treasure Hunt Reset Password")
                                builder2.setMessage("Enter the verification code that was emailed to you.")
                                builder2.setView(vcodeET)
                                builder2.setPositiveButton("Submit", DialogInterface.OnClickListener { _, _ ->
                                    if (vcodeET.text.toString() == vcode) {
                                        val builder3 = AlertDialog.Builder(ctx)
                                        builder3.setTitle("Treasure Hunt Reset Password")
                                        builder3.setMessage("Enter your new password.")
                                        val pwET = EditText(ctx)
                                        pwET.hint = "New Password"
                                        pwET.inputType = InputType.TYPE_TEXT_VARIATION_PASSWORD
                                        builder3.setView(pwET)
                                        builder3.setPositiveButton("Submit", DialogInterface.OnClickListener {_, _ ->
                                            val bodyJson3 = Gson().toJson(
                                                hashMapOf(
                                                    "email" to emailET.text.toString(),
                                                    "new_password" to pwET.text.toString(),
                                                )
                                            )
                                            CoroutineScope(Dispatchers.IO).launch {
                                                val (_, response3, _) = Fuel.post("${getString(R.string.host)}/api/reset_password")
                                                    .body(bodyJson3)
                                                    .header("Content-Type" to "application/json")
                                                    .response()

                                                withContext(Dispatchers.Main) {
                                                    runOnUiThread {
                                                        if (response3.statusCode == 200) {
                                                            Toast.makeText(ctx, "Reset Password Success", Toast.LENGTH_SHORT).show()
                                                        }
                                                        else {
                                                            Toast.makeText(ctx, "Reset Password Failure", Toast.LENGTH_LONG).show()
                                                        }
                                                    }
                                                }
                                            }
                                        })
                                        builder3.setNegativeButton("Cancel", DialogInterface.OnClickListener {_, _ -> })
                                        builder3.show()
                                    } else {
                                        Toast.makeText(ctx, "Verification Code Incorrect", Toast.LENGTH_LONG).show()
                                    }
                                })
                                builder2.setNegativeButton("Cancel") { _, _ -> }
                                builder2.show()
                            }

                            else {
                                Toast.makeText(ctx, "Network Error", Toast.LENGTH_LONG).show()
                            }
                        }

                        else if (status == 404) {
                            Toast.makeText(ctx, "Please enter a valid email", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        })
        builder.setNegativeButton("Cancel") { _, _ -> }
        builder.show()
    }


    private fun httpCall(email: String, pw: String) {
        val bodyJson = Gson().toJson(hashMapOf(
            "email" to email,
            "pw" to pw
        ))
        CoroutineScope(Dispatchers.IO).launch {
            val (request, response, result) = Fuel.post("${getString(R.string.host)}/api/login")
                .body(bodyJson)
                .header("Content-Type" to "application/json")
                .response()

            withContext(Dispatchers.Main) {
                runOnUiThread {
                    val status = response.statusCode
                    if (status == 200) {
                        val (bytes, _) = result
                        if (bytes != null) {
                            val userData = Gson().fromJson(String(bytes), HashMap::class.java) as HashMap<String, Any>
                            userData["user_id"] = userData.get("user_id").toString().toDouble().toInt()

                            val sharedPref = application.getSharedPreferences("userInfo", Context.MODE_PRIVATE)
                            with(sharedPref.edit()) {
                                putString("email", userData.get("email") as String)
                                putString("pw", userData.get("password") as String)
                                apply()
                            }

                            val intent = Intent(ctx, HomeActivity::class.java).apply {
                                putExtra("userData", userData)
                            }
                            startActivity(intent)
                        }

                        else {
                            Toast.makeText(ctx, "Network Error", Toast.LENGTH_LONG).show()
                        }
                    }

                    else if (status == 404) {
                        Toast.makeText(ctx, "Log In Failure", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }


    private fun logInOnClick() {
        // get ui objects

        val emailET = findViewById<EditText>(R.id.log_email)
        val pwET = findViewById<EditText>(R.id.log_pw)

        val email = emailET.text.toString()
        val pw = pwET.text.toString()

        // make login api call

        httpCall(email, pw)
    }
}