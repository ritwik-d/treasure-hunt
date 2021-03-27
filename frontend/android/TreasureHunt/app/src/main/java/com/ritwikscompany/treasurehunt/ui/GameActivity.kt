package com.ritwikscompany.treasurehunt.ui

import com.ritwikscompany.treasurehunt.R
import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.github.kittinunf.fuel.Fuel
import com.google.android.gms.location.*
import com.google.android.gms.location.LocationServices.getFusedLocationProviderClient
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class GameActivity : AppCompatActivity(), OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener {


    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var lastLocation: Location
    private lateinit var locationCallback: LocationCallback
    private var mLocationRequest: LocationRequest? = null

    private val UPDATE_INTERVAL = 10 * 1000 /* 10 secs */.toLong()
    private val FASTEST_INTERVAL: Long = 5000 /* 5 sec */


    // Notifications
    lateinit var notificationManager: NotificationManager
    lateinit var notificationChannel: NotificationChannel
    lateinit var builder: Notification.Builder

    private val channelId = "com.legolegions.teasurehunt.notifications"
    private var ex_type = ""
    private val ctx = this@GameActivity
    private var userData = HashMap<String, Any>()
    private var challengeData = HashMap<String, Any>()
    private var challengeName = ""
    private lateinit var latlng: LatLng

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    // asks for permission if already not given.
    // checks if GPS is enabled.
    // sets last location.
    private fun setUpMap() {
        if (ActivityCompat.checkSelfPermission(
                        this,
                        android.Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                    LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }

        map.isMyLocationEnabled = true
        fusedLocationClient.lastLocation.addOnSuccessListener(this) { location ->
            if (location != null) {
                lastLocation = location
                val currentLatLng = LatLng(location.latitude, location.longitude)
                placeMarkerOnMap(currentLatLng)

            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_game)
        challengeName = intent.getStringExtra("challengeName") as String
        userData = intent.getSerializableExtra("userData") as HashMap<String, Any>

        val buttonBack = findViewById<ImageButton>(R.id.imageButton2)
        buttonBack.setOnClickListener {
            finish()
        }
        val buttonHome = findViewById<ImageButton>(R.id.imageButton)
        buttonHome.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        val bodyJson = Gson().toJson(hashMapOf(
                "user_id" to userData.get("user_id"),
                "pw" to userData.get("password"),
                "name" to challengeName
        ))
        CoroutineScope(Dispatchers.IO).launch {
            val (request, response, result) = Fuel.post("${getString(R.string.host)}/get_challenge_data")
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
                            challengeData = Gson().fromJson(String(bytes), type) as HashMap<String, Any>
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

        println("challengedata: $challengeData")
        findViewById<TextView>(R.id.puzzle).text = "Puzzle: ${challengeData["puzzle"] as String}"

        val start = findViewById<Button>(R.id.start_challenge)
        start.setOnClickListener {
            val builder: AlertDialog.Builder = AlertDialog.Builder(ctx)
            val array = ctx.resources.getStringArray(R.array.exerciseTypes)

            builder?.setTitle("How are you exercising?")
            builder?.setItems(array) { _, which ->
                println(which)
                ex_type = when (which) {
                    0 -> "bike"
                    1 -> "run"
                    2 -> "walk"
                    else -> "walk"
                }
                println(ex_type)
            }
            val dialog: AlertDialog? = builder.create()
            dialog?.show()


            val buttonHere = findViewById<Button>(R.id.buttonhere)
            val lat = challengeData.get("latitude") as Double
            val long = challengeData.get("longitude") as Double

            latlng = LatLng(lat, long)

            // makes buttonHere visible
            buttonHere.visibility = View.VISIBLE
            start.visibility = View.INVISIBLE
            // start time
            val startTime: Long = System.currentTimeMillis()
            // start location

            if (!ctx::lastLocation.isInitialized){
                lastLocation = Location("Self")
                lastLocation.latitude = lat
                lastLocation.longitude = long
            }

            val startLocation: Location = lastLocation
            buttonHere.setOnClickListener {
                val results = FloatArray(1)

                Location.distanceBetween(
                    lastLocation.latitude,
                    lastLocation.longitude,
                    latlng.latitude,
                    latlng.longitude,
                    results
                )

                if (results[0] <= 50) {
                    // end time
                    val endTime: Long = System.currentTimeMillis()
                    // elapsed time (seconds)
                    // dividing by 1000 is to convert milliseconds into seconds
                    val elapsed: Int = (endTime - startTime).toInt() / 1000
                    // distance traveled
                    val results2 = FloatArray(1)
                    latlng?.let { it1 ->
                        Location.distanceBetween(
                            startLocation.latitude,
                            startLocation.longitude,
                            latlng.latitude,
                            it1.longitude,
                            results2
                        )
                    }
                    println("meters: ${results[0]}")
                    println("elapsed: $elapsed")
                    // converts seconds to hours
                    val hours = elapsed / 3600
                    println(hours)
                    // converts meters to miles
                    val miles = results[0].toInt() / 1609
                    // calculates miles per hour
                    val mph = miles.toDouble() / hours.toDouble()

                    when {
                        (mph > 20.toDouble()) and (ex_type == "run") -> {
                            Toast.makeText(ctx, "Your speed is unreasonable for running", Toast.LENGTH_LONG).show()
                            val intent = Intent(ctx, PickChallengeActivity::class.java).apply {
                                putExtra("userData", userData)
                            }
                            startActivity(intent)
                        }
                        (mph > 100.toDouble()) and (ex_type == "bike") -> {
                            Toast.makeText(ctx, "Your speed is unreasonable for biking", Toast.LENGTH_LONG).show()
                            val intent = Intent(ctx, PickChallengeActivity::class.java).apply {
                                putExtra("userData", userData)
                            }
                            startActivity(intent)
                        }
                        (mph > 5.toDouble()) and (ex_type == "walk") -> {
                            Toast.makeText(ctx, "Your speed is unreasonable for walking", Toast.LENGTH_LONG).show()
                            val intent = Intent(ctx, PickChallengeActivity::class.java).apply {
                                putExtra("userData", userData)
                            }
                            startActivity(intent)
                        }
                        else -> {
                            completeChallenge()
                        }
                    }
                }

                else {
                    val msg = "You did not get it, Try again"
                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                }

            }
            val mapFragment = supportFragmentManager
                    .findFragmentById(R.id.map) as SupportMapFragment
            mapFragment.getMapAsync(this)
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

            startLocationUpdates()

            // Notifications
            notificationManager =
                    getSystemService(
                            Context.NOTIFICATION_SERVICE
                    ) as NotificationManager

            createNotificationChannel(
                    channelId,
                    "Treasure Hunt",
                    "Treasure Hunt Channel"
            )

        }
    }


    private fun completeChallenge() {
        val challengeId: Int = challengeData.get("challenge_id") as Int

        val bodyJson = Gson().toJson(hashMapOf(
            "user_id" to userData.get("user_id"),
            "pw" to userData.get("password"),
            "challenge_id" to challengeId
        ))
        CoroutineScope(Dispatchers.IO).launch {
            val (request, response, result) = Fuel.post("${getString(R.string.host)}/complete_challenge")
                    .body(bodyJson)
                    .header("Content-Type" to "application/json")
                    .response()

            withContext(Dispatchers.Main) {
                runOnUiThread {
                    val status = response.statusCode
                    if (status == 200) {
                        val builder2: AlertDialog.Builder = AlertDialog.Builder(ctx)
                        val image = ImageView(ctx)
                        image.setImageResource(R.drawable.opened_treasure_chest)

                        builder2?.setTitle("Congratulations on completing the challenge, $challengeName! You get a point!")
                        builder2?.setView(image)
                        builder2?.setPositiveButton("OK", DialogInterface.OnClickListener {_, _ ->
                            val intent = Intent(ctx, PickChallengeActivity::class.java).apply {
                                putExtra("userData", userData)
                            }
                            startActivity(intent)
                        })
                        builder2?.show()
                    }

                    else if (status == 400) {
                        Toast.makeText(ctx, "ERROR", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(
            id: String, name: String,
            description: String
    ) {

        val importance = NotificationManager.IMPORTANCE_LOW
        val channel = NotificationChannel(id, name, importance)

        channel.description = description
        channel.enableLights(true)
        channel.lightColor = Color.RED
        channel.enableVibration(true)
        channel.vibrationPattern =
                longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
        notificationManager?.createNotificationChannel(channel)
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun sendNotification(title: String, message: String) {

        val notificationID = 101

        val notification = Notification.Builder(
                ctx,
                channelId
        )
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setChannelId(channelId)
                .build()

        notificationManager?.notify(notificationID, notification)

    }

    // Trigger new location updates at interval
    protected fun startLocationUpdates() {

        // Create the location request to start receiving updates
        mLocationRequest = LocationRequest()
        mLocationRequest!!.priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
        mLocationRequest!!.interval = UPDATE_INTERVAL
        mLocationRequest!!.fastestInterval = FASTEST_INTERVAL

        // Create LocationSettingsRequest object using location request
        val builder = LocationSettingsRequest.Builder()
        builder.addLocationRequest(mLocationRequest!!)
        val locationSettingsRequest = builder.build()
        //settings
        val settingsClient = LocationServices.getSettingsClient(this)
        settingsClient.checkLocationSettings(locationSettingsRequest)
// checking for fine and course access permission
        if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
        ) {
// if permission is not given by the user it ends the program.
            return
        }
        getFusedLocationProviderClient(this).requestLocationUpdates(
                mLocationRequest, object : LocationCallback() {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onLocationResult(locationResult: LocationResult) {
                // check if location changes
                onLocationChanged(locationResult.lastLocation)

            }
        },
                // loops program
                Looper.myLooper()
        )
    }


    // called when change in current location is updated.
    @RequiresApi(Build.VERSION_CODES.O)
    fun onLocationChanged(location: Location) {

// calculates distance between current location and destination(CVS pharmacy)
        lastLocation = location
    }

    //suppresses the warning on map.isMyLocationEnabled = true.
    @SuppressLint("MissingPermission")
    // adds zoom buttons and center location button.
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.uiSettings.isZoomControlsEnabled = true
        fusedLocationClient.lastLocation.addOnSuccessListener(this) { location ->

            if (location != null) {
// sets last location if GPS is enabled.
                lastLocation = location
            }
        }

        setUpMap()

        map.isMyLocationEnabled = true
    }


    override fun onMarkerClick(p0: Marker?) = false


    //places the marker on the map
    private fun placeMarkerOnMap(location1: LatLng) {
        val markerOptions = MarkerOptions().position(location1)
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(location1, 15f))

        val circleOptions =
                CircleOptions().center(latlng?.latitude?.let { LatLng(it, latlng?.longitude) })
        circleOptions.radius(50.0)
        circleOptions.fillColor(Color.TRANSPARENT)
        circleOptions.strokeColor(Color.BLACK)
        map.addCircle(circleOptions)
    }
}