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

class MessageAdapter(
    private val messages: List<Message>,
    private val currentUserId: String
) : RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

    class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val messageTextView: TextView = itemView.findViewById(R.id.messageTextView)
        val container: LinearLayout = itemView.findViewById(R.id.messageContainer)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_message, parent, false)
        return MessageViewHolder(view)
    }

    override fun getItemCount(): Int = messages.size

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messages[position]
        holder.messageTextView.text = message.messageText

        val params = holder.container.layoutParams as ViewGroup.MarginLayoutParams
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        if (message.senderId == currentUserId) {
            layoutParams.gravity = Gravity.END
            layoutParams.marginStart = 50
            layoutParams.marginEnd = 0
            holder.container.setBackgroundResource(R.drawable.bg_message_sent)
        } else {
            layoutParams.gravity = Gravity.START
            layoutParams.marginStart = 0
            layoutParams.marginEnd = 50
            holder.container.setBackgroundResource(R.drawable.bg_message_received)
        }

        holder.container.layoutParams = layoutParams
    }
}
