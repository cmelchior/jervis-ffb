package com.jervisffb.utils

import dev.hydraulic.conveyor.control.SoftwareUpdateController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual object AppUpdater {

    actual val platformSupportAutoUpdate: Boolean = true
    private var updateInProgress = false
    private val _isUpdateAvailable = MutableStateFlow(false)
    actual val isUpdateAvailable: StateFlow<Boolean> = _isUpdateAvailable
    private val _isUpdateInProgress = MutableStateFlow(false)
    actual val isUpdateInProgress: StateFlow<Boolean> = _isUpdateInProgress
    private val controller: SoftwareUpdateController? = SoftwareUpdateController.getInstance()

    actual fun checkForUpdate(scope: CoroutineScope, resultFunc: (Boolean) -> Unit) {
        scope.launch(Dispatchers.Default) {
            try {
                val currentVersion: SoftwareUpdateController.Version? = controller?.currentVersion
                val latestVersion: SoftwareUpdateController.Version? = controller?.currentVersionFromRepository

                // Something went wrong when checking for versions
                if (currentVersion == null || latestVersion == null) {
                    _isUpdateAvailable.value = false
                    resultFunc(false)
                } else {
                    val isUpdateAvailable = (latestVersion > currentVersion)
                    _isUpdateAvailable.value = isUpdateAvailable
                    updateInProgress = if (isUpdateAvailable) false else updateInProgress
                    resultFunc(isUpdateAvailable)
                }
            } catch (e: Exception) {
                jervisLogger().w {
                    "Error checking for updates: ${e.message}"
                }
                resultFunc(false)
            }
        }
    }

    actual fun updateNow() {
        // A newer version is available
        val updateAvailability = controller?.canTriggerUpdateCheckUI()
        if (updateAvailability != SoftwareUpdateController.Availability.AVAILABLE) {
            jervisLogger().e {
                "Update is not available. Update availability: $updateAvailability"
            }
        } else {
            try {
                // Make sure to save all user data before calling this method
                controller.triggerUpdateCheckUI()
                _isUpdateInProgress.value = true
            } catch (e: Exception) {
                jervisLogger().e(e) {
                    "Error triggering update"
                }
            }
        }
    }
}
