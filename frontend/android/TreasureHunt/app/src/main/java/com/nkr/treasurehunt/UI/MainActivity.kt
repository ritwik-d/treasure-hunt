package com.nkr.treasurehunt.UI

import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.nkr.treasurehunt.R
import java.io.File
import java.io.FileReader


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //check if there is already a user logged in
        if (checkForAccount()) {
            //redirect the user to home screen
            startActivity(Intent(this, HomeActivity::class.java).apply {
                putExtra("email", readAccount())
            })
        }
    }

    private fun checkForAccount() : Boolean {
        //get a reference to the folder
        val root = File(Environment.getExternalStorageDirectory(), "account")

        //return if it exists or not
        return root.exists()
    }

    private fun readAccount() : HashMap<String, String> {
        //read the json data from the local file
        val accountData = Gson().fromJson(FileReader(File(File(Environment
            .getExternalStorageDirectory(),
            "account"), "account.json"))
            .readText(), HashMap::class.java)
                as HashMap<String, String>

        //return the account info
        return accountData
    }

    fun goToSignUpScreen(view: View) {
        startActivity(Intent(this, SignupActivity::class.java))
    }

    fun goToLoginScreen(view: View) {
        startActivity(Intent(this, LoginActivity::class.java))
    }
}