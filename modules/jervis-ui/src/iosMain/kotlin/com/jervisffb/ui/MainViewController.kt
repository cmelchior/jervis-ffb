package com.jervisffb.ui

import androidx.compose.ui.window.ComposeUIViewController
import com.jervisffb.ui.game.viewmodel.MenuViewModel

fun MainViewController() = ComposeUIViewController {
    val menuViewModel = MenuViewModel()
    App(menuViewModel)
}
