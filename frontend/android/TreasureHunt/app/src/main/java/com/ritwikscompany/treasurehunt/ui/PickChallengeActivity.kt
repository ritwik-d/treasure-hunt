package com.ritwikscompany.treasurehunt.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.kittinunf.fuel.Fuel
import com.google.android.material.tabs.TabLayout
import com.google.gson.Gson
import com.ritwikscompany.treasurehunt.R
import com.ritwikscompany.treasurehunt.utils.FindChallengeRVA
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class PickChallengeActivity : AppCompatActivity() {

    private val ctx = this@PickChallengeActivity
    private var userData = HashMap<String, Any>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pick_challenge)

        this.userData = intent.getSerializableExtra("userData") as HashMap<String, Any>
        initialize()
    }


    private fun initialize() {
        val bodyJson = Gson().toJson(hashMapOf<String, Any>(
            "user_id" to userData.get("user_id") as Int,
            "pw" to userData.get("password") as String
        ))
        println(bodyJson)
        CoroutineScope(Dispatchers.IO).launch {
            val (request, response, result) = Fuel.post("${getString(R.string.host)}/api/get_challenges")
                    .body(bodyJson)
                    .header("Content-Type" to "application/json")
                    .response()

            withContext(Dispatchers.Main) {
                runOnUiThread {
                        val status = response.statusCode
                        if (status == 200) {
                            val (bytes, _) = result
                            if (bytes != null) {
                                val challengeData = Gson().fromJson(String(bytes), HashMap::class.java) as HashMap<String, ArrayList<String>>
                                val tabLayout = findViewById<TabLayout>(R.id.pc_tab_layout)
                                val rv = findViewById<RecyclerView>(R.id.pc_rview)
                                val tabNames = challengeData.keys.toTypedArray()
                                for (group in tabNames) {
                                    tabLayout.addTab(tabLayout.newTab().setText(group))
                                }

                                val firstTab = tabLayout.getTabAt(tabLayout.selectedTabPosition)?.text.toString()
                                val challenges = challengeData[firstTab] as ArrayList

                                val startOnClick = { challengeName: String ->
                                    val intent = Intent(ctx, GameActivity::class.java).apply {
                                        putExtra("userData", userData)
                                        putExtra("challengeName", challengeName)
                                    }
                                    startActivity(intent)
                                }

                                rv.layoutManager = LinearLayoutManager(ctx)
                                rv.adapter = FindChallengeRVA(challenges, startOnClick)

                                tabLayout.addOnTabSelectedListener(object: TabLayout.OnTabSelectedListener {
                                    override fun onTabSelected(tab: TabLayout.Tab?) {
                                        rv.adapter = FindChallengeRVA(challengeData[tab?.text.toString()] as ArrayList, startOnClick)
                                        findViewById<EditText>(R.id.pc_search_bar).setText("")
                                    }

                                    override fun onTabUnselected(tab: TabLayout.Tab?) {}

                                    override fun onTabReselected(tab: TabLayout.Tab?) {}
                                })

                                findViewById<EditText>(R.id.pc_search_bar).addTextChangedListener(object: TextWatcher {
                                    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }

                                    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                                        val text = p0.toString().toLowerCase(Locale.ROOT)

                                        val currentChallenges = challengeData[tabNames[tabLayout.selectedTabPosition]] as ArrayList
                                        val newChallenges = arrayListOf<String>()

                                        for (challenge in currentChallenges) {
                                            val challenge2 = challenge.toLowerCase(Locale.ROOT)
                                            if (text in challenge2) { newChallenges.add(challenge) }
                                        }

                                        rv.adapter = FindChallengeRVA(newChallenges, startOnClick)
                                    }

                                    override fun afterTextChanged(p0: Editable?) { }
                                })
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
}