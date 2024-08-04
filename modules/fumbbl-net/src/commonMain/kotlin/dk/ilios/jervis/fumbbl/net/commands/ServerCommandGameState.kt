package dk.ilios.jervis.fumbbl.net.commands

import dk.ilios.jervis.fumbbl.model.Game
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("serverGameState")
data class ServerCommandGameState(
    override val netCommandId: String,
    override val commandNr: Int,
    val game: Game,
) : ServerCommand
