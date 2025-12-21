package com.example.colorpalette.utils

import android.util.Log

/**
 * Performance monitoring utility class
 * 
 * In a production environment, this would integrate with:
 * - Firebase Crashlytics for crash reporting
 * - Firebase Performance Monitoring for performance metrics
 * - Google Analytics or similar for user behavior
 */
object PerformanceMonitor {
    
    private const val TAG = "PerformanceMonitor"
    
    // Metrics storage
    private val metrics = mutableMapOf<String, MutableList<Long>>()
    private var crashCount = 0
    private var errorCount = 0
    
    /**
     * Tracks the execution time of an operation
     */
    fun trackOperationTime(operationName: String, timeMs: Long) {
        if (!metrics.containsKey(operationName)) {
            metrics[operationName] = mutableListOf()
        }
        metrics[operationName]?.add(timeMs)
        
        Log.d(TAG, "$operationName completed in ${timeMs}ms")
        
        // Alert if operation is too slow
        if (timeMs > 3000) {
            Log.w(TAG, "WARNING: $operationName took longer than 3 seconds!")
        }
    }
    
    /**
     * Records an error occurrence
     */
    fun recordError(errorType: String, errorMessage: String) {
        errorCount++
        Log.e(TAG, "Error recorded: $errorType - $errorMessage")
        
        // In production, send to Crashlytics or similar
        // FirebaseCrashlytics.getInstance().recordException(exception)
    }
    
    /**
     * Records a crash
     */
    fun recordCrash(exception: Throwable) {
        crashCount++
        Log.e(TAG, "Crash recorded", exception)
        
        // In production, send to Crashlytics
        // FirebaseCrashlytics.getInstance().recordException(exception)
    }
    
    /**
     * Gets average time for an operation
     */
    fun getAverageTime(operationName: String): Double {
        val times = metrics[operationName] ?: return 0.0
        return if (times.isNotEmpty()) {
            times.average()
        } else {
            0.0
        }
    }
    
    /**
     * Gets all performance metrics
     */
    fun getAllMetrics(): Map<String, Any> {
        return mapOf(
            "operations" to metrics.mapValues { entry ->
                mapOf(
                    "count" to entry.value.size,
                    "average" to getAverageTime(entry.key),
                    "min" to (entry.value.minOrNull() ?: 0),
                    "max" to (entry.value.maxOrNull() ?: 0)
                )
            },
            "errorCount" to errorCount,
            "crashCount" to crashCount,
            "crashRate" to if (metrics.values.sumOf { it.size } > 0) {
                crashCount.toDouble() / metrics.values.sumOf { it.size }
            } else 0.0
        )
    }
    
    /**
     * Gets key quality metrics
     */
    fun getQualityReport(): String {
        val sb = StringBuilder()
        sb.appendLine("=== App Quality Report ===")
        sb.appendLine()
        
        // Performance metrics
        sb.appendLine("Performance Metrics:")
        metrics.forEach { (operation, times) ->
            sb.appendLine("  $operation:")
            sb.appendLine("    Count: ${times.size}")
            sb.appendLine("    Avg: ${String.format("%.2f", getAverageTime(operation))}ms")
            sb.appendLine("    Min: ${times.minOrNull() ?: 0}ms")
            sb.appendLine("    Max: ${times.maxOrNull() ?: 0}ms")
        }
        
        // Reliability metrics
        sb.appendLine()
        sb.appendLine("Reliability Metrics:")
        sb.appendLine("  Error Count: $errorCount")
        sb.appendLine("  Crash Count: $crashCount")
        val totalOps = metrics.values.sumOf { it.size }
        if (totalOps > 0) {
            sb.appendLine("  Crash Rate: ${String.format("%.2f", (crashCount.toDouble() / totalOps) * 100)}%")
            sb.appendLine("  Error Rate: ${String.format("%.2f", (errorCount.toDouble() / totalOps) * 100)}%")
        }
        
        return sb.toString()
    }
    
    /**
     * Logs the quality report
     */
    fun logQualityReport() {
        Log.i(TAG, getQualityReport())
    }
}

