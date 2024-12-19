package com.jervisffb.utils

import co.touchlab.kermit.Logger

// Returns a logger instance for the given class
inline fun <reified T : Any> T.jervisLogger(): Logger = Logger

// Returns a logger instance for top-level functions
fun jervisLogger(): Logger = Logger
