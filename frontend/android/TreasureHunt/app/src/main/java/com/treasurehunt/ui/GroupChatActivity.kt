package com.ritwikscompany.treasurehunt.ui

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import com.github.kittinunf.fuel.Fuel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ritwikscompany.treasurehunt.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GroupChatActivity : AppCompatActivity() {

    private var userData = HashMap<String, Any>()
    private var groupName = String()
    private lateinit var scrollView: ScrollView
    private lateinit var inflater: LayoutInflater
    private lateinit var linearLayout: LinearLayout
    private val ctx = this@GroupChatActivity
    @Volatile private var stopThread = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_chat)

        this.userData = intent.getSerializableExtra("userData") as HashMap<String, Any>
        this.groupName = intent.getStringExtra("groupName") as String
        this.linearLayout = findViewById(R.id.gc_linear_layout)
        this.inflater = LayoutInflater.from(ctx)
        this.scrollView = findViewById(R.id.gc_sv)

        title = groupName

        val bodyJson = Gson().toJson(hashMapOf(
                "user_id" to userData["user_id"],
                "pw" to userData["password"],
                "group_name" to groupName
        ))
        CoroutineScope(Dispatchers.IO).launch {
            val (_, response, result) = Fuel.post("${getString(R.string.host)}/api/get_group_row")
                    .body(bodyJson)
                    .header("Content-Type" to "application/json")
                    .response()

            withContext(Dispatchers.Main) {
                runOnUiThread {
                    val status = response.statusCode
                    if (status == 200) {
                        val (bytes, _) = result
                        if (bytes != null) {
                            val type = object: TypeToken<HashMap<String, Any>>(){}.type
                            val groupData = Gson().fromJson(String(bytes), type) as HashMap<String, Any>
                            val groupId: Int = (groupData["group_id"] as Double).toInt()
                            val messageET = findViewById<EditText>(R.id.gc_message)

                            scrollView.post {
                                scrollView.fullScroll(View.FOCUS_DOWN)
                            }

                            findViewById<ImageButton>(R.id.gc_send).setOnClickListener {
                                val message = messageET.text.toString()
                                sendMessage(groupId, message)
                                messageET.setText("")

                                scrollView.post {
                                    scrollView.fullScroll(View.FOCUS_DOWN)
                                }
                            }

                            getMessagesFinal(groupId)
                        }

                        else {
                            Toast.makeText(ctx, "Network Error", Toast.LENGTH_LONG).show()
                        }
                    }

                    else if (status == 404) {
                        Toast.makeText(ctx, "Log In Failure", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }


    override fun onStart() {
        super.onStart()
        stopThread = false
    }

    override fun onStop() {
        super.onStop()
        stopThread = true
    }

    override fun onBackPressed() {
        stopThread = true

        val intent = Intent(ctx, GroupPageActivity::class.java).apply {
            putExtra("userData", userData)
            putExtra("groupName", groupName)
        }
        startActivity(intent)
    }

    private fun getMessagesFinal(groupId: Int) {
        val updateChatThread = UpdateChatThread(groupId)
        updateChatThread.start()
    }

    private fun updateLinearLayout(messages: ArrayList<HashMap<String, String>>) {
        for (index in (linearLayout.childCount) until messages.size) {
            val message = messages[index]
            val viewGroup: View =

            if (message["username"] as String == (userData["username"] as String)) {
                inflater.inflate(R.layout.row_chat_user_pov, linearLayout, false)
            }

            else {
                inflater.inflate(R.layout.row_chat_other_pov, linearLayout, false)
            }

            viewGroup.findViewById<TextView>(R.id.chat_timestamp).text = message["timestamp"] as String
            viewGroup.findViewById<TextView>(R.id.chat_message).text = message["message"] as String

            if (message["username"] as String != (userData["username"] as String)) {
                viewGroup.findViewById<TextView>(R.id.chat_uname).text = message["username"]
            }

            linearLayout.addView(viewGroup)
        }
    }

    private fun getMessages(groupId: Int) {
        val bodyJson = Gson().toJson(hashMapOf(
                "user_id" to userData["user_id"],
                "pw" to userData["password"],
                "group_id" to groupId
        ))
        CoroutineScope(Dispatchers.IO).launch {
            val (_, response, result) = Fuel.post("${getString(R.string.host)}/api/get_messages")
                    .body(bodyJson)
                    .header("Content-Type" to "application/json")
                    .response()

            withContext(Dispatchers.Main) {
                runOnUiThread {
                    val status = response.statusCode
                    if (status == 200) {
                        val (bytes, _) = result
                        if (bytes != null) {
                            val type = object: TypeToken<ArrayList<HashMap<String, String>>>(){}.type
                            val messages = Gson().fromJson(String(bytes), type) as ArrayList<HashMap<String, String>>

                            updateLinearLayout(messages)
                        }

                        else {
                            Toast.makeText(ctx, "Network Error", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }


    private fun sendMessage(groupId: Int, message: String) {
        val bodyJson = Gson().toJson(hashMapOf(
                "user_id" to userData["user_id"],
                "pw" to userData["password"],
                "group_id" to groupId,
                "message" to message
        ))
        CoroutineScope(Dispatchers.IO).launch {
            val (_, response, result) = Fuel.post("${getString(R.string.host)}/api/send_message")
                    .body(bodyJson)
                    .header("Content-Type" to "application/json")
                    .response()

            withContext(Dispatchers.Main) {
                runOnUiThread {
                    val status = response.statusCode
                    if (status == 200) {
                        val (bytes, _) = result
                        if (bytes != null) {
                            val type = object: TypeToken<ArrayList<HashMap<String, Any>>>(){}.type
                            val messages = Gson().fromJson(String(bytes), type) as ArrayList<HashMap<String, String>>

                            updateLinearLayout(messages)
                        }

                        else {
                            Toast.makeText(ctx, "Network Error", Toast.LENGTH_LONG).show()
                        }
                    }

                    else if (status == 404) {
                        Toast.makeText(ctx, "Log In Failure", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }


    private fun setUp() {
        findViewById<EditText>(R.id.gc_message).addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (p0.toString().isEmpty()) {
                    findViewById<ImageButton>(R.id.gc_send).isEnabled = false
                    return
                }
                findViewById<ImageButton>(R.id.gc_send).isEnabled = true
            }

            override fun afterTextChanged(p0: Editable?) {}

        })
    }


    inner class UpdateChatThread(private val groupId: Int): Thread() {
        override fun run() {
            while (!ctx.stopThread) {
                sleep(1000)
                runOnUiThread {
                    ctx.getMessages(this.groupId)
                }
            }
        }
    }
}