package com.example.colorpalette.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Error tracking entity
 * Records non-fatal errors and exceptions
 */
@Entity(tableName = "errors")
data class ErrorEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long,
    val errorType: String,
    val errorMessage: String,
    val context: String, // Where the error occurred
    val severity: String, // LOW, MEDIUM, HIGH, CRITICAL
    val resolved: Boolean = false
)

