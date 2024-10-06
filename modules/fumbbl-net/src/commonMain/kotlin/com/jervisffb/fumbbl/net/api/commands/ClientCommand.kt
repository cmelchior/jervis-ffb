package com.jervisffb.fumbbl.net.api.commands

import com.jervisffb.fumbbl.net.model.PlayerAction
import com.jervisffb.fumbbl.net.model.ReRolledAction
import com.jervisffb.fumbbl.net.model.change.PlayerId
import com.jervisffb.fumbbl.net.api.ClientMode
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface ClientCommand : NetCommand

@Serializable
sealed interface ClientCommandWithEntropy : NetCommand {
    override val netCommandId: String
    val entropy: Byte?
}

@Serializable
@SerialName("clientCoinChoice")
data class ClientCommandCoinChoice(
    override val netCommandId: String,
    val choiceHeads: Boolean,
) : ClientCommand

@Serializable
@SerialName("clientEndTurn")
data class ClientCommandEndTurn(
    override val netCommandId: String,
    override val entropy: Byte? = null,
    val turnMode: String?,
    val playersAtCoordinates: Map<String, Array<Int>>?,
) : ClientCommandWithEntropy

@Serializable
@SerialName("clientKickoff")
data class ClientCommandKickoff(
    override val netCommandId: String,
    override val entropy: Byte? = null,
    val ballCoordinate: List<Int>,
) : ClientCommandWithEntropy

@Serializable
@SerialName("clientJoin")
data class ClientCommandJoin(
    override val netCommandId: String,
    val clientMode: ClientMode,
    val coach: String,
    val password: String,
    val gameId: Int,
    val gameName: String?,
    val teamId: String?,
    val teamName: String?,
) : ClientCommand

@Serializable
@SerialName("clientPing")
data class ClientCommandPing(
    override val netCommandId: String,
    val timestamp: Long,
) : NetCommand

@Serializable
@SerialName("clientRequestVersion")
data class ClientCommandRequestVersion(
    override val netCommandId: String,
) : ClientCommand

@Serializable
@SerialName("clientSetupPlayer")
data class ClientCommandSetupPlayer(
    override val netCommandId: String,
    override val entropy: Byte? = null,
    val playerId: PlayerId,
    val coordinate: List<Int>,
) : ClientCommandWithEntropy

@Serializable
@SerialName("clientStartGame")
data class ClientCommandStartGame(
    override val netCommandId: String,
    override val entropy: Byte,
) : ClientCommandWithEntropy

@Serializable
@SerialName("clientActingPlayer")
data class ClientCommandActingPlayer(
    override val netCommandId: String,
    val playerId: PlayerId?,
    val playerAction: PlayerAction?,
    val leaping: Boolean,
) : ClientCommand

@Serializable
@SerialName("clientMove")
data class ClientCommandMove(
    override val netCommandId: String,
    override val entropy: Byte? = null,
    val actingPlayerId: PlayerId,
    val coordinateFrom: List<Int>,
    val coordinatesTo: List<List<Int>>,
) : ClientCommandWithEntropy

@Serializable
@SerialName("clientReceiveChoice")
data class ClientCommandReceiveChoice(
    override val netCommandId: String,
    val choiceReceive: Boolean,
) : ClientCommand

@Serializable
@SerialName("clientUseReRoll")
data class ClientCommandUseReroll(
    override val netCommandId: String,
    val reRolledAction: ReRolledAction,
    val reRollSource: String?,
) : ClientCommand

@Serializable
@SerialName("clientUseApothecary")
data class ClientCommandUseApothecary(
    override val netCommandId: String,
    val playerId: PlayerId,
    val apothecaryUsed: Boolean,
) : ClientCommand
