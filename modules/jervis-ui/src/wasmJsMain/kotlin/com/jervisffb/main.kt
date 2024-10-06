package com.jervisffb

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.jervisffb.ui.App
import com.jervisffb.ui.viewmodel.MenuViewModel
import kotlinx.browser.document

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    try {
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
