package com.ritwikscompany.treasurehunt.ui

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.app.TimePickerDialog
import android.app.TimePickerDialog.OnTimeSetListener
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.github.kittinunf.fuel.Fuel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ritwikscompany.treasurehunt.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class RacesActivity : AppCompatActivity() {

    private val ctx = this@RacesActivity
    private var userData = HashMap<String, Any>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_races)

        this.userData = intent.getSerializableExtra("userData") as HashMap<String, Any>

        val scheduleRaceBTN = findViewById<Button>(R.id.races_schedule_race)
        scheduleRaceBTN
            .setOnClickListener {
                showDateDialog()
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
                                // put error on title edit text
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
}