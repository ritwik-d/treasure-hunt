package com.ritwikscompany.treasurehunt.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import com.github.kittinunf.fuel.Fuel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ritwikscompany.treasurehunt.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GroupSettingsActivity : AppCompatActivity() {

    private val ctx = this@GroupSettingsActivity
    private lateinit var joinCode: TextView
    private lateinit var name: TextView
    private lateinit var checkBox: CheckBox
    private lateinit var minPoints: EditText
    private lateinit var saveButton: Button
    private lateinit var discButton: Button
    private var userData = HashMap<String, Any>()
    private var groupName = String()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_settings)

        this.joinCode = findViewById(R.id.gs_join_code)
        this.name = findViewById(R.id.gs_name)
        this.checkBox = findViewById(R.id.gs_amc_cb)
        this.minPoints = findViewById(R.id.gs_mp_et)
        this.saveButton = findViewById(R.id.gs_save)
        this.discButton = findViewById(R.id.gs_discard)
        this.userData = intent.getSerializableExtra("userData") as HashMap<String, Any>
        this.groupName = intent.getStringExtra("groupName") as String

        initialize()
    }


    private fun initialize() {
        val bodyJson = Gson().toJson(hashMapOf(
            "user_id" to userData.get("user_id"),
            "pw" to userData.get("password"),
            "group_name" to groupName
        ))
        CoroutineScope(Dispatchers.IO).launch {
            val (request, response, result) = Fuel.post("${getString(R.string.host)}/get_group_settings")
                    .body(bodyJson)
                    .header("Content-Type" to "application/json")
                    .response()

            withContext(Dispatchers.Main) {
                runOnUiThread {
                    val status = response.statusCode
                    if (status == 200) {
                        val (bytes, _) = result
                        if (bytes != null) {
                            val type = object: TypeToken<HashMap<String, Any>>(){}.type
                            val groupData = Gson().fromJson(String(bytes), type) as HashMap<String, Any>

                            name.text = "Group Name: $groupName"
                            println("groupdata: $groupData")
                            println("userdata: $userData")
                            if ((userData["user_id"] as Int == (groupData["creator_id"] as Double).toInt()) or (groupData["allow_members_code"] == "true")) {
                                joinCode.text = "Join Code: ${groupData.get("join_code") as String}"
                            }
                            if (userData["user_id"] as Int == (groupData["creator_id"] as Double).toInt()) {
                                checkBox.visibility = View.VISIBLE
                                minPoints.visibility = View.VISIBLE
                                discButton.visibility = View.VISIBLE
                                saveButton.visibility = View.VISIBLE
                                findViewById<TextView>(R.id.textView4).visibility = View.VISIBLE

                                checkBox.isChecked = groupData.get("allow_members_code") == "true"
                                val minPoints2: String = (groupData.get("minimum_points") as Double).toInt().toString()
                                minPoints.setText(minPoints2)

                                discButton.setOnClickListener {
                                    discOnClick(groupData.get("join_code") as String, groupData.get("name") as String, groupData.get("allow_members_code") == "true", (groupData.get("minimum_points") as Double).toInt())
                                }

                                saveButton.setOnClickListener {
                                    saveOnClick(checkBox.isChecked, minPoints.text.toString().toInt(), (groupData.get("group_id") as Double).toInt())
                                }
                            }
                        }

                        else {
                            Toast.makeText(ctx, "Network Error", Toast.LENGTH_LONG).show()
                        }
                    }

                    else if (status == 404) {
                        Toast.makeText(ctx, "Log In Failure", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }


    private fun discOnClick(joinCode2: String, name2: String, isChecked2: Boolean, minPoints2: Int) {
        joinCode.text = "Join Code: $joinCode2"
        name.text = "Group Name: $name2"
        checkBox.isChecked = isChecked2
        minPoints.setText(minPoints2.toString())
    }


    private fun saveOnClick(isChecked2: Boolean, minPoints2: Int, groupId: Int) {
        val allowMembersCode: Int = if (isChecked2) {
            1
        }
        else {
            0
        }

        val bodyJson = Gson().toJson(
            hashMapOf(
                "pw" to userData.get("password"),
                "user_id" to userData.get("user_id"),
                "group_id" to groupId,
                "allow_members_code" to allowMembersCode,
                "min_points" to minPoints2
            )
        )
        CoroutineScope(Dispatchers.IO).launch {
            val (_, response, _) = Fuel.post("${getString(R.string.host)}/update_group_settings")
                    .body(bodyJson)
                    .header("Content-Type" to "application/json")
                    .response()

            withContext(Dispatchers.Main) {
                runOnUiThread {
                    if (response.statusCode == 200) {
                        discButton.isEnabled = false
                        Toast.makeText(ctx, "Save Successful", Toast.LENGTH_SHORT).show()
                    }
                    else {
                        Toast.makeText(ctx, "Save Failure", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }
}