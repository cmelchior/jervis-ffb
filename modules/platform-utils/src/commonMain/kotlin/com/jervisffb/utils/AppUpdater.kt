package com.jervisffb.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

/**
 * Class wrapping capabilities for app update checks and notifications.
 * For now, this is mostly to wrap Conveyour functionality on JVM.
 */
@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect object AppUpdater {
    // Returns whether the platform supports auto updating the client.
    val platformSupportAutoUpdate: Boolean
    // Returns whether an update is available.
    val isUpdateAvailable: StateFlow<Boolean>
    // Returns true if an update is currently in progress.
    val isUpdateInProgress: StateFlow<Boolean>
    // Run a manual check for new updates. `resultFunc` will return the result
    // of the check. `false` will be returned in case of any network errors.
    fun checkForUpdate(scope: CoroutineScope, resultFunc: (Boolean) -> Unit)
    // This will trigger what-ever platfor specific update is required. Most likely the app
    // will restart
    fun updateNow()
}
