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

class CreateGroupActivity : AppCompatActivity() {

    private val ctx = this@CreateGroupActivity
    private lateinit var userData: HashMap<String, Any>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_group)
        this.userData = intent.getSerializableExtra("userData") as HashMap<String, Any>

        findViewById<Button>(R.id.cg_create).setOnClickListener {
            createGroupOnClick()
        }
    }


    private fun createGroupOnClick() {
        val groupNameET = findViewById<EditText>(R.id.cg_name)
        val groupDescET = findViewById<EditText>(R.id.cg_desc)

        val groupName = groupNameET.text.toString()
        var groupDesc: String? = groupDescET.text.toString()

        if (groupName.length < 3) {
            groupNameET.error = "Group name must be at least 3 characters long"
            groupNameET.requestFocus()
            return
        }

        if (groupDesc == "") {
            groupDesc = null
        }

        httpCall(groupName, groupDesc)
    }


    private fun httpCall(groupName: String, groupDesc: String?) {
        val bodyJson = Gson().toJson(hashMapOf(
            "pw" to userData.get("password"),
            "user_id" to userData.get("user_id"),
            "name" to groupName,
            "description" to groupDesc
        ))
        CoroutineScope(Dispatchers.IO).launch {
            val (request, response, result) = Fuel.post("${getString(R.string.host)}/create_group")
                    .body(bodyJson)
                    .header("Content-Type" to "application/json")
                    .response()

            withContext(Dispatchers.Main) {
                runOnUiThread {
                    if (response.isSuccessful) {
                        val status = response.statusCode
                        if (status == 201) {
                            val intent = Intent(ctx, GroupPageActivity::class.java).apply {
                                putExtra("groupName", groupName)
                                putExtra("userData", userData)
                            }
                            startActivity(intent)
                        } else if (status == 400) {
                            Toast.makeText(
                                    ctx,
                                    "A group has already been created with this name",
                                    Toast.LENGTH_LONG
                            ).show()
                        }
                    }

                    else {
                        Toast.makeText(ctx, "Network Error", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }
}