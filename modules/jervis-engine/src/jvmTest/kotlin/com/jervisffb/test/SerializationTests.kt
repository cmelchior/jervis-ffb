package com.jervisffb.test

import com.jervisffb.engine.GameEngineController
import com.jervisffb.engine.rules.StandardBB2020Rules
import com.jervisffb.engine.serialize.FILE_EXTENSION_GAME_FILE
import com.jervisffb.engine.serialize.JervisSerialization
import okio.Path
import okio.Path.Companion.toPath
import kotlin.io.path.createTempFile
import kotlin.io.path.deleteIfExists
import kotlin.io.path.pathString
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.fail

// TODO Figure out the best way to read test resources in Multiplatform.
//  For now, just keep this test in JVM
class SerializationTests {

    // Helper function for loading a file from the `/resources` folder
    private fun getFile(resourcePath: String): Path {
        val resource = javaClass.getResource(resourcePath)
        return resource.path.toPath()
    }

    @Test
    fun loadGameStateFromFile() {
        val game = createDefaultGameState(StandardBB2020Rules())
        val controller = GameEngineController(game)
        controller.startManualMode(false)
        val gameFile = createTempFile("test-game-state-file", suffix = FILE_EXTENSION_GAME_FILE).pathString.toPath()
        try {
            JervisSerialization.saveToFile(controller, gameFile)
            val result = JervisSerialization.loadFromFile(gameFile)
            if (!result.isSuccess) {
                fail(result.exceptionOrNull()?.stackTraceToString())
            }
        } finally {
            gameFile.toNioPath().deleteIfExists()
        }
    }

    @Test
    fun failLoadingGameFile() {
        val gameFile = createTempFile("empty-file", suffix = FILE_EXTENSION_GAME_FILE).pathString.toPath()
        try {
            val result = JervisSerialization.loadFromFile(gameFile)
            assertTrue(result.isFailure)
        } finally {
            gameFile.toNioPath().deleteIfExists()
        }
    }
}
