package com.ritwikscompany.treasurehunt.ui

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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
    private lateinit var searchView: SearchView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pick_challenge)

        userData = intent.getSerializableExtra("userData") as HashMap<String, Any>
        initialize()
    }


    override fun onBackPressed() {
        val intent = Intent(ctx, HomeActivity::class.java).apply {
            putExtra("userData", userData)
        }
        startActivity(intent)
    }


    private fun initialize() {
        val bodyJson = Gson().toJson(hashMapOf<String, Any>(
                "user_id" to userData["user_id"] as Int,
                "pw" to userData["password"] as String
        ))
        CoroutineScope(Dispatchers.IO).launch {
            val (_, response, result) = Fuel.post("${getString(R.string.host)}/api/get_challenges")
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
                                for (group in challengeData.keys) {
                                    if ((challengeData[group] as ArrayList<String>).isNotEmpty()) {
                                        tabLayout.addTab(tabLayout.newTab().setText(group))
                                    }
                                }

                                val firstTab = tabLayout.getTabAt(tabLayout.selectedTabPosition)?.text.toString()
                                try {
                                    val challenges = challengeData[firstTab] as ArrayList

                                    val onStartClicked = { challengeName: String ->
                                        val intent = Intent(ctx, GameActivity::class.java).apply {
                                            putExtra("userData", userData)
                                            putExtra("challengeName", challengeName)
                                        }
                                        startActivity(intent)
                                    }

                                    rv.layoutManager = LinearLayoutManager(ctx)
                                    rv.adapter = FindChallengeRVA(challenges, onStartClicked)

                                    tabLayout.addOnTabSelectedListener(object: TabLayout.OnTabSelectedListener {
                                        override fun onTabSelected(tab: TabLayout.Tab?) {
                                            rv.adapter = FindChallengeRVA(challengeData[tab?.text.toString()] as ArrayList, onStartClicked)

                                            if (ctx::searchView.isInitialized) {
                                                searchView.setQuery("", false)
                                            }
                                        }

                                        override fun onTabUnselected(tab: TabLayout.Tab?) {}

                                        override fun onTabReselected(tab: TabLayout.Tab?) {}
                                    })
                                }
                                catch (e: java.lang.NullPointerException) {
                                    tabLayout.visibility = View.INVISIBLE
                                    findViewById<TextView>(R.id.pc_nochal).visibility = View.VISIBLE
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

    private fun initializeMenu(menu: Menu?) {
        val menuInflater = menuInflater
        menuInflater.inflate(R.menu.menu_search, menu)

        val menuItem = menu!!.findItem(R.id.menu_sb)
        searchView = menuItem.actionView as SearchView
        searchView.queryHint = getString(R.string.searchHint)

        val bodyJson = Gson().toJson(hashMapOf<String, Any>(
                "user_id" to userData["user_id"] as Int,
                "pw" to userData["password"] as String
        ))

        CoroutineScope(Dispatchers.IO).launch {
            val (_, response, result) = Fuel.post("${getString(R.string.host)}/api/get_challenges")
                    .body(bodyJson)
                    .header("Content-Type" to "application/json")
                    .response()

            withContext(Dispatchers.Main) {
                runOnUiThread {
                    val status = response.statusCode
                    if (status == 200) {
                        val (bytes, _) = result

                        if (bytes != null) {
                            val startOnClick = { challengeName: String ->
                                val intent = Intent(ctx, GameActivity::class.java).apply {
                                    putExtra("userData", userData)
                                    putExtra("challengeName", challengeName)
                                }
                                startActivity(intent)
                            }

                            val challengeData = Gson().fromJson(String(bytes), HashMap::class.java) as HashMap<String, ArrayList<String>>
                            val tabLayout = findViewById<TabLayout>(R.id.pc_tab_layout)
                            val rv = findViewById<RecyclerView>(R.id.pc_rview)

                            searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener {
                                override fun onQueryTextSubmit(query: String?): Boolean {
                                    val text = query!!.toLowerCase(Locale.ROOT)
                                    try {
                                        val currentChallenges = challengeData[tabLayout.getTabAt(tabLayout.selectedTabPosition)!!.text.toString()] as ArrayList
                                        val newChallenges = ArrayList<String>()

                                        for (challenge in currentChallenges) {
                                            val challenge2 = challenge.toLowerCase(Locale.ROOT)
                                            if (text in challenge2) {
                                                newChallenges.add(challenge)
                                            }
                                        }

                                        rv.adapter = FindChallengeRVA(newChallenges, startOnClick)
                                    }
                                    catch (e: java.lang.NullPointerException) {}

                                    return true
                                }

                                override fun onQueryTextChange(newText: String?): Boolean {
                                    return this.onQueryTextSubmit(newText)
                                }
                            })
                        }
                    }
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        userData = intent.getSerializableExtra("userData") as HashMap<String, Any>
        initializeMenu(menu)

        return true
    }
}