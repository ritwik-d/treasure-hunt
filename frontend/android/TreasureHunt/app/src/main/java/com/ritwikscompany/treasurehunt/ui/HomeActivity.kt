package com.ritwikscompany.treasurehunt.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.github.kittinunf.fuel.Fuel
import com.google.gson.Gson
import com.ritwikscompany.treasurehunt.R
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File


class HomeActivity : AppCompatActivity() {

    private val ctx = this@HomeActivity
    private var userData = HashMap<String, Any>()
    private val REQUEST_GALLERY = 200
    private val PERMISSION_REQUEST_CODE = 1
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        this.userData = intent.getSerializableExtra("userData") as HashMap<String, Any>

        setProfilePicture()

        findViewById<TextView>(R.id.home_name).text = "Hello ${userData.get("username").toString()}!"
        findViewById<Button>(R.id.home_find_challenge).setOnClickListener {
            findChallengeOnClick()
        }

        findViewById<Button>(R.id.home_my_challenges).setOnClickListener {
            myChallengesOnClick()
        }

        findViewById<Button>(R.id.home_groups).setOnClickListener {
            groupsOnClick()
        }
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
                startActivityForResult(Intent.createChooser(intent, "Select an Image"), PERMISSION_REQUEST_CODE)
            }

            R.id.menu_race -> {
                val intent = Intent(ctx, RacesActivity::class.java).apply {
                    putExtra("userData", userData)
                }
                startActivity(intent)
            }
        }
        return true
    }


    private fun requestPermissions() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(ctx,
                        Manifest.permission.READ_EXTERNAL_STORAGE)) {
            Toast.makeText(
                    ctx,
                    "Please give us permission to access your files.",
                    Toast.LENGTH_SHORT
            ).show()
        } else {
            ActivityCompat.requestPermissions(
                    ctx, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    PERMISSION_REQUEST_CODE)
        }
    }


    private fun convertToByteArray(filePath: String): ByteArray {
        val image = File(filePath)
        val bmOptions = BitmapFactory.Options()
        var bitmap = BitmapFactory.decodeFile(image.absolutePath, bmOptions)
        val pfpView = findViewById<CircleImageView>(R.id.home_pfp)
        bitmap = Bitmap.createScaledBitmap(
                bitmap, pfpView.width,
                pfpView.height, true
        )

        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 30, baos)
        return baos.toByteArray()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val pfp: CircleImageView = findViewById(R.id.home_pfp)
        if (requestCode == REQUEST_GALLERY && resultCode == RESULT_OK) {
            val filePath = getRealPathFromUri(data!!.data, ctx
)
            val image = convertToByteArray(filePath!!)

        }
    }


    @SuppressLint("Recycle")
    private fun getRealPathFromUri(uri: Uri?, activity: Activity): String? {
        val proj = arrayOf(MediaStore.Images.Media.DATA)
        val cursor: Cursor? = activity.contentResolver.query(
                uri!!, proj, null,
                null, null
        )
        return if (cursor == null) {
            uri.path
        } else {
            cursor.moveToFirst()
            val id: Int = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
            cursor.getString(id)
        }
    }


    private fun checkPermissions(): Boolean {
        val result = ContextCompat.checkSelfPermission(
                ctx,
                Manifest.permission.READ_EXTERNAL_STORAGE)
        return result == PackageManager.PERMISSION_GRANTED
    }


    private fun filePicker() {

        //.Now Permission Working
        Toast.makeText(ctx
, "File Picker Call", Toast.LENGTH_SHORT).show()
        //Let's Pick File
        val openGallery = Intent(Intent.ACTION_PICK)
        openGallery.type = "image/*"
        startActivityForResult(openGallery, REQUEST_GALLERY)
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.home_menu, menu)
        return true
    }


    private fun setProfilePicture() {
        val bodyJson = Gson().toJson(
                hashMapOf(
                        "pw" to userData.get("password"),
                        "user_id" to userData.get("user_id")
                )
        )
        CoroutineScope(Dispatchers.IO).launch {
            val (_, response, result) = Fuel.post("${getString(R.string.host)}/api/download_pfp")
                    .body(bodyJson)
                    .header("Content-Type" to "application/json")
                    .response()

            withContext(Dispatchers.Main) {
                runOnUiThread {
                    val status = response.statusCode
                    if (status == 200) {
                        val (bytes, _) = result
                        val pfpImageView = findViewById<CircleImageView>(R.id.home_pfp)
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