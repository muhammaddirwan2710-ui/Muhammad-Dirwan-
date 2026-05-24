package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CommentDao {
    @Query("SELECT * FROM comments WHERE articleId = :articleId ORDER BY id ASC")
    fun getCommentsForArticle(articleId: Int): Flow<List<Comment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: Comment): Long

    @Delete
    suspend fun deleteComment(comment: Comment)
}
