package com.ritwikscompany.treasurehunt.ui

import android.content.Intent
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.*
import com.github.kittinunf.fuel.Fuel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ritwikscompany.treasurehunt.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.collections.HashMap

class CreateChallengeActivity : AppCompatActivity() {

    private val ctx = this@CreateChallengeActivity
    private var userData = hashMapOf<String, Any>()
    private lateinit var diffSpinner: Spinner
    private lateinit var groupSpinner: Spinner
    private lateinit var puzzleET: EditText
    private lateinit var nameET: EditText
    private lateinit var puzzle: String
    private lateinit var name: String
    private lateinit var checkMark: Drawable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_challenge)
        this.userData = intent.getSerializableExtra("userData") as HashMap<String, Any>

        checkMark = com.ritwikscompany.treasurehunt.utils.Utils.getCheckMark(ctx)!!

        verifyFields()
        setSpinnerVals()
    }


    private fun setSpinnerVals() {
        diffSpinner = findViewById(R.id.cc_diff)
        groupSpinner = findViewById(R.id.cc_groups)

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


    private fun verifyFields() {
        puzzleET = findViewById(R.id.cc_puzzle)
        puzzle = puzzleET.text.toString()
        nameET = findViewById(R.id.cc_name)
        name = nameET.text.toString()

        puzzleET.addTextChangedListener ( object: TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                puzzle = puzzleET.text.toString()

                if (puzzle.length < 3) {
                    puzzleET.error = "Puzzle must be at least 3 characters"
                    puzzleET.requestFocus()
                    return
                }

                else {
                    puzzleET.setError("Good", checkMark)
                    puzzleET.requestFocus()
                }
            }

            override fun afterTextChanged(p0: Editable?) {}
        } )

        nameET.addTextChangedListener ( object: TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                name = nameET.text.toString()

                if (name.length < 3) {
                    nameET.error = "Puzzle must be at least 3 characters"
                    nameET.requestFocus()
                    return
                }

                else {
                    nameET.setError("Good", checkMark)
                    nameET.requestFocus()
                }
            }

            override fun afterTextChanged(p0: Editable?) {}
        })
    }


    private fun createOnClick(latitude: Double, longitude: Double) {
        var group: String? = groupSpinner.selectedItem.toString()
        if (group == "Public") {
            group = null
        }
        val bodyJson = Gson().toJson(hashMapOf(
            "user_id" to userData.get("user_id"),
            "pw" to userData.get("password"),
            "latitude" to latitude,
            "longitude" to longitude,
            "difficulty" to diffSpinner.selectedItem.toString().toLowerCase(Locale.ROOT),
            "group_name" to group,
            "name" to name,
            "puzzle" to puzzle
        ))
        CoroutineScope(Dispatchers.IO).launch {
            val (request, response, result) = Fuel.post("${getString(R.string.host)}/create_challenge")
                    .body(bodyJson)
                    .header("Content-Type" to "application/json")
                    .response()

            withContext(Dispatchers.Main) {
                runOnUiThread {
                    when (response.statusCode) {
                        201 -> {
                            val intent = Intent(ctx, MyChallengesActivity::class.java).apply {
                                putExtra("userData", userData)
                            }
                            startActivity(intent)
                        }
                        400 -> {
                            Toast.makeText(
                                    ctx,
                                    "A challenge has already been created with this name",
                                    Toast.LENGTH_LONG
                            ).show()
                        }
                        404 -> {
                            Toast.makeText(
                                    ctx,
                                    "You have already created 3 challenges",
                                    Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            }
        }
    }
}