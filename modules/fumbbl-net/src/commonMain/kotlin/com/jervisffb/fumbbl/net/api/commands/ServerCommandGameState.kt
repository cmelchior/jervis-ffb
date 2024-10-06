package com.jervisffb.fumbbl.net.api.commands

import com.jervisffb.fumbbl.net.model.Game
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("serverGameState")
data class ServerCommandGameState(
    override val netCommandId: String,
    override val commandNr: Int,
    val game: Game,
) : ServerCommand
