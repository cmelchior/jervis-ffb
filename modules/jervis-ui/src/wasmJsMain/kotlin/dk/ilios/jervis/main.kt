package dk.ilios.jervis

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import dk.ilios.jervis.ui.App
import dk.ilios.jervis.ui.viewmodel.MenuViewModel
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
