package com.ritwikscompany.treasurehunt.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.View.INVISIBLE
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.isSuccessful
import com.google.gson.Gson
import com.ritwikscompany.treasurehunt.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MyChallengesActivity : AppCompatActivity() {

    private val ctx = this@MyChallengesActivity

    private lateinit var name1: TextView
    private lateinit var edit1: ImageButton
    private lateinit var trash1: ImageButton
    private lateinit var name2: TextView
    private lateinit var edit2: ImageButton
    private lateinit var trash2: ImageButton
    private lateinit var name3: TextView
    private lateinit var edit3: ImageButton
    private lateinit var trash3: ImageButton

    private lateinit var userData: HashMap<String, Any>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_challenges)
        this.userData = intent.getSerializableExtra("userData") as HashMap<String, Any>

        name1 = findViewById(R.id.mc_name_1)
        edit1 = findViewById(R.id.mc_edit_1)
        trash1 = findViewById(R.id.mc_trash_1)
        name2 = findViewById(R.id.mc_name_2)
        edit2 = findViewById(R.id.mc_edit_2)
        trash2 = findViewById(R.id.mc_trash_2)
        name3 = findViewById(R.id.mc_name_3)
        edit3 = findViewById(R.id.mc_edit_3)
        trash3 = findViewById(R.id.mc_trash_3)

        initialize()

        findViewById<Button>(R.id.mc_create_challenge).setOnClickListener {
            createChallengeOnClick()
        }
    }


    private fun initialize() {
        name1.visibility = INVISIBLE
        name2.visibility = INVISIBLE
        name3.visibility = INVISIBLE
        edit1.visibility = INVISIBLE
        edit2.visibility = INVISIBLE
        edit3.visibility = INVISIBLE
        trash1.visibility = INVISIBLE
        trash2.visibility = INVISIBLE
        trash3.visibility = INVISIBLE

        val bodyJson = Gson().toJson(hashMapOf(
                "user_id" to userData.get("user_id") as Double,
                "pw" to userData.get("password") as String
        ))
        CoroutineScope(Dispatchers.IO).launch {
            val (request, response, result) = Fuel.post("${getString(R.string.host)}/get_user_challenges")
                    .body(bodyJson)
                    .header("Content-Type" to "application/json")
                    .response()

            withContext(Dispatchers.Main) {
                runOnUiThread {
                    if (response.isSuccessful) {
                        val status = response.statusCode
                        if (status == 200) {
                            val (bytes, _) = result
                            if (bytes != null) {
                                val userChallenges = Gson().fromJson(String(bytes), MutableList::class.java) as MutableList<HashMap<String, Any>>

                                when (userChallenges.size) {
                                    0 -> {
                                        findViewById<TextView>(R.id.mc_no_chal).visibility = View.VISIBLE
                                    }
                                    1 -> {
                                        challengeSizeOne(userChallenges)
                                    }
                                    2 -> {
                                        challengeSizeTwo(userChallenges)
                                    }
                                    3 -> {
                                        challengeSizeThree(userChallenges)
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

                    else {
                        Toast.makeText(ctx, "Network Error", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }


    private fun challengeSizeOne(challenges: MutableList<HashMap<String, Any>>) {
        name1.text = challenges[0].get("name").toString()
        edit1.setOnClickListener {
            editOnClick(challenges[0])
        }

        trash1.setOnClickListener {
            trashOnClick(challenges[0].get("challenge_id") as Int, 0)
        }
    }


    private fun challengeSizeTwo(challenges: MutableList<HashMap<String, Any>>) {
        challengeSizeOne(challenges)

        name2.text = challenges[1].get("name").toString()
        edit2.setOnClickListener {
            editOnClick(challenges[1])
        }

        trash2.setOnClickListener {
            trashOnClick(challenges[1].get("challenge_id") as Int, 1)
        }
    }


    private fun challengeSizeThree(challenges: MutableList<HashMap<String, Any>>) {
        challengeSizeTwo(challenges)

        name3.text = challenges[2].get("name").toString()
        edit3.setOnClickListener {
            editOnClick(challenges[2])
        }

        trash3.setOnClickListener {
            trashOnClick(challenges[2].get("challenge_id") as Int, 2)
        }
    }


    private fun editOnClick(challengeData: HashMap<String, Any>) {
        val intent = Intent(ctx, EditChallengeActivity::class.java).apply {
            putExtra("userData", userData)
            putExtra("challengeData", challengeData)
        }
        startActivity(intent)
    }


    private fun trashOnClick(challengeId: Int, buttonRow: Int) {
        val bodyJson = Gson().toJson(hashMapOf(
            "user_id" to userData.get("user_id") as Int,
            "pw" to userData.get("password"),
            "challenge_id" to challengeId
        ))
        CoroutineScope(Dispatchers.IO).launch {
            val (request, response, result) = Fuel.post("${getString(R.string.host)}/register")
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

    private fun createChallengeOnClick() {
        val intent = Intent(ctx, CreateChallengeActivity::class.java).apply {
            putExtra("userData", userData)
        }
        startActivity(intent)
    }
}