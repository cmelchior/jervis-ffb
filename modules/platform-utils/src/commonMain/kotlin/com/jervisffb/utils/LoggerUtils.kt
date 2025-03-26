package com.jervisffb.utils

import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity

// Returns a logger instance for the given class
inline fun <reified T : Any> T.jervisLogger(): Logger = Logger.apply {
    setMinSeverity(Severity.Debug)
}

// Returns a logger instance for top-level functions
fun jervisLogger(): Logger = Logger.apply {
    setMinSeverity(Severity.Debug)
}
