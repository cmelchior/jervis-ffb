package com.jervisffb.ui.menu

// Have a uniform way to handle back navigation.
// See https://github.com/adrielcafe/voyager/issues/287
// This class is not thread safe. Not sure how big of a problem that is.
object BackNavigationHandler {
    private val callbacks = mutableListOf<OnBackPress>()
    fun register(onBackPress: OnBackPress) {
        callbacks += onBackPress
    }
    fun unregister(onBackPress: OnBackPress) {
        callbacks.remove(onBackPress)
    }
    fun execute() {
        // Iterate from the back, so we give the highest priority
        // to callbacks added last.
        for (index in callbacks.indices.reversed()) {
            val callback: OnBackPress = callbacks[index]
            if (callback.onBackPressed()) {
                break
            }
        }
    }
}

fun interface OnBackPress {
    // Return true if callback consumed the event, which will stop
    // propagating it.
    fun onBackPressed(): Boolean
}
