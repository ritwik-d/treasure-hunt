package com.ritwikscompany.treasurehunt.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.github.kittinunf.fuel.Fuel
import com.google.gson.Gson
import com.ritwikscompany.treasurehunt.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class MainActivity : AppCompatActivity() {

    private val ctx = this@MainActivity
//    private lateinit var mGoogleSignInClient: GoogleSignInClient
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//                .requestEmail()
//                .build()
//
//        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        checkIsLoggedIn()

        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.main_login).setOnClickListener {
            logInOnClick()
        }

        findViewById<Button>(R.id.main_signup).setOnClickListener {
            signUpOnClick()
        }

//        findViewById<SignInButton>(R.id.signInButton).setOnClickListener {
//            signUpWithGoogle()
//        }
    }

//    private fun signUpWithGoogle() {
//        val signInIntent = mGoogleSignInClient.signInIntent
//        startActivityForResult(signInIntent, RC_G_SIGN_IN)
//    }


    private fun logInOnClick() {
        startActivity(Intent(ctx, LoginActivity::class.java))
    }


    private fun signUpOnClick() {
        startActivity(Intent(ctx, SignUpActivity::class.java))
    }


    private fun checkIsLoggedIn() {
//        val lastSignedInAccount = GoogleSignIn.getLastSignedInAccount(ctx)
//
//        if (lastSignedInAccount != null) {
//            httpCall(lastSignedInAccount.email, null)
//        }

        val sharedPref = application.getSharedPreferences("userInfo", Context.MODE_PRIVATE)
        val email: String? = sharedPref.getString("email", null)

        if (email != null) {
            val pw = sharedPref.getString("pw", null)!!
            httpCall(email, pw)
        }
    }


    private fun httpCall(email: String?, pw: String?) {
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

//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//
//        if (requestCode == RC_G_SIGN_IN) {
//            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
//            handleSignInResult(task)
//        }
//    }
//
//    private fun handleSignInResult(task: Task<GoogleSignInAccount>) {
//        try {
//            val account: GoogleSignInAccount = task.getResult(ApiException::class.java)
//            val email = account.email
//            val username = account.displayName
//
//            gSignInApiCall(email, username)
//        } catch (e: ApiException) {
//            println(e)
//            Toast.makeText(ctx, "Google Sign in Error!", Toast.LENGTH_SHORT).show()
//        }
//    }
//
//    private fun gSignInApiCall(email: String?, username: String?) {
//        val bodyJson = Gson().toJson(hashMapOf(
//                "email" to email,
//                "username" to username
//         ))
//
//        CoroutineScope(Dispatchers.IO).launch {
//            val (_, response, result) = Fuel.post("${getString(R.string.host)}/api/google_sign_in")
//                    .body(bodyJson)
//                    .header("Content-Type" to "application/json")
//                    .response()
//
//            withContext(Dispatchers.Main) {
//                runOnUiThread {
//                    when (response.statusCode) {
//                        200, 201 -> {
//                            val (bytes, _) = result
//
//                            if (bytes != null) {
//                                val type = object : TypeToken<HashMap<String, Any>>() {}.type
//
//                                val userData = Gson().fromJson(String(bytes), type) as HashMap<String, Any>
//
//                                val sharedPref = application.getSharedPreferences("userInfo", Context.MODE_PRIVATE)
//
//                                with(sharedPref.edit()) {
//                                    putString("email", userData["email"] as String)
//                                    putString("pw", userData["password"] as String)
//                                    apply()
//                                }
//
//                                startActivity(Intent(ctx, HomeActivity::class.java).apply {
//                                    putExtra("userData", userData)
//                                })
//                            }
//                        }
//                    }
//                }
//            }
//        }
//    }
}