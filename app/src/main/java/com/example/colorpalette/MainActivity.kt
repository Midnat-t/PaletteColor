package com.example.colorpalette

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.colorpalette.monitoring.AppMonitor
import com.example.colorpalette.monitoring.PerformanceMetricType
import com.example.colorpalette.ui.components.ColorPaletteScreen
import com.example.colorpalette.ui.theme.ColorPaletteTheme
import com.example.colorpalette.viewmodel.ColorPaletteViewModel

/**
 * Main Activity for the Color Palette Extractor application
 * 
 * This app demonstrates:
 * - Clean architecture with MVVM pattern
 * - Modern Material 3 design
 * - Performance monitoring
 * - User feedback collection
 * - Proper error handling
 */
class MainActivity : ComponentActivity() {
    private lateinit var appMonitor: AppMonitor
    private var startTime: Long = 0
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize app monitor
        appMonitor = AppMonitor.getInstance(this)
        
        // Record app start time for performance monitoring
        startTime = System.currentTimeMillis()
        
        enableEdgeToEdge()
        setContent {
            ColorPaletteTheme {
                val viewModel: ColorPaletteViewModel = viewModel()
                
                ColorPaletteScreen(
                    viewModel = viewModel,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
        
        // Log startup time and record in analytics
        val startupTime = System.currentTimeMillis() - startTime
        appMonitor.recordPerformance(
            type = PerformanceMetricType.APP_START,
            duration = startupTime,
            success = true
        )
        android.util.Log.d("Performance", "App startup time: ${startupTime}ms")
    }
    
    override fun onResume() {
        super.onResume()
        // Start session when app comes to foreground
        appMonitor.startSession()
        android.util.Log.d("Analytics", "Session started - App resumed")
    }
    
    override fun onPause() {
        super.onPause()
        // End session when app goes to background
        // This is more reliable than onDestroy()
        appMonitor.endSession()
        android.util.Log.d("Analytics", "Session ended - App paused")
    }
    
    override fun onDestroy() {
        super.onDestroy()
        android.util.Log.d("Analytics", "App destroyed")
    }
}