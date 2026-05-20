package com.jervisffb

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import cafe.adriel.voyager.core.screen.Screen
import com.jervisffb.ui.App
import com.jervisffb.ui.game.viewmodel.MenuViewModel
import com.jervisffb.ui.initApplication
import com.jervisffb.ui.menu.BackNavigationHandler
import kotlinx.browser.document
import kotlinx.browser.window

@OptIn(ExperimentalComposeUiApi::class)
suspend fun main() {
    initApplication()
    val menuViewModel = MenuViewModel()
    window.onkeydown = { event ->
        if (event.key == "Escape") {
            BackNavigationHandler.execute()
        }
        if ((event.ctrlKey || event.metaKey) && event.key.lowercase() == "z") {
            event.preventDefault() // Stop propagating into browser Undo
            menuViewModel.undoAction()
        }
    }
    ComposeViewport(document.body!!) {
        var savedScreenStack by remember { mutableStateOf<List<Screen>>(emptyList()) }
        App(
            menuViewModel = menuViewModel,
            initialScreens = savedScreenStack,
            onSaveScreenStack = { savedScreenStack = it }
        )
    }
}
