package com.example.chatapp10.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp10.R
import com.example.chatapp10.adapters.UsersAdapter
import com.example.chatapp10.data.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class UsersListActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var usersRecyclerView: RecyclerView
    private val users = mutableListOf<User>() // Create your User data class

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_users_list)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        usersRecyclerView = findViewById(R.id.usersRecyclerView)

        usersRecyclerView.layoutManager = LinearLayoutManager(this)
        val adapter = UsersAdapter(users) { selectedUser ->
            val intent = Intent(this, ChatActivity::class.java)
            intent.putExtra("receiverId", selectedUser.uid)
            startActivity(intent)
        }
        usersRecyclerView.adapter = adapter

        db.collection("users").get().addOnSuccessListener { snapshot ->
            users.clear()
            for (doc in snapshot.documents) {
                val user = doc.toObject(User::class.java)
                if (user != null && user.uid != auth.currentUser?.uid) {
                    users.add(user)
                }
            }
            adapter.notifyDataSetChanged()
        }
    }
}
