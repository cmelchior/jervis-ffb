package com.jervisffb.ui.game.view.utils

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection

/**
 * Shape describing the standard D6 dice image.
 * They are slightly cut at the corners.
 */
val D6Shape: Shape = object : Shape {
    private val CORNER_FACTOR = 3/48f
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val cornerWidth = size.width * CORNER_FACTOR
        val cornerHeight = size.height * CORNER_FACTOR
        // Standard D6 dice image is 48x48 pixels with a 3px cut at each corner.
        val path = Path().apply {
            moveTo(cornerWidth, 0f)
            lineTo(size.width - cornerWidth, 0f)
            lineTo(size.width, cornerHeight)
            lineTo(size.width, size.height - cornerHeight)
            lineTo(size.width - cornerWidth, size.height)
            lineTo(cornerWidth, size.height)
            lineTo(0f, size.height - cornerHeight)
            lineTo(0f, cornerHeight)
            close()
        }
        return Outline.Generic(path)
    }
}

/**
 * Shape describing the standard D8 dice image.
 */
val D8Shape: Shape = object : Shape {
    // Standard D8 dice image is 51x58 pixels
    val TOP_HEIGHT_FACTOR = 16/58f
    val TOP_WIDTH_FACTOR = 25/51f
    val BOTTOM_HEIGHT_FACTOR = 13/58f
    val BOTTOM_WIDTH_FACTOR = 25/51f
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val topHeight = TOP_HEIGHT_FACTOR * size.height
        val topWidth = TOP_WIDTH_FACTOR * size.width
        val bottomHeight = BOTTOM_HEIGHT_FACTOR * size.height
        val bottomWidth = BOTTOM_WIDTH_FACTOR * size.width
        val path = Path().apply {
            moveTo(size.width / 2f, 0f)
            lineTo(size.width, topHeight)
            lineTo(size.width, size.height - bottomHeight)
            lineTo(size.width / 2f, size.height)
            lineTo(0f, size.height - bottomHeight)
            lineTo(0f, topHeight)
            close()
        }
        return Outline.Generic(path)
    }
}

/**
 * Shape describing the standard D20 dice image.
 */
val D20Shape: Shape = object : Shape {
    // Standard D20 dice image is 51x58 pixels
    val TOP_HEIGHT_FACTOR = 14/58f
    val TOP_WIDTH_FACTOR = 25/51f
    val BOTTOM_HEIGHT_FACTOR = 14/58f
    val BOTTOM_WIDTH_FACTOR = 25/51f
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val topHeight = TOP_HEIGHT_FACTOR * size.height
        val topWidth = TOP_WIDTH_FACTOR * size.width
        val bottomHeight = BOTTOM_HEIGHT_FACTOR * size.height
        val bottomWidth = BOTTOM_WIDTH_FACTOR * size.width
        val path = Path().apply {
            moveTo(size.width / 2f, 0f)
            lineTo(size.width, topHeight)
            lineTo(size.width, size.height - bottomHeight)
            lineTo(size.width / 2f, size.height)
            lineTo(0f, size.height - bottomHeight)
            lineTo(0f, topHeight)
            close()
        }
        return Outline.Generic(path)
    }
}
