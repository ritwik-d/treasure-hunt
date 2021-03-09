package com.nkr.treasurehunt.UI

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.nkr.treasurehunt.R


class SignupActivity : AppCompatActivity() {
    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        //create a reference to a button
        val button = findViewById<Button>(R.id.Signup)

        //create a reference to the username input
        val username = findViewById<EditText>(R.id.username)

        //check if the username is unique
        username.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(charSequence: CharSequence, start: Int, before: Int, count: Int) {
                val drawable = resources.getDrawable(R.drawable.ic_baseline_done_24)
                drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
                username.setError("Available", drawable)
            }

            override fun afterTextChanged(editable: Editable) {}
        })
    }
}