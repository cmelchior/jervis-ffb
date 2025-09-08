package com.jervisffb.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.toSize
import cafe.adriel.voyager.navigator.CurrentScreen
import cafe.adriel.voyager.navigator.Navigator
import com.jervis.generated.resetSettings
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
import com.jervisffb.utils.SettingsManager
import com.jervisffb.utils.initializePlatform
import com.jervisffb.utils.jervisLogger

val FILE_MANAGER = FileManager()
val SETTINGS_MANAGER: SettingsManager = SettingsManager()

suspend fun initApplication() {
    initializePlatform()
    val LOG = jervisLogger()

    // For now, we re-initialize the default teams for every new version. This is done because
    //  we are still iterating on the file format and serialization format. Once these are
    // stable, we should avoid this.
    //
    // For local dev builds (those ending with .local), we always clear the properties to make the DevEx nicer.
    val clientReleaseVersion = BuildConfig.releaseVersion
    val isClientInitialized = SETTINGS_MANAGER.getBoolean(PROP_INITIALIZED, false)
    val initializedVersion = SETTINGS_MANAGER.getStringOrNull(PROP_INITIALIZED_VERSION)
    val isLocalBuild = BuildConfig.releaseVersion.endsWith(".local")
    if (!isClientInitialized || initializedVersion != clientReleaseVersion || isLocalBuild) {
        LOG.i { "Initializing application: $clientReleaseVersion" }
        CacheManager.createInitialTeamFiles()
        CacheManager.createInitialSetupFiles()
        resetSettings(SETTINGS_MANAGER)
        SETTINGS_MANAGER[PROP_INITIALIZED] = true
        SETTINGS_MANAGER[PROP_INITIALIZED_VERSION] = clientReleaseVersion
    } else {
        LOG.i { "Application already initialized. Skipping." }
    }

    // Populate FUMBBL image mapping, so `IconFactory` knows where to download
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

/**
 * Composable used to calculate the size of the game window across all platforms.
 */
@Composable
fun rememberWindowSize() {
    val density = LocalDensity.current
    Box(
        Modifier
            .fillMaxSize()
            .onSizeChanged { windowSize ->
                // Update Jervis Theme, when the window re-sizes. This is needed so we can scale other
                // UI elements correctly
                val sizePx = windowSize
                val sizeDp = with(density) {
                    DpSize(sizePx.width.toDp(), sizePx.height.toDp())
                }
                JervisTheme.notifyWindowsSizeChange(density, sizeDp, sizePx.toSize())
            }
    )
}

@Composable
fun App(menuViewModel: MenuViewModel) {
    val windowSize = rememberWindowSize()
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
                    // On the Game screen, we intercept this and show the game menu instead
                    // of directly moving back the navigation stack.
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
