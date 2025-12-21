package com.example.colorpalette.utils

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Persistent storage for analytics data
 * Saves data to SharedPreferences so it survives app restarts
 */
class AnalyticsStorage(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "analytics_storage",
        Context.MODE_PRIVATE
    )
    
    private val gson = Gson()
    
    companion object {
        private const val KEY_SESSION_COUNT = "session_count"
        private const val KEY_FEATURE_USAGE = "feature_usage"
        private const val KEY_USER_FEEDBACK = "user_feedback"
        private const val KEY_FIRST_LAUNCH = "first_launch"
        private const val KEY_LAST_SESSION = "last_session"
    }
    
    // Session tracking
    fun getSessionCount(): Int {
        return prefs.getInt(KEY_SESSION_COUNT, 0)
    }
    
    fun incrementSessionCount() {
        val count = getSessionCount() + 1
        prefs.edit().putInt(KEY_SESSION_COUNT, count).apply()
        prefs.edit().putLong(KEY_LAST_SESSION, System.currentTimeMillis()).apply()
    }
    
    fun getFirstLaunchTime(): Long {
        val time = prefs.getLong(KEY_FIRST_LAUNCH, 0)
        if (time == 0L) {
            val now = System.currentTimeMillis()
            prefs.edit().putLong(KEY_FIRST_LAUNCH, now).apply()
            return now
        }
        return time
    }
    
    fun getLastSessionTime(): Long {
        return prefs.getLong(KEY_LAST_SESSION, 0)
    }
    
    // Feature usage tracking
    fun getFeatureUsage(): Map<String, Int> {
        val json = prefs.getString(KEY_FEATURE_USAGE, null) ?: return emptyMap()
        val type = object : TypeToken<Map<String, Int>>() {}.type
        return gson.fromJson(json, type)
    }
    
    fun saveFeatureUsage(usageMap: Map<String, Int>) {
        val json = gson.toJson(usageMap)
        prefs.edit().putString(KEY_FEATURE_USAGE, json).apply()
    }
    
    fun incrementFeatureUsage(featureName: String) {
        val usage = getFeatureUsage().toMutableMap()
        usage[featureName] = (usage[featureName] ?: 0) + 1
        saveFeatureUsage(usage)
    }
    
    // User feedback tracking
    fun getUserFeedback(): List<Analytics.UserFeedback> {
        val json = prefs.getString(KEY_USER_FEEDBACK, null) ?: return emptyList()
        val type = object : TypeToken<List<Analytics.UserFeedback>>() {}.type
        return gson.fromJson(json, type)
    }
    
    fun saveFeedback(feedback: List<Analytics.UserFeedback>) {
        val json = gson.toJson(feedback)
        prefs.edit().putString(KEY_USER_FEEDBACK, json).apply()
    }
    
    fun addFeedback(feedback: Analytics.UserFeedback) {
        val currentFeedback = getUserFeedback().toMutableList()
        currentFeedback.add(feedback)
        saveFeedback(currentFeedback)
    }
    
    // Clear all analytics data
    fun clearAll() {
        prefs.edit().clear().apply()
    }
    
    // Export all data as JSON
    fun exportAsJson(): String {
        val data = mapOf(
            "sessionCount" to getSessionCount(),
            "firstLaunch" to getFirstLaunchTime(),
            "lastSession" to getLastSessionTime(),
            "featureUsage" to getFeatureUsage(),
            "userFeedback" to getUserFeedback()
        )
        return gson.toJson(data)
    }
}

