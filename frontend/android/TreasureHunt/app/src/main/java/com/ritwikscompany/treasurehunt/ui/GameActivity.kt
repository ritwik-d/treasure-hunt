package com.ritwikscompany.treasurehunt.ui

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
import com.ritwikscompany.treasurehunt.R
import com.ritwikscompany.treasurehunt.utils.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.collections.HashMap
import kotlin.math.cos


class GameActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private val ctx = this@GameActivity
    private var userData = HashMap<String, Any>()
    private var challengeName = ""
    private lateinit var map: GoogleMap
    private lateinit var lastLocation: Location
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    @Volatile private var stopThread = false

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
                                "easy" -> radius = 20.0
                                "medium" -> radius = 60.0
                                "hard" -> radius = 100.0
                            }
                            placeMarkerOnMap(challengeData["latitude"] as Double, challengeData["longitude"] as Double, radius)

                            findViewById<TextView>(R.id.game_puzzle).text = "Puzzle: ${challengeData["puzzle"] as String}"
                            findViewById<TextView>(R.id.game_creator).text = "Creator: ${challengeData["creator_name"] as String}"

                            launchDaemon(challengeData)
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

    override fun onStart() {
        super.onStart()
        stopThread = false
    }

    override fun onStop() {
        super.onStop()
        stopThread = true
    }

    override fun onMapReady(p0: GoogleMap?) {
        map = p0!!
        map.uiSettings.isZoomControlsEnabled = true
        map.setOnMarkerClickListener(ctx)

        setUpMap()

        findViewById<ImageButton>(R.id.game_satellite).setOnClickListener {
            if (map.mapType == GoogleMap.MAP_TYPE_NORMAL) {
                map.mapType = GoogleMap.MAP_TYPE_HYBRID
            }
            else {
                map.mapType = GoogleMap.MAP_TYPE_NORMAL
            }
        }
    }


    private fun placeMarkerOnMap(challengeLatitude: Double, challengeLongitude: Double, radius: Double) {
        var chalLatFinal = challengeLatitude
        var chalLongFinal = challengeLongitude
        val circleOptions = CircleOptions()

        val random = Random()
        val latRandom = (random.nextInt() % (radius * 2 / 3)) / 111111
        chalLatFinal += latRandom
        val longRandom = (random.nextInt() % (radius * 2 / 3)) / (111111 * cos(Math.toRadians(chalLatFinal)))
        chalLongFinal += longRandom

        circleOptions.center(LatLng(chalLatFinal, chalLongFinal))
        circleOptions.radius(radius)
        circleOptions.fillColor(Color.TRANSPARENT)
        circleOptions.strokeColor(Color.BLACK)

        map.addCircle(circleOptions)

        val marker = MarkerOptions()
        marker.position(LatLng(chalLatFinal, chalLongFinal))
        marker.title("This challenge's general area")

        map.addMarker(marker)
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

        fusedLocationClient.requestLocationUpdates(locationRequest, object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                lastLocation = p0.lastLocation
            }
        }, Looper.myLooper())
    }


    private fun launchDaemon(challengeData: HashMap<String, Any>) {
        CoroutineScope(Dispatchers.IO).launch {
            var distanceTraveled = 0.0
            var previousLocation: Location? = null
            if (ctx::lastLocation.isInitialized) {
                previousLocation = lastLocation
            }
            while (!ctx.stopThread) {
                Thread.sleep(1000)
                if (ctx::lastLocation.isInitialized && previousLocation != null) {
                    val resultsDistance = FloatArray(1)
                    Location.distanceBetween(
                            previousLocation.latitude,
                            previousLocation.longitude,
                            lastLocation.latitude,
                            lastLocation.longitude,
                            resultsDistance
                    )

                    distanceTraveled += resultsDistance[0]

                    val results = FloatArray(1)
                    Location.distanceBetween(
                            lastLocation.latitude,
                            lastLocation.longitude,
                            challengeData["latitude"] as Double,
                            challengeData["longitude"] as Double,
                            results
                    )

                    if (results[0] <= 1) {
                        completeChallenge(challengeData)
                        break
                    }
                }

                if (ctx::lastLocation.isInitialized) {
                    previousLocation = lastLocation
                }
            }
        }
    }


    private fun completeChallenge(challengeData: HashMap<String, Any>) {
        val challengeId: Int = (challengeData["challenge_id"] as Double).toInt()

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
                        AlertDialog.Builder(ctx)
                            .setTitle("Congratulations!")
                            .setMessage("Congratulations on completing the challenge, $challengeName! You get a point!")
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

    override fun onMarkerClick(p0: Marker?): Boolean = false
}