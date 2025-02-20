package com.jervisffb.ui.menu

import androidx.compose.foundation.layout.Box
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.window.Dialog
import com.jervisffb.ui.game.viewmodel.MenuViewModel

// Base screen that all screens should use. This allows us to control global
// dialogs in all screens in single location. E.g., like a Settings screen.
@Composable
fun JervisScreen(menuViewModel: MenuViewModel, content: @Composable () -> Unit) {
    Box() {
        SettingsDialog(menuViewModel)
        content()

    }
}

@Composable
fun SettingsDialog(menuViewModel: MenuViewModel) {
    val visible: Boolean by menuViewModel.showSettingsDialog().collectAsState()
    if (!visible) return
    Dialog(
        onDismissRequest = { menuViewModel.openSettings(false) },
    ) {
        Text("Hello")
    }
}
