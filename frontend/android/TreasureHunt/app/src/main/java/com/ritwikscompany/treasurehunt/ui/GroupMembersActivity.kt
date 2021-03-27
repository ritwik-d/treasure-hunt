package com.ritwikscompany.treasurehunt.ui

import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.recyclerview.widget.RecyclerView
import com.github.kittinunf.fuel.Fuel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ritwikscompany.treasurehunt.R
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

        val bodyJson = Gson().toJson(hashMapOf<String, Any>(
            "user_id" to userData["user_id"] as Int,
            "pw" to userData["password"] as String,
            "group_name" to groupName
        ))
        CoroutineScope(Dispatchers.IO).launch {
            val (request, response, result) = Fuel.post("${getString(R.string.host)}/get_group_members")
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
                            val users = Gson().fromJson(String(bytes), type) as ArrayList<String>

                            val recyclerView = findViewById<RecyclerView>(R.id.gm_rview)
                            var pfps = arrayListOf<Bitmap>()
                            for (i in 0..(users.size)) {
                                pfps.add(ContextCompat.getDrawable(ctx, R.drawable.no_pfp)!!.toBitmap())
                            }
                            recyclerView.adapter = com.ritwikscompany.treasurehunt.utils.GroupAdminRecyclerView(users, pfps)
                            // set recycler view adapter with the array "users"
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