package com.jervisffb.utils

import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

val DEFAULT_LOG_LEVEL = Severity.Debug

/**
 * Console log writer that prepends a timestamp to every line. Kermit's default
 * console writer has no timestamp, so we replace it with this.
 */
private object TimestampedConsoleWriter : LogWriter() {

    private val timeFormat = LocalTime.Format {
        hour()
        char(':')
        minute()
        char(':')
        second()
        char('.')
        secondFraction(fixedLength = 3)
    }

    private fun formattedNow(): String {
        return Clock.System.now().toLocalDateTime(TimeZone.UTC).time.format(timeFormat)
    }

    override fun log(severity: Severity, message: String, tag: String, throwable: Throwable?) {
        val throwableMsg = if (throwable == null) "" else "\n${throwable.stackTraceToString()}"
        println("${formattedNow()} ${severity.name}: $message$throwableMsg")
    }
}

// This needs to be expanded so we create a logger instance for each type
// since it should also affect the output.
val loggerInstance by lazy {
    Logger.apply {
        setMinSeverity(DEFAULT_LOG_LEVEL)
        // Replace Kermit's default (untimestamped) console writer with our own, then
        // re-attach the platform writer (the JVM log file; `null` on iOS/wasm).
        // setLogWriters (not addLogWriter) is required so the default writer is dropped
        // instead of printing every line twice.
        setLogWriters(listOfNotNull(TimestampedConsoleWriter, getPlatformLogWriter()))
    }
}

// Returns a logger instance for the given class
inline fun <reified T : Any> T.jervisLogger(): Logger = loggerInstance

// Returns a logger instance for top-level functions
fun jervisLogger(): Logger = loggerInstance
