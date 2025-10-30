package com.jervisffb.utils

import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity

val DEFAULT_LOG_LEVEL = Severity.Debug

// This needs to be expanded so we create a logger instance for each type
// since it should also affect the output.
val loggerInstance by lazy {
    Logger.apply {
        setMinSeverity(DEFAULT_LOG_LEVEL)
        getPlatformLogWriter()?.let { logger ->
            addLogWriter(logger)
        }
    }
}

// Returns a logger instance for the given class
inline fun <reified T : Any> T.jervisLogger(): Logger = loggerInstance

// Returns a logger instance for top-level functions
fun jervisLogger(): Logger = loggerInstance
