package com.example.colorpalette.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * User feedback entity
 * Stores user ratings and comments
 */
@Entity(tableName = "feedback")
data class FeedbackEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long,
    val rating: Int, // 1-5 stars
    val comment: String,
    val appVersion: String,
    val contextInfo: String? = null // What they were doing when leaving feedback
)

