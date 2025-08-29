package com.jervisffb.ui.menu

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import com.jervisffb.ui.game.viewmodel.MenuViewModel
import com.jervisffb.ui.menu.components.about.AboutDialogComponent
import com.jervisffb.ui.menu.components.error.ErrorDialogComponent
import com.jervisffb.ui.menu.components.settings.SettingsDialogComponent

// Base screen that all screens should use. This allows us to control global
// dialogs in all screens in single location. E.g., like a Settings screen.
@Composable
fun JervisScreen(menuViewModel: MenuViewModel, content: @Composable () -> Unit) {
    Box() {
        content()
        AboutDialogComponent(menuViewModel)
        SettingsDialogComponent(menuViewModel)
        ErrorDialogComponent(menuViewModel)
    }
}
