package com.ritwikscompany.treasurehunt.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
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
    private lateinit var linearLayout: LinearLayout
    private val ctx = this@GroupChatActivity
    var active = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_chat)

        this.userData = intent.getSerializableExtra("userData") as HashMap<String, Any>
        this.groupName = intent.getStringExtra("groupName") as String
        this.linearLayout = findViewById(R.id.gc_linear_layout)

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

                            findViewById<ImageButton>(R.id.gc_send).setOnClickListener {
                                val message = messageET.text.toString()
                                sendMessage(groupId, message)
                                messageET.setText("")
                            }

                            getMessagesFinal(groupId)
                            val scrollView = findViewById<ScrollView>(R.id.gc_sv)
                            scrollView.fullScroll(ScrollView.FOCUS_DOWN)
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
        active = true
    }

    override fun onStop() {
        super.onStop()
        active = false
    }


    private fun getMessagesFinal(groupId: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            while (ctx.active) {
                Thread.sleep(1000)
                getMessages(groupId)
            }
        }
    }


    private fun getMessages(groupId: Int) {
        val bodyJson = Gson().toJson(hashMapOf(
                "user_id" to userData["user_id"],
                "pw" to userData["password"],
                "group_id" to groupId
        ))
        CoroutineScope(Dispatchers.IO).launch {
            val (request, response, result) = Fuel.post("${getString(R.string.host)}/api/get_messages")
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

                            linearLayout.removeAllViews()
                            val inflater = LayoutInflater.from(ctx)

                            for (message in messages) {
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
            val (request, response, result) = Fuel.post("${getString(R.string.host)}/api/send_message")
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
                            val messages = Gson().fromJson(String(bytes), type) as ArrayList<HashMap<String, Any>>

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
}