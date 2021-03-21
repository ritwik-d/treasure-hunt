package com.ritwikscompany.treasurehunt.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import com.ritwikscompany.treasurehunt.R

class EditChallengeActivity : AppCompatActivity() {

    private val ctx = this@EditChallengeActivity
    private lateinit var userData: HashMap<String, Any>
    private lateinit var challengeData: HashMap<String, Any>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_challenge)
        this.userData = intent.getSerializableExtra("userData") as HashMap<String, Any>
        this.challengeData = intent.getSerializableExtra("challengeData") as HashMap<String, Any>
    }


    private fun autoFillText() {
        findViewById<EditText>(R.id.ec_puzzle).setText(challengeData.get("puzzle") as String)
    }
}