package com.example.colorpalette.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Crash tracking entity
 * Records app crashes with stack traces
 */
@Entity(tableName = "crashes")
data class CrashEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long,
    val exceptionType: String,
    val exceptionMessage: String,
    val stackTrace: String,
    val appVersion: String,
    val androidVersion: Int,
    val deviceModel: String
)

