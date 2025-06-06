package com.example.chatapp10.activities

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp10.R
import com.example.chatapp10.adapters.MessageAdapter
import com.example.chatapp10.data.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.json.JSONObject
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.MediaType.Companion.toMediaType
import java.io.IOException



class ChatActivity : AppCompatActivity() {

    private lateinit var messagesRecyclerView: RecyclerView
    private lateinit var messageEditText: EditText
    private lateinit var sendButton: Button

    private val messages = mutableListOf<Message>()
    private lateinit var adapter: MessageAdapter

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private var chatUserId: String? = null
    private var chatUsername: String? = null
    private var currentUserId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        messagesRecyclerView = findViewById(R.id.messagesRecyclerView)
        messageEditText = findViewById(R.id.messageEditText)
        sendButton = findViewById(R.id.sendButton)

        currentUserId = auth.currentUser?.uid
        chatUserId = intent.getStringExtra("receiverId") // ✅ FIXED LINE
        chatUsername = intent.getStringExtra("chatUsername")

        supportActionBar?.title = chatUsername

        adapter = MessageAdapter(messages, currentUserId ?: "")
        messagesRecyclerView.layoutManager = LinearLayoutManager(this)
        messagesRecyclerView.adapter = adapter

        Toast.makeText(this, "Message1", Toast.LENGTH_SHORT).show()

        sendButton.setOnClickListener {
            Toast.makeText(this, "Message2", Toast.LENGTH_SHORT).show()
            sendMessage()
        }

        listenForMessages()
    }

    private fun sendMessage() {
        val messageText = messageEditText.text.toString().trim()

        // Debugging logs
        Log.d("ChatDebug", "chatUserId: $chatUserId, currentUserId: $currentUserId, messageText: $messageText")

        if (messageText.isEmpty() || chatUserId == null || currentUserId == null) return

        val message = Message(
            senderId = currentUserId!!,
            receiverId = chatUserId!!,
            messageText = messageText,
            timestamp = System.currentTimeMillis()
        )

        val chatId = getChatId(currentUserId!!, chatUserId!!)

        db.collection("chats")
            .document(chatId)
            .collection("messages")
            .add(message)
            .addOnSuccessListener {
                Toast.makeText(this, "Message4: Sent!", Toast.LENGTH_SHORT).show()
                messageEditText.text.clear()
                sendPushNotification(chatUserId!!, messageText, currentUserId!!)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to send: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun listenForMessages() {
        if (chatUserId == null || currentUserId == null) return

        val chatId = getChatId(currentUserId!!, chatUserId!!)

        db.collection("chats")
            .document(chatId)
            .collection("messages")
            .orderBy("timestamp")
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Toast.makeText(this, "Failed to load messages", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    messages.clear()
                    for (doc in snapshots.documents) {
                        val msg = doc.toObject(Message::class.java)
                        if (msg != null) messages.add(msg)
                    }
                    adapter.notifyDataSetChanged()
                    messagesRecyclerView.scrollToPosition(messages.size - 1)
                }
            }
    }

    private fun sendPushNotification(receiverId: String, messageText: String, senderId: String) {
        db.collection("users").document(senderId).get()
            .addOnSuccessListener { senderSnapshot ->
                val senderName = senderSnapshot.getString("username") ?: "Chat App"

                db.collection("tokens").document(receiverId).get()
                    .addOnSuccessListener { tokenSnapshot ->
                        val token = tokenSnapshot.getString("token")
                        if (!token.isNullOrEmpty()) {
                            val data = mapOf(
                                "to" to token,
                                "notification" to mapOf(
                                    "title" to senderName,
                                    "body" to messageText
                                ),
                                "data" to mapOf(
                                    "senderId" to senderId,
                                    "senderName" to senderName
                                )
                            )

                            // Send with HTTP call (see below)
                            sendNotificationToToken(data)
                        }
                    }
            }
    }


    private fun getChatId(user1: String, user2: String): String {
        // Ensure chatId is consistent regardless of user order
        return if (user1 < user2) "$user1-$user2" else "$user2-$user1"
    }

    private fun sendNotificationToToken(data: Map<String, Any>) {
        val client = OkHttpClient()

        val jsonBody = JSONObject(data).toString()
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = jsonBody.toRequestBody(mediaType)

        val request = Request.Builder()
            .url("https://fcm.googleapis.com/fcm/send")
            .addHeader("Authorization", "key=dhKNe9qeQFZJtHCiNIQ9eFzvNJ1GMeJOp0kN7Mj8BGs") // 🔐 Replace with your actual server key!
            .addHeader("Content-Type", "application/json")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("FCM", "Failed to send notification", e)
            }

            override fun onResponse(call: Call, response: Response) {
                Log.d("FCM", "Notification response: ${response.body?.string()}")
            }
        })
    }

}





