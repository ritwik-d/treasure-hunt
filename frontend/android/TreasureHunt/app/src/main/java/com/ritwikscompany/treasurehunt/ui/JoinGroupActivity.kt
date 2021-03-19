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

class JoinGroupActivity : AppCompatActivity() {

    private val ctx = this@JoinGroupActivity
    private lateinit var userData: HashMap<String, Any>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_join_group)
        this.userData = intent.getSerializableExtra("userData") as HashMap<String, Any>

        findViewById<Button>(R.id.jg_join).setOnClickListener {
            joinGroupOnClick()
        }
    }


    private fun joinGroupOnClick() {
        val joinCodeET = findViewById<EditText>(R.id.jg_code)
        val joinCode = joinCodeET.text.toString()

        val bodyJson = Gson().toJson(hashMapOf(
            "pw" to userData.get("password") as String,
            "user_id" to userData.get("user_id") as Int,
            "join_code" to joinCode
        ))
        CoroutineScope(Dispatchers.IO).launch {
            val (request, response, result) = Fuel.post("${getString(R.string.host)}/join_group")
                    .body(bodyJson)
                    .header("Content-Type" to "application/json")
                    .response()

            withContext(Dispatchers.Main) {
                runOnUiThread {
                    when (response.statusCode) {
                        200 -> {
                            val intent = Intent(ctx, GroupsActivity::class.java).apply {
                                putExtra("userData", userData)
                            }
                            startActivity(intent)
                        }
                        404 -> {
                            joinCodeET.error = "Invalid join code"
                            joinCodeET.requestFocus()
                        }
                        400 -> {
                            joinCodeET.error = "You have already joined/created this group"
                        }
                    }
                }
            }
        }
    }
}