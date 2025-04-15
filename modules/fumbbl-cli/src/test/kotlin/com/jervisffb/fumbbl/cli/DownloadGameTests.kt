package com.jervisffb.fumbbl.cli

import com.jervisffb.fumbbl.cli.gamedownloader.DownloadGameRunner
import java.io.File
import kotlin.test.Ignore
import kotlin.test.Test

class DownloadGameTests {

    @Test
    @Ignore("Should only be run manually")
    fun testPrepareDebugClient() {
        val runner = DownloadGameRunner(true)
        runner.run("3222280", File("./"))
    }
}
