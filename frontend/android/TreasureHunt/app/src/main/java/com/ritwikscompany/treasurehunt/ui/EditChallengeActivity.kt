package com.ritwikscompany.treasurehunt.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.kittinunf.fuel.Fuel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ritwikscompany.treasurehunt.R
import com.ritwikscompany.treasurehunt.utils.AddGroupsRVA
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class EditChallengeActivity : AppCompatActivity() {

    private val ctx = this@EditChallengeActivity
    private lateinit var userData: HashMap<String, Any>
    private lateinit var challengeData: HashMap<String, Any>
    private lateinit var spinnerDiff: Spinner
    private lateinit var puzzleET: EditText
    private lateinit var nameTV: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_challenge)

        this.userData = intent.getSerializableExtra("userData") as HashMap<String, Any>
        this.challengeData = intent.getSerializableExtra("challengeData") as HashMap<String, Any>

        initialize()
    }


    private fun initialize() {
        puzzleET = findViewById(R.id.ec_puzzle)
        nameTV = findViewById(R.id.ec_name)
        spinnerDiff = findViewById(R.id.ec_difficulty)

        puzzleET.setText(challengeData["puzzle"] as String)
        nameTV.text = challengeData["name"] as String

        val diffArray = ctx.resources.getStringArray(R.array.difficulties)

        val adapter = ArrayAdapter(
            ctx,
            android.R.layout.simple_spinner_item,
            diffArray
        )
        spinnerDiff.adapter = adapter

        val bodyJson = Gson().toJson(hashMapOf(
            "user_id" to userData["user_id"],
            "pw" to userData["password"]
        ))
        CoroutineScope(Dispatchers.IO).launch {
            val (_, response, result) = Fuel.post("${getString(R.string.host)}/api/get_groups")
                    .body(bodyJson)
                    .header("Content-Type" to "application/json")
                    .response()

            withContext(Dispatchers.Main) {
                runOnUiThread {
                    val status = response.statusCode
                    if (status == 200) {
                        val (bytes, _) = result
                        if (bytes != null) {
                            val type = object: TypeToken<ArrayList<String>>(){}.type
                            val groups = Gson().fromJson(String(bytes), type) as ArrayList<String>

                            findViewById<FloatingActionButton>(R.id.ec_edit_challenge).setOnClickListener {
                                val adapterGroups = AddGroupsRVA(groups, challengeData["user_groups_names"] as ArrayList<String>)

                                val rv = RecyclerView(ctx)
                                rv.layoutManager = LinearLayoutManager(ctx)
                                rv.adapter = adapterGroups

                                AlertDialog.Builder(ctx)
                                    .setTitle("Add Groups")
                                    .setMessage("NOTE: If you do not specify any group(s), the challenge will be public.\n\n")
                                    .setView(rv)
                                    .setPositiveButton("Finish") { _, _ ->
                                        updateChallenge(0.0, 0.0, (rv.adapter as AddGroupsRVA).checkedGroups)
                                    }
                                    .setNegativeButton("Cancel") { builder, _ ->
                                        builder.cancel()
                                    }
                                    .show()
                            }
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


    private fun updateChallenge(latitude: Double, longitude: Double, newGroups: ArrayList<String>) {
        val bodyJson = Gson().toJson(
            hashMapOf(
                "pw" to userData["password"],
                "user_id" to userData["user_id"],
                "challenge_id" to challengeData["challenge_id"],
                "new_latitude" to latitude,
                "new_longitude" to longitude,
                "new_puzzle" to puzzleET.text.toString(),
                "new_difficulty" to spinnerDiff.selectedItem.toString().toLowerCase(Locale.ROOT),
                "new_groups" to newGroups
            )
        )
        CoroutineScope(Dispatchers.IO).launch {
            val (_, response, _) = Fuel.post("${getString(R.string.host)}/api/update_challenge")
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