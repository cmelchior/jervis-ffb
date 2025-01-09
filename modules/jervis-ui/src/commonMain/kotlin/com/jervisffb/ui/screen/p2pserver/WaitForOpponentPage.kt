package com.jervisffb.ui.screen.p2pserver

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asSkiaBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.github.ajalt.colormath.extensions.android.composecolor.toComposeColor
import com.github.ajalt.colormath.model.HSL
import com.github.ajalt.colormath.model.RGB
import com.jervisffb.ui.screen.P2PServerScreenModel
import com.jervisffb.ui.screen.TeamInfo
import com.kmpalette.palette.graphics.Palette

@Composable
fun WaitForOpponentPage(viewModel: P2PServerScreenModel) {
    val availableTeams by viewModel.availableTeams.collectAsState()
    var showImportFumbblTeam by remember { mutableStateOf(false) }
    val selectedTeam: TeamInfo? by viewModel.selectedTeam.collectAsState()
    Column(
        modifier = Modifier.padding(16.dp).fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        selectedTeam?.let {
            ComplementaryBackgroundImage(it.logo)
        }
    }
}


@Composable
fun ComplementaryBackgroundImage(bitmap: ImageBitmap) {
    val dominantColor = extractDominantColor(bitmap)
    // val dominantColor = extractAverageColor(bitmap) // extractDominantColor(bitmap)
    val complementaryColor = dominantColor?.let { getComplementaryColor(it) } ?: Color.White
    val bgColor = complementaryColor// ?: Color.Black
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Row(modifier = Modifier.fillMaxWidth().height(48.dp)) {
            Box(
                modifier = Modifier.aspectRatio(1f).weight(1f).background(
                    color = dominantColor ?: Color.Black
                )
            )
            Box(
                modifier = Modifier.aspectRatio(1f).weight(1f).background(
                    color = complementaryColor
                )
            )
        }
        Box(
            modifier = Modifier.fillMaxSize().background(bgColor)
        ) {
            Image(
                bitmap = bitmap,
                contentDescription = "Image with complementary background",
                contentScale = ContentScale.None,
                modifier = Modifier.matchParentSize()
            )
        }
    }



}

// Function to extract the dominant color from a Bitmap using Palette
private fun extractDominantColor(bitmap: ImageBitmap): Color? {
    val palette = Palette.from(bitmap).generate()
    val dominantSwatch = palette.dominantSwatch!!
    val color = HSL(dominantSwatch.hsl[0], dominantSwatch.hsl[1], dominantSwatch.hsl[2])
    return color.toComposeColor()
}

// Function to calculate the complementary color
private fun getComplementaryColor(color: Color): Color {

    val hsl = RGB(color.red, color.green, color.blue).toHSL()
    return hsl.copy(h = (hsl.h + 180) % 360).toComposeColor()
//
//    val r = 255 - color.red * 255
//    val g = 255 - color.green * 255
//    val b = 255 - color.blue * 255
//    return Color(r / 255f, g / 255f, b / 255f)
}

// Function to extract the average color from a Bitmap
// See https://sighack.com/post/averaging-rgb-colors-the-right-way
private fun extractAverageColor(bitmap: ImageBitmap): Color? {
    var redSum = 0f
    var greenSum = 0f
    var blueSum = 0f
    var pixelCount = 0
    val width = bitmap.width
    val height = bitmap.height
    val skiaBitmap = bitmap.asSkiaBitmap()

    // Loop through all pixels
    for (x in 0 until width) {
        for (y in 0 until height) {
            val color = Color(skiaBitmap.getColor(x, y))
            val alpha = skiaBitmap.getAlphaf(x, y)
            if (alpha > 0f) {
                redSum += color.red
                greenSum += color.green
                blueSum += color.blue
                pixelCount++
            }
        }
    }

    // Calculate average RGB values
    val avgRed = (redSum * 255 / pixelCount).toInt()
    val avgGreen = (greenSum * 255 / pixelCount).toInt()
    val avgBlue = (blueSum * 255 / pixelCount).toInt()
    return Color(avgRed, avgGreen, avgBlue)
}

