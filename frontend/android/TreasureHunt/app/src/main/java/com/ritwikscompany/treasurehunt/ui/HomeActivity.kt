package com.ritwikscompany.treasurehunt.ui

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import com.ritwikscompany.treasurehunt.R

class HomeActivity : AppCompatActivity() {

    private val ctx = this@HomeActivity
    private lateinit var userData: HashMap<String, Any>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        this.userData = intent.getSerializableExtra("userData") as HashMap<String, Any>

        findViewById<TextView>(R.id.home_name).text = userData.get("username").toString()
        findViewById<Button>(R.id.home_find_challenge).setOnClickListener {
            findChallengeOnClick()
        }

        findViewById<Button>(R.id.home_feedback).setOnClickListener {
            feedbackOnClick()
        }

        findViewById<Button>(R.id.home_my_challenges).setOnClickListener {
            myChallengesOnClick()
        }

        findViewById<Button>(R.id.home_groups).setOnClickListener {
            groupsOnClick()
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
}