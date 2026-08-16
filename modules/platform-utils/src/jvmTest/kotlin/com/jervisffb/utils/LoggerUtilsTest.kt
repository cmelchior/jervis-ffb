package com.jervisffb.utils

import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class LoggerUtilsTest {

    @Test
    fun warningLogWriterOnlyCollectsBetweenStartAndStop() {
        val logger = jervisLogger()

        // Starts empty
        assertTrue(WarningLogWriter.warnings.isEmpty())

        // Does not log anything if not started
        logger.w("warning before game")
        assertTrue(WarningLogWriter.warnings.isEmpty())

        // Start logger
        WarningLogWriter.start()
        logger.w("warning starting game")
        assertNotNull(WarningLogWriter.warnings.single { it.message == "warning starting game" })

        // Stop logger
        assertNotNull(WarningLogWriter.stop().single { it.message == "warning starting game" })
        assertTrue(WarningLogWriter.warnings.isEmpty())

        // No logs after it was stopped
        logger.w("warning after game")
        assertTrue(WarningLogWriter.warnings.isEmpty())
    }
}
