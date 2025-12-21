package com.example.colorpalette.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Feature usage tracking entity
 * Records when features are used
 */
@Entity(tableName = "feature_usage")
data class FeatureUsageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long,
    val featureName: String,
    val sessionId: Long? = null
)

