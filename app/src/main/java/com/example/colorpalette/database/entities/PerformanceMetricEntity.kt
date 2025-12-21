package com.example.colorpalette.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Performance metrics entity
 * Tracks various performance measurements
 */
@Entity(tableName = "performance_metrics")
data class PerformanceMetricEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long,
    val metricType: String, // APP_START, IMAGE_PROCESSING, SCREEN_LOAD, etc.
    val duration: Long, // in milliseconds
    val success: Boolean,
    val additionalData: String? = null // JSON for extra info
)

