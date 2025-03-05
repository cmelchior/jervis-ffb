package com.jervisffb.ui.menu

// Have a uniform way to handle back navigation.
// See https://github.com/adrielcafe/voyager/issues/287
// This class is not thread safe. Not sure how big of a problem that is.
object BackNavigationHandler {
    private val callbacks = mutableSetOf<OnBackPress>()
    fun register(onBackPress: OnBackPress) {
        callbacks += onBackPress
    }
    fun unregister(onBackPress: OnBackPress) {
        callbacks.remove(onBackPress)
    }
    fun execute() {
        for (callback in callbacks) {
            callback.onBackPressed()
        }
    }
}

fun interface OnBackPress {
    fun onBackPressed()
}
