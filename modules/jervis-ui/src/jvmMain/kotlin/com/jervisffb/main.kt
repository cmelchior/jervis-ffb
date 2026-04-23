package com.jervisffb

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.jervisffb.ui.App
import com.jervisffb.ui.IssueTracker
import com.jervisffb.ui.game.viewmodel.MenuViewModel
import com.jervisffb.ui.initApplication
import com.jervisffb.ui.menu.BackNavigationHandler
import com.jervisffb.ui.utils.pixelsToDp
import com.jervisffb.utils.hasMacKeyboard
import com.jervisffb.utils.runBlocking
import java.awt.Desktop


fun main() = runBlocking {
    // Save all uncaught exceptions so they can be reported on next startup
    Thread.setDefaultUncaughtExceptionHandler { _, throwable ->
        IssueTracker.saveUncaughtException(throwable)
    }

    initApplication()
    application {
        val menuViewModel = remember { MenuViewModel() }

        // Register an system "About" page on platforms that support it (Only Desktop for now).
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

        // This setup mirrors the size of the current FUMBBL Client, but make it slightly larger.
        // The default should probably be to open in full screen instead, and if minimized,
        // resize to either 16:9 or 16:10 depending on the aspect ratio of the screen.
        val scale = 1.22f
        val width = 145f + 782f + 145f
        val height = 690f
        val windowState =
            rememberWindowState(
                size = (
                    DpSize(pixelsToDp(width), pixelsToDp(height)) * scale) // Game content
                    + DpSize(0.dp, pixelsToDp(28f)),  // Window decoration
            )

        // When going into fullscreen mode, we want to be able to restore the previous size.
        var sizeBeforeFullscreen by remember { mutableStateOf(windowState.size) }

        // We need to re-create the Compose and create a new AWT Frame as changing `undecorated` cannot be done once
        // the frame is visible.
        val isFullscreen = (windowState.placement == WindowPlacement.Fullscreen)
        key(isFullscreen) {
            Window(
                onCloseRequest = ::exitApplication,
                state = windowState,
                onKeyEvent = { event ->
                    if (event.type == KeyEventType.KeyDown) {
                        val isFullscreenShortcut = detectFullscreenShortcut(event)
                        when {
                            isFullscreenShortcut -> {
                                if (windowState.placement == WindowPlacement.Fullscreen) {
                                    windowState.placement = WindowPlacement.Floating
                                    windowState.size = sizeBeforeFullscreen
                                } else {
                                    sizeBeforeFullscreen = windowState.size
                                    windowState.placement = WindowPlacement.Fullscreen
                                }
                                true
                            }
                            // Also allow Esc to be used to exit fullscreen as that is a natural "oh shit"-button
                            event.key == Key.Escape && windowState.placement == WindowPlacement.Fullscreen -> {
                                windowState.placement = WindowPlacement.Floating
                                windowState.size = sizeBeforeFullscreen
                                true
                            }
                            event.key == Key.Escape -> {
                                BackNavigationHandler.execute()
                                true
                            }
                            else -> false
                        }
                    } else {
                        false
                    }
                },
                // On Mac, native fullscreen handles decoration automatically.
                // On Windows/Linux, we remove decorations manually when entering fullscreen.
                undecorated = !hasMacKeyboard() && isFullscreen,
                title = "Jervis Fantasy Football"
            ) {
                // Hide the Window tool bar for now. Ideally, the UI should be work-able across all platforms,
                // and having a Window Toolbar goes against that goal. However, there might be good reasons for
                // having one, so do not completely remove it yet.
                // WindowMenuBar(menuViewModel)
                App(menuViewModel)
            }
        }
    }
}

// Defaults for switching to Fullscreen mode differ slightly between platforms.
// It is unclear if we can detect if people have mapped these default keys to something else,
// so for now, just assume the default shortcuts.
private fun detectFullscreenShortcut(event: KeyEvent): Boolean {
    return when (hasMacKeyboard()) {
        true -> event.isCtrlPressed && event.isMetaPressed && event.key == Key.F
        false -> event.key == Key.F11 // Use same shortcut on Linux and Windows
    }
}

