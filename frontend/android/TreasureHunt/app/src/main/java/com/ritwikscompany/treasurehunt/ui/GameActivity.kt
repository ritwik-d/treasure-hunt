package com.ritwikscompany.treasurehunt.ui

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.github.kittinunf.fuel.Fuel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ritwikscompany.treasurehunt.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GameActivity : AppCompatActivity() {

    private lateinit var challengeName: String
    private var challengeData = HashMap<String, Any>()
    private lateinit var userData: HashMap<String, Any>
    private val ctx = this@GameActivity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        this.challengeName = intent.getStringExtra("challengeName").toString()
        this.userData = intent.getSerializableExtra("userData") as HashMap<String, Any>

        getChallengeData()
        initializeHeaders()

        findViewById<Button>(R.id.game_is_here).setOnClickListener {
            isHereOnClick()
        }
    }


    private fun completeChallenge() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(ctx)
        val image = ImageView(ctx)
        image.setImageResource(R.drawable.opened_treasure_chest)
        builder?.setTitle("Congratulations on completing the challenge, $challengeName! You get a point!")
        builder?.setView(image)
        builder?.setPositiveButton("OK", DialogInterface.OnClickListener {_, _ ->
            val intent = Intent(ctx, PickChallengeActivity::class.java).apply {
                putExtra("userData", userData)
            }
            startActivity(intent)
        })
        builder?.show()
    }


    private fun getChallengeData() {
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
                            ctx.challengeData = Gson().fromJson(String(bytes), type) as HashMap<String, Any>
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


    private fun initializeHeaders() {
        findViewById<TextView>(R.id.game_name).text = challengeName
        findViewById<TextView>(R.id.game_puzzle).text = "Puzzle: ${challengeData.get("puzzle") as String}"
    }


    private fun isHereOnClick() {
        completeChallenge()
    }
}