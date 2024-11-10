package com.jervisffb.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import cafe.adriel.voyager.navigator.CurrentScreen
import cafe.adriel.voyager.navigator.Navigator
import com.jervisffb.ui.screen.IntroScreen
import com.jervisffb.ui.viewmodel.MenuViewModel

@Composable
fun App(menuViewModel: MenuViewModel) {
    Navigator(
        screen = IntroScreen(menuViewModel),
        onBackPressed = {
            BackNavigationHandler.execute()
            true
        }
    ) { navigator ->
        DisposableEffect(navigator) {
            val observer = OnBackPress {
                // TODO Figure out how to handle accidential pressing Escape during
                //  a game. Can we intercept it directly in the Game Screen instead?
                navigator.pop()
            }
            BackNavigationHandler.register(observer)
            onDispose {
                BackNavigationHandler.unregister(observer)
            }
        }
        CurrentScreen()
    }
}
