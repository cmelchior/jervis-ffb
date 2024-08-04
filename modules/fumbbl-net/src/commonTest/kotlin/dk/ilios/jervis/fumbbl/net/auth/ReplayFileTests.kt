package dk.ilios.jervis.fumbbl.net.auth

import dk.ilios.jervis.fumbbl.FumbblFileReplayAdapter
import dk.ilios.jervis.fumbbl.FumbblReplayAdapter
import dk.ilios.jervis.fumbbl.net.commands.ServerCommandReplay
import dk.ilios.jervis.fumbbl.platformFileSystem
import dk.ilios.jervis.fumbbl.utils.fromFumbblState
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.rules.BB2020Rules
import kotlinx.coroutines.runBlocking
import okio.Path.Companion.toPath
import org.junit.jupiter.api.Test

class ReplayFileTests {
    @Test
    fun readReplayFile() =
        runBlocking {
            val file = platformFileSystem.canonicalize("".toPath()) / ("../../replays/game-1624379.json".toPath())
            val adapter = FumbblFileReplayAdapter(file)
            adapter.start()
            val game = adapter.getGame()
            val jervisGame: Game = Game.fromFumbblState(game)
            var isDone = false
            while (!isDone) {
                val cmd = adapter.receive()
                isDone =
                    when (cmd) {
                        is ServerCommandReplay -> cmd.lastCommand
                        else -> false
                    }
            }
            adapter.close()
        }

    @Test
    fun convertReplayFileToJervisCommands() =
        runBlocking {
            val rules = BB2020Rules
            val fumbbl = FumbblReplayAdapter("../../replays/game-1624379.json".toPath())
            runBlocking {
                fumbbl.loadCommands()
            }
        }
}
