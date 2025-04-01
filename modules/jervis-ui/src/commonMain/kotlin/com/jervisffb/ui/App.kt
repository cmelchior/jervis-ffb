package com.jervisffb.ui

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import cafe.adriel.voyager.navigator.CurrentScreen
import cafe.adriel.voyager.navigator.Navigator
import com.jervisffb.ui.game.view.JervisTheme
import com.jervisffb.ui.game.viewmodel.MenuViewModel
import com.jervisffb.ui.menu.BackNavigationHandler
import com.jervisffb.ui.menu.OnBackPress
import com.jervisffb.ui.menu.intro.FrontpageScreen
import com.jervisffb.utils.FileManager
import com.jervisffb.utils.PropertiesManager
import com.jervisffb.utils.initializePlatform
import com.jervisffb.utils.jervisLogger
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

val FILE_MANAGER = FileManager()
val PROPERTIES_MANAGER = PropertiesManager()

fun initApplication() {

    initializePlatform()

    @OptIn(DelicateCoroutinesApi::class)
    GlobalScope.launch {
        if (PROPERTIES_MANAGER.getBoolean("initialized") != true) {
            jervisLogger().i { "initializing application" }
            CacheManager.createInitialTeamFiles()
            PROPERTIES_MANAGER.setProperty("initialized", true)
        } else {
            jervisLogger().i { "Application already initialized. Skipping." }
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
            screen = FrontpageScreen(menuViewModel),
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
