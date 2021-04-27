package com.ritwikscompany.treasurehunt.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory
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
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.math.floor

@Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS", "DEPRECATION")
class RaceDataActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {
    private var ctx = this@RaceDataActivity
    private lateinit var raceData: HashMap<*, *>
    private lateinit var userData: HashMap<*, *>
    private lateinit var startTimeTV: TextView
    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var lastLocation: Location
    private lateinit var locationRequest: LocationRequest
    private val mainHandler = Handler()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_race_data)

        raceData = intent.getSerializableExtra("raceData") as HashMap<*, *>
        userData = intent.getSerializableExtra("userData") as HashMap<*, *>

        setUpUI()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setUpUI() {
        setUpRaceTitle()
        setUpRaceTimeRemaining()
    }

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
                moveCamera(currentLatLng, DEFAULT_ZOOM)
            }
        }

        val mapFragment = supportFragmentManager.findFragmentById(R.id.rd_map) as SupportMapFragment

        mapFragment.getMapAsync(ctx)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(ctx)

        startLocationUpdates()
    }

    private fun moveCamera(latLng: LatLng, zoom: Float) {
        if (ctx::map.isInitialized) {
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom))
        }
    }

    @SuppressLint("SimpleDateFormat")
    @RequiresApi(Build.VERSION_CODES.O)
    private fun setUpRaceTimeRemaining() {
        val currentTime = Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant())

        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")
        val startTime = simpleDateFormat.parse(raceData["startTime"] as String)

        if (startTime.after(currentTime)) {
            val timeRemaining = (startTime.time - currentTime.time)

            val timer = object : CountDownTimer(timeRemaining, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    startTimeTV.text = formatSeconds(millisUntilFinished / 1000)

                    if (millisUntilFinished / 1000 < 60) {
                        startTimeTV.setTextColor(
                            ContextCompat.getColor(ctx, R.color.red)
                        )
                    } else if (millisUntilFinished / 1000 < 600) {
                        startTimeTV.setTextColor(
                            ContextCompat.getColor(ctx, R.color.yellow)
                        )
                    }
                }

                @SuppressLint("SetTextI18n")
                override fun onFinish() {
                    startTimeTV.text = "GO!"

                    setUpRaceMap()

                    val updateRaceMapThread = UpdateRaceMapThread()
                    updateRaceMapThread.start()
                }
            }

            timer.start()
        } else {
            setUpRaceMap()

            val updateRaceMapThread = UpdateRaceMapThread()
            updateRaceMapThread.start()
        }
    }

    private fun updateRaceMap() {
        if (!ctx::lastLocation.isInitialized || !ctx::map.isInitialized) {
            return
        }

        map.clear()

        val bodyJson = Gson().toJson(hashMapOf(
            "pw" to userData["password"] as String,
            "user_id" to (userData["user_id"] as Double).toInt(),
            "race_id" to (raceData["race_id"] as Double).toInt(),
            "latitude" to lastLocation.latitude,
            "longitude" to lastLocation.longitude
        ))

        checkForLocation()

        CoroutineScope(Dispatchers.IO).launch {
            val (_, response, result) = Fuel.post("${getString(R.string.host)}/update_race_location")
                .body(bodyJson)
                .header("Content-Type" to "application/json")
                .response()

            withContext(Dispatchers.Main) {
                runOnUiThread {
                    if (response.statusCode == 200) {
                        val (bytes, _) = result

                        if (bytes != null) {
                            val type = object : TypeToken<ArrayList<HashMap<String, Any>>>() {}.type
                            val usersInRace = Gson().fromJson(String(bytes), type) as ArrayList<HashMap<String, Any>>

                            for (userInRace in usersInRace) {
                                val noPFP = BitmapDescriptorFactory.fromResource(R.drawable.no_pfp)

                                val user = MarkerOptions()
                                    .icon(noPFP)
                                    .title(userInRace["username"] as String)

                                map.addMarker(user)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun checkForLocation() {
        val bodyJson = Gson().toJson(hashMapOf(
            "pw" to userData["password"] as String,
            "user_id" to (userData["user_id"] as Double).toInt(),
            "race_id" to (raceData["race_id"] as Double).toInt(),
            "group_name" to raceData["group_name"] as String
        ))

        CoroutineScope(Dispatchers.IO).launch {
            val (_, response, result) = Fuel.post("${getString(R.string.host)}/get_race")
                .body(bodyJson)
                .header("Content-Type" to "application/json")
                .response()

            withContext(Dispatchers.Main) {
                runOnUiThread {
                    if (response.statusCode == 200) {
                        val (bytes, _) = result

                        if (bytes != null) {
                            val type = object : TypeToken<HashMap<String, Any>>() {}.type
                            val usersInRace = Gson().fromJson(String(bytes), type) as HashMap<String, Any>

                            if (usersInRace["longitude"] == lastLocation.longitude && usersInRace["latitude"] == lastLocation.latitude) {
                                completeRace()
                            }
                        }
                    } else {
                        Toast.makeText(
                            ctx,
                            "You have failed to complete the race",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    private fun completeRace() {
        val bodyJson = Gson().toJson(hashMapOf(
            "pw" to userData["password"] as String,
            "user_id" to (userData["user_id"] as Double).toInt(),
            "race_id" to (raceData["race_id"] as Double).toInt(),
            "group_name" to raceData["group_name"] as String
        ))

        CoroutineScope(Dispatchers.IO).launch {
            val (_, response, _) = Fuel.post("${getString(R.string.host)}/complete_race")
                .body(bodyJson)
                .header("Content-Type" to "application/json")
                .response()

            withContext(Dispatchers.Main) {
                runOnUiThread {
                    if (response.statusCode == 200) {
                        Toast.makeText(
                            ctx,
                            "You have successfully completed the race before anyone else",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }


    private fun setUpRaceMap() {
        if (!ctx::lastLocation.isInitialized || !ctx::map.isInitialized) {
            return
        }

        map.clear()

        val bodyJson = Gson().toJson(hashMapOf(
            "pw" to userData["password"] as String,
            "user_id" to (userData["user_id"] as Double).toInt(),
            "race_id" to (raceData["race_id"] as Double).toInt(),
            "latitude" to lastLocation.latitude,
            "longitude" to lastLocation.longitude
        ))

        CoroutineScope(Dispatchers.IO).launch {
            val (_, response, result) = Fuel.post("${getString(R.string.host)}/join_race")
                .body(bodyJson)
                .header("Content-Type" to "application/json")
                .response()

            withContext(Dispatchers.Main) {
                runOnUiThread {
                    if (response.statusCode == 200) {
                        val (bytes, _) = result

                        if (bytes != null) {
                            val type = object : TypeToken<ArrayList<HashMap<String, Any>>>() {}.type
                            val usersInRace = Gson().fromJson(String(bytes), type) as ArrayList<HashMap<String, Any>>

                            for (userInRace in usersInRace) {
                                val noPFP = BitmapDescriptorFactory.fromResource(R.drawable.no_pfp)

                                val user = MarkerOptions()
                                    .icon(noPFP)
                                    .title(userInRace["username"] as String)

                                map.addMarker(user)
                            }
                        }
                    }
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SimpleDateFormat")
    override fun onBackPressed() {
        if (ctx::raceData.isInitialized) {
            super.onBackPressed()
            return
        }

        val currentTime = Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant())

        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")
        val startTime = simpleDateFormat.parse(raceData["startTime"] as String)

        if (startTime.after(currentTime)) {
            super.onBackPressed()
            return
        }

        leaveRace()
        super.onBackPressed()
    }

    private fun leaveRace() {
        val bodyJson = Gson().toJson(hashMapOf(
            "pw" to userData["password"] as String,
            "user_id" to (userData["user_id"] as Double).toInt(),
            "race_id" to (raceData["race_id"] as Double).toInt(),
        ))

        CoroutineScope(Dispatchers.IO).launch {
            val (_, response, _) = Fuel.post("${getString(R.string.host)}/leave_race")
                .body(bodyJson)
                .header("Content-Type" to "application/json")
                .response()

            withContext(Dispatchers.Main) {
                runOnUiThread {
                    if (response.statusCode == 200) {
                        startActivity(Intent(ctx, RacesActivity::class.java).apply {
                            putExtra("userData", userData)
                        })
                    }
                }
            }
        }

        super.onBackPressed()
    }

    private fun setUpRaceTitle() {
        title = raceData["title"] as String
    }

    override fun onMapReady(p0: GoogleMap?) {
        map = p0!!
        map.uiSettings.isZoomControlsEnabled = true

        map.setOnMarkerClickListener(ctx)

        setUpMap()
    }

    override fun onMarkerClick(p0: Marker?): Boolean = false

    companion object {
        private const val DEFAULT_ZOOM = 15f
    }

    private fun formatSeconds(seconds: Long): String {
        val secondsRemaining: Long

        if (seconds < 60) {
            return "$seconds"
        } else {
            var formattedTime = ""
            val minutes = floor((seconds / 60).toDouble()).toLong()
            secondsRemaining = seconds - (minutes * 60)

            if (minutes < 60) {
                formattedTime += "$minutes:$secondsRemaining"
                return formattedTime
            } else {
                val hours = floor((minutes / 60).toDouble()).toLong()
                val minutesRemaining: Long = minutes - hours

                return if (hours < 24) {
                    formattedTime += "$hours:$minutesRemaining:$secondsRemaining"
                    formattedTime
                } else {
                    val days = floor((hours / 24).toDouble()).toLong()
                    val hoursRemaining = hours - (days * 24)

                    formattedTime += "$days:$hoursRemaining:$minutesRemaining:$secondsRemaining"
                    formattedTime
                }
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

    inner class UpdateRaceMapThread : Thread() {
        override fun run() {
            while (true) {
                sleep(1000)
                
                mainHandler.post {
                    updateRaceMap()
                }
            }
        }
    }
}