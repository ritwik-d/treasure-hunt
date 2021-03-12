package com.ritwikscompany.treasurehunt.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import com.ritwikscompany.treasurehunt.R

class CreateGroupActivity : AppCompatActivity() {

    private val ctx = this@CreateGroupActivity
    private lateinit var userData: HashMap<String, Any>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_group)
        this.userData = intent.getSerializableExtra("userData") as HashMap<String, Any>

        findViewById<Button>(R.id.cg_create).setOnClickListener {
            createGroupOnClick()
        }
    }


    private fun createGroupOnClick() {
        val groupNameET = findViewById<EditText>(R.id.cg_name)
        val groupDescET = findViewById<EditText>(R.id.cg_desc)

        val groupName = groupNameET.text.toString()
        var groupDesc = groupDescET.text.toString()

        if (groupName.length < 3) {
            groupNameET.error = "Group name must be at least 3 characters long"
            groupNameET.requestFocus()
            return
        }
    }
}