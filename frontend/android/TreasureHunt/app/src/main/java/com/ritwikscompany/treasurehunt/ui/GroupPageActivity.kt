package com.ritwikscompany.treasurehunt.ui

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity.CENTER
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.kittinunf.fuel.Fuel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ritwikscompany.treasurehunt.R
import com.ritwikscompany.treasurehunt.utils.GroupAdminRecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.collections.HashMap

class GroupPageActivity : AppCompatActivity() {

    private var groupName = ""
    private var userData = HashMap<String, Any>()
    private val ctx = this@GroupPageActivity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_page)

        this.groupName = intent.getStringExtra("groupName") as String
        this.userData = intent.getSerializableExtra("userData") as HashMap<String, Any>

        title = groupName
        initializeTableLayout()
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.group_page_menu, menu)
        return true
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_leave_group -> {
                leaveGroup()
            }

            R.id.menu_group_members -> {
                val intent = Intent(ctx, GroupMembersActivity::class.java).apply {
                    putExtra("userData", userData)
                    putExtra("groupName", groupName)
                }
                startActivity(intent)
            }

            R.id.menu_group_settings -> {
                val intent = Intent(ctx, GroupSettingsActivity::class.java).apply {
                    putExtra("userData", userData)
                    putExtra("groupName", groupName)
                }
                startActivity(intent)
            }

            R.id.menu_group_chat -> {
                val intent = Intent(ctx, GroupChatActivity::class.java).apply {
                    putExtra("userData", userData)
                    putExtra("groupName", groupName)
                }
                startActivity(intent)
            }
        }
        return true
    }


    private fun leaveGroup(newAdmin: String? = null, ask: Boolean = true) {
        fun leaveFR() {
            val bodyJson = Gson().toJson(
                hashMapOf(
                    "user_id" to userData.get("user_id"),
                    "pw" to userData.get("password"),
                    "group_name" to groupName,
                    "new_admin" to newAdmin
                )
            )
            CoroutineScope(Dispatchers.IO).launch {
                val (_, response, _) = Fuel.post("${getString(R.string.host)}/api/leave_group")
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
                                adminLeaveFinal()
                            }
                        }
                    }
                }
            }
        }

        if (ask) {
            val builder: AlertDialog.Builder = AlertDialog.Builder(ctx)
            builder.setTitle("Are you sure you want to leave $groupName?")
            builder.setPositiveButton("Yes") { _, _ ->
                leaveFR()
            }
            builder.setNegativeButton("No") { _, _ -> }
            builder.show()
        }
        else {
            leaveFR()
        }
    }


    private fun adminLeaveFinal() {
        val bodyJson = Gson().toJson(hashMapOf(
            "user_id" to userData["user_id"] as Int,
            "pw" to userData["password"] as String,
            "group_name" to groupName
        ))
        CoroutineScope(Dispatchers.IO).launch {
            val (request, response, result) = Fuel.post("${getString(R.string.host)}/api/get_group_row")
                .body(bodyJson)
                .header("Content-Type" to "application/json")
                .response()

            withContext(Dispatchers.Main) {
                runOnUiThread {
                    val status = response.statusCode
                    if (status == 200) {
                        val (bytes, _) = result
                        if (bytes != null) {
                            val type = object: TypeToken<HashMap<String, Any>>(){}.type
                            val groupData = Gson().fromJson(String(bytes), type) as HashMap<String, Any>
                            val groupId = (groupData["group_id"] as Double).toInt()

                            adminLeave(groupId)
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


    private fun adminLeave(groupId: Int) {
        val bodyJson = Gson().toJson(hashMapOf<String, Any>(
            "user_id" to userData["user_id"] as Int,
            "pw" to userData["password"] as String,
            "group_id" to groupId
        ))
        CoroutineScope(Dispatchers.IO).launch {
            val (request, response, result) = Fuel.post("${getString(R.string.host)}/api/get_group_members")
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
                            val members = Gson().fromJson(String(bytes), type) as ArrayList<String>
                            members.remove(userData["username"])

                            val alertView = layoutInflater.inflate(R.layout.dialog_new_admin, null)
                            val radioGroup = alertView.findViewById<RadioGroup>(R.id.dna_rgroup)

                            for (member in members) {
                                val radioButton = RadioButton(ctx)
                                radioButton.text = member
                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
                                    radioButton.id = View.generateViewId()
                                } else {
                                    radioButton.id = members.indexOf(member) + 999
                                }
                                radioButton.setTextColor(Color.BLACK)

                                radioGroup.addView(radioButton)
                            }

                            AlertDialog.Builder(ctx)
                                .setTitle("Assign a New Admin")
                                .setMessage("For the admin of a group to leave a group, they need to assign a new admin.\n\n")
                                .setView(alertView)
                                .setPositiveButton("Assign & Leave") { _, _ ->
                                    val newAdmin = radioGroup.findViewById<RadioButton>(radioGroup.checkedRadioButtonId).text.toString()
                                    leaveGroup(newAdmin, false)
                                }
                                .setNegativeButton(getString(R.string.cancel)) { builder, _ ->
                                    builder.cancel()
                                }
                                .show()
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


    private fun initializeTableLayout() {
        val tableLayout = findViewById<TableLayout>(R.id.gp_tb)
        val bodyJson = Gson().toJson(hashMapOf(
            "user_id" to userData.get("user_id"),
            "pw" to userData.get("password"),
            "name" to groupName
        ))
        CoroutineScope(Dispatchers.IO).launch {
            val (request, response, result) = Fuel.post("${getString(R.string.host)}/api/get_group_data")
                    .body(bodyJson)
                    .header("Content-Type" to "application/json")
                    .response()

            withContext(Dispatchers.Main) {
                runOnUiThread {
                    val status = response.statusCode
                    if (status == 200) {
                        val (bytes, _) = result
                        if (bytes != null) {
                            val type = object: TypeToken<HashMap<String, List<HashMap<String, Any>>>>(){}.type
                            val data: HashMap<String, List<HashMap<String, Any>>> = Gson().fromJson(String(bytes), type) as HashMap<String, List<HashMap<String, Any>>>
                            val tableData: List<HashMap<String, Any>> = data["table_layout"]!!
                            for ((rank: Int, member: HashMap<String, Any>) in (tableData).withIndex()) {
                                val titles = tableLayout!!.findViewById<TableRow>(R.id.gp_titles)
                                val row = TableRow(ctx)

                                val rankTV = TextView(ctx)
                                val usernameTV = TextView(ctx)
                                val pointsTV = TextView(ctx)
                                val statusTV = TextView(ctx)

                                rankTV.layoutParams = titles.findViewById<TextView>(R.id.gp_rank_tv).layoutParams
                                usernameTV.layoutParams = titles.findViewById<TextView>(R.id.gp_username_tv).layoutParams
                                pointsTV.layoutParams = titles.findViewById<TextView>(R.id.gp_points_tv).layoutParams
                                statusTV.layoutParams = titles.findViewById<TextView>(R.id.gp_status_tv).layoutParams

                                rankTV.gravity = CENTER
                                usernameTV.gravity = CENTER
                                pointsTV.gravity = CENTER
                                statusTV.gravity = CENTER

                                rankTV.text = (rank + 1).toString()
                                usernameTV.text = member["username"] as String
                                val points: Double = member["points"] as Double
                                pointsTV.text = points.toInt().toString()
                                if (member.containsKey("is_admin")) {
                                    statusTV.text = "Admin"
                                }
                                else {
                                    statusTV.text = "Member"
                                }

                                if (member["username"] as String == userData["username"]) {
                                    rankTV.setTextColor(ContextCompat.getColor(ctx, R.color.colorLBHighlight))
                                    usernameTV.setTextColor(ContextCompat.getColor(ctx, R.color.colorLBHighlight))
                                    pointsTV.setTextColor(ContextCompat.getColor(ctx, R.color.colorLBHighlight))
                                    statusTV.setTextColor(ContextCompat.getColor(ctx, R.color.colorLBHighlight))
                                }

                                row.addView(rankTV)
                                row.addView(usernameTV)
                                row.addView(pointsTV)
                                row.addView(statusTV)

                                tableLayout.addView(row)
                            }
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
}
