package com.ritwikscompany.treasurehunt.ui

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.widget.EditText
import android.widget.SearchView
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
import kotlin.collections.HashMap

class PickChallengeActivity : AppCompatActivity() {

    private val ctx = this@PickChallengeActivity
    private var userData = HashMap<String, Any>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pick_challenge)

        userData = intent.getSerializableExtra("userData") as HashMap<String, Any>
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

                                tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                                    override fun onTabSelected(tab: TabLayout.Tab?) {
                                        rv.adapter = FindChallengeRVA(challengeData[tab?.text.toString()] as ArrayList, startOnClick)
                                    }

                                    override fun onTabUnselected(tab: TabLayout.Tab?) {}

                                    override fun onTabReselected(tab: TabLayout.Tab?) {}
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val menuInflater = menuInflater
        menuInflater.inflate(R.menu.menu_search, menu)

        val menuItem = menu!!.findItem(R.id.menu_sb)
        val searchView = menuItem.actionView as SearchView

        searchView.queryHint = getString(R.string.searchHint)

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                //run search query
                //...
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                //run search query
                //...
                return true
            }
        })

        return true
    }
}