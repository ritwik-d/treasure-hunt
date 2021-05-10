package com.ritwikscompany.treasurehunt.ui

import android.content.Intent
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
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
    }


    override fun onBackPressed() {
        val intent = Intent(ctx, GroupPageActivity::class.java).apply {
            putExtra("userData", userData)
            putExtra("groupName", groupName)
        }
        startActivity(intent)
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.group_members_menu, menu)

        initialize(menu!!)
        return true
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_add_person -> {
                val unameET = EditText(ctx)
                unameET.hint = getString(R.string.username)

                AlertDialog.Builder(ctx)
                        .setTitle("Add Member")
                        .setMessage("Enter the username of the person you would like to invite.")
                        .setView(unameET)
                        .setPositiveButton("Submit") { _, _ ->
                            inviteUser(unameET.text.toString())
                        }
                        .setNegativeButton("Cancel") { builder, _ ->
                            builder.cancel()
                        }
                        .show()
            }
        }
        return true
    }


    private fun inviteUser(username: String) {
        val bodyJson = Gson().toJson(hashMapOf(
                "user_id" to userData["user_id"],
                "pw" to userData["password"],
                "group_name" to groupName,
                "to_username" to username
        ))
        CoroutineScope(Dispatchers.IO).launch {
            val (_, _, result) = Fuel.post("${getString(R.string.host)}/api/invite_user")
                    .body(bodyJson)
                    .header("Content-Type" to "application/json")
                    .response()

            withContext(Dispatchers.Main) {
                runOnUiThread {
                    val (bytes, _) = result
                    if (bytes != null) {
                        when ((Gson().fromJson(String(bytes), object: TypeToken<HashMap<String, Double>>(){}.type) as HashMap<String, Double>)["status"]!!.toInt()) {
                            200 -> {
                                Toast.makeText(
                                        ctx,
                                        "Invitation Successful",
                                        Toast.LENGTH_LONG
                                ).show()
                            }
                            400 -> {
                                Toast.makeText(
                                        ctx,
                                        "$username has already been invited to $groupName",
                                        Toast.LENGTH_LONG
                                ).show()
                            }
                            404 -> {
                                Toast.makeText(
                                        ctx,
                                        "There is no such player with username $username",
                                        Toast.LENGTH_LONG
                                ).show()
                            }
                            401 -> {
                                Toast.makeText(
                                        ctx,
                                        "$username has already joined/created $groupName",
                                        Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    }
                    else {
                        Toast.makeText(ctx, "Network Error", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }


    private fun initialize(menu: Menu? = null) {
        val bodyJson1 = Gson().toJson(hashMapOf(
            "user_id" to userData.get("user_id") as Int,
            "pw" to userData.get("password") as String,
            "group_name" to groupName
        ))
        CoroutineScope(Dispatchers.IO).launch {
            val (request, response, result) = Fuel.post("${getString(R.string.host)}/api/get_group_row")
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

                            if ((menu != null) && (userData["user_id"] as Int == (groupData["creator_id"] as Double).toInt())) {
                                menu.findItem(R.id.menu_add_person).isVisible = true
                            }

                            val bodyJson2 = Gson().toJson(hashMapOf<String, Any>(
                                "user_id" to userData["user_id"] as Int,
                                "pw" to userData["password"] as String,
                                "group_id" to groupData["group_id"].toString().toDouble().toInt()
                            ))
                            CoroutineScope(Dispatchers.IO).launch {
                                val (request2, response2, result2) = Fuel.post("${getString(R.string.host)}/api/get_group_members")
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

                                                val recyclerView = findViewById<RecyclerView>(R.id.gm_rview)
                                                recyclerView.layoutManager = LinearLayoutManager(ctx)

                                                val pfps = arrayListOf<Bitmap>()
                                                for (i in 1..(users.size)) {
                                                    pfps.add(ContextCompat.getDrawable(ctx, R.drawable.no_pfp)!!.toBitmap())
                                                }

                                                if ((userData.get("user_id") as Int) == (groupData.get("creator_id").toString().toDouble().toInt())) {
                                                    recyclerView.adapter = GroupAdminRecyclerView(users, pfps, ctx, ctx::removeMemberOnClick, userData, groupData)
                                                } else {
                                                    recyclerView.adapter = GroupMemberRecyclerView(users, pfps)
                                                }
                                            }

                                            else {
                                                Toast.makeText(ctx, "Network Error", Toast.LENGTH_LONG).show()
                                            }
                                        }

                                        else if (status2 == 400) {
                                            Toast.makeText(ctx, "ERROR", Toast.LENGTH_LONG).show()
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


    private fun removeMemberOnClick(member: String, userData2: HashMap<String, Any>, groupData2: HashMap<String, Any>) {
        val bodyJson = Gson().toJson(
            hashMapOf(
                "user_id" to userData2["user_id"],
                "pw" to userData2["password"],
                "username" to member,
                "group_id" to groupData2["group_id"]
            )
        )
        CoroutineScope(Dispatchers.IO).launch {
            val (_, response3, _) = Fuel.post("${getString(R.string.host)}/api/remove_group_member")
                .body(bodyJson)
                .header("Content-Type" to "application/json")
                .response()

            withContext(Dispatchers.Main) {
                runOnUiThread {
                    if (response3.statusCode == 400) {
                        Toast.makeText(ctx, "ERROR", Toast.LENGTH_LONG).show()
                    }
                    else {
                        initialize()
                    }
                }
            }
        }
    }
}