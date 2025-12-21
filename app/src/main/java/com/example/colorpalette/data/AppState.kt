package com.example.colorpalette.data

import android.net.Uri

/**
 * Sealed class representing the different states of the application
 */
sealed class AppState {
    /**
     * Initial state - no image selected
     */
    object Initial : AppState()
    
    /**
     * Loading state - processing the image
     */
    object Loading : AppState()
    
    /**
     * Success state - colors extracted successfully
     * @param imageUri The URI of the selected image
     * @param colors List of extracted colors
     */
    data class Success(
        val imageUri: Uri,
        val colors: List<ColorInfo>
    ) : AppState()
    
    /**
     * Error state - something went wrong
     * @param message The error message
     */
    data class Error(val message: String) : AppState()
}

