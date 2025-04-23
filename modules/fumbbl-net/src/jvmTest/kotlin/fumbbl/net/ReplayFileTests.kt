package fumbbl.net

import com.jervisffb.fumbbl.net.FumbblFileReplayAdapter
import com.jervisffb.fumbbl.net.adapter.FumbblReplayAdapter
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
            var isDone = false
            while (!isDone) {
                val cmd = adapter.receive()
                isDone = cmd.lastCommand
            }
            adapter.close()
        }

    @Test
    @Ignore // Fouling is broken. We need to revisit How rules are setup. Also, only run manually
    fun convertReplayFileToJervisCommands() =
        runBlocking {
            val fumbbl = FumbblReplayAdapter("../../replays/game-1624379.json".toPath(), checkCommandsWhenLoading = true)
            runBlocking {
                fumbbl.loadCommands()
            }
        }

    @Test
    @Ignore // See above
    fun convertReplayFileToJervisCommands2() =
        runBlocking {
            val fumbbl = FumbblReplayAdapter("../../replays/game-1744037.json".toPath(), checkCommandsWhenLoading = true)
            runBlocking {
                fumbbl.loadCommands()
            }
        }
}
