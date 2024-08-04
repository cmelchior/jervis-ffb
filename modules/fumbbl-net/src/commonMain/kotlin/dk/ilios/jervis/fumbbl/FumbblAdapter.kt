package dk.ilios.jervis.fumbbl

import dk.ilios.jervis.fumbbl.model.Game
import dk.ilios.jervis.fumbbl.net.commands.ClientCommand
import dk.ilios.jervis.fumbbl.net.commands.ServerCommand

interface FumbblAdapter {
    suspend fun start()

    suspend fun send(command: ClientCommand)

    suspend fun receive(): ServerCommand

    suspend fun getGame(): Game

    fun close()

    val isClosed: Boolean
}
