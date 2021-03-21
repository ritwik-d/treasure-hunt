package com.ritwikscompany.treasurehunt.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import com.github.kittinunf.fuel.Fuel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ritwikscompany.treasurehunt.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CreateChallengeActivity : AppCompatActivity() {

    private val ctx = this@CreateChallengeActivity
    private var userData = hashMapOf<String, Any>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_challenge)
        this.userData = intent.getSerializableExtra("userData") as HashMap<String, Any>

        setSpinnerVals()
    }


    private fun setSpinnerVals() {
        val diffSpinner = findViewById<Spinner>(R.id.cc_diff)
        val diffAdapter: ArrayAdapter<CharSequence> = ArrayAdapter.createFromResource(ctx, R.array.difficulties, android.R.layout.simple_spinner_item)
        diffAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        diffSpinner.adapter = diffAdapter

        val groupSpinner = findViewById<Spinner>(R.id.cc_groups)

        val bodyJson = Gson().toJson(hashMapOf(
            "user_id" to userData.get("user_id"),
            "pw" to userData.get("password")
        ))
        CoroutineScope(Dispatchers.IO).launch {
            val (request, response, result) = Fuel.post("${getString(R.string.host)}/get_groups")
                .body(bodyJson)
                .header("Content-Type" to "application/json")
                .response()

            withContext(Dispatchers.Main) {
                runOnUiThread {
                    val status = response.statusCode
                    if (status == 200) {
                        val (bytes, _) = result
                        if (bytes != null) {
                            val type = object: TypeToken<MutableList<String>>(){}.type
                            val groups1 = Gson().fromJson(String(bytes), type) as MutableList<String>
                            groups1.add("Public")
                            val groups = groups1.toTypedArray()

                            val groupAdapter: ArrayAdapter<CharSequence> = ArrayAdapter(ctx, android.R.layout.simple_spinner_item, groups)
                            groupAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                            groupSpinner.adapter = groupAdapter
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


    private fun createOnClick(latitude: Double, longitude: Double) {
        val bodyJson = Gson().toJson(hashMapOf(
            "user_id" to userData.get("user_id"),
            "pw" to userData.get("password"),
            "latitude" to latitude,
            "longitude" to longitude,
            "difficulty" to
        ))
    }
}