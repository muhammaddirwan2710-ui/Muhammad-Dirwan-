package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "articles")
data class Article(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val category: String, // "Karya Ilmiah" or "Pendapat Hukum"
    val description: String, // Short summary
    val content: String, // Detailed body text
    val author: String = "Muhammad Dirwan, S.H.",
    val pdfUri: String? = null, // URI of PDF in local storage
    val pdfFileName: String? = null, // Name of the attached PDF
    val date: String, // e.g. "24 Mei 2026"
    val readTime: String, // e.g. "5 menit"
    val categoryShortcut: String = "Hukum" // Subcategory tag e.g. "Perdata", "Pidana", "Masyarakat"
) : Serializable
