package com.ritwikscompany.treasurehunt.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import com.github.kittinunf.fuel.Fuel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ritwikscompany.treasurehunt.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.collections.HashMap

class EditChallengeActivity : AppCompatActivity() {

    private val ctx = this@EditChallengeActivity
    private lateinit var userData: HashMap<String, Any>
    private lateinit var challengeData: HashMap<String, Any>
    private lateinit var spinnerDiff: Spinner
    private lateinit var spinnerGroups: Spinner
    private lateinit var puzzleET: EditText
    private lateinit var nameTV: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_challenge)

        this.userData = intent.getSerializableExtra("userData") as HashMap<String, Any>
        this.challengeData = intent.getSerializableExtra("challengeData") as HashMap<String, Any>

        autoFill()

        findViewById<FloatingActionButton>(R.id.ec_edit_challenge).setOnClickListener {
            updateChallenge(0.0, 0.0)
        }
    }


    private fun autoFill() {
        puzzleET = findViewById(R.id.ec_puzzle)
        nameTV = findViewById(R.id.ec_name)
        spinnerDiff = findViewById(R.id.ec_difficulty)
        spinnerGroups = findViewById(R.id.ec_groups)

        puzzleET.setText(challengeData.get("puzzle") as String)
        nameTV.text = challengeData.get("name") as String
        actionBar!!.title = challengeData["name"] as String

        val diffArray = ctx.resources.getStringArray(R.array.difficulties).toMutableList()

        val adapter = ArrayAdapter(
            ctx,
            android.R.layout.simple_spinner_item,
            diffArray
        )
        spinnerDiff.adapter = adapter

        val bodyJson = Gson().toJson(hashMapOf(
            "user_id" to userData.get("user_id"),
            "pw" to userData.get("password")
        ))
        CoroutineScope(Dispatchers.IO).launch {
            val (request, response, result) = Fuel.post("${getString(R.string.host)}/get_groups")
                    .body(bodyJson)
                    .header("Content-Type" to "application/json")
                    .response()

            withContext(Dispatchers.Main) {
                runOnUiThread {
                    val status = response.statusCode
                    if (status == 200) {
                        val (bytes, _) = result
                        if (bytes != null) {
                            val type = object: TypeToken<MutableList<String>>(){}.type
                            val groups1 = Gson().fromJson(String(bytes), type) as MutableList<String>
                            groups1.add("Public")
                            val groups = groups1.toTypedArray()

                            val groupAdapter: ArrayAdapter<CharSequence> = ArrayAdapter(ctx, android.R.layout.simple_spinner_item, groups)
                            groupAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                            spinnerGroups.adapter = groupAdapter
                        }

                        else {
                            Toast.makeText(ctx, "Network Error", Toast.LENGTH_LONG).show()
                        }
                    }

                    else if (status == 400) {
                        Toast.makeText(ctx, "ERROR", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }


    private fun updateChallenge(latitude: Double, longitude: Double) {
        val bodyJson = Gson().toJson(
            hashMapOf(
                "pw" to userData.get("password"),
                "user_id" to userData.get("user_id"),
                "challenge_id" to challengeData.get("challenge_id"),
                "new_latitude" to latitude,
                "new_longitude" to longitude,
                "new_puzzle" to puzzleET.text.toString(),
                "new_difficulty" to spinnerDiff.selectedItem.toString().toLowerCase(Locale.ROOT),
                "new_group_name" to spinnerGroups.selectedItem.toString()
            )
        )
        CoroutineScope(Dispatchers.IO).launch {
            val (_, response, _) = Fuel.post("${getString(R.string.host)}/update_challenge")
                    .body(bodyJson)
                    .header("Content-Type" to "application/json")
                    .response()

            withContext(Dispatchers.Main) {
                runOnUiThread {
                    if (response.statusCode == 200) {
                        val intent = Intent(ctx, MyChallengesActivity::class.java).apply {
                            putExtra("userData", userData)
                        }
                        startActivity(intent)
                    }
                    else {
                        Toast.makeText(ctx, "Update Challenge Failure", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }
}