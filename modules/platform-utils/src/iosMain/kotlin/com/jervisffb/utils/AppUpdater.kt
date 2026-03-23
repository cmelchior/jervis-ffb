package com.jervisffb.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

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
