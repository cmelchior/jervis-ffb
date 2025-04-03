package com.jervisffb

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.jervisffb.ui.pixelsToDp
import java.awt.Desktop
import java.awt.desktop.AboutEvent
import java.awt.desktop.AboutHandler


fun main() {

// TODO Create a better About page
//    java.awt.Desktop.getDesktop().setAboutHandler {
//
//        val versionLabel = JLabel("<html><b>Version: 0.1.0.dev.local (dev build)<b></html>").apply {
//            horizontalAlignment = SwingConstants.CENTER
//            alignmentX = Component.CENTER_ALIGNMENT
//            font = Font("SansSerif", Font.PLAIN, 12)
//        }
//
//        val contentPanel = JPanel().apply {
//            border = EmptyBorder(12, 12, 12, 12)
//            layout = BoxLayout(this, BoxLayout.Y_AXIS)
//            alignmentX = JPanel.CENTER_ALIGNMENT
//
////            add(iconLabel)
//            add(Box.createVerticalStrut(10)) // Adds spacing
//            add(versionLabel)
//            add(Box.createVerticalStrut(5))
////            add(descriptionLabel)
//        }
//
//        val frame = JFrame("About Jervis Fantasy Football")
//        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
//        frame.contentPane = contentPanel
//        frame.pack()
//        frame.isVisible = true
//        frame.isResizable = false
//    }
    initApplication()
    try {
        application {
            val menuViewModel = MenuViewModel()
            var showAbout by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) {
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().setAboutHandler(object : AboutHandler {
                        override fun handleAbout(e: AboutEvent?) {
                            menuViewModel.showAboutDialog(true)
                        }
                    })
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
    }
}

