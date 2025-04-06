package com.jervisffb.utils

import co.touchlab.kermit.Logger
import co.touchlab.kermit.Logger.Companion.setMinSeverity
import co.touchlab.kermit.Severity

// This needs to be expanded so we create a logger instance for each type
// since it should also affect the output.
val loggerInstace by lazy {
    Logger.apply {
        setMinSeverity(Severity.Debug)
        getPlatformLogWriter()?.let { logger ->
            addLogWriter(logger)
        }
    }
}

// Returns a logger instance for the given class
inline fun <reified T : Any> T.jervisLogger(): Logger = loggerInstace

// Returns a logger instance for top-level functions
fun jervisLogger(): Logger = loggerInstace
