package com.jervisffb

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.jervisffb.ui.App
import com.jervisffb.ui.game.viewmodel.MenuViewModel
import com.jervisffb.ui.initApplication
import com.jervisffb.ui.menu.BackNavigationHandler
import com.jervisffb.ui.utils.pixelsToDp
import com.jervisffb.utils.runBlocking
import java.awt.Desktop


fun main() = runBlocking {
    try {
        initApplication()
        application {
            val menuViewModel = MenuViewModel()
            LaunchedEffect(Unit) {
                if (Desktop.isDesktopSupported()) {
                    val desktop = Desktop.getDesktop()
                    if (desktop.isSupported(Desktop.Action.APP_ABOUT)) {
                        desktop.setAboutHandler {
                            menuViewModel.showAboutDialog(true)
                        }
                    }
                }
            }
            val scale = 1.22f
            val windowState =
                rememberWindowState(
                    size = (
                        DpSize(pixelsToDp(145f + 782f + 145f), pixelsToDp(690f)) * scale) // Game content
                        + DpSize(0.dp, pixelsToDp(28f)),  // Window decoration
                )
            Window(
                onCloseRequest = ::exitApplication,
                state = windowState,
                onKeyEvent = { event ->
                    if (event.key == Key.Escape && event.type == KeyEventType.KeyDown) {
                        BackNavigationHandler.execute()
                        true
                    } else {
                        false
                    }
                },
                title = "Jervis Fantasy Football"
            ) {
                WindowMenuBar(menuViewModel)
                App(menuViewModel)
            }
        }
    } catch (ex: Throwable) {
        // TODO Show crash dialog
        throw ex
    }
}

