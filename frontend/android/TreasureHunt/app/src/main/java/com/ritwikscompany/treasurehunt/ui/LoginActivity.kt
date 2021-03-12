package com.ritwikscompany.treasurehunt.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
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
    }


    private fun cancelOnClick() {
        startActivity(Intent(ctx, MainActivity::class.java))
    }


    private fun httpCall(email: String, pw: String) {
        val bodyJson = Gson().toJson(hashMapOf(
            "email" to email,
            "pw" to pw
        ))
        CoroutineScope(Dispatchers.IO).launch {
            val (request, response, result) = Fuel.post("${getString(R.string.host)}/login")
                .body(bodyJson)
                .header("Content-Type" to "application/json")
                .response()

            withContext(Dispatchers.Main) {
                runOnUiThread {
                    if (response.isSuccessful) {
                        val status = response.statusCode
                        if (status == 200) {
                            val (bytes, _) = result
                            if (bytes != null) {
                                val userData = Gson().fromJson(String(bytes), HashMap::class.java) as HashMap<String, Any>
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

                    else {
                        Toast.makeText(ctx, "Network Error", Toast.LENGTH_LONG).show()
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