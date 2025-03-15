package com.jervisffb.ui.menu

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import com.jervisffb.ui.game.view.SettingsDialog
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
