package com.ritwikscompany.treasurehunt.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.github.kittinunf.fuel.Fuel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.gson.Gson
import com.ritwikscompany.treasurehunt.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


@Suppress("UNCHECKED_CAST")
class MainActivity : AppCompatActivity() {

    private val ctx = this@MainActivity
    private lateinit var mGoogleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkIsLoggedIn()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(ctx, gso)

        val account = GoogleSignIn.getLastSignedInAccount(ctx)
        updateUI(account)

        findViewById<Button>(R.id.main_login).setOnClickListener {
            logInOnClick()
        }

        findViewById<Button>(R.id.main_signup).setOnClickListener {
            signUpOnClick()
        }

        findViewById<SignInButton>(R.id.main_sign_in_button)
            .setOnClickListener {
                signInWithGoogle()
            }
    }

    private fun signInWithGoogle() {
        val signInIntent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, G_SIGN_IN)
    }

    private fun updateUI(account: GoogleSignInAccount?) {
        if (account != null) {
            val sharedPreferences = application.getSharedPreferences("userInfo", Context.MODE_PRIVATE)

            with (sharedPreferences.edit()) {
                putString("email", account.email)
                putString("username", account.displayName)
                putString("pw", null)
                apply()
            }

            gSignUp(account)
        }
    }

    private fun logInOnClick() {
        startActivity(Intent(ctx, LoginActivity::class.java))
    }

    private fun signUpOnClick() {
        startActivity(Intent(ctx, SignUpActivity::class.java))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == G_SIGN_IN) {
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)

            updateUI(account)
        } catch (e: ApiException) {
            Log.w(TAG, "signInResult:failed code=" + e.statusCode)
        }
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
        val bodyJson = Gson().toJson(
            hashMapOf(
                "email" to email,
                "pw" to pw,
                "is_hashed" to 1
            )
        )
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
                            val userData: HashMap<String, Any> = Gson().fromJson(
                                String(bytes),
                                HashMap::class.java
                            ) as HashMap<String, Any>
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

    companion object {
        private const val TAG = "MainActivity"
        private const val G_SIGN_IN = 9567
    }
}