package com.ritwikscompany.treasurehunt.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.kittinunf.fuel.Fuel
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ritwikscompany.treasurehunt.R
import com.ritwikscompany.treasurehunt.utils.AddGroupsRVA
import com.ritwikscompany.treasurehunt.utils.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class CreateChallengeActivity : AppCompatActivity(), OnMapReadyCallback {

    private val ctx = this@CreateChallengeActivity
    private var userData: HashMap<String, Any> = hashMapOf()
    private lateinit var diffSpinner: Spinner
    private lateinit var puzzleET: EditText
    private lateinit var nameET: EditText
    private lateinit var puzzle: String
    private lateinit var name: String
    private lateinit var checkMark: Drawable
    private lateinit var map: GoogleMap
    private lateinit var lastLocation: Location
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_challenge)
        this.userData = intent.getSerializableExtra("userData") as HashMap<String, Any>

        checkMark = Utils.getCheckMark(ctx)

        verifyFields()

        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.cc_map) as SupportMapFragment
        mapFragment.getMapAsync(ctx)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(ctx)

        startLocationUpdates()

        findViewById<Button>(R.id.cc_create).setOnClickListener {
            createOnClick(lastLocation.latitude, lastLocation.longitude)
        }
    }


    override fun onMapReady(p0: GoogleMap?) {
        map = p0!!
        map.uiSettings.isZoomControlsEnabled = true

        setUpMap()

        findViewById<ImageButton>(R.id.cc_satellite).setOnClickListener {
            if (map.mapType == GoogleMap.MAP_TYPE_NORMAL) {
                map.mapType = GoogleMap.MAP_TYPE_HYBRID
            }
            else {
                map.mapType = GoogleMap.MAP_TYPE_NORMAL
            }
        }
    }


    private fun setUpMap() {
        if (ActivityCompat.checkSelfPermission(ctx,
                        Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(ctx,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), Utils.LOCATION_PERMISSION_REQUEST_CODE)
            return
        }

        map.isMyLocationEnabled = true

        fusedLocationClient.lastLocation.addOnSuccessListener(ctx) { location ->
            // Got last known location. In some rare situations this can be null.
            if (location != null) {
                lastLocation = location
                val currentLatLng = LatLng(location.latitude, location.longitude)
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 17f))
            }
        }
    }


    private fun startLocationUpdates() {
        locationRequest = LocationRequest()
        locationRequest.interval = Utils.UPDATE_INTERVAL
        locationRequest.fastestInterval = Utils.FASTEST_INTERVAL
        locationRequest.priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY

        val builder = LocationSettingsRequest.Builder()
        builder.addLocationRequest(locationRequest)
        val locationSettingsRequest = builder.build()
        val settingsClient = LocationServices.getSettingsClient(ctx)
        settingsClient.checkLocationSettings(locationSettingsRequest)
        if (ActivityCompat.checkSelfPermission(
                        ctx,
                        Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        ctx,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        if (ActivityCompat.checkSelfPermission(ctx,
                        Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(ctx,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    Utils.LOCATION_PERMISSION_REQUEST_CODE)
            return
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, object: LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                lastLocation = p0.lastLocation
            }
        }, Looper.myLooper())
    }


    private fun verifyFields() {
        puzzleET = findViewById(R.id.cc_puzzle)
        puzzle = puzzleET.text.toString()
        nameET = findViewById(R.id.cc_name)
        name = nameET.text.toString()

        val button = findViewById<Button>(R.id.cc_create)

        puzzleET.addTextChangedListener ( object: TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                puzzle = puzzleET.text.toString()

                if (puzzle.length < 3) {
                    puzzleET.error = "Puzzle must be at least 3 characters"
                    puzzleET.requestFocus()
                    button.isEnabled = false
                    return
                }

                puzzleET.setError("Good", checkMark)
                puzzleET.requestFocus()

                if (puzzleET.error == "Good" && nameET.error == "Good") {
                    button.isEnabled = true
                }
            }

            override fun afterTextChanged(p0: Editable?) {}
        } )

        nameET.addTextChangedListener ( object: TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                name = nameET.text.toString()

                if (name.length < 3) {
                    nameET.error = "Puzzle must be at least 3 characters"
                    nameET.requestFocus()
                    button.isEnabled = false
                    return
                }

                nameET.setError("Good", checkMark)
                nameET.requestFocus()

                if (puzzleET.error == "Good" && nameET.error == "Good") {
                    button.isEnabled = true
                }
            }

            override fun afterTextChanged(p0: Editable?) {}
        })
    }


    private fun createOnClick(latitude: Double, longitude: Double) {
        val bodyJson = Gson().toJson(hashMapOf(
            "user_id" to userData["user_id"],
            "pw" to userData["password"]
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
                            val type = object: TypeToken<MutableList<String>>(){}.type
                            val groups = Gson().fromJson(String(bytes), type) as MutableList<String>
                            if (groups.size == 0) {
                                createChallenge(latitude, longitude)
                            }
                            else {
                                val adapter = AddGroupsRVA(groups as ArrayList<String>)

                                val rv = RecyclerView(ctx)
                                rv.layoutManager = LinearLayoutManager(ctx)
                                rv.adapter = adapter

                                val builder = AlertDialog.Builder(ctx)
                                builder.setTitle("Add Groups")
                                builder.setMessage("NOTE: If you do not specify any group(s), the challenge will be public.\n\n")
                                builder.setView(rv)
                                builder.setPositiveButton("Finish") { _, _ ->
                                    val addedGroups = (rv.adapter as AddGroupsRVA).checkedGroups
                                    createChallenge(latitude, longitude, addedGroups)
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


    private fun createChallenge(latitude: Double, longitude: Double, groups: ArrayList<String> = arrayListOf()) {
        diffSpinner = findViewById(R.id.cc_diff)
        val bodyJson = Gson().toJson(hashMapOf(
            "user_id" to userData["user_id"],
            "pw" to userData["password"],
            "latitude" to latitude,
            "longitude" to longitude,
            "difficulty" to diffSpinner.selectedItem.toString().toLowerCase(Locale.ROOT),
            "groups" to groups,
            "name" to name,
            "puzzle" to puzzle
        ))
        CoroutineScope(Dispatchers.IO).launch {
            val (_, response, _) = Fuel.post("${getString(R.string.host)}/api/create_challenge")
                    .body(bodyJson)
                    .header("Content-Type" to "application/json")
                    .response()

            withContext(Dispatchers.Main) {
                runOnUiThread {
                    when (response.statusCode) {
                        201 -> {
                            val intent = Intent(ctx, MyChallengesActivity::class.java).apply {
                                putExtra("userData", userData)
                            }
                            startActivity(intent)
                        }
                        400 -> {
                            Toast.makeText(
                                    ctx,
                                    "A challenge has already been created with this name",
                                    Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            }
        }
    }
}