package com.ritwikscompany.treasurehunt.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import com.github.kittinunf.fuel.Fuel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ritwikscompany.treasurehunt.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MyGroupsActivity : AppCompatActivity() {

    private val ctx = this@MyGroupsActivity
    private var userData = HashMap<String, Any>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_groups)

        this.userData = intent.getSerializableExtra("userData") as HashMap<String, Any>

        initializeListView()
    }


    private fun initializeListView() {
        val lview = findViewById<ListView>(R.id.mg_lview)

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
                            val type = object: TypeToken<Array<String>>(){}.type
                            val groups = Gson().fromJson(String(bytes), type) as Array<String>
                            val arrayAdapter: ArrayAdapter<*>
                            arrayAdapter = ArrayAdapter(ctx,
                                android.R.layout.simple_list_item_1, groups)
                            lview.adapter = arrayAdapter

                            lview.setOnItemClickListener { _, _, position: Int, _ ->
                                val group: String = groups[position]
                                val intent = Intent(ctx, GroupPageActivity::class.java).apply {
                                    putExtra("userData", userData)
                                    putExtra("groupName", group)
                                }
                                startActivity(intent)
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
}