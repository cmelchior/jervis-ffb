package com.jervisffb.test

import com.jervisffb.engine.serialize.JervisSerialization
import okio.Path
import okio.Path.Companion.toPath
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.fail

// TODO Figure out the best way to read test resources in Multiplatform.
//  For now, just keep this test in JVM
class SerializationTests {

    private fun getFile(resourcePath: String): Path {
        val resource = javaClass.getResource(resourcePath)
        return resource.path.toPath()
    }

    @Test
    fun loadGameStateFromFile() {
        // We should probably create the file from scratch so serialization is also tested.
        // Using real files shouldn't be needed until we need some guarantee of stability
        // across versions.
        val gameFile = getFile("/game.jrg")
        val result = JervisSerialization.loadFromFile(gameFile)
        if (!result.isSuccess) {
            fail(result.exceptionOrNull()?.stackTraceToString())
        }
    }

    @Test
    fun failLoadingGameFile() {
        val gameFile = getFile("/invalid-game.jrg")
        val result = JervisSerialization.loadFromFile(gameFile)
        assertTrue(result.isFailure)
    }
}
