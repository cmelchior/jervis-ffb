package com.jervisffb.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import cafe.adriel.voyager.navigator.CurrentScreen
import cafe.adriel.voyager.navigator.Navigator
import com.jervisffb.BuildConfig
import com.jervisffb.ui.game.icons.IconFactory
import com.jervisffb.ui.game.view.JervisTheme
import com.jervisffb.ui.game.viewmodel.MenuViewModel
import com.jervisffb.ui.game.viewmodel.Setups
import com.jervisffb.ui.menu.BackNavigationHandler
import com.jervisffb.ui.menu.OnBackPress
import com.jervisffb.ui.menu.intro.FrontpageScreen
import com.jervisffb.utils.FileManager
import com.jervisffb.utils.PROP_INITIALIZED
import com.jervisffb.utils.PROP_INITIALIZED_VERSION
import com.jervisffb.utils.PropertiesManager
import com.jervisffb.utils.initializePlatform
import com.jervisffb.utils.jervisLogger

val FILE_MANAGER = FileManager()
val PROPERTIES_MANAGER = PropertiesManager()

suspend fun initApplication() {
    initializePlatform()
    val LOG = jervisLogger()

    // For now, we re-initialize the default teams for every new version. This is done because
    //  we are still iterating on the file format and serialization format. Once these are
    // stable, we should avoid this.
    val clientReleaseVersion = BuildConfig.releaseVersion
    val isClientInitialized = PROPERTIES_MANAGER.getBoolean(PROP_INITIALIZED) ?: false
    val initializedVersion = PROPERTIES_MANAGER.getString(PROP_INITIALIZED_VERSION)
    if (!isClientInitialized || initializedVersion != clientReleaseVersion) {
        LOG.i { "Initializing application: $clientReleaseVersion" }
        CacheManager.createInitialTeamFiles()
        CacheManager.createInitialSetupFiles()
        PROPERTIES_MANAGER.setProperty(PROP_INITIALIZED, true)
        PROPERTIES_MANAGER.setProperty(PROP_INITIALIZED_VERSION, clientReleaseVersion)
    } else {
        LOG.i { "Application already initialized. Skipping." }
    }

    // Populate FUMMBL image mapping, so `IconFactory` knows where to download
    // images from.
    IconFactory.initializeFumbblMapping()

    // Initialize setup cache
    Setups.initialize()
}

// Apply the custom theme
@Composable
fun MyAppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = MaterialTheme.colorScheme.copy(
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
