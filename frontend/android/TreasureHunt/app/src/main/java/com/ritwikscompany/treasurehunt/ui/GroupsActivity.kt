package com.ritwikscompany.treasurehunt.ui

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.kittinunf.fuel.Fuel
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ritwikscompany.treasurehunt.R
import com.ritwikscompany.treasurehunt.utils.AddGroupsRVA
import com.ritwikscompany.treasurehunt.utils.InvitationsRVA
import com.ritwikscompany.treasurehunt.utils.Utils.Utils.getCheckMark
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class GroupsActivity: AppCompatActivity() {

    private val ctx = this@GroupsActivity
    private var userData = HashMap<String, Any>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_groups)
        userData = intent.getSerializableExtra("userData") as HashMap<String, Any>
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.groups_menu, menu)

        val lview = findViewById<ListView>(R.id.mg_lview)
        val jgCode = findViewById<EditText>(R.id.jg_code)
        val jgJoin = findViewById<Button>(R.id.jg_join)
        val cgDesc = findViewById<EditText>(R.id.cg_desc)
        val cgName = findViewById<EditText>(R.id.cg_name)
        val cgCreate = findViewById<Button>(R.id.cg_create)
        val invRview = findViewById<RecyclerView>(R.id.inv_rview)

        cgName.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }


            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                val groupName = p0.toString()

                if (groupName.length < 3) {
                    cgName.error = "Group name must be greater than or equal to 3 characters"
                    cgName.requestFocus()
                }
                else {
                    cgName.setError("Good", getCheckMark(ctx))
                    cgName.requestFocus()
                }
            }


            override fun afterTextChanged(p0: Editable?) { }
        })

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
                    invRview.visibility = View.INVISIBLE
                    menu!!.findItem(R.id.menu_invite).isVisible = false
                }
                R.id.bn_join_group -> {
                    lview.visibility = View.INVISIBLE
                    jgCode.visibility = View.VISIBLE
                    jgJoin.visibility = View.VISIBLE
                    cgDesc.visibility = View.INVISIBLE
                    cgName.visibility = View.INVISIBLE
                    cgCreate.visibility = View.INVISIBLE
                    invRview.visibility = View.INVISIBLE
                    menu!!.findItem(R.id.menu_invite).isVisible = false
                }
                R.id.bn_my_groups -> {
                    lview.visibility = View.VISIBLE
                    jgCode.visibility = View.INVISIBLE
                    jgJoin.visibility = View.INVISIBLE
                    cgDesc.visibility = View.INVISIBLE
                    cgName.visibility = View.INVISIBLE
                    cgCreate.visibility = View.INVISIBLE
                    invRview.visibility = View.INVISIBLE
                    menu!!.findItem(R.id.menu_invite).isVisible = false

                    initializeListView()
                }
                R.id.bn_invitations -> {
                    lview.visibility = View.INVISIBLE
                    jgCode.visibility = View.INVISIBLE
                    jgJoin.visibility = View.INVISIBLE
                    cgDesc.visibility = View.INVISIBLE
                    cgName.visibility = View.INVISIBLE
                    cgCreate.visibility = View.INVISIBLE
                    invRview.visibility = View.VISIBLE
                    menu!!.findItem(R.id.menu_invite).isVisible = true

                    initializeRview(invRview)
                }
            }
            true
        }
        return true
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_invite -> {
                val bodyJson = Gson().toJson(hashMapOf(
                    "user_id" to userData["user_id"],
                    "pw" to userData["password"],
                    "is_admin" to 1
                ))
                CoroutineScope(Dispatchers.IO).launch {
                    val (_, response, result) = Fuel.post("${getString(R.string.host)}/api/get_groups")
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
                                    val groups = Gson().fromJson(String(bytes), type) as ArrayList<String>

                                    val builder = AlertDialog.Builder(ctx)
                                    val alertView = layoutInflater.inflate(R.layout.dialog_invite, null)
                                    val radioGroup = alertView.findViewById<RadioGroup>(R.id.di_rgroup)
                                    val usernameET = alertView.findViewById<EditText>(R.id.di_username)

                                    for (groupName in groups) {
                                        val radioButton = RadioButton(ctx)
                                        radioButton.text = groupName
                                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
                                            radioButton.id = View.generateViewId()
                                        } else {
                                            radioButton.id = groups.indexOf(groupName) + 999
                                        }
                                        radioButton.setTextColor(Color.BLACK)
                                        radioGroup.addView(radioButton)
                                    }

                                    if (radioGroup.childCount == 0) {
                                        val builder2 = AlertDialog.Builder(ctx)
                                        builder2.setTitle("Invitation Creation Failed")
                                        builder2.setMessage("You are not privileged to invite anyone to a group.")
                                        builder2.setPositiveButton("OK") { builder3, _ ->
                                            builder3.cancel()
                                        }
                                        builder2.show()
                                    }
                                    else {
                                        builder.setTitle("Create Invitation")
                                        builder.setMessage("Enter the username of the person you would like to invite.\n\n")
                                        builder.setView(alertView)
                                        builder.setPositiveButton("Invite") { _, _ ->
                                            val groupName =
                                                radioGroup.findViewById<RadioButton>(radioGroup.checkedRadioButtonId).text.toString()
                                            val username = usernameET.text.toString()

                                            inviteUser(groupName, username)
                                        }
                                        builder.setNegativeButton("Cancel") { builder1, _ ->
                                            builder1.cancel()
                                        }
                                        builder.show()
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
        return true
    }


    private fun inviteUser(groupName: String, username: String) {
        val bodyJson = Gson().toJson(hashMapOf(
            "user_id" to userData["user_id"],
            "pw" to userData["password"],
            "group_name" to groupName,
            "to_username" to username
        ))
        CoroutineScope(Dispatchers.IO).launch {
            val (_, response, result) = Fuel.post("${getString(R.string.host)}/api/invite_user")
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


    private fun initializeRview(invRview: RecyclerView) {
        val bodyJson = Gson().toJson(hashMapOf<String, Any>(
            "user_id" to userData.get("user_id") as Int,
            "pw" to userData.get("password") as String
        ))
        CoroutineScope(Dispatchers.IO).launch {
            val (request, response, result) = Fuel.post("${getString(R.string.host)}/api/get_invitations")
                .body(bodyJson)
                .header("Content-Type" to "application/json")
                .response()

            withContext(Dispatchers.Main) {
                runOnUiThread {
                    val status = response.statusCode
                    if (status == 200) {
                        val (bytes, _) = result
                        if (bytes != null) {
                            val type = object: TypeToken<ArrayList<HashMap<String, Any>>>(){}.type
                            val invitations = Gson().fromJson(String(bytes), type) as ArrayList<HashMap<String, Any>>

                            invRview.layoutManager = LinearLayoutManager(ctx)
                            invRview.adapter = InvitationsRVA(invitations, { invitationId ->
                                operateInvitation(invitationId, invRview)
                            }, { invitationId ->
                                operateInvitation(invitationId, invRview,"decline")
                            })
                        }

                        else {
                            Toast.makeText(ctx, "Network Error", Toast.LENGTH_LONG).show()
                        }
                    }

                    else if (status == 404) {
                        Toast.makeText(ctx, "ERROR", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }


    private fun operateInvitation(invitationId: Int, rview: RecyclerView, operation: String = "accept") {
        val bodyJson = Gson().toJson(hashMapOf(
            "user_id" to userData["user_id"],
            "pw" to userData["password"],
            "invitation_id" to invitationId
        ))
        CoroutineScope(Dispatchers.IO).launch {
            val (_, response, _) = Fuel.post("${getString(R.string.host)}/api/${operation}_invitation")
                .body(bodyJson)
                .header("Content-Type" to "application/json")
                .response()

            withContext(Dispatchers.Main) {
                runOnUiThread {
                    when (response.statusCode) {
                        200 -> {
                            initializeRview(rview)
                        }
                        400 -> {
                            Toast.makeText(
                                ctx,
                                "ERROR",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            }
        }
    }


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
            val (request, response, result) = Fuel.post("${getString(R.string.host)}/api/create_group")
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
            val (request, response, result) = Fuel.post("${getString(R.string.host)}/api/join_group")
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
            val (request, response, result) = Fuel.post("${getString(R.string.host)}/api/get_groups")
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