package com.ritwikscompany.treasurehunt.ui

import  android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.kittinunf.fuel.Fuel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ritwikscompany.treasurehunt.R
import com.ritwikscompany.treasurehunt.utils.AddGroupsRVA
import com.ritwikscompany.treasurehunt.utils.MyChallengesRVA
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class EditChallengeActivity : AppCompatActivity() {

    private lateinit var mMap: GoogleMap
    private val ctx = this@EditChallengeActivity
    private lateinit var userData: HashMap<String, Any>
    private lateinit var challengeData: HashMap<String, Any>
    private lateinit var spinnerDiff: Spinner
    private lateinit var puzzleET: EditText
    private lateinit var nameTV: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_challenge)

        this.userData = intent.getSerializableExtra("userData") as HashMap<String, Any>
        this.challengeData = intent.getSerializableExtra("challengeData") as HashMap<String, Any>

        initialize()
    }


    private fun initialize() {
        puzzleET = findViewById(R.id.ec_puzzle)
        nameTV = findViewById(R.id.ec_name)
        spinnerDiff = findViewById(R.id.ec_difficulty)

        puzzleET.setText(challengeData["puzzle"] as String)
        nameTV.text = challengeData["name"] as String

        val diffArray = ctx.resources.getStringArray(R.array.difficulties).toMutableList()

        spinnerDiff.adapter = ArrayAdapter(
                ctx,
                android.R.layout.simple_spinner_item,
                diffArray
        )

        val bodyJson = Gson().toJson(hashMapOf(
            "user_id" to userData["user_id"],
            "pw" to userData["password"]
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
                            val type = object: TypeToken<ArrayList<String>>(){}.type
                            val groups = Gson().fromJson(String(bytes), type) as ArrayList<String>

                            findViewById<FloatingActionButton>(R.id.ec_edit_challenge).setOnClickListener {
                                createDialog(0.5, 1.79, groups)
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


    private fun createDialog(latitude: Double, longitude: Double, groups: ArrayList<String>) {
        val bodyJson = Gson().toJson(hashMapOf(
                "user_id" to userData["user_id"],
                "pw" to userData["password"],
                "name" to challengeData["name"] as String
        ))
        CoroutineScope(Dispatchers.IO).launch {
            val (request, response, result) = Fuel.post("${getString(R.string.host)}/api/get_challenge_data")
                    .body(bodyJson)
                    .header("Content-Type" to "application/json")
                    .response()

            withContext(Dispatchers.Main) {
                runOnUiThread {
                    val status = response.statusCode
                    if (status == 200) {
                        val (bytes, _) = result
                        if (bytes != null) {
                            val type = object : TypeToken<HashMap<String, Any>>(){}.type
                            val checkedGroups: ArrayList<String> = (Gson().fromJson(String(bytes), type) as HashMap<String, Any>)["user_groups_names"] as ArrayList<String>
                            println("checkedGroups: $checkedGroups")
                            val rv = RecyclerView(ctx)
                            rv.layoutManager = LinearLayoutManager(ctx)
                            rv.adapter = AddGroupsRVA(groups, checkedGroups)

                            AlertDialog.Builder(ctx)
                                    .setTitle("Edit Groups")
                                    .setMessage("NOTE: If you do not specify any group(s), the challenge will be public.\n\n")
                                    .setView(rv)
                                    .setPositiveButton("Edit") { _, _ ->
                                        updateChallenge(latitude, longitude, (rv.adapter as AddGroupsRVA).checkedGroups)
                                    }
                                    .setNegativeButton("Cancel") { builder, _ ->
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


    private fun updateChallenge(latitude: Double, longitude: Double, newGroups: ArrayList<String>) {
        val bodyJson = Gson().toJson(
            hashMapOf(
                "pw" to userData["password"],
                "user_id" to userData["user_id"],
                "challenge_id" to challengeData["challenge_id"],
                "new_latitude" to latitude,
                "new_longitude" to longitude,
                "new_puzzle" to puzzleET.text.toString(),
                "new_difficulty" to spinnerDiff.selectedItem.toString().toLowerCase(Locale.ROOT),
                "new_groups" to newGroups
            )
        )
        CoroutineScope(Dispatchers.IO).launch {
            val (_, response, _) = Fuel.post("${getString(R.string.host)}/api/update_challenge")
                    .body(bodyJson)
                    .header("Content-Type" to "application/json")
                    .response()

            withContext(Dispatchers.Main) {
                runOnUiThread {
                    if (response.statusCode == 200) {
                        val intent = Intent(ctx, MyChallengesActivity::class.java).apply {
                            putExtra("userData", userData)
                        }
                        startActivity(intent)
                    }
                    else {
                        Toast.makeText(ctx, "Update Challenge Failure", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }
//    override fun onMapReady(googleMap: GoogleMap) {
//        mMap = googleMap
//
//            val latitude = challengeData.get("latitude.")
//
//
//        val latlng = latitude
//            googleMap.addMarker(
//                latlng?.let {
//                    MarkerOptions()
//                        .position(it)
//                        .title("Marker somewhere")
//                })
//            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latlng, 17f))
//            googleMap.uiSettings.isZoomControlsEnabled = true
//
//
//
//
//        }
    }
