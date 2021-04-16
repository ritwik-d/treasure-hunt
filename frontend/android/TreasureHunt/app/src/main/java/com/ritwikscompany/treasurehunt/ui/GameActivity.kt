package com.ritwikscompany.treasurehunt.ui

import com.ritwikscompany.treasurehunt.R
import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.github.kittinunf.fuel.Fuel
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ritwikscompany.treasurehunt.utils.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class GameActivity : AppCompatActivity(), OnMapReadyCallback {

    private val ctx = this@GameActivity
    private var userData = HashMap<String, Any>()
    private var challengeName = ""
    private lateinit var map: GoogleMap
    private lateinit var lastLocation: Location
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        challengeName = intent.getStringExtra("challengeName") as String
        userData = intent.getSerializableExtra("userData") as HashMap<String, Any>

        title = challengeName

        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.game_map) as SupportMapFragment
        mapFragment.getMapAsync(ctx)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(ctx)

        startLocationUpdates()

        val bodyJson = Gson().toJson(hashMapOf(
            "user_id" to userData.get("user_id"),
            "pw" to userData.get("password"),
            "name" to challengeName
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
                            val type = object : TypeToken<HashMap<String, Any>>() {}.type
                            val challengeData: HashMap<String, Any> = Gson().fromJson(String(bytes), type) as HashMap<String, Any>
                            var radius: Double = Double.MIN_VALUE

                            when (challengeData["difficulty"] as String) {
                                "easy" -> radius = 10.0
                                "medium" -> radius = 5.0
                                "hard" -> radius = 1.0
                            }
                            placeMarkerOnMap(challengeData["latitude"] as Double, challengeData["longitude"] as Double, radius)

                            findViewById<TextView>(R.id.game_puzzle).text = "Puzzle: ${challengeData["puzzle"] as String}"
                            findViewById<TextView>(R.id.game_creator).text = "Creator: ${challengeData["creator_name"] as String}"
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


    override fun onMapReady(p0: GoogleMap?) {
        map = p0!!
        map.uiSettings.isZoomControlsEnabled = true

        setUpMap()
    }


    private fun placeMarkerOnMap(challengeLatitude: Double, challengeLongitude: Double, radius: Double) {
        val circleOptions = CircleOptions()

        circleOptions.center(LatLng(challengeLatitude, challengeLongitude))
        circleOptions.radius(radius)
        circleOptions.fillColor(Color.TRANSPARENT)
        circleOptions.strokeColor(Color.BLACK)

        map.addCircle(circleOptions)
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


    private fun completeChallenge(challengeData: HashMap<String, Any>) {
        val challengeId: Int = challengeData["challenge_id"] as Int

        val bodyJson = Gson().toJson(hashMapOf(
            "user_id" to userData["user_id"],
            "pw" to userData["password"],
            "challenge_id" to challengeId
        ))
        CoroutineScope(Dispatchers.IO).launch {
            val (_, response, _) = Fuel.post("${getString(R.string.host)}/api/complete_challenge")
                    .body(bodyJson)
                    .header("Content-Type" to "application/json")
                    .response()

            withContext(Dispatchers.Main) {
                runOnUiThread {
                    val status = response.statusCode
                    if (status == 200) {
                        val image = ImageView(ctx)
                        image.setImageResource(R.drawable.opened_treasure_chest)

                        AlertDialog.Builder(ctx)
                            .setTitle("Congratulations on completing the challenge, $challengeName! You get a point!")
                            .setView(image)
                            .setPositiveButton("OK") { _, _ ->
                                val intent = Intent(ctx, PickChallengeActivity::class.java).apply {
                                    putExtra("userData", userData)
                                }
                                startActivity(intent)
                            }
                            .show()
                    }

                    else if (status == 400) {
                        Toast.makeText(ctx, "ERROR", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }
}