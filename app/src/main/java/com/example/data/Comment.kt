package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "comments")
data class Comment(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val articleId: Int,
    val authorName: String,
    val commentText: String,
    val timestamp: String,
    val isAuthorAdmin: Boolean = false
)
