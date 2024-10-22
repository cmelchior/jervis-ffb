package com.jervisffb.ui

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.navigator.Navigator
import com.jervisffb.ui.screen.IntroScreen
import com.jervisffb.ui.viewmodel.MenuViewModel

@Composable
fun App(menuViewModel: MenuViewModel) {
    Navigator(IntroScreen(menuViewModel))
}
