package com.nkr.treasurehunt.UI

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
//import com.nkr.treasurehunt.Data.Account
import com.nkr.treasurehunt.R


@Suppress("DEPRECATION")
class SignupActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        //create a reference to a button
        val button = findViewById<Button>(R.id.Signup)

        //create a reference to the all of the inputs
        val usernameOBJ = findViewById<EditText>(R.id.username)
//        val fnameOBJ = findViewById<EditText>(R.id.fname)
//        val lnameOBJ = findViewById<EditText>(R.id.lname)
        val emailOBJ = findViewById<EditText>(R.id.Email)
//        val pwdOBJ = findViewById<EditText>(R.id.Password)

        //check if the username and email are unique
        checkForUniqueness(usernameOBJ)
        checkForUniqueness(emailOBJ)

        button.setOnClickListener {
            if (usernameOBJ.error == "Username is already taken") {
                return@setOnClickListener
            }

//            val email = emailOBJ.text.toString()
//            val pwd = pwdOBJ.text.toString()
//            val fname = fnameOBJ.text.toString()
//            val lname = lnameOBJ.text.toString()
//            val username = usernameOBJ.text.toString()

//            val account = Account(
//                    email, pwd, fname, lname, username
//            )
        }
    }

    private fun checkForUniqueness(editText: EditText) {
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            @SuppressLint("UseCompatLoadingForDrawables")
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                TODO("Have Not Setup Database and API")
//                val drawable = resources.getDrawable(R.drawable.ic_baseline_done_24)
//                drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
//                editText.setError("Available", drawable)
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    companion object {
//        private const val TAG = "SignupActivity"
    }
}