package com.jervisffb

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
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
import cafe.adriel.voyager.core.screen.Screen
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
        val defaultWindowSize =
            (DpSize(pixelsToDp(width), pixelsToDp(height)) * scale) +
                DpSize(0.dp, pixelsToDp(28f))

        var sizeBeforeFullscreen by remember { mutableStateOf(defaultWindowSize) }
        var savedScreenStack by remember { mutableStateOf<List<Screen>>(emptyList()) }

        @Composable
        fun AppContent() {
            App(
                menuViewModel = menuViewModel,
                initialScreens = savedScreenStack,
                onSaveScreenStack = { savedScreenStack = it }
            )
        }

        if (hasMacKeyboard()) {
            // On macOS, native fullscreen handles decorations automatically, so there is no
            // need to recreate the Window. Both the shortcut and the green maximize button
            // work via windowState.placement, so they are always consistent with each other.
            val windowState = rememberWindowState(size = defaultWindowSize)
            LaunchedEffect(windowState) {
                snapshotFlow { windowState.placement to windowState.size }
                    .collect { (placement, size) ->
                        if (placement == WindowPlacement.Floating) {
                            sizeBeforeFullscreen = size
                        }
                    }
            }
            Window(
                onCloseRequest = ::exitApplication,
                state = windowState,
                onKeyEvent = { event ->
                    if (event.type == KeyEventType.KeyDown) {
                        when {
                            detectFullscreenShortcut(event) -> {
                                if (windowState.placement == WindowPlacement.Fullscreen) {
                                    windowState.placement = WindowPlacement.Floating
                                    windowState.size = sizeBeforeFullscreen
                                } else {
                                    windowState.placement = WindowPlacement.Fullscreen
                                }
                                true
                            }
                            event.key == Key.Escape -> BackNavigationHandler.execute()
                            else -> false
                        }
                    } else false
                },
                title = "Jervis Fantasy Football"
            ) {
                AppContent()
            }
        } else {
            // On Windows/Linux, `undecorated` cannot change on a visible AWT frame, so we must
            // recreate the Window when toggling fullscreen mode.
            var isFullscreen by remember { mutableStateOf(false) }
            key(isFullscreen) {
                val windowState = rememberWindowState(
                    placement = if (isFullscreen) WindowPlacement.Fullscreen else WindowPlacement.Floating,
                    size = sizeBeforeFullscreen
                )
                LaunchedEffect(windowState) {
                    snapshotFlow { windowState.placement to windowState.size }
                        .collect { (placement, size) ->
                            if (placement == WindowPlacement.Floating) {
                                sizeBeforeFullscreen = size
                            }
                        }
                }
                Window(
                    onCloseRequest = ::exitApplication,
                    state = windowState,
                    onKeyEvent = { event ->
                        if (event.type == KeyEventType.KeyDown) {
                            when {
                                detectFullscreenShortcut(event) -> { isFullscreen = !isFullscreen; true }
                                event.key == Key.Escape -> BackNavigationHandler.execute()
                                else -> false
                            }
                        } else false
                    },
                    undecorated = isFullscreen,
                    title = "Jervis Fantasy Football"
                ) {
                    AppContent()
                }
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

