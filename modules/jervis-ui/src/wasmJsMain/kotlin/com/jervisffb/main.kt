package com.jervisffb

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.jervisffb.ui.App
import com.jervisffb.ui.BackNavigationHandler
import com.jervisffb.ui.initApplication
import com.jervisffb.ui.viewmodel.MenuViewModel
import kotlinx.browser.document
import kotlinx.browser.window

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    try {
        clearLoadingScreen()
        initApplication()
        window.onkeydown = { event ->
            if (event.key == "Escape") {
                BackNavigationHandler.execute()
            }
        }
        ComposeViewport(document.body!!) {
            val menuViewModel = MenuViewModel()
            // WindowMenuBar(menuViewModel)
            App(menuViewModel)
        }
    } catch(ex: Throwable) {
        // Work-around for thrown exceptions not showing the root cause in WebAssembly.
        ex.printStackTrace()
        throw ex
    }
}

private fun clearLoadingScreen() {
    document.body?.innerHTML = ""
}
