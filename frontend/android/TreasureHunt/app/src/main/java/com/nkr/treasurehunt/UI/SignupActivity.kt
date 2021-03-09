package com.nkr.treasurehunt.UI

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.nkr.treasurehunt.Data.Account
import com.nkr.treasurehunt.R
import com.parse.ParseUser


class SignupActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        //create a reference to a button
        val button = findViewById<Button>(R.id.Signup)

        //create a reference to the all of the inputs
        val usernameOBJ = findViewById<EditText>(R.id.username)
        val fnameOBJ = findViewById<EditText>(R.id.fname)
        val lnameOBJ = findViewById<EditText>(R.id.lname)
        val emailOBJ = findViewById<EditText>(R.id.Email)
        val pwdOBJ = findViewById<EditText>(R.id.Password)

        //check if the username is unique

        button.setOnClickListener { view ->
            if (usernameOBJ.error.equals("Username is already taken")) {
                return@setOnClickListener
            }

            val email = emailOBJ.text.toString()
            val pwd = pwdOBJ.text.toString()
            val fname = fnameOBJ.text.toString()
            val lname = lnameOBJ.text.toString()
            val username = usernameOBJ.text.toString()

            val user = ParseUser()
            val account = Account(
                    email, pwd, fname, lname, username
            )

            user.email = email
            user.setPassword(pwd)
            user.username = username

            user.signUpInBackground{
                if (it != null) {
                    Log.e(TAG, "onCreate: ", it)
                    ParseUser.logOut()
                    return@signUpInBackground
                }

                
            }
        }
    }

    fun checkForUniqueness(editText: EditText) {
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            @SuppressLint("UseCompatLoadingForDrawables")
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val drawable = resources.getDrawable(R.drawable.ic_baseline_done_24)
                drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
                editText.setError("Available", drawable)
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    companion object {
        private const val TAG = "SignupActivity"
    }
}