package com.example.colorpalette.data

import androidx.compose.ui.graphics.Color

/**
 * Data class representing a color extracted from the image
 * @param color The color value
 * @param hexCode The hexadecimal representation of the color
 * @param population The number of pixels with this color in the image
 * @param name A descriptive name for the color type
 */
data class ColorInfo(
    val color: Color,
    val hexCode: String,
    val population: Int,
    val name: String
)

