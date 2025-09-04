package com.jervisffb

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.ComposeViewport
import com.jervisffb.ui.App
import com.jervisffb.ui.game.view.JervisTheme
import com.jervisffb.ui.game.viewmodel.MenuViewModel
import com.jervisffb.ui.initApplication
import com.jervisffb.ui.menu.BackNavigationHandler
import kotlinx.browser.document
import kotlinx.browser.window

@OptIn(ExperimentalComposeUiApi::class)
suspend fun main() {
    try {
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
        ComposeViewport(document.body!!) {
            OnBrowserResize { sizeDp, sizePx ->
                JervisTheme.notifyWindowsSizeChange(sizeDp, sizePx)
            }
            App(menuViewModel)
        }
    } catch (ex: Throwable) {
        // Work-around for thrown exceptions not showing the root cause in WebAssembly.
        ex.printStackTrace()
        throw ex
    }
}

// Remove all the loading screen elements from the DOM.
// They are defined directly inside `index.html`.
private fun clearLoadingScreen() {
    document.body?.innerHTML = ""
}

@Composable
private fun OnBrowserResize(onResize: (sizeDp: DpSize, sizePx: Size) -> Unit) {
    val density = LocalDensity.current
    DisposableEffect(Unit) {
        val listener: (org.w3c.dom.events.Event) -> Unit = {
            val sizePx = Size(window.innerWidth.toFloat(), window.innerHeight.toFloat())
            val sizeDp = with(density) {
                val widthDp = window.innerWidth.dp
                val heightDp = window.innerHeight.dp
                DpSize(widthDp, heightDp)
            }
            onResize(sizeDp, sizePx)
        }
        window.addEventListener("resize", listener)
        listener.invoke(org.w3c.dom.events.Event("resize"))
        onDispose {
            window.removeEventListener("resize", listener)
        }
    }
}
