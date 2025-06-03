package com.example.chatapp10.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp10.R
import com.example.chatapp10.data.User

class UsersAdapter(
    private val users: List<User>,
    private val onUserClick: (User) -> Unit
) : RecyclerView.Adapter<UsersAdapter.UserViewHolder>() {

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val usernameTextView: TextView = itemView.findViewById(R.id.usernameTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view)
    }

    override fun getItemCount() = users.size

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = users[position]
        holder.usernameTextView.text = user.username
        holder.itemView.setOnClickListener { onUserClick(user) }
    }
}
