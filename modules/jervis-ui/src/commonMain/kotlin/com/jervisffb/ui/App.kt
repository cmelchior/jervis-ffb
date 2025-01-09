package com.jervisffb.ui

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import cafe.adriel.voyager.navigator.CurrentScreen
import cafe.adriel.voyager.navigator.Navigator
import com.jervisffb.ui.screen.IntroScreen
import com.jervisffb.ui.view.JervisTheme
import com.jervisffb.ui.viewmodel.MenuViewModel
import com.jervisffb.utils.FileManager
import com.jervisffb.utils.PropertiesManager
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

val FILE_MANAGER = FileManager()
val PROPERTIES_MANAGER = PropertiesManager()

fun initApplication() {
    GlobalScope.launch {
        if (PROPERTIES_MANAGER.getBoolean("initialized") != true) {
            println("Initializing application...")
            CacheManager.createInitialTeamFiles()
            PROPERTIES_MANAGER.setProperty("initialized", true)
        } else {
            println("Application already initialized.")
        }
    }
}

// Apply the custom theme
@Composable
fun MyAppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colors = MaterialTheme.colors.copy(
            primary = JervisTheme.homeTeamColor,
        ),
        typography = MaterialTheme.typography.copy(),
        shapes = MaterialTheme.shapes.copy(),
        content = content
    )
}

@Composable
fun App(menuViewModel: MenuViewModel) {
    MyAppTheme {
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
}
