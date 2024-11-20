package com.jervisffb.net.messages

import com.jervisffb.engine.actions.GameAction
import com.jervisffb.net.GameId
import kotlinx.serialization.Serializable

/**
 * Interface describing all messages sent from a Server to a Client.
 */
@Serializable
sealed interface ServerMessage: NetMessage

@Serializable
data class PlayerJoinedMessage(val coachName: String): ServerMessage

// Response to JoinGameAsSpectator
@Serializable
data class GameNotFoundMessage(val gameId: String): ServerMessage

// Send to all cl
@Serializable
data class SyncGameActionMessage(val action: GameAction): ServerMessage

@Serializable
data class TeamData(
    val coach: String,
    val teamName: String,
    val teamRoster: String,
    val teamValue: Int
)

// Ask players to accept if they want to start the game with the provided teams.
@Serializable
data class ConfirmGameStartMessage(val gameId: GameId, val teams: List<TeamData>): ServerMessage

// Game was accepted by all parties and is starting
@Serializable
data class GameReadyMessage(val gameId: GameId): ServerMessage


enum class JervisErrorCode(val code: Short) {
    INVALID_TEAM(1), // Team is not allowed to join the given game
    READ_MESSAGE_ERROR(2), // It wasn't possible to read an incoming message (for some reason)
    PROTOCOL_ERROR(3), // The message could not be accepted due to some invariant being broken
}

@Serializable
data class ServerError(val errorCode: JervisErrorCode, val message: String): ServerMessage
