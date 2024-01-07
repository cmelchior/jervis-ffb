package dk.ilios.jervis.ui

import dk.ilios.jervis.actions.ActionDescriptor
import dk.ilios.jervis.actions.Continue
import dk.ilios.jervis.actions.GameAction
import dk.ilios.jervis.controller.GameController
import dk.ilios.jervis.fumbbl.FumbblFileReplayAdapter
import dk.ilios.jervis.fumbbl.model.change.ModelChange
import dk.ilios.jervis.fumbbl.net.commands.ServerCommandReplay
import dk.ilios.jervis.fumbbl.platformFileSystem
import dk.ilios.jervis.fumbbl.utils.FumbblGame
import dk.ilios.jervis.fumbbl.utils.fromFumbblState
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.procedures.PreGame
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking
import okio.Path.Companion.toPath

class FumbblReplay(
    private val actionRequestChannel: Channel<Pair<GameController, List<ActionDescriptor>>>,
    private val actionSelectedChannel: Channel<GameAction>
) {

    private var gameState: FumbblGame? = null
    private var modelChangeIndex: Int = 0
    private lateinit var modelChangeCommands: List<ModelChange>
    private lateinit var adapter: FumbblFileReplayAdapter

    suspend fun loadCommands() {
        val file = platformFileSystem.canonicalize("".toPath()) / ("../../../replays/game-1624379.json".toPath())
        adapter = FumbblFileReplayAdapter(file)
        val commands: MutableList<ServerCommandReplay> = mutableListOf()
        runBlocking {
           adapter.start()
            gameState = adapter.getGame()
            var isDone = false
            while(!isDone) {
                val cmd = adapter.receive()
                isDone = when(cmd) {
                    is ServerCommandReplay -> cmd.lastCommand
                    else -> false
                }
                commands.add(cmd)
            }
        }
        adapter.close()

        // Normalize replay to a list of model changes
        modelChangeCommands = commands.flatMap {
            it.commandArray.flatMap {
                it.modelChangeList.modelChangeArray
            }
        }
    }

    fun getGame(): Game = Game.fromFumbblState(gameState ?: throw IllegalStateException("Call loadCommands() first."))

    fun getActionProvider(): (controller: GameController, availableActions: List<ActionDescriptor>) -> GameAction {
        TODO()
    }
}