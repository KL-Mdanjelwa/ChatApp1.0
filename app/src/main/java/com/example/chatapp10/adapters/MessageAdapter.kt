package com.example.chatapp10.adapters

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp10.R
import com.example.chatapp10.data.Message

class MessageAdapter(private val messages: List<Message>, private val currentUserId: String) :
    RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

    inner class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val messageTextView: TextView = itemView.findViewById(R.id.messageTextView)
        val container: LinearLayout = itemView.findViewById(R.id.messageContainer)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_message, parent, false)
        return MessageViewHolder(view)
    }

    override fun getItemCount() = messages.size

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messages[position]
        holder.messageTextView.text = message.messageText

        // Align message left or right based on sender
        val params = holder.container.layoutParams as ViewGroup.MarginLayoutParams
        if (message.senderId == currentUserId) {
            holder.container.gravity = Gravity.END
            params.marginStart = 50
            params.marginEnd = 0
            holder.container.setBackgroundResource(R.drawable.bg_message_sent)
        } else {
            holder.container.gravity = Gravity.START
            params.marginStart = 0
            params.marginEnd = 50
            holder.container.setBackgroundResource(R.drawable.bg_message_received)
        }
        holder.container.layoutParams = params
    }
}
