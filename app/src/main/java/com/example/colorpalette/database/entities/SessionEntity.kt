package com.example.colorpalette.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Session tracking entity
 * Tracks each app session with duration and timestamps
 */
@Entity(tableName = "sessions")
data class SessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val startTime: Long,
    val endTime: Long? = null,
    val duration: Long? = null, // in milliseconds
    val appVersion: String,
    val androidVersion: Int,
    val deviceModel: String
)

