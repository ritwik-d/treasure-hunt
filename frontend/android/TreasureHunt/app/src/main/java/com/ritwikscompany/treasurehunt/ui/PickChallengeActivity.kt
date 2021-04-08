package com.ritwikscompany.treasurehunt.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.isSuccessful
import com.google.android.material.tabs.TabLayout
import com.google.gson.Gson
import com.ritwikscompany.treasurehunt.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
                                val challengeData = Gson().fromJson(String(bytes), HashMap::class.java) as HashMap<String, MutableList<String>>
                                val tabLayout = findViewById<TabLayout>(R.id.pc_tab_layout)
                                val listView = findViewById<ListView>(R.id.pc_list_view)
                                for (group in challengeData.keys) {
                                    tabLayout.addTab(tabLayout.newTab().setText(group))
                                }

                                val firstTab = tabLayout.getTabAt(tabLayout.selectedTabPosition)?.text.toString()
                                val listItems = challengeData.get(firstTab) as MutableList
                                val adapter = ArrayAdapter(
                                    ctx,
                                    android.R.layout.simple_list_item_1,
                                    listItems
                                )
                                listView.adapter = adapter

                                listView.setOnItemClickListener { _, _, position, _ ->
                                    val intent = Intent(ctx, GameActivity::class.java).apply {
                                        putExtra("userData", userData)
                                        putExtra("challengeName", listItems[position])
                                    }
                                    startActivity(intent)
                                }

                                tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                                    override fun onTabSelected(tab: TabLayout.Tab?) {
                                        val listItems = challengeData.get(tab?.text.toString()) as MutableList
                                        val adapter = ArrayAdapter(
                                                ctx,
                                                android.R.layout.simple_list_item_1,
                                                listItems
                                        )
                                        listView.adapter = adapter

                                        listView.setOnItemClickListener { _, _, position, _ ->
                                            val intent = Intent(ctx, GameActivity::class.java).apply {
                                                putExtra("userData", userData)
                                                putExtra("challengeName", listItems[position])
                                            }
                                            startActivity(intent)
                                        }
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
}