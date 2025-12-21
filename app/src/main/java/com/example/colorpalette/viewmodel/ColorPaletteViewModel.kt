package com.example.colorpalette.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.colorpalette.data.AppState
import com.example.colorpalette.monitoring.AppMonitor
import com.example.colorpalette.monitoring.ErrorSeverity
import com.example.colorpalette.monitoring.PerformanceMetricType
import com.example.colorpalette.utils.ColorPaletteExtractor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for managing color palette extraction and UI state
 */
class ColorPaletteViewModel : ViewModel() {
    
    private val _uiState = MutableStateFlow<AppState>(AppState.Initial)
    val uiState: StateFlow<AppState> = _uiState.asStateFlow()
    
    private val extractor = ColorPaletteExtractor()
    private var appMonitor: AppMonitor? = null
    
    // Metrics for monitoring
    private var _extractionTime: Long = 0
    val extractionTime: Long get() = _extractionTime
    
    private var _errorCount: Int = 0
    val errorCount: Int get() = _errorCount
    
    /**
     * Processes an image and extracts its color palette
     * @param context Application context
     * @param imageUri URI of the image to process
     */
    fun processImage(context: Context, imageUri: Uri) {
        viewModelScope.launch {
            // Initialize AppMonitor if needed
            if (appMonitor == null) {
                appMonitor = AppMonitor.getInstance(context)
            }
            
            try {
                _uiState.value = AppState.Loading
                
                // Track feature usage
                appMonitor?.trackFeatureUsage("color_extraction")
                
                val startTime = System.currentTimeMillis()
                val colors = extractor.extractPalette(context, imageUri)
                _extractionTime = System.currentTimeMillis() - startTime
                
                // Record performance metric
                appMonitor?.recordPerformance(
                    type = PerformanceMetricType.IMAGE_PROCESSING,
                    duration = _extractionTime,
                    success = colors.isNotEmpty()
                )
                
                if (colors.isEmpty()) {
                    _uiState.value = AppState.Error("Could not extract colors from the image")
                    _errorCount++
                    
                    // Record error
                    appMonitor?.recordError(
                        errorType = "EmptyPalette",
                        message = "Could not extract colors from image",
                        context = "ColorPaletteViewModel.processImage",
                        severity = ErrorSeverity.MEDIUM
                    )
                } else {
                    _uiState.value = AppState.Success(imageUri, colors)
                }
            } catch (e: Exception) {
                _uiState.value = AppState.Error(e.message ?: "Unknown error occurred")
                _errorCount++
                
                // Record error
                appMonitor?.recordError(
                    errorType = e.javaClass.simpleName,
                    message = e.message ?: "Unknown error",
                    context = "ColorPaletteViewModel.processImage",
                    severity = ErrorSeverity.HIGH
                )
            }
        }
    }
    
    /**
     * Resets the app state to initial
     */
    fun resetState() {
        _uiState.value = AppState.Initial
    }
    
    /**
     * Gets key performance metrics
     */
    fun getMetrics(): Map<String, Any> {
        return mapOf(
            "extractionTime" to _extractionTime,
            "errorCount" to _errorCount
        )
    }
}

