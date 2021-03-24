package com.ritwikscompany.treasurehunt.ui

import android.os.Bundle
import android.view.Gravity.CENTER
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.github.kittinunf.fuel.Fuel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ritwikscompany.treasurehunt.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.jvm.internal.Intrinsics

class GroupPageActivity : AppCompatActivity() {

    private var groupName = ""
    private var userData = HashMap<String, Any>()
    private val ctx = this@GroupPageActivity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_page)

        this.groupName = intent.getStringExtra("groupName") as String
        this.userData = intent.getSerializableExtra("userData") as HashMap<String, Any>

        initializeTableLayout()
    }


    private fun initializeTableLayout() {
        val tableLayout = findViewById<TableLayout>(R.id.gp_tb)
        val bodyJson = Gson().toJson(hashMapOf(
                "user_id" to userData.get("user_id"),
                "pw" to userData.get("password"),
                "name" to groupName
        ))
        CoroutineScope(Dispatchers.IO).launch {
            val (request, response, result) = Fuel.post("${getString(R.string.host)}/get_group_data")
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
                            val tableData: List<HashMap<String, Any>> = data.get("table_layout")!!
                            println("tableData: $tableData")
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
                                usernameTV.text = member.get("username") as String
                                val points: Double = member.get("points") as Double
                                pointsTV.text = points.toInt().toString()
                                statusTV.text = member.get("status") as String

                                if (member.get("username") as String == userData.get("username")) {
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
