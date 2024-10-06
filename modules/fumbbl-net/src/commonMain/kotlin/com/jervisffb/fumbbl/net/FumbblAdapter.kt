package com.jervisffb.fumbbl.net

import com.jervisffb.fumbbl.net.api.commands.ClientCommand
import com.jervisffb.fumbbl.net.api.commands.ServerCommand
import com.jervisffb.fumbbl.net.model.Game

interface FumbblAdapter {
    suspend fun start()

    suspend fun send(command: ClientCommand)

    suspend fun receive(): ServerCommand

    suspend fun getGame(): Game

    fun close()

    val isClosed: Boolean
}
