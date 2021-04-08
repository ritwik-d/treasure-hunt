package com.ritwikscompany.treasurehunt.ui

import android.animation.ObjectAnimator
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.isSuccessful
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ritwikscompany.treasurehunt.R
import com.ritwikscompany.treasurehunt.utils.MyChallengesRVA
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MyChallengesActivity : AppCompatActivity() {

    private val ctx = this@MyChallengesActivity
    private lateinit var minusButton: FloatingActionButton
    private lateinit var plusButton: FloatingActionButton

    private var userData = HashMap<String, Any>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_challenges)

        this.userData = intent.getSerializableExtra("userData") as HashMap<String, Any>
        this.minusButton = findViewById(R.id.mc_del_challenge)
        this.plusButton = findViewById(R.id.mc_create_challenge)

        initialize()

        plusButton.setOnClickListener {
            createChallengeOnClick()
        }
    }


    private fun initialize() {
        val bodyJson = Gson().toJson(hashMapOf<String, Any>(
            "user_id" to userData.get("user_id") as Int,
            "pw" to userData.get("password") as String
        ))
        CoroutineScope(Dispatchers.IO).launch {
            val (_, response, result) = Fuel.post("${getString(R.string.host)}/api/get_user_challenges")
                    .body(bodyJson)
                    .header("Content-Type" to "application/json")
                    .response()

            withContext(Dispatchers.Main) {
                runOnUiThread {
                    val status = response.statusCode
                    if (status == 200) {
                        val (bytes, _) = result
                        if (bytes != null) {
                            val type = object: TypeToken<MutableList<HashMap<String, Any>>>(){}.type
                            val userChallenges = Gson().fromJson(String(bytes), type) as MutableList<HashMap<String, Any>>
                            when (userChallenges.size) {
                                0 -> {
                                    findViewById<TextView>(R.id.mc_no_chal).visibility = View.VISIBLE
                                }
                                10 -> {
                                    plusButton.isEnabled = false
                                    plusButton.contentDescription = "You have reached the limit for the number of challenges that you are allowed to creaet."
                                }
                                else -> {
                                    val challengeNames = ArrayList<String>()
                                    for (challenge in userChallenges) {
                                        challengeNames.add(challenge.get("name") as String)
                                    }

                                    val rview = findViewById<RecyclerView>(R.id.mc_rview)
                                    val adapter = MyChallengesRVA(challengeNames,
                                        { challengeName ->
                                            val builder = AlertDialog.Builder(ctx)
                                            builder.setTitle("Are you sure you want to delete $challengeName?")
                                            builder.setPositiveButton("Yes") { _, _ ->
                                                val challengeId = (userChallenges[challengeNames.indexOf(challengeName)]["challenge_id"] as Double).toInt()
                                                deleteChallenge(challengeId)
                                            }
                                            builder.setNegativeButton("No") {builder1, _ ->
                                                builder1.cancel()
                                            }
                                            builder.show()
                                        },
                                        { challengeName ->
                                            val challengeData = userChallenges[challengeNames.indexOf(challengeName)]
                                            editChallenge(challengeData)
                                        }, minusButton)
                                    rview.layoutManager = LinearLayoutManager(ctx)
                                    rview.adapter = adapter

                                    minusButton.setOnClickListener {
                                        val builder = AlertDialog.Builder(ctx)
                                        builder.setTitle("Are you sure you want to do this?")
                                        builder.setPositiveButton("Yes") { _, _ ->
                                            for (challengeName in adapter.checkedChallenges) {
                                                val challengeId = (userChallenges[challengeNames.indexOf(challengeName)]["challenge_id"] as Double).toInt()
                                                deleteChallenge(challengeId)
                                            }
                                            adapter.checkedChallenges = ArrayList()
                                            val animation = ObjectAnimator.ofFloat(minusButton, "translationY", 175f)
                                            animation.duration = 1000
                                            animation.start()
                                            minusButton.visibility = View.INVISIBLE
                                            initialize()
                                        }
                                        builder.setNegativeButton("No") {_, _ -> }
                                        builder.show()
                                    }
                                }
                            }
                        }

                        else {
                            Toast.makeText(ctx, "Network Error", Toast.LENGTH_LONG).show()
                        }
                    }

                    else if (status == 404) {
                        Toast.makeText(ctx, "ERROR", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }


    private fun deleteChallenge(challengeId: Int) {
        val bodyJson = Gson().toJson(hashMapOf(
                "user_id" to userData.get("user_id") as Int,
                "pw" to userData.get("password"),
                "challenge_id" to challengeId
        ))
        CoroutineScope(Dispatchers.IO).launch {
            val (_, response, _) = Fuel.post("${getString(R.string.host)}/api/delete_challenge")
                    .body(bodyJson)
                    .header("Content-Type" to "application/json")
                    .response()

            withContext(Dispatchers.Main) {
                runOnUiThread {
                    if (response.isSuccessful) {
                        val status = response.statusCode
                        if (status == 200) {
                            initialize()
                        }

                        else if (status == 400) {
                            Toast.makeText(
                                    ctx,
                                    "ERROR",
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


    private fun editChallenge(challengeData: HashMap<String, Any>) {
        val intent = Intent(ctx, EditChallengeActivity::class.java).apply {
            putExtra("userData", userData)
            putExtra("challengeData", challengeData)
        }
        startActivity(intent)
    }


    private fun createChallengeOnClick() {
        val intent = Intent(ctx, CreateChallengeActivity::class.java).apply {
            putExtra("userData", userData)
        }
        startActivity(intent)
    }
}