package com.ritwikscompany.treasurehunt.ui

import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.kittinunf.fuel.Fuel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ritwikscompany.treasurehunt.R
import com.ritwikscompany.treasurehunt.utils.GroupAdminRecyclerView
import com.ritwikscompany.treasurehunt.utils.GroupMemberRecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GroupMembersActivity : AppCompatActivity() {

    private var userData = HashMap<String, Any>()
    private var groupName = String()
    private val ctx = this@GroupMembersActivity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_members)

        this.userData = intent.getSerializableExtra("userData") as HashMap<String, Any>
        this.groupName = intent.getStringExtra("groupName") as String

        println("userdata: $userData")
        println("groupName: $groupName")

        val bodyJson1 = Gson().toJson(hashMapOf(
            "user_id" to userData.get("user_id") as Int,
            "pw" to userData.get("password") as String,
            "group_name" to groupName
        ))
        CoroutineScope(Dispatchers.IO).launch {
            val (request, response, result) = Fuel.post("${getString(R.string.host)}/get_group_row")
                .body(bodyJson1)
                .header("Content-Type" to "application/json")
                .response()

            withContext(Dispatchers.Main) {
                runOnUiThread {
                    val status = response.statusCode
                    if (status == 200) {
                        val (bytes, _) = result
                        if (bytes != null) {
                            val type = object: TypeToken<HashMap<String, Any>>(){}.type
                            val groupData: HashMap<String, Any> = Gson().fromJson(String(bytes), type) as HashMap<String, Any>
                            println("gorupdata: $groupData")
                            val bodyJson2 = Gson().toJson(hashMapOf<String, Any>(
                                "user_id" to userData["user_id"] as Int,
                                "pw" to userData["password"] as String,
                                "group_id" to groupData["group_id"].toString().toDouble().toInt()
                            ))
                            CoroutineScope(Dispatchers.IO).launch {
                                val (request2, response2, result2) = Fuel.post("${getString(R.string.host)}/get_group_members")
                                    .body(bodyJson2)
                                    .header("Content-Type" to "application/json")
                                    .response()

                                withContext(Dispatchers.Main) {
                                    runOnUiThread {
                                        val status2 = response2.statusCode
                                        if (status2 == 200) {
                                            val (bytes2, _) = result2
                                            if (bytes2 != null) {
                                                val type2 = object: TypeToken<ArrayList<String>>(){}.type
                                                val users = Gson().fromJson(String(bytes2), type2) as ArrayList<String>

                                                println("users: $users")

                                                val recyclerView = findViewById<RecyclerView>(R.id.gm_rview)
                                                recyclerView.layoutManager = LinearLayoutManager(ctx)

                                                val pfps = arrayListOf<Bitmap>()
                                                for (i in 1..(users.size)) {
                                                    pfps.add(ContextCompat.getDrawable(ctx, R.drawable.no_pfp)!!.toBitmap())
                                                }

                                                if (userData.get("user_id") as Int == groupData.get("creator_id").toString().toDouble().toInt()) {
                                                    recyclerView.adapter = GroupAdminRecyclerView(users, pfps)
                                                } else {
                                                    recyclerView.adapter = GroupMemberRecyclerView(users, pfps)
                                                }
                                            }

                                            else {
                                                Toast.makeText(ctx, "Network Error", Toast.LENGTH_LONG).show()
                                            }
                                        }

                                        else if (status2 == 400) {
                                            Toast.makeText(ctx, "ERROR2", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                }
                            }
                        }

                        else {
                            Toast.makeText(ctx, "Network Error", Toast.LENGTH_LONG).show()
                        }
                    }

                    else if (status == 404) {
                        Toast.makeText(ctx, "ERROR1", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }
}