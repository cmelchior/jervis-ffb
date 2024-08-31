package dk.ilios.jervis.cli

import dk.ilios.jervis.fumbblcli.debugclient.CreateDebugClientRunner
import java.io.File
import kotlin.test.Test

class PrepareDebugClientTests {

    @Test
//    @Ignore("Should only be run manually")
    fun testPrepareDebugClient() {
        val output: File = File("/Users/christian.melchior/Private/ffb-debug-client/libs")
        val runner = CreateDebugClientRunner(getJarFileLocation())
        runner.run(output)
    }

    private fun getJarFileLocation(): File {
        return File(
            this::class.java.getProtectionDomain()
                .codeSource
                .location
                .toURI(),
        )
    }
}
