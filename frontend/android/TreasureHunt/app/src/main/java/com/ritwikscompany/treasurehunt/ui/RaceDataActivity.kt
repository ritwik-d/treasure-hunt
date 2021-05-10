package com.ritwikscompany.treasurehunt.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.*
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.github.kittinunf.fuel.Fuel
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ritwikscompany.treasurehunt.R
import com.ritwikscompany.treasurehunt.utils.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.cos
import kotlin.math.floor

@Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class RaceDataActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {
    private val ctx = this@RaceDataActivity
    private lateinit var userData: HashMap<String, Any>
    private lateinit var startTimeTV: TextView
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    @Volatile private var stopThread = false
    @Volatile private lateinit var lastLocation: Location
    @Volatile private lateinit var map: GoogleMap
    @Volatile private lateinit var raceData: HashMap<String, Any>
    @Volatile private var markers = ArrayList<Marker>()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_race_data)

        this.raceData = intent.getSerializableExtra("raceData") as HashMap<String, Any>
        this.userData = intent.getSerializableExtra("userData") as HashMap<String, Any>
        this.startTimeTV = findViewById(R.id.rd_start_time)

        getRaceData()

        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.rd_map) as SupportMapFragment
        mapFragment.getMapAsync(ctx)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(ctx)

        initialize()
    }

    override fun onStart() {
        super.onStart()

        stopThread = false
    }

    override fun onStop() {
        super.onStop()

        stopThread = true
        leaveRace()

        val intent = Intent(ctx, RacesActivity::class.java).apply {
            putExtra("userData", userData)
        }
        startActivity(intent)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBackPressed() {
//        if (ctx::raceData.isInitialized) {
//            super.onBackPressed()
//            return
//        }
//
//        val currentTime = Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant())
//
//        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")
//        val startTime = simpleDateFormat.parse(raceData["startTime"] as String)
//
//        if (startTime.after(currentTime)) {
//            super.onBackPressed()
//            return
//        }

        stopThread = true
        leaveRace()

        val intent = Intent(ctx, RacesActivity::class.java).apply {
            putExtra("userData", userData)
        }
        startActivity(intent)
    }


    override fun onMapReady(p0: GoogleMap?) {
        map = p0!!
        map.uiSettings.isZoomControlsEnabled = true
        map.setOnMarkerClickListener(ctx)

        setUpMap()
    }


    override fun onMarkerClick(p0: Marker?): Boolean = false


    private fun setUpMap() {
        if (ActivityCompat.checkSelfPermission(
                        ctx,
                        Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    ctx,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    Utils.LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }

        map.isMyLocationEnabled = true

        fusedLocationClient.lastLocation.addOnSuccessListener(ctx) { location ->
            // Got last known location. In some rare situations this can be null.
            if (location != null) {
                lastLocation = location
                val currentLatLng = LatLng(location.latitude, location.longitude)
                if (ctx::map.isInitialized) {
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
                }
            }
        }

        val mapFragment = supportFragmentManager.findFragmentById(R.id.rd_map) as SupportMapFragment

        mapFragment.getMapAsync(ctx)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(ctx)

        startLocationUpdates()
    }


    @Suppress("DEPRECATION")
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

        if (ActivityCompat.checkSelfPermission(ctx,
                        Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(ctx,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    Utils.LOCATION_PERMISSION_REQUEST_CODE)
            return
        }

        fusedLocationClient.requestLocationUpdates(
                locationRequest,
                object: LocationCallback() {
                    override fun onLocationResult(p0: LocationResult) {
                        lastLocation = p0.lastLocation
                    }
                },
                Looper.myLooper())
    }


    @SuppressLint("SimpleDateFormat")
    @RequiresApi(Build.VERSION_CODES.O)
    private fun initialize() {
        title = raceData["title"] as String

        // set up time remaining

        val currentTime = Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant())

        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")
        val startTime = simpleDateFormat.parse(raceData["startTime"] as String)

        if (startTime.after(currentTime)) {
            val timeRemaining = (startTime.time - currentTime.time)

            val timer = object: CountDownTimer(timeRemaining, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    if (millisUntilFinished / 1000 < 60) {
                        startTimeTV.setTextColor(
                                ContextCompat.getColor(ctx, R.color.red)
                        )
                    } else if (millisUntilFinished / 1000 < 600) {
                        startTimeTV.setTextColor(
                                ContextCompat.getColor(ctx, R.color.yellow)
                        )
                    }

                    startTimeTV.text = formatSeconds(millisUntilFinished)
                }

                override fun onFinish() {
                    startRace()
                }
            }

            timer.start()
        } else {
            startRace()
        }
    }

    private fun getRaceData() {
        val bodyJson = Gson().toJson(hashMapOf<String, Any>(
            "user_id" to userData["user_id"] as Int,
            "pw" to userData["password"] as String,
            "race_id" to raceData["race_id"] as Int
        ))
        CoroutineScope(Dispatchers.IO).launch {
            val (_, response, result) = Fuel.post("${getString(R.string.host)}/api/get_race_data")
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
                            ctx.raceData = Gson().fromJson(String(bytes), type) as HashMap<String, Any>
                        }

                        else {
                            Toast.makeText(ctx, "Network Error", Toast.LENGTH_LONG).show()
                        }
                    }

                    else if (status == 404) {
                        Toast.makeText(ctx, "Log In Failure", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }


    private fun startRace() {
        startTimeTV.text = getString(R.string.go)

        setUpRaceMap()

        val updateRaceMapThread = UpdateRaceMapThread()
        updateRaceMapThread.start()

        val isCompletedThread = IsCompletedThread()
        isCompletedThread.start()
    }


    private fun completeRace() {
        val bodyJson = Gson().toJson(hashMapOf(
            "pw" to userData["password"] as String,
            "user_id" to userData["user_id"],
            "race_id" to raceData["race_id"],
        ))

        CoroutineScope(Dispatchers.IO).launch {
            val (_, response, _) = Fuel.post("${getString(R.string.host)}/api/complete_race")
                .body(bodyJson)
                .header("Content-Type" to "application/json")
                .response()

            withContext(Dispatchers.Main) {
                runOnUiThread {
                    if (response.statusCode == 200) {
                        ctx.stopThread = true
                    }
                }
            }
        }
    }


    private fun setUpRaceMap() {
        if (!ctx::lastLocation.isInitialized || !ctx::map.isInitialized) {
            return
        }

        val bodyJson = Gson().toJson(hashMapOf(
            "pw" to userData["password"] as String,
            "user_id" to userData["user_id"],
            "race_id" to raceData["race_id"],
            "latitude" to lastLocation.latitude,
            "longitude" to lastLocation.longitude
        ))

        CoroutineScope(Dispatchers.IO).launch {
            val (_, response, result) = Fuel.post("${getString(R.string.host)}/api/join_race")
                .body(bodyJson)
                .header("Content-Type" to "application/json")
                .response()

            withContext(Dispatchers.Main) {
                runOnUiThread {
                    if (response.statusCode == 200) {
                        val (bytes, _) = result

                        if (bytes != null) {
                            val type = object: TypeToken<ArrayList<HashMap<String, Any>>>() {}.type
                            val usersInRace = Gson().fromJson(String(bytes), type) as ArrayList<HashMap<String, Any>>
                            val noPFP = BitmapDescriptorFactory.fromResource(R.drawable.no_pfp)

                            for (user in usersInRace) {
                                val userMarker = MarkerOptions()
                                    .icon(noPFP)
                                    .title(user["username"] as String)
                                    .position(LatLng(
                                        user["latitude"] as Double,
                                        user["longitude"] as Double
                                    ))

                                map.addMarker(userMarker)
                            }

                            if (ctx.raceData.containsKey("latitude")) {
                                var chalLatFinal = raceData["latitude"] as Double
                                var chalLongFinal = raceData["longitude"] as Double
                                val circleOptions = CircleOptions()

                                var radius: Double = Double.MIN_VALUE
                                when (raceData["difficulty"] as String) {
                                    "easy" -> radius = 20.0
                                    "medium" -> radius = 60.0
                                    "hard" -> radius = 100.0
                                }

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
                        }
                    }
                }
            }
        }
    }

    private fun leaveRace() {
        val bodyJson = Gson().toJson(hashMapOf(
                "pw" to userData["password"] as String,
                "user_id" to userData["user_id"] ,
                "race_id" to raceData["race_id"],
        ))

        CoroutineScope(Dispatchers.IO).launch {
            val (_, _, _) = Fuel.post("${getString(R.string.host)}/api/leave_race")
                .body(bodyJson)
                .header("Content-Type" to "application/json")
                .response()
        }
    }


    private fun formatSeconds(millisFromNow: Long): String {
        var seconds = floor(millisFromNow.toDouble() / 1000)
        val days = floor(seconds / (24 * 3600))
        seconds %= (24 * 3600)
        val hours = floor(seconds / 3600)
        seconds %= 3600
        val minutes = floor(seconds / 60)
        seconds %= 60

        return "${days.toInt()}:${hours.toInt()}:${minutes.toInt()}:${seconds.toInt()}"
    }


    private fun updateRaceLocation() {
        if (!ctx::lastLocation.isInitialized || !ctx::map.isInitialized) {
            return
        }

        val bodyJson = Gson().toJson(hashMapOf(
                "user_id" to userData["user_id"],
                "pw" to userData["password"] as String,
                "race_id" to raceData["race_id"],
                "latitude" to lastLocation.latitude,
                "longitude" to lastLocation.longitude
        ))
        CoroutineScope(Dispatchers.IO).launch {
            val (_, response, result) = Fuel.post("${getString(R.string.host)}/api/update_race_location")
                    .body(bodyJson)
                    .header("Content-Type" to "application/json")
                    .response()

            withContext(Dispatchers.Main) {
                runOnUiThread {
                    val (bytes, _) = result
                    if (response.statusCode == 200 && bytes != null) {
                        val type = object: TypeToken<ArrayList<HashMap<String, Any>>>(){}.type
                        val userLocations = Gson().fromJson(String(bytes), type) as ArrayList<HashMap<String, Any>>
                        val noPFP = BitmapDescriptorFactory.fromResource(R.drawable.no_pfp)

                        for (currentMarker in ctx.markers) {
                            currentMarker.remove()
                        }

                        for (user in userLocations) {
                            val marker = MarkerOptions()
                            marker.position(LatLng(
                                user["latitude"] as Double,
                                user["longitude"] as Double
                            ))
                            marker.title(user["username"] as String)
                            marker.icon(noPFP)

                            val markerObject = ctx.map.addMarker(marker)
                            ctx.markers.add(markerObject)
                        }
                    }
                }
            }
        }
    }


    inner class UpdateRaceMapThread: Thread() {
        override fun run() {
            while (!ctx.stopThread) {
                sleep(1000)

                runOnUiThread {
                    ctx.updateRaceLocation()
                }
            }
        }
    }

    inner class IsCompletedThread: Thread() {
        override fun run() {
            while (!ctx.stopThread) {
                sleep(1000)

                if (!ctx::lastLocation.isInitialized || !ctx::map.isInitialized || !raceData.containsKey("latitude")) {
                    continue
                }

                val results = FloatArray(1)
                Location.distanceBetween(
                    lastLocation.latitude,
                    lastLocation.longitude,
                    raceData["latitude"] as Double,
                    raceData["longitude"] as Double,
                    results
                )

                if (results[0] <= 3) {
                    runOnUiThread {
                        ctx.completeRace()
                    }
                }
            }
        }
    }
}


//    private fun updateRaceMap() {
//        if (!ctx::lastLocation.isInitialized || !ctx::map.isInitialized) {
//            return
//        }
//
//        map.clear()
//
//        val bodyJson = Gson().toJson(hashMapOf(
//            "pw" to userData["password"] as String,
//            "user_id" to userData["user_id"] as Int,
//            "race_id" to raceData["race_id"] as Int,
//            "latitude" to lastLocation.latitude,
//            "longitude" to lastLocation.longitude
//        ))
//
//        checkForLocation()
//
//        CoroutineScope(Dispatchers.IO).launch {
//            val (_, response, result) = Fuel.post("${getString(R.string.host)}/update_race_location")
//                .body(bodyJson)
//                .header("Content-Type" to "application/json")
//                .response()
//
//            withContext(Dispatchers.Main) {
//                runOnUiThread {
//                    if (response.statusCode == 200) {
//                        val (bytes, _) = result
//
//                        if (bytes != null) {
//                            val type = object : TypeToken<ArrayList<HashMap<String, Any>>>(){}.type
//                            val usersInRace = Gson().fromJson(String(bytes), type) as ArrayList<HashMap<String, Any>>
//
//                            for (userInRace in usersInRace) {
//                                val noPFP = BitmapDescriptorFactory.fromResource(R.drawable.no_pfp)
//
//                                val user = MarkerOptions()
//                                    .icon(noPFP)
//                                    .title(userInRace["username"] as String)
//
//                                map.addMarker(user)
//                            }
//                        }
//                    }
//                }
//            }
//        }
//    }

//    private fun checkForLocation() {
//        val bodyJson = Gson().toJson(hashMapOf(
//            "pw" to userData["password"] as String,
//            "user_id" to userData["user_id"],
//            "race_id" to raceData["race_id"]
//        ))
//
//        CoroutineScope(Dispatchers.IO).launch {
//            val (_, response, result) = Fuel.post("${getString(R.string.host)}/get_race")
//                .body(bodyJson)
//                .header("Content-Type" to "application/json")
//                .response()
//
//            withContext(Dispatchers.Main) {
//                runOnUiThread {
//                    if (response.statusCode == 200) {
//                        val (bytes, _) = result
//
//                        if (bytes != null) {
//                            val type = object: TypeToken<HashMap<String, Any>>() {}.type
//                            val usersInRace = Gson().fromJson(String(bytes), type) as HashMap<String, Any>
//
//                            if (usersInRace["longitude"] == lastLocation.longitude && usersInRace["latitude"] == lastLocation.latitude) {
//                                completeRace()
//                            }
//                        }
//                    } else {
//                        Toast.makeText(
//                            ctx,
//                            "You have failed to complete the race",
//                            Toast.LENGTH_SHORT
//                        ).show()
//                    }
//                }
//            }
//        }
//    }