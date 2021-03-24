package com.ritwikscompany.treasurehunt.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.ritwikscompany.treasurehunt.R

class GroupsActivity : AppCompatActivity() {

    private val ctx = this@GroupsActivity
    private var userData = HashMap<String, Any>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_groups)
        userData = intent.getSerializableExtra("userData") as HashMap<String, Any>

        findViewById<Button>(R.id.groups_create_group).setOnClickListener {
            createGroupOnClick()
        }

        findViewById<Button>(R.id.groups_join_group).setOnClickListener {
            joinGroupOnClick()
        }

        findViewById<Button>(R.id.groups_my_groups).setOnClickListener {
            myGroupsOnClick()
        }

//        val tabLayout = findViewById<TabLayout>(R.id.grps_tb)
//
//        val viewPager = findViewById<ViewPager>(R.id.viewPager)
//
//        val adapterView = ViewPagerAdapter(supportFragmentManager, tabLayout.tabCount)
//
//        viewPager.adapter = adapterView
//
//        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
//            override fun onTabReselected(tab: TabLayout.Tab?) {}
//
//            override fun onTabSelected(tab: TabLayout.Tab?) {
//                adapterView.getItem(tab?.position)
//            }
//
//            override fun onTabUnselected(tab: TabLayout.Tab?) {}
//        })
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