package com.ritwikscompany.treasurehunt.ui

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.github.kittinunf.fuel.Fuel
import com.google.gson.Gson
import com.ritwikscompany.treasurehunt.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class HomeActivity : AppCompatActivity() {

    private val ctx = this@HomeActivity
    private val PICK_IMAGE_REQUEST: Int = 234
    private var userData = HashMap<String, Any>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        this.userData = intent.getSerializableExtra("userData") as HashMap<String, Any>

        setProfilePicture()

        findViewById<TextView>(R.id.home_name).text = "Hello ${userData.get("username").toString()}!"
        findViewById<Button>(R.id.home_find_challenge).setOnClickListener {
            findChallengeOnClick()
        }

//        findViewById<Button>(R.id.home_feedback).setOnClickListener {
//            feedbackOnClick()
//        }

        findViewById<Button>(R.id.home_my_challenges).setOnClickListener {
            myChallengesOnClick()
        }

        findViewById<Button>(R.id.home_groups).setOnClickListener {
            groupsOnClick()
        }
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.home_menu, menu)
        return true
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_log_out -> {
                clearSharedPref()
                startActivity(Intent(ctx, MainActivity::class.java))
            }

            R.id.menu_upload_pfp -> {
                val intent = Intent().apply {
                    type = "image/*"
                    action = Intent.ACTION_GET_CONTENT
                }
                startActivityForResult(Intent.createChooser(intent, "Select an Image"), PICK_IMAGE_REQUEST)
            }
        }
        return true
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.data != null) {
            val imageBytes = ctx.contentResolver.openInputStream(data.data!!)!!.buffered().use { it.readBytes() }
            val bodyJson = Gson().toJson(
                    hashMapOf(
                        "image" to imageBytes,
                        "pw" to userData.get("password"),
                        "user_id" to userData.get("user_id")
                    )
            )
            CoroutineScope(Dispatchers.IO).launch {
                val (_, response, _) = Fuel.post("${getString(R.string.host)}/upload_pfp")
                        .body(bodyJson)
                        .header("Content-Type" to "application/json")
                        .response()

                withContext(Dispatchers.Main) {
                    runOnUiThread {
                        if (response.statusCode == 201) {
                            Toast.makeText(ctx, "Image Upload Success", Toast.LENGTH_SHORT).show()
                        }
                        else {
                            Toast.makeText(ctx, "Image Upload Failure", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }


    private fun setProfilePicture() {
        val bodyJson = Gson().toJson(
            hashMapOf(
                "pw" to userData.get("password"),
                "user_id" to userData.get("user_id")
            )
        )
        CoroutineScope(Dispatchers.IO).launch {
            val (request, response, result) = Fuel.post("${getString(R.string.host)}/download_pfp")
                    .body(bodyJson)
                    .header("Content-Type" to "application/json")
                    .response()

            withContext(Dispatchers.Main) {
                runOnUiThread {
                    val status = response.statusCode
                    if (status == 200) {
                        val (bytes, _) = result
                        val pfpImageView = findViewById<de.hdodenhof.circleimageview.CircleImageView>(R.id.home_pfp)
                        if (bytes != null) {
                            val bitmap: Bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                            pfpImageView.setImageBitmap(bitmap)
                        }
                        else {
                            pfpImageView.setImageDrawable(ContextCompat.getDrawable(ctx, R.drawable.no_pfp))
                        }
                        pfpImageView.requestLayout()
                    }

                    else if (status == 404) {
                        Toast.makeText(ctx, "Profile Picture Download Failure", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }


    private fun findChallengeOnClick() {
        val intent = Intent(ctx, PickChallengeActivity::class.java).apply {
            putExtra("userData", userData)
        }
        startActivity(intent)
    }


    private fun feedbackOnClick() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.surveyLink)))
        startActivity(intent)
    }


    private fun myChallengesOnClick() {
        val intent = Intent(ctx, MyChallengesActivity::class.java).apply {
            putExtra("userData", userData)
        }
        startActivity(intent)
    }


    private fun groupsOnClick() {
        val intent = Intent(ctx, GroupsActivity::class.java).apply {
            putExtra("userData", userData)
        }
        startActivity(intent)
    }


    private fun clearSharedPref() {
        val sharedPref = application.getSharedPreferences("userInfo", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            remove("email")
            remove("pw")
            apply()
        }
    }
}