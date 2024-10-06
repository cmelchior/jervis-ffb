package com.jervisffb

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.jervisffb.ui.App
import com.jervisffb.ui.viewmodel.MenuViewModel


fun main() =
    application {
        val scale = 1.22f
        val menuViewModel = MenuViewModel()
        val windowState =
            rememberWindowState(
                size = (
                    DpSize(pixelsToDp(145f + 782f + 145f), pixelsToDp(690f)) * scale) // Game content
                    + DpSize(0.dp, pixelsToDp(28f)),  // Window decoration
            )
        Window(onCloseRequest = ::exitApplication, state = windowState) {
            WindowMenuBar(menuViewModel)
            App(menuViewModel)
        }
    }

@Composable
fun pixelsToDp(pixels: Float): Dp {
    val density = LocalDensity.current
    return with(density) { (this.density * pixels).toDp() }
}
