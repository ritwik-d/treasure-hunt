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
import org.w3c.dom.Text


class GameActivity : AppCompatActivity() {

    private val ctx = this@GameActivity
    private var userData = HashMap<String, Any>()
    private var challengeName = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        challengeName = intent.getStringExtra("challengeName") as String
        userData = intent.getSerializableExtra("userData") as HashMap<String, Any>

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
                            val type = object : TypeToken<HashMap<String, Any>>() {}.type
                            val challengeData: HashMap<String, Any> = Gson().fromJson(String(bytes), type) as HashMap<String, Any>

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


    private fun completeChallenge(challengeData: HashMap<String, Any>) {
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

                        builder2.setTitle("Congratulations on completing the challenge, $challengeName! You get a point!")
                        builder2.setView(image)
                        builder2.setPositiveButton("OK", DialogInterface.OnClickListener { _, _ ->
                            val intent = Intent(ctx, PickChallengeActivity::class.java).apply {
                                putExtra("userData", userData)
                            }
                            startActivity(intent)
                        })
                        builder2.show()
                    }

                    else if (status == 400) {
                        Toast.makeText(ctx, "ERROR", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }
}