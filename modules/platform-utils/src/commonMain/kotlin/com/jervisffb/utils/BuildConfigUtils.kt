package com.jervisffb.utils

import com.jervisffb.BuildConfig

// Returns `true` if the app is running in a local/debug, `false` otherwise.
val BuildConfig.isLocalBuild: Boolean
    get() = BuildConfig.releaseVersion.endsWith(".local")
