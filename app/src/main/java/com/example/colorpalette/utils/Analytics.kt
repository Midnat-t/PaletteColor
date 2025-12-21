package com.example.colorpalette.utils

import android.util.Log

/**
 * Analytics utility for tracking user behavior and app usage
 * 
 * In production, this would integrate with:
 * - Google Analytics
 * - Firebase Analytics
 * - Custom analytics solution
 */
object Analytics {
    
    private const val TAG = "Analytics"
    
    // User engagement metrics
    private var sessionCount = 0
    private var featureUsageCount = mutableMapOf<String, Int>()
    private var userFeedback = mutableListOf<UserFeedback>()
    
    data class UserFeedback(
        val rating: Int,
        val comment: String,
        val timestamp: Long = System.currentTimeMillis()
    )
    
    /**
     * Tracks when a user starts a session
     */
    fun trackSessionStart() {
        sessionCount++
        Log.d(TAG, "Session started. Total sessions: $sessionCount")
    }
    
    /**
     * Tracks feature usage
     */
    fun trackFeatureUsage(featureName: String) {
        featureUsageCount[featureName] = (featureUsageCount[featureName] ?: 0) + 1
        Log.d(TAG, "Feature used: $featureName (${featureUsageCount[featureName]} times)")
    }
    
    /**
     * Records user feedback
     */
    fun recordFeedback(rating: Int, comment: String) {
        userFeedback.add(UserFeedback(rating, comment))
        Log.d(TAG, "Feedback received: $rating stars - $comment")
    }
    
    /**
     * Calculates Net Promoter Score (NPS)
     * NPS = % Promoters (9-10) - % Detractors (0-6)
     */
    fun calculateNPS(): Double {
        if (userFeedback.isEmpty()) return 0.0
        
        val ratings = userFeedback.map { 
            // Convert 5-star rating to 10-point scale
            it.rating * 2
        }
        
        val promoters = ratings.count { it >= 9 }
        val detractors = ratings.count { it <= 6 }
        val total = ratings.size
        
        return ((promoters - detractors).toDouble() / total) * 100
    }
    
    /**
     * Calculates Customer Satisfaction Index (CSI)
     */
    fun calculateCSI(): Double {
        if (userFeedback.isEmpty()) return 0.0
        
        val averageRating = userFeedback.map { it.rating }.average()
        return (averageRating / 5.0) * 100 // Convert to percentage
    }
    
    /**
     * Gets analytics report
     */
    fun getAnalyticsReport(): String {
        val sb = StringBuilder()
        sb.appendLine("=== Analytics Report ===")
        sb.appendLine()
        
        sb.appendLine("User Engagement:")
        sb.appendLine("  Total Sessions: $sessionCount")
        sb.appendLine()
        
        sb.appendLine("Feature Usage:")
        featureUsageCount.forEach { (feature, count) ->
            sb.appendLine("  $feature: $count times")
        }
        sb.appendLine()
        
        sb.appendLine("User Feedback:")
        sb.appendLine("  Total Feedback: ${userFeedback.size}")
        if (userFeedback.isNotEmpty()) {
            val avgRating = userFeedback.map { it.rating }.average()
            sb.appendLine("  Average Rating: ${String.format("%.2f", avgRating)}/5")
            sb.appendLine("  CSI: ${String.format("%.2f", calculateCSI())}%")
            sb.appendLine("  NPS: ${String.format("%.2f", calculateNPS())}")
        }
        
        return sb.toString()
    }
    
    /**
     * Logs the analytics report
     */
    fun logAnalyticsReport() {
        Log.i(TAG, getAnalyticsReport())
    }
}

