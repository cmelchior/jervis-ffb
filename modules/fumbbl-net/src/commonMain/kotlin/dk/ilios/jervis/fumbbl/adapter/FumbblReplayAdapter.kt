package dk.ilios.jervis.fumbbl.adapter

import dk.ilios.jervis.fumbbl.FumbblFileReplayAdapter
import dk.ilios.jervis.fumbbl.net.commands.ServerCommandModelSync
import dk.ilios.jervis.fumbbl.net.commands.ServerCommandReplay
import dk.ilios.jervis.fumbbl.utils.FumbblGame
import dk.ilios.jervis.fumbbl.utils.fromFumbblState
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.serialize.platformFileSystem
import kotlinx.coroutines.runBlocking
import okio.Path
import okio.Path.Companion.toPath

// TODO This approach is ultimately broken. It isn't possible to extract actions
//  from incomplete data. It looks like FUMBBL is sometimes sending the same
//  data multiple times, so figuring out which to convert to actions is impossible
//  without the context of the game.
//
// What else can we do
// Whenever there is a request for action send current Node + actions to a method that will roll forward until it
// finds what it needs... Counter: Will be tricky, some Fumbbl commands will create multiple actions, then they need to
// stored somewhere and depleted (might be possible)
//
// Only other approach would be to replicate the state machine that FUMBBL use and update the FUMBBL metadata.
// This will give us a full state to query. It might be more helpful if others want to convert a FUMBBL state into
// something else as well...but will probably be more work. Hmm, maybe it is just porting the ModelChangeProcessor
// which is just a lot of trivial code.
class FumbblReplayAdapter(private var replayFile: Path) {

    private lateinit var gameCommands: List<JervisActionHolder>
    private lateinit var game: FumbblGame
    private lateinit var jervisGame: Game

    suspend fun loadCommands() {
        val file = platformFileSystem.canonicalize("".toPath()) / (replayFile)
        val modelChangeCommands = loadCommandsFromFile(file)
        processCommands(modelChangeCommands)
    }

    private fun loadCommandsFromFile(file: Path): List<ServerCommandModelSync> {
        val adapter = FumbblFileReplayAdapter(file)
        val commands: MutableList<ServerCommandReplay> = mutableListOf()
        runBlocking {
            adapter.start()
            game = adapter.getGame()
            jervisGame = Game.fromFumbblState(game)
            var isDone = false
            while (!isDone) {
                val cmd = adapter.receive()
                isDone =
                    when (cmd) {
                        is ServerCommandReplay -> cmd.lastCommand
                        else -> false
                    }
                commands.add(cmd)
            }
        }
        adapter.close()

        // Normalize replay to a list of model changes
        return commands.flatMap { replayCommand: ServerCommandReplay ->
            replayCommand.commandArray
        }
    }

    private fun processCommands(commands: List<ServerCommandModelSync>) {
        val chain = MapperChain(jervisGame, game)
        gameCommands = chain.process(commands)
    }

    fun getGame(): Game = jervisGame

    fun getCommands(): List<JervisActionHolder> = gameCommands
}
