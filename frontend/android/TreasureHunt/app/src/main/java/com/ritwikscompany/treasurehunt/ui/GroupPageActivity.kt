package com.ritwikscompany.treasurehunt.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TableLayout
import com.ritwikscompany.treasurehunt.R

class GroupPageActivity : AppCompatActivity() {

    private var groupName = ""
    private var userData = HashMap<String, Any>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_page)

        this.groupName = intent.getStringExtra("groupName") as String
        this.userData = intent.getSerializableExtra("userData") as HashMap<String, Any>
    }


    private fun initializeTableLayout() {
        val tableLayout = findViewById<TableLayout>(R.id.gp_tb)
        
    }
}
