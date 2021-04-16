package com.ritwikscompany.treasurehunt.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.ritwikscompany.treasurehunt.R

class RacesActivity : AppCompatActivity() {

    private var userData = HashMap<String, Any>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_races)

        this.userData = intent.getSerializableExtra("userData") as HashMap<String, Any>
    }
}