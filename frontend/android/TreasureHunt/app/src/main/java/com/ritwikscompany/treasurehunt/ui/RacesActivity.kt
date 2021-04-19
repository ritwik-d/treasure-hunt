package com.ritwikscompany.treasurehunt.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.app.TimePickerDialog
import android.app.TimePickerDialog.OnTimeSetListener
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.github.kittinunf.fuel.Fuel
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
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
import java.util.*
import kotlin.collections.ArrayList

class RacesActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private val ctx = this@RacesActivity
    private lateinit var map: GoogleMap
    private lateinit var titleET: EditText
    private lateinit var diffSpinner: Spinner
    private lateinit var lastLocation: Location
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private var userData = HashMap<String, Any>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_races)

        this.userData = intent.getSerializableExtra("userData") as HashMap<String, Any>
        this.titleET = findViewById(R.id.race_title)
        this.diffSpinner = findViewById(R.id.race_diff)

        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.race_create_map) as SupportMapFragment
        mapFragment.getMapAsync(ctx)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(ctx)

        startLocationUpdates()

        val scheduleRaceBTN = findViewById<Button>(R.id.race_schedule)
        scheduleRaceBTN.setOnClickListener {
            showDateDialog()
        }

        diffSpinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                placeMarkerOnMap(diffSpinner.selectedItem.toString())
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }
    }


    override fun onMapReady(p0: GoogleMap?) {
        map = p0!!
        map.uiSettings.isZoomControlsEnabled = true
        map.setOnMarkerClickListener(ctx)

        setUpMap()

//        findViewById<ImageButton>(R.id.cc_satellite).setOnClickListener {
//            if (map.mapType == GoogleMap.MAP_TYPE_NORMAL) {
//                map.mapType = GoogleMap.MAP_TYPE_HYBRID
//            }
//            else {
//                map.mapType = GoogleMap.MAP_TYPE_NORMAL
//            }
//        }
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


    private fun placeMarkerOnMap(difficulty: String) {
        val bodyJson = Gson().toJson(hashMapOf(
                "user_id" to userData["user_id"],
                "pw" to userData["password"],
                "difficulty" to difficulty,
                "latitude" to lastLocation.latitude,
                "longitude" to lastLocation.longitude
        ))
        CoroutineScope(Dispatchers.IO).launch {
            val (_, response, result) = Fuel.post("${getString(R.string.host)}/api/simulate_race_location")
                    .body(bodyJson)
                    .header("Content-Type" to "application/json")
                    .response()

            withContext(Dispatchers.Main) {
                runOnUiThread {
                    val status = response.statusCode
                    if (status == 200) {
                        val (bytes, _) = result
                        if (bytes != null) {
                            val raceLatLong = Gson().fromJson(String(bytes), object: TypeToken<ArrayList<Double>>(){}.type) as ArrayList<Double>

                            val marker = MarkerOptions()
                            marker.position(LatLng(raceLatLong[0], raceLatLong[1]))
                            marker.title("This Race's Location")

                            map.addMarker(marker)
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


    private fun scheduleRace(title: String, startTime: String, latitude: Double, longitude: Double, groupName: String) {
        val bodyJson = Gson().toJson(hashMapOf(
                "user_id" to userData["user_id"],
                "pw" to userData["password"],
                "title" to title,
                "start_time" to startTime,
                "latitude" to latitude,
                "longitude" to longitude,
                "group_name" to groupName
        ))
        CoroutineScope(Dispatchers.IO).launch {
            val (_, _, result) = Fuel.post("${getString(R.string.host)}/api/create_race")
                    .body(bodyJson)
                    .header("Content-Type" to "application/json")
                    .response()

            withContext(Dispatchers.Main) {
                runOnUiThread {
                    val (bytes, _) = result
                    if (bytes != null) {
                        when ((Gson().fromJson(String(bytes), object: TypeToken<HashMap<String, String>>(){}.type) as HashMap<String, String>)["error"]) {
                            "title exists" -> {
                                titleET.error = "Title already exists"
                                titleET.requestFocus()
                            }
                            "success" -> Toast.makeText(ctx, "Race Scheduled", Toast.LENGTH_SHORT).show()
                        }
                    }

                    else {
                        Toast.makeText(ctx, "Network Error", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun showDateDialog() {
        val calendar: Calendar = Calendar.getInstance()
        val dateSetListener =
            OnDateSetListener { _, year, month, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                val timeSetListener =
                    OnTimeSetListener { _, hourOfDay, minute ->
                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                        calendar.set(Calendar.MINUTE, minute)
                        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")
                        scheduleRace(titleET.text.toString(), simpleDateFormat.format(calendar.time), 0.0, 0.0, findViewById<Spinner>(R.id.race_groups).selectedItem.toString())
                    }
                TimePickerDialog(
                    ctx,
                    timeSetListener,
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    false
                ).show()
            }

        DatePickerDialog(
            ctx,
            dateSetListener,
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    override fun onMarkerClick(p0: Marker?): Boolean = false
}