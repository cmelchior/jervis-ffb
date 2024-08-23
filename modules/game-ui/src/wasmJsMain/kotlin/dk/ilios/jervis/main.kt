package dk.ilios.jervis

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import dk.ilios.jervis.ui.App
import dk.ilios.jervis.ui.viewmodel.MenuViewModel
import kotlinx.browser.document

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    ComposeViewport(document.body!!) {
        val menuViewModel = MenuViewModel()
        // WindowMenuBar(menuViewModel)
        App(menuViewModel)
    }
}
