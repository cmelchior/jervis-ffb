package com.jervisffb.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Not supported right now, since our `index.html` already has something
 * similar, we should be able to move it here as well.
 */
@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual object AppUpdater {
    actual val platformSupportAutoUpdate: Boolean = false
    actual val isUpdateAvailable: StateFlow<Boolean> = MutableStateFlow(false)
    actual val isUpdateInProgress: StateFlow<Boolean> = MutableStateFlow(false)
    actual fun checkForUpdate(scope: CoroutineScope, resultFunc: (Boolean) -> Unit) {
        // Do nothing
    }
    actual fun updateNow() { /* Do nothing */ }
}
