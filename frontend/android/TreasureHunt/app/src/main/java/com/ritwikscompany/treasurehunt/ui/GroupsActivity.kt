package com.ritwikscompany.treasurehunt.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.github.kittinunf.fuel.Fuel
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ritwikscompany.treasurehunt.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class GroupsActivity : AppCompatActivity() {

    private val ctx = this@GroupsActivity
    private var userData = HashMap<String, Any>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_groups)
        userData = intent.getSerializableExtra("userData") as HashMap<String, Any>

//        findViewById<Button>(R.id.groups_create_group).setOnClickListener {
//            createGroupOnClick()
//        }
//
//        findViewById<Button>(R.id.groups_join_group).setOnClickListener {
//            joinGroupOnClick()
//        }
//
//        findViewById<Button>(R.id.groups_my_groups).setOnClickListener {
//            myGroupsOnClick()
//        }

        val lview = findViewById<ListView>(R.id.mg_lview)
        val jgCode = findViewById<EditText>(R.id.jg_code)
        val jgJoin = findViewById<Button>(R.id.jg_join)
        val cgDesc = findViewById<EditText>(R.id.cg_desc)
        val cgName = findViewById<EditText>(R.id.cg_name)
        val cgCreate = findViewById<Button>(R.id.cg_create)

        initializeListView()

        jgJoin.setOnClickListener {
            joinGroupOnClick()
        }

        cgCreate.setOnClickListener {
            createGroupOnClick()
        }

        val bottomNavigationView = findViewById<View>(R.id.groups_navigation) as BottomNavigationView
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.bn_create_group -> {
                    lview.visibility = View.INVISIBLE
                    jgCode.visibility = View.INVISIBLE
                    jgJoin.visibility = View.INVISIBLE
                    cgDesc.visibility = View.VISIBLE
                    cgName.visibility = View.VISIBLE
                    cgCreate.visibility = View.VISIBLE
                }
                R.id.bn_join_group -> {
                    lview.visibility = View.INVISIBLE
                    jgCode.visibility = View.VISIBLE
                    jgJoin.visibility = View.VISIBLE
                    cgDesc.visibility = View.INVISIBLE
                    cgName.visibility = View.INVISIBLE
                    cgCreate.visibility = View.INVISIBLE
                }
                R.id.bn_my_groups -> {
                    lview.visibility = View.VISIBLE
                    jgCode.visibility = View.INVISIBLE
                    jgJoin.visibility = View.INVISIBLE
                    cgDesc.visibility = View.INVISIBLE
                    cgName.visibility = View.INVISIBLE
                    cgCreate.visibility = View.INVISIBLE

                    initializeListView()
                }
            }
            true
        }
    }


//    private fun createGroupOnClick() {
//        val intent = Intent(ctx, CreateGroupActivity::class.java).apply {
//            putExtra("userData", userData)
//        }
//        startActivity(intent)
//    }
//
//
//    private fun joinGroupOnClick() {
//        val intent = Intent(ctx, JoinGroupActivity::class.java).apply {
//            putExtra("userData", userData)
//        }
//        startActivity(intent)
//    }
//
//
//    private fun myGroupsOnClick() {
//        val intent = Intent(ctx, MyGroupsActivity::class.java).apply {
//            putExtra("userData", userData)
//        }
//        startActivity(intent)
//    }


    private fun createGroupOnClick() {
        val groupNameET = findViewById<EditText>(R.id.cg_name)
        val groupDescET = findViewById<EditText>(R.id.cg_desc)

        val groupName = groupNameET.text.toString()
        var groupDesc: String? = groupDescET.text.toString()

        if (groupName.length < 3) {
            groupNameET.error = "Group name must be at least 3 characters long"
            groupNameET.requestFocus()
            return
        }

        if (groupDesc == "") {
            groupDesc = null
        }

        httpCall(groupName, groupDesc)
    }


    private fun httpCall(groupName: String, groupDesc: String?) {
        val bodyJson = Gson().toJson(hashMapOf(
            "pw" to userData.get("password"),
            "user_id" to userData.get("user_id"),
            "name" to groupName,
            "description" to groupDesc
        ))
        CoroutineScope(Dispatchers.IO).launch {
            val (request, response, result) = Fuel.post("${getString(R.string.host)}/create_group")
                .body(bodyJson)
                .header("Content-Type" to "application/json")
                .response()

            withContext(Dispatchers.Main) {
                runOnUiThread {
                    val status = response.statusCode
                    if (status == 201) {
                        val intent = Intent(ctx, GroupPageActivity::class.java).apply {
                            putExtra("groupName", groupName)
                            putExtra("userData", userData)
                        }
                        startActivity(intent)
                    } else if (status == 400) {
                        Toast.makeText(
                            ctx,
                            "A group has already been created with this name",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }
    }


    private fun joinGroupOnClick() {
        val joinCodeET = findViewById<EditText>(R.id.jg_code)
        val joinCode = joinCodeET.text.toString()

        val bodyJson = Gson().toJson(hashMapOf(
            "pw" to userData.get("password") as String,
            "user_id" to userData.get("user_id") as Int,
            "join_code" to joinCode
        ))
        CoroutineScope(Dispatchers.IO).launch {
            val (request, response, result) = Fuel.post("${getString(R.string.host)}/join_group")
                .body(bodyJson)
                .header("Content-Type" to "application/json")
                .response()

            withContext(Dispatchers.Main) {
                runOnUiThread {
                    when (response.statusCode) {
                        200 -> {
                            val intent = Intent(ctx, GroupsActivity::class.java).apply {
                                putExtra("userData", userData)
                            }
                            startActivity(intent)
                        }
                        404 -> {
                            joinCodeET.error = "Invalid join code"
                            joinCodeET.requestFocus()
                        }
                        400 -> {
                            joinCodeET.error = "You have already joined/created this group"
                        }
                    }
                }
            }
        }
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