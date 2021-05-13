package com.ritwikscompany.treasurehunt.ui

import android.Manifest
import  android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
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
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ritwikscompany.treasurehunt.R
import com.ritwikscompany.treasurehunt.utils.AddGroupsRVA
import com.ritwikscompany.treasurehunt.utils.MyChallengesRVA
import com.ritwikscompany.treasurehunt.utils.Utils.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class EditChallengeActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private val ctx = this@EditChallengeActivity
    private lateinit var userData: HashMap<String, Any>
    private lateinit var challengeData: HashMap<String, Any>
    private lateinit var spinnerDiff: Spinner
    private lateinit var puzzleET: EditText
    private lateinit var nameTV: TextView
    private lateinit var map: GoogleMap
    private lateinit var lastLocation: Location
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_challenge)

        this.userData = intent.getSerializableExtra("userData") as HashMap<String, Any>
        this.challengeData = intent.getSerializableExtra("challengeData") as HashMap<String, Any>

        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.ec_map) as SupportMapFragment
        mapFragment.getMapAsync(ctx)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(ctx)

        startLocationUpdates()
        initialize()
    }


    override fun onBackPressed() {
        val intent = Intent(ctx, MyChallengesActivity::class.java).apply {
            putExtra("userData", userData)
        }
        startActivity(intent)
    }


    override fun onMapReady(p0: GoogleMap?) {
        map = p0!!
        map.setPadding(0, 0, 0, 250)
        map.uiSettings.isZoomControlsEnabled = true
        map.setOnMarkerClickListener(ctx)

        val marker = MarkerOptions()
        marker.position(LatLng(challengeData["latitude"] as Double, challengeData["longitude"] as Double))
        marker.title("This challenge's current location")

        map.addMarker(marker)

        setUpMap()

        findViewById<ImageButton>(R.id.ec_satellite).setOnClickListener {
            if (map.mapType == GoogleMap.MAP_TYPE_NORMAL) {
                map.mapType = GoogleMap.MAP_TYPE_HYBRID
            }
            else {
                map.mapType = GoogleMap.MAP_TYPE_NORMAL
            }
        }
    }


    override fun onMarkerClick(p0: Marker?) = false


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
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
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
        }, Looper.myLooper()!!)
    }


    private fun initialize() {
        puzzleET = findViewById(R.id.ec_puzzle)
        nameTV = findViewById(R.id.ec_name)
        spinnerDiff = findViewById(R.id.ec_difficulty)

        puzzleET.setText(challengeData["puzzle"] as String)
        nameTV.text = challengeData["name"] as String

        var diffArray: ArrayList<String> = ArrayList()
        when (challengeData["difficulty"]) {
            "easy" -> {
                diffArray = arrayListOf("Easy", "Medium", "Hard")
            }
            "medium" -> {
                diffArray = arrayListOf("Medium", "Easy", "Hard")
            }
            "hard" -> {
                diffArray = arrayListOf("Hard", "Easy", "Medium")
            }
        }

        spinnerDiff.adapter = ArrayAdapter(
                ctx,
                android.R.layout.simple_spinner_item,
                diffArray
        )

//        val previousIndex = diffArray.indexOf(challengeData["difficulty"] as String)
//        val oldItem = diffArray[0] // always easy; just didn't want to hardcode
//        diffArray[0] = challengeData["difficulty"] as String
//        diffArray[previousIndex] = oldItem

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
                            val type = object: TypeToken<ArrayList<String>>(){}.type
                            val groups = Gson().fromJson(String(bytes), type) as ArrayList<String>

                            findViewById<FloatingActionButton>(R.id.ec_edit_challenge).setOnClickListener {
                                if (!ctx::lastLocation.isInitialized) {
                                    return@setOnClickListener
                                }
                                createDialog(lastLocation.latitude, lastLocation.longitude, groups)
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
            val (_, response, result) = Fuel.post("${getString(R.string.host)}/api/get_challenge_data")
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
