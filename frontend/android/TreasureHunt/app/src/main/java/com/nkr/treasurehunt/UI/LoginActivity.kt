package com.nkr.treasurehunt.UI

import android.os.Bundle
import android.os.Environment
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.nkr.treasurehunt.R
import java.io.File
import java.io.FileWriter
import java.io.IOException


class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val login: Button? = findViewById(R.id.Login)

        val emailOBJ = findViewById<EditText>(R.id.Email)
        val pwdOBJ = findViewById<EditText>(R.id.Password)

        login?.setOnClickListener({ view ->
            generateJsonAccountFile(emailOBJ.text.toString(), pwdOBJ.text.toString())
        })
    }

    companion object {
        public fun generateJsonAccountFile(email: String?, pwd: String?) {
            try {
                //create a reference to the account directory
                val root = File(Environment.getExternalStorageDirectory(), "account")

                //check if the account directory exists
                if (!root.exists()) {
                    //create the account directory
                    root.mkdirs()
                }

                //create a reference to the json account file
                val gpxfile = File(root, "account.json")

                //delete the file to override the file later
                gpxfile.delete()

                //create the json account file
                gpxfile.createNewFile()

                //create a file writer to write to the json file
                val writer = FileWriter(gpxfile)

                //create a map for the data
                val map = mapOf("email" to email,"pwd" to pwd)

                //convert the map to json format
                val accountInfo = Gson().toJson(map)

                //write json to the file
                writer.append(accountInfo)
                writer.flush()

                //close the file
                writer.close()
            } catch (e: IOException) {
                //a common error has occurred
                e.printStackTrace()
            }
        }
    }
}