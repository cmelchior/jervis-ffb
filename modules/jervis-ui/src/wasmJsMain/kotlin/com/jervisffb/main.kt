package com.jervisffb

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.jervisffb.ui.App
import com.jervisffb.ui.game.viewmodel.MenuViewModel
import com.jervisffb.ui.initApplication
import com.jervisffb.ui.menu.BackNavigationHandler
import kotlinx.browser.document
import kotlinx.browser.window

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val menuViewModel = MenuViewModel()
    try {
        clearLoadingScreen()
        initApplication()
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
        ComposeViewport(document.body!!) {
            // WindowMenuBar(menuViewModel)
            App(menuViewModel)
        }
    } catch (ex: Throwable) {
        // Work-around for thrown exceptions not showing the root cause in WebAssembly.
        ex.printStackTrace()
        throw ex
    }
}

private fun clearLoadingScreen() {
    document.body?.innerHTML = ""
}
