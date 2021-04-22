package com.ritwikscompany.treasurehunt.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.app.TimePickerDialog
import android.app.TimePickerDialog.OnTimeSetListener
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.kittinunf.fuel.Fuel
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.tabs.TabLayout
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ritwikscompany.treasurehunt.R
import com.ritwikscompany.treasurehunt.utils.Race
import com.ritwikscompany.treasurehunt.utils.RacesRecyclerViewAdapter
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

@Suppress("DEPRECATION", "NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class RacesActivity : AppCompatActivity(),
    OnMapReadyCallback, GoogleMap.OnMarkerClickListener,
    AdapterView.OnItemSelectedListener {

    private val ctx = this@RacesActivity
    private lateinit var map: GoogleMap
    private lateinit var titleET: EditText
    private lateinit var diffSpinner: Spinner
    private lateinit var groupsSpinner: Spinner
    private lateinit var lastLocation: Location
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var racesRV: RecyclerView
    private lateinit var groupsTB: TabLayout
    private lateinit var userData: HashMap<*, *>

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_races)

        Log.d(TAG, "onCreate: ")

        setUpUI()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setUpUI() {
        userData = intent.getSerializableExtra("userData") as HashMap<*, *>
        titleET = findViewById(R.id.race_title)
        diffSpinner = findViewById(R.id.race_diff)
        groupsSpinner = findViewById(R.id.race_groups)
        racesRV = findViewById(R.id.race_races_rv)
        groupsTB = findViewById(R.id.race_groups_tb)

        Log.d(TAG, "setUpUI: ")

        setUpGroupsSpinner()

        setUpBottomNavigation()

        setUpRacesRVAndGroupsTB()

        setUpScheduleRace()
    }

    private fun setUpGroupsSpinner() {
        val bodyJson = Gson().toJson(hashMapOf(
            "pw" to userData["password"] as String,
            "user_id" to userData["user_id"] as Int,
            "is_hashed" to 1
        ))

        Log.d(TAG, "setUpGroupsSpinner: ")

        Log.d(TAG, "setUpGroupsSpinner: $bodyJson")

        CoroutineScope(Dispatchers.IO).launch {
            val (_, response, result) = Fuel.post("${getString(R.string.host)}/api/get_groups")
                .body(bodyJson)
                .header("Content-Type" to "application/json")
                .response()

            withContext(Dispatchers.Main) {
                runOnUiThread {
                    if (response.statusCode == 200) {
                        val (bytes, _) = result

                        if (bytes != null) {
                            val type = object : TypeToken<ArrayList<String>>() {}.type
                            val groups = Gson().fromJson(String(bytes), type) as ArrayList<String>

                            if (groups.isEmpty()) {
                                Toast.makeText(
                                    ctx,
                                    "You cannot create a race since you have not created or joined any group",
                                    Toast.LENGTH_SHORT
                                ).show()

                                Log.d(TAG, "setUpGroupsSpinner: here")

                                startActivity(Intent(ctx, HomeActivity::class.java).apply {
                                    putExtra("userData", userData)
                                })
                            }

                            Log.d(TAG, "setUpGroupsSpinner: $groups")

                            val groupsAdapter = ArrayAdapter(ctx, android.R.layout.simple_dropdown_item_1line, groups)

                            groupsSpinner.adapter = groupsAdapter
                        }
                    } else {
                        Log.d(TAG, "setUpGroupsSpinner: here1")
                    }
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setUpBottomNavigation() {
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.race_bn)

        bottomNavigationView.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.bn_races_schedule_race ->
                    setUpScheduleRace()

                R.id.bn_races_upcoming_races ->
                    setUpUpcomingRaces()
            }

            true
        }
    }

    private fun setUpRacesRVAndGroupsTB() {
        val bodyJson = Gson().toJson(hashMapOf(
                "pw" to userData["password"],
                "user_id" to userData["user_id"]
        ))

        CoroutineScope(Dispatchers.IO).launch {
            val (_, response, result) = Fuel.post("${getString(R.string.host)}/api/get_races")
                    .body(bodyJson)
                    .header("Content-Type" to "application/json")
                    .response()

            withContext(Dispatchers.Main) {
                runOnUiThread {
                    if (response.statusCode == 200) {
                        val (bytes, _) = result

                        if (bytes != null) {
                            val type = object : TypeToken<ArrayList<HashMap<String, Any>>>() {}.type
                            val racesData = Gson().fromJson(String(bytes), type) as ArrayList<HashMap<String, Any>>
                            val races = ArrayList<Race>()
                            val groups = ArrayList<String>()
                            val groupsRaces = HashMap<String, ArrayList<Race>>()

                            for (raceData in racesData) {
                                val race = Race(
                                        raceData["title"] as String,
                                        raceData["start_time"] as String,
                                        raceData["creator_id"] as Int,
                                        raceData["creator_username"] as String,
                                        raceData["group_name"] as String,
                                        raceData["race_id"] as Int
                                )

                                races.add(race)

                                if (!groups.contains(race.groupName)) {
                                    val racesOfGroup = ArrayList<Race>()
                                    racesOfGroup.add(race)

                                    groups.add(race.groupName)
                                    groupsRaces[race.groupName] = racesOfGroup
                                }
                            }

                            groupsTB.addTab(groupsTB.newTab().setText("All"))

                            for (group in groups) {
                                groupsTB.addTab(groupsTB.newTab().setText(group))
                            }

                            val racesAdapter = RacesRecyclerViewAdapter(races) { race ->
                                val raceData = hashMapOf(
                                        "title" to race.title,
                                        "startTime" to race.startTime,
                                        "creator" to race.creatorName,
                                        "groupName" to race.groupName,
                                        "raceID" to race.raceID
                                )

                                startActivity(Intent(this@RacesActivity, RaceDataActivity::class.java).apply {
                                    putExtra("raceData", raceData)
                                    putExtra("userData", userData)
                                })
                            }

                            racesRV.adapter = racesAdapter
                            racesRV.layoutManager = LinearLayoutManager(ctx)

                            groupsTB.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                                override fun onTabSelected(tab: TabLayout.Tab?) {
                                    val racesOfGroup = groupsRaces[tab?.text.toString()]

                                    val racesOfGroupAdapter = RacesRecyclerViewAdapter(racesOfGroup!!) { race ->
                                        val raceData = hashMapOf(
                                                "title" to race.title,
                                                "startTime" to race.startTime,
                                                "creator" to race.creatorName,
                                                "groupName" to race.groupName,
                                                "raceID" to race.raceID
                                        )

                                        startActivity(Intent(this@RacesActivity, RaceDataActivity::class.java).apply {
                                            putExtra("raceData", raceData)
                                            putExtra("userData", userData)
                                        })
                                    }

                                    racesRV.adapter = racesOfGroupAdapter
                                }

                                override fun onTabUnselected(tab: TabLayout.Tab?) {}

                                override fun onTabReselected(tab: TabLayout.Tab?) {}
                            })
                        }
                    }
                }
            }
        }
    }

    private fun setUpUpcomingRaces() {
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.race_create_map) as SupportMapFragment

        val fm = supportFragmentManager
        fm.beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .hide(mapFragment)
                .commit()

        val scheduleRaceBTN = findViewById<Button>(R.id.race_schedule)
        scheduleRaceBTN.visibility = View.INVISIBLE

        titleET.visibility = View.INVISIBLE
        diffSpinner.visibility = View.INVISIBLE
        groupsSpinner.visibility = View.INVISIBLE
        racesRV.visibility = View.VISIBLE
        groupsTB.visibility = View.VISIBLE
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setUpScheduleRace() {
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.race_create_map) as SupportMapFragment

        val fm = supportFragmentManager
        fm.beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .show(mapFragment)
                .commit()

        mapFragment.getMapAsync(ctx)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(ctx)

        startLocationUpdates()

        val scheduleRaceBTN = findViewById<Button>(R.id.race_schedule)
        scheduleRaceBTN.visibility = View.VISIBLE
        titleET.visibility = View.VISIBLE
        diffSpinner.visibility = View.VISIBLE
        groupsSpinner.visibility = View.VISIBLE
        racesRV.visibility = View.INVISIBLE
        groupsTB.visibility = View.INVISIBLE

        scheduleRaceBTN.setOnClickListener {
            showDateDialog()
        }

        diffSpinner.onItemSelectedListener = this
    }


    override fun onMapReady(p0: GoogleMap?) {
        map = p0!!
        map.uiSettings.isZoomControlsEnabled = true
        map.setOnMarkerClickListener(ctx)

        setUpMap()
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
                moveCamera(currentLatLng, DEFAULT_ZOOM)
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
        }, Looper.myLooper())
    }

    private fun moveCamera(latLng: LatLng, zoom: Float) {
        if (ctx::map.isInitialized) {
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom))
        }
    }


    private fun placeMarkerOnMap(difficulty: String) {
        if (!ctx::lastLocation.isInitialized || !ctx::map.isInitialized) {
            return
        }

        val circle = CircleOptions()
        val currentLocation = LatLng(
                lastLocation.latitude,
                lastLocation.longitude
        )

        circle.center(currentLocation)

        when (difficulty) {
            "Easy" ->
                circle.radius(500.0)
            "Medium" ->
                circle.radius(1000.0)
            "Hard" ->
                circle.radius(1500.0)
        }

        map.addCircle(circle)
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
                        val errorAndRaceID = Gson().fromJson(String(bytes), object: TypeToken<HashMap<String, *>>() {}.type) as HashMap<String, *>
                        when (errorAndRaceID["error"]) {
                            "title exists" -> {
                                titleET.error = "Title already exists"
                                titleET.requestFocus()
                            }
                            "success" -> {
                                Toast.makeText(ctx, "Race Scheduled", Toast.LENGTH_SHORT).show()
                                val raceData = hashMapOf(
                                    "title" to title,
                                    "startTime" to startTime,
                                    "creator" to userData["username"] as String,
                                    "groupName" to groupName,
                                    "raceID" to errorAndRaceID["race_id"] as Int
                                )

                                startActivity(Intent(ctx, RaceDataActivity::class.java).apply {
                                    putExtra("raceData", raceData)
                                })
                            }
                        }
                    }

                    else {
                        Toast.makeText(ctx, "Network Error", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
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
                        val currentTime = Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant())

                        val calendar2 = Calendar.getInstance()
                        calendar2.time = currentTime
                        calendar2.add(Calendar.DATE, 2)

                        if (calendar.time.before(calendar2.time)) {
                            Toast.makeText(
                                ctx,
                                "Date selected must be two days after today's date",
                                Toast.LENGTH_SHORT
                            ).show()

                            Log.d(TAG, "showDateDialog: error of two days")

                            return@OnTimeSetListener
                        }

                        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")
                        scheduleRace(
                            titleET.text.toString(),
                            simpleDateFormat.format(calendar.time),
                            lastLocation.latitude,
                            lastLocation.longitude,
                            findViewById<Spinner>(R.id.race_groups)
                                .selectedItem.toString())
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

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        placeMarkerOnMap(diffSpinner.selectedItem.toString())
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {}

    companion object {
        private const val DEFAULT_ZOOM = 15f
        private const val TAG = "RacesActivity"
    }
}