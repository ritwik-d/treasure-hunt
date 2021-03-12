package com.ritwikscompany.treasurehunt.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.ritwikscompany.treasurehunt.R

class MapsActivity : AppCompatActivity() {

    private lateinit var challengeName: String
    private lateinit var userData: HashMap<String, Any>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        this.challengeName = intent.getStringExtra("challengeName").toString()
        this.userData = intent.getSerializableExtra("userData") as HashMap<String, Any>
    }
}