package com.ritwikscompany.treasurehunt.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.ritwikscompany.treasurehunt.R

class GroupsActivity : AppCompatActivity() {

    private val ctx = this@GroupsActivity
    private lateinit var userData: HashMap<String, Any>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_groups)
        this.userData = intent.getSerializableExtra("userData") as HashMap<String, Any>

        findViewById<Button>(R.id.groups_create_group).setOnClickListener {
            createGroupOnClick()
        }

        findViewById<Button>(R.id.groups_join_group).setOnClickListener {
            joinGroupOnClick()
        }

        findViewById<Button>(R.id.groups_my_groups).setOnClickListener {
            myGroupsOnClick()
        }
    }


    private fun createGroupOnClick() {
        val intent = Intent(ctx, CreateGroupActivity::class.java).apply {
            putExtra("userData", userData)
        }
        startActivity(intent)
    }


    private fun joinGroupOnClick() {
        val intent = Intent(ctx, JoinGroupActivity::class.java).apply {
            putExtra("userData", userData)
        }
        startActivity(intent)
    }


    private fun myGroupsOnClick() {
        val intent = Intent(ctx, MyGroupsActivity::class.java).apply {
            putExtra("userData", userData)
        }
        startActivity(intent)
    }
}