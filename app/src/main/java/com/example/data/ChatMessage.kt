package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val senderName: String,
    val messageText: String,
    val timestamp: String,
    val senderTitle: String = "Tamu",
    val isSenderAdmin: Boolean = false
)
