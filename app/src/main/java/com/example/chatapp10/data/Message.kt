package com.example.chatapp10.data

data class Message(
    val senderId: String = "",
    val receiverId: String = "",
    val messageText: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
