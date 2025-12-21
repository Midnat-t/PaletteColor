package com.example.colorpalette.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.ui.graphics.Color
import androidx.palette.graphics.Palette
import com.example.colorpalette.data.ColorInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream

/**
 * Utility class for extracting color palette from images
 */
class ColorPaletteExtractor {
    
    /**
     * Extracts a color palette from an image URI
     * @param context Application context
     * @param imageUri URI of the image to process
     * @return List of ColorInfo objects representing the palette
     */
    suspend fun extractPalette(context: Context, imageUri: Uri): List<ColorInfo> {
        return withContext(Dispatchers.Default) {
            try {
                val bitmap = loadBitmap(context, imageUri)
                val palette = Palette.from(bitmap).generate()
                
                val colors = mutableListOf<ColorInfo>()
                
                // Extract various color swatches from the palette
                palette.vibrantSwatch?.let { swatch ->
                    colors.add(
                        ColorInfo(
                            color = Color(swatch.rgb),
                            hexCode = String.format("#%06X", 0xFFFFFF and swatch.rgb),
                            population = swatch.population,
                            name = "Vibrant"
                        )
                    )
                }
                
                palette.lightVibrantSwatch?.let { swatch ->
                    colors.add(
                        ColorInfo(
                            color = Color(swatch.rgb),
                            hexCode = String.format("#%06X", 0xFFFFFF and swatch.rgb),
                            population = swatch.population,
                            name = "Light Vibrant"
                        )
                    )
                }
                
                palette.darkVibrantSwatch?.let { swatch ->
                    colors.add(
                        ColorInfo(
                            color = Color(swatch.rgb),
                            hexCode = String.format("#%06X", 0xFFFFFF and swatch.rgb),
                            population = swatch.population,
                            name = "Dark Vibrant"
                        )
                    )
                }
                
                palette.mutedSwatch?.let { swatch ->
                    colors.add(
                        ColorInfo(
                            color = Color(swatch.rgb),
                            hexCode = String.format("#%06X", 0xFFFFFF and swatch.rgb),
                            population = swatch.population,
                            name = "Muted"
                        )
                    )
                }
                
                palette.lightMutedSwatch?.let { swatch ->
                    colors.add(
                        ColorInfo(
                            color = Color(swatch.rgb),
                            hexCode = String.format("#%06X", 0xFFFFFF and swatch.rgb),
                            population = swatch.population,
                            name = "Light Muted"
                        )
                    )
                }
                
                palette.darkMutedSwatch?.let { swatch ->
                    colors.add(
                        ColorInfo(
                            color = Color(swatch.rgb),
                            hexCode = String.format("#%06X", 0xFFFFFF and swatch.rgb),
                            population = swatch.population,
                            name = "Dark Muted"
                        )
                    )
                }
                
                palette.dominantSwatch?.let { swatch ->
                    // Only add if not already in the list
                    if (colors.none { it.hexCode == String.format("#%06X", 0xFFFFFF and swatch.rgb) }) {
                        colors.add(
                            ColorInfo(
                                color = Color(swatch.rgb),
                                hexCode = String.format("#%06X", 0xFFFFFF and swatch.rgb),
                                population = swatch.population,
                                name = "Dominant"
                            )
                        )
                    }
                }
                
                // Sort by population (most common first)
                colors.sortedByDescending { it.population }
            } catch (e: Exception) {
                throw Exception("Failed to extract palette: ${e.message}")
            }
        }
    }
    
    /**
     * Loads a bitmap from a URI
     * @param context Application context
     * @param uri URI of the image
     * @return Bitmap object
     */
    private fun loadBitmap(context: Context, uri: Uri): Bitmap {
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        inputStream?.close()
        
        // Resize bitmap if too large to improve performance
        return if (bitmap.width > 1024 || bitmap.height > 1024) {
            val scale = minOf(1024f / bitmap.width, 1024f / bitmap.height)
            Bitmap.createScaledBitmap(
                bitmap,
                (bitmap.width * scale).toInt(),
                (bitmap.height * scale).toInt(),
                true
            )
        } else {
            bitmap
        }
    }
}

