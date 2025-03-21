package com.jervisffb.fumbbl.net.adapter

import com.jervisffb.engine.model.Game
import com.jervisffb.engine.rules.StandardBB2020Rules
import com.jervisffb.fumbbl.net.FumbblFileReplayAdapter
import com.jervisffb.fumbbl.net.api.commands.ServerCommandModelSync
import com.jervisffb.fumbbl.net.api.commands.ServerCommandReplay
import com.jervisffb.fumbbl.net.utils.FumbblGame
import com.jervisffb.fumbbl.net.utils.fromFumbblState
import com.jervisffb.utils.platformFileSystem
import okio.Path
import okio.Path.Companion.toPath

/**
 * Create an adapter that is able to load a FUMBBL replay file and convert it to an Jervis equivalent.
 * This is very much work-in-progress, but it has been shown to be possible.
 *
 * We have taken the FUMBBL client model and converted to Kotlin. This means that we can also keep the FUMBBL
 * state machine up to date, which makes it easier to figure out what is going on.
 *
 * BIG PROBLEM: How do we make sure that our duplicated Fumbbl Model stay up to date with changes to the FUMBBL
 * code. Luckily there isn't many changes, so it might be possible?
 *
 * TODO Right now we only support loading files on the JVM. Figure out how to refactor this so we can use it both
 *  for testing and across WASM and iOS. Should we move the CacheManager to platform-utils? Or move the file loading
 *  out of this class?
 */
class FumbblReplayAdapter(private var replayFile: Path, private val checkCommandsWhenLoading: Boolean = false) {

    private lateinit var gameCommands: List<JervisActionHolder>
    private lateinit var fumbblGame: FumbblGame
    private lateinit var jervisGame: Game

    suspend fun loadCommands() {
        val file = platformFileSystem.canonicalize("".toPath()) / (replayFile)
        val modelChangeCommands = loadCommandsFromFile(file)
        processCommands(modelChangeCommands)
    }

    private suspend fun loadCommandsFromFile(file: Path): List<ServerCommandModelSync> {
        val adapter = FumbblFileReplayAdapter(file)
        val commands: MutableList<ServerCommandReplay> = mutableListOf()

        adapter.start()
        val rules = StandardBB2020Rules() // How do we figure out which ruleset this game is uing?
        fumbblGame = adapter.getGame()
        jervisGame = Game.fromFumbblState(rules, fumbblGame)
        var isDone = false
        while (!isDone) {
            val cmd = adapter.receive()
            commands.add(cmd)
            isDone = cmd.lastCommand
        }
        adapter.close()

        // Normalize replay to a list of model changes
        return commands.flatMap { replayCommand: ServerCommandReplay ->
            replayCommand.commandArray
        }
    }

    private fun processCommands(commands: List<ServerCommandModelSync>) {
        val chain = MapperChain(jervisGame, fumbblGame, checkCommandsWhenLoading)
        gameCommands = chain.process(commands)
    }

    fun getGame(): Game = jervisGame

    fun getCommands(): List<JervisActionHolder> = gameCommands
}
