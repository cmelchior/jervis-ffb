package fumbbl.net.auth

import com.jervisffb.engine.model.Game
import com.jervisffb.engine.rules.StandardBB2020Rules
import com.jervisffb.fumbbl.net.FumbblFileReplayAdapter
import com.jervisffb.fumbbl.net.adapter.FumbblReplayAdapter
import com.jervisffb.fumbbl.net.api.commands.ServerCommandReplay
import com.jervisffb.fumbbl.net.utils.fromFumbblState
import com.jervisffb.utils.platformFileSystem
import kotlinx.coroutines.runBlocking
import okio.Path.Companion.toPath
import org.junit.jupiter.api.Test
import kotlin.test.Ignore

class ReplayFileTests {

    @Test
    @Ignore // Only run manually
    fun readReplayFile() =
        runBlocking {
            val file = platformFileSystem.canonicalize("".toPath()) / ("../../replays/game-1624379.json".toPath())
            val adapter = FumbblFileReplayAdapter(file)
            adapter.start()
            val game = adapter.getGame()
            val jervisGame: Game = Game.fromFumbblState(StandardBB2020Rules, game)
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
    @Ignore // Fouling is broken. We need to revisit How rules are setup. Also, only run manually
    fun convertReplayFileToJervisCommands() =
        runBlocking {
            val rules = StandardBB2020Rules
            val fumbbl = FumbblReplayAdapter("../../replays/game-1624379.json".toPath(), checkCommandsWhenLoading = true)
            runBlocking {
                fumbbl.loadCommands()
            }
        }

    @Test
    @Ignore // See above
    fun convertReplayFileToJervisCommands2() =
        runBlocking {
            val rules = StandardBB2020Rules
            val fumbbl = FumbblReplayAdapter("../../replays/game-1744037.json".toPath(), checkCommandsWhenLoading = true)
            runBlocking {
                fumbbl.loadCommands()
            }
        }
}
