package com.example.colorpalette.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.colorpalette.database.dao.AnalyticsDao
import com.example.colorpalette.database.entities.*

/**
 * Room Database for analytics storage
 * Stores all analytics data locally in SQLite database
 */
@Database(
    entities = [
        SessionEntity::class,
        CrashEntity::class,
        ErrorEntity::class,
        PerformanceMetricEntity::class,
        FeatureUsageEntity::class,
        FeedbackEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AnalyticsDatabase : RoomDatabase() {
    
    abstract fun analyticsDao(): AnalyticsDao
    
    companion object {
        @Volatile
        private var INSTANCE: AnalyticsDatabase? = null
        
        fun getDatabase(context: Context): AnalyticsDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AnalyticsDatabase::class.java,
                    "analytics_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

