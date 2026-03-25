@file:OptIn(ExperimentalWasmJsInterop::class)

package com.jervisffb

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.jervisffb.ui.App
import com.jervisffb.ui.IssueTracker
import com.jervisffb.ui.game.viewmodel.MenuViewModel
import com.jervisffb.ui.initApplication
import com.jervisffb.ui.menu.BackNavigationHandler
import kotlinx.browser.document
import kotlinx.browser.window

@OptIn(ExperimentalComposeUiApi::class)
suspend fun main() {
    initApplication()
    val menuViewModel = MenuViewModel()
    clearLoadingScreen()
    window.onkeydown = { event ->
        if (event.key == "Escape") {
            BackNavigationHandler.execute()
        }
        // TODO How to handle shortcuts in general here?
        //  Should we capture all keybinds in the browser, and how do we
        //  keep them in sync with the JVM keybinds? For now, just capture
        //  Undo, which is by far the most important.
        if ((event.ctrlKey || event.metaKey) && event.key.lowercase() == "z") {
            event.preventDefault() // Stop propagating into browser Undo
            menuViewModel.undoAction()
        }
    }
    // This only seems to work in Compose 1.9.0+
    // See https://developer.mozilla.org/en-US/docs/Web/API/Window/error_event
    window.onerror = { message, source, lineno, colno, error ->
        error?.toThrowableOrNull()?.let {
            IssueTracker.saveUncaughtException(it)
        }
        error
    }
    ComposeViewport(document.body!!) {
        App(menuViewModel)
    }
}

// Remove all the loading screen elements from the DOM.
// They are defined directly inside `index.html`.
private fun clearLoadingScreen() {
    document.body?.innerHTML = ""
}
