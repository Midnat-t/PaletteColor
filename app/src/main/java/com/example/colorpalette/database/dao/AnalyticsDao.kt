package com.example.colorpalette.database.dao

import androidx.room.*
import com.example.colorpalette.database.entities.*
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for analytics database
 */
@Dao
interface AnalyticsDao {
    
    // ========== Sessions ==========
    @Insert
    suspend fun insertSession(session: SessionEntity): Long
    
    @Update
    suspend fun updateSession(session: SessionEntity)
    
    @Query("SELECT * FROM sessions ORDER BY startTime DESC LIMIT :limit")
    fun getRecentSessions(limit: Int = 10): Flow<List<SessionEntity>>
    
    @Query("SELECT COUNT(*) FROM sessions")
    suspend fun getTotalSessionCount(): Int
    
    @Query("SELECT AVG(duration) FROM sessions WHERE duration IS NOT NULL")
    suspend fun getAverageSessionDuration(): Double?
    
    @Query("SELECT * FROM sessions WHERE startTime >= :since")
    suspend fun getSessionsSince(since: Long): List<SessionEntity>
    
    // ========== Crashes ==========
    @Insert
    suspend fun insertCrash(crash: CrashEntity)
    
    @Query("SELECT * FROM crashes ORDER BY timestamp DESC")
    fun getAllCrashes(): Flow<List<CrashEntity>>
    
    @Query("SELECT COUNT(*) FROM crashes")
    suspend fun getTotalCrashCount(): Int
    
    @Query("SELECT COUNT(*) FROM crashes WHERE timestamp >= :since")
    suspend fun getCrashCountSince(since: Long): Int
    
    @Query("SELECT * FROM crashes ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastCrash(): CrashEntity?
    
    // ========== Errors ==========
    @Insert
    suspend fun insertError(error: ErrorEntity)
    
    @Query("SELECT * FROM errors ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentErrors(limit: Int = 20): Flow<List<ErrorEntity>>
    
    @Query("SELECT COUNT(*) FROM errors")
    suspend fun getTotalErrorCount(): Int
    
    @Query("SELECT COUNT(*) FROM errors WHERE severity = :severity")
    suspend fun getErrorCountBySeverity(severity: String): Int
    
    @Query("SELECT COUNT(*) FROM errors WHERE timestamp >= :since")
    suspend fun getErrorCountSince(since: Long): Int
    
    // ========== Performance Metrics ==========
    @Insert
    suspend fun insertPerformanceMetric(metric: PerformanceMetricEntity)
    
    @Query("SELECT * FROM performance_metrics WHERE metricType = :type ORDER BY timestamp DESC LIMIT :limit")
    fun getMetricsByType(type: String, limit: Int = 50): Flow<List<PerformanceMetricEntity>>
    
    @Query("SELECT AVG(duration) FROM performance_metrics WHERE metricType = :type AND success = 1")
    suspend fun getAverageDuration(type: String): Double?
    
    @Query("SELECT MIN(duration) FROM performance_metrics WHERE metricType = :type AND success = 1")
    suspend fun getMinDuration(type: String): Long?
    
    @Query("SELECT MAX(duration) FROM performance_metrics WHERE metricType = :type AND success = 1")
    suspend fun getMaxDuration(type: String): Long?
    
    @Query("SELECT COUNT(*) FROM performance_metrics WHERE metricType = :type AND success = 0")
    suspend fun getFailureCount(type: String): Int
    
    // ========== Feature Usage ==========
    @Insert
    suspend fun insertFeatureUsage(usage: FeatureUsageEntity)
    
    @Query("SELECT featureName, COUNT(*) as count FROM feature_usage GROUP BY featureName ORDER BY count DESC")
    suspend fun getFeatureUsageCounts(): List<FeatureUsageCount>
    
    @Query("SELECT COUNT(*) FROM feature_usage WHERE featureName = :featureName")
    suspend fun getFeatureUsageCount(featureName: String): Int
    
    @Query("SELECT COUNT(DISTINCT DATE(timestamp/1000, 'unixepoch')) FROM feature_usage WHERE featureName = :featureName")
    suspend fun getFeatureUsageDays(featureName: String): Int
    
    // ========== Feedback ==========
    @Insert
    suspend fun insertFeedback(feedback: FeedbackEntity)
    
    @Query("SELECT * FROM feedback ORDER BY timestamp DESC")
    fun getAllFeedback(): Flow<List<FeedbackEntity>>
    
    @Query("SELECT AVG(rating) FROM feedback")
    suspend fun getAverageRating(): Double?
    
    @Query("SELECT COUNT(*) FROM feedback")
    suspend fun getTotalFeedbackCount(): Int
    
    @Query("SELECT COUNT(*) FROM feedback WHERE rating >= 4")
    suspend fun getPositiveFeedbackCount(): Int
    
    @Query("SELECT COUNT(*) FROM feedback WHERE rating <= 2")
    suspend fun getNegativeFeedbackCount(): Int
    
    // ========== Aggregated Queries ==========
    @Query("""
        SELECT 
            (SELECT COUNT(*) FROM crashes WHERE timestamp >= :since) as crashCount,
            (SELECT COUNT(*) FROM errors WHERE timestamp >= :since) as errorCount,
            (SELECT COUNT(*) FROM sessions WHERE startTime >= :since) as sessionCount
    """)
    suspend fun getHealthMetrics(since: Long): HealthMetrics
    
    // ========== Cleanup ==========
    @Query("DELETE FROM sessions WHERE startTime < :before")
    suspend fun deleteOldSessions(before: Long)
    
    @Query("DELETE FROM performance_metrics WHERE timestamp < :before")
    suspend fun deleteOldMetrics(before: Long)
    
    @Query("DELETE FROM feature_usage WHERE timestamp < :before")
    suspend fun deleteOldUsage(before: Long)
    
    // ========== Clear All Data ==========
    @Query("DELETE FROM sessions")
    suspend fun clearAllSessions()
    
    @Query("DELETE FROM crashes")
    suspend fun clearAllCrashes()
    
    @Query("DELETE FROM errors")
    suspend fun clearAllErrors()
    
    @Query("DELETE FROM performance_metrics")
    suspend fun clearAllMetrics()
    
    @Query("DELETE FROM feature_usage")
    suspend fun clearAllUsage()
    
    @Query("DELETE FROM feedback")
    suspend fun clearAllFeedback()
}

// Helper data classes for queries
data class FeatureUsageCount(
    val featureName: String,
    val count: Int
)

data class HealthMetrics(
    val crashCount: Int,
    val errorCount: Int,
    val sessionCount: Int
)

