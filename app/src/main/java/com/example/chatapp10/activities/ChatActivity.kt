package com.example.chatapp10.activities

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp10.R
import com.example.chatapp10.adapters.MessageAdapter
import com.example.chatapp10.data.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

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
        chatUserId = intent.getStringExtra("chatUserId")
        chatUsername = intent.getStringExtra("chatUsername")

        supportActionBar?.title = chatUsername

        adapter = MessageAdapter(messages, currentUserId ?: "")
        messagesRecyclerView.layoutManager = LinearLayoutManager(this)
        messagesRecyclerView.adapter = adapter

        sendButton.setOnClickListener {
            sendMessage()
        }

        listenForMessages()
    }

    private fun sendMessage() {
        val messageText = messageEditText.text.toString().trim()
        if (messageText.isEmpty() || chatUserId == null || currentUserId == null) return

        val message = Message(
            senderId = currentUserId!!,
            receiverId = chatUserId!!,
            messageText = messageText,
            timestamp = System.currentTimeMillis()
        )

        // Store messages under a "chats" collection with a combined chatId for both users
        val chatId = getChatId(currentUserId!!, chatUserId!!)

        db.collection("chats")
            .document(chatId)
            .collection("messages")
            .add(message)
            .addOnSuccessListener {
                messageEditText.text.clear()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to send message", Toast.LENGTH_SHORT).show()
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

    private fun getChatId(user1: String, user2: String): String {
        // Ensure chatId is consistent regardless of user order
        return if (user1 < user2) "$user1-$user2" else "$user2-$user1"
    }
}
