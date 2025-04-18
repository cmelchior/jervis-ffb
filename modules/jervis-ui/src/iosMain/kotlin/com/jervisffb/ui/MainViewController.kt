package com.jervisffb.ui

import androidx.compose.ui.window.ComposeUIViewController
import com.jervisffb.ui.game.viewmodel.MenuViewModel
import com.jervisffb.utils.runBlocking
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController {
    runBlocking {
        initApplication()
    }
    return ComposeUIViewController {
        val menuViewModel = MenuViewModel()
        App(menuViewModel)
    }
}

