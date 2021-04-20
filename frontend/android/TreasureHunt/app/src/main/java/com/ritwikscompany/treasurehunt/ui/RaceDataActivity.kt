package com.ritwikscompany.treasurehunt.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.ritwikscompany.treasurehunt.R
import com.ritwikscompany.treasurehunt.utils.Utils
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

@Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class RaceDataActivity : AppCompatActivity(), OnMapReadyCallback,
            GoogleMap.OnMarkerClickListener{

    private var ctx = this@RaceDataActivity
    private lateinit var raceData: HashMap<String, Any>
    private lateinit var userData: HashMap<String, Any>
    private lateinit var titleTV: TextView
    private lateinit var startTimeTV: TextView
    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var lastLocation: Location
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_race_data)

        raceData = intent.getSerializableExtra("raceData") as HashMap<String, Any>
        userData = intent.getSerializableExtra("userData") as HashMap<String, Any>

        setUpUI()
    }

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

        val diff: Long = startTime.time - currentTime.time
    }

    private fun setUpRaceTitle() {
        titleTV = findViewById(R.id.race_data_race_title)

        titleTV.text = raceData["title"] as String
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
}