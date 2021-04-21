package com.ritwikscompany.treasurehunt.ui

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import com.github.kittinunf.fuel.Fuel
import com.google.gson.Gson
import com.ritwikscompany.treasurehunt.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private val ctx = this@MainActivity
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkIsLoggedIn()

        findViewById<Button>(R.id.main_login).setOnClickListener {
            logInOnClick()
        }

        findViewById<Button>(R.id.main_signup).setOnClickListener {
            signUpOnClick()
        }
    }


    private fun logInOnClick() {
        startActivity(Intent(ctx, LoginActivity::class.java))
    }


    private fun signUpOnClick() {
        startActivity(Intent(ctx, SignUpActivity::class.java))
    }


    private fun checkIsLoggedIn() {
        val sharedPref = application.getSharedPreferences("userInfo", Context.MODE_PRIVATE)
        val email: String? = sharedPref.getString("email", null)

        if (email != null) {
            val pw = sharedPref.getString("pw", null)!!
            httpCall(email, pw)
        }
    }


    private fun httpCall(email: String, pw: String) {
        val bodyJson = Gson().toJson(hashMapOf(
            "email" to email,
            "pw" to pw,
            "is_hashed" to 1
        ))
        CoroutineScope(Dispatchers.IO).launch {
            val (_, response, result) = Fuel.post("${getString(R.string.host)}/api/login")
                .body(bodyJson)
                .header("Content-Type" to "application/json")
                .response()

            withContext(Dispatchers.Main) {
                runOnUiThread {
                    val status = response.statusCode
                    if (status == 200) {
                        val (bytes, _) = result
                        if (bytes != null) {
                            val userData: HashMap<String, Any> = Gson().fromJson(String(bytes), HashMap::class.java) as HashMap<String, Any>
                            userData["user_id"] = userData["user_id"].toString().toDouble().toInt()

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
                        Toast.makeText(ctx, "Error", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }
}