package com.jervisffb.net.messages

import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionId
import com.jervisffb.engine.model.Coach
import com.jervisffb.engine.model.CoachId
import com.jervisffb.engine.model.Spectator
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.serialize.JervisSerialization
import com.jervisffb.net.GameId
import com.jervisffb.net.messages.P2PClientState.JOIN_SERVER
import com.jervisffb.net.messages.P2PHostState.JOIN_SERVER
import kotlinx.serialization.Serializable

enum class GameState {
    PLANNED, // Game was created, but no teams have joined yet.
    JOINING, // Not all teams have joined yet.
    STARTING, // Teams have joined, but haven't accepted starting yet.
    ACTIVE, // Both teams have agreed to start and the game is running.
    FINISHED, // The game has finished
//    UPLOADED,
//    BACKED_UP,
//    LOADING
}

/**
 * This enum represents the states for clients connecting to a hosted game.
 * In this mode, only one type of client exists since the game is fully
 * controlled by a server.
 */
@Serializable
enum class HostedClientState {
    JOIN_SERVER,
    ACCEPT_GAME,
    RUN_GAME,
    CLOSE_GAME,
    DONE,
}

/**
 * This enum represents the different high-level states a P2P game client can be in.
 * After [JOIN_SERVER], the Server updates the state the through [UpdateClientStateMessage]
 *
 * This is only used when running Peer-to-Peer games.
 */
@Serializable
enum class P2PClientState {
    START,
    JOIN_SERVER, // Client is waiting to connect to the server
    SELECT_TEAM, // Client is connected and has received rules / setup constraints. Both teams must now select teams
    ACCEPT_GAME, // Both coaches has selected their team and is waiting for each other to accept the game.
    RUN_GAME, // Game is running through the Game Engine, sending GameActions as needed.
    CLOSE_GAME, // Game is done and in the process of shutting down.
    DONE // Game is over and client is disconnecting or has disconnected.
}

/**
 * This enum represents the different high-level states a P2P Game Host can be in.
 * After [JOIN_SERVER], the Server updates the state the through
 * [UpdateHostStateMessage]
 *
 * This is only used when running Peer-to-Peer games.
 */
@Serializable
enum class P2PHostState {
    START,
    SETUP_GAME,
    SELECT_TEAM,
    START_SERVER,
    JOIN_SERVER,
    WAIT_FOR_CLIENT,
    ACCEPT_GAME,
    RUN_GAME,
    CLOSE_GAME,
    DONE
}

@Serializable
enum class SpectatorState {
    START,
    JOIN_HOST,
    RUN_GAME,
    DONE
}

/**
 * Interface describing all messages sent from a Server to a Client.
 */
@Serializable
sealed interface ServerMessage: NetMessage

@Serializable
data class CoachJoinedMessage(val coach: Coach, val isHomeCoach: Boolean): ServerMessage

@Serializable
data class CoachLeftMessage(val coach: Coach) : ServerMessage

@Serializable
data class SpectatorJoinedMessage(val spectator: Spectator): ServerMessage

@Serializable
data class SpectatorLeftMessage(val spectator: Spectator): ServerMessage

// Used to synchronize a client with the current server state
@Serializable
data class GameStateSyncMessage(
    val rules: Rules,
    val coaches: List<Coach>,
    val spectators: List<Spectator>,
    val hostState: P2PHostState = P2PHostState.START,
    val clientState: P2PClientState = P2PClientState.START,
    val spectatorState: SpectatorState = SpectatorState.START,
    val homeTeam: Team?,
    val awayTeam: Team?,
    // Chat history,
    // Action history,
): ServerMessage



@Serializable
data class UpdateClientStateMessage(val state: P2PClientState): ServerMessage

@Serializable
data class UpdateHostStateMessage(val state: P2PHostState): ServerMessage

@Serializable
data class UpdateSpectatorStateMessage(val state: SpectatorState): ServerMessage


@Serializable
data class UserMessage(val username: String): ServerMessage

@Serializable
data class TeamJoinedMessage(val isHomeTeam: Boolean, private val team: Team): ServerMessage {
    fun getTeam() = JervisSerialization.fixTeamRefs(team)
}


// Response to JoinGameAs* if the server cannot find a game with that Id
@Serializable
data class GameNotFoundMessage(val gameId: String): ServerMessage

/**
 * Send this to all clients to notify them about a new game action that has been processed by the server.
 *
 * @param serverIndex the id of the [com.jervisffb.engine.GameDelta] in the server model, that was created from the [action].
 * @param action the action to send
 */
@Serializable
data class SyncGameActionMessage(val producer: CoachId, val serverIndex: GameActionId, val action: GameAction): ServerMessage

@Serializable
data class TeamData(
    val coach: String,
    val teamName: String,
    val teamRoster: String,
    val teamValue: Int
)

// Ask players to accept if they want to start the game with the provided teams.
@Serializable
data class ConfirmGameStartMessage(
    val gameId: GameId, 
    val rules: Rules,
    val initialActions: List<GameAction>,
    val teams: List<TeamData>
): ServerMessage

// Game was accepted by all parties and is starting
@Serializable
data class GameReadyMessage(val gameId: GameId): ServerMessage

// Codes sent as part of `ServerErrorMessage` payloads.
enum class JervisErrorCode(val code: Short) {
    // Catch-all error if a more specific error code could not be determined
    UNKNOWN_ERROR(1),
    // Team is not allowed to join the given game
    INVALID_TEAM(2),
    READ_MESSAGE_ERROR(3), // It wasn't possible to read an incoming message (for some reason)
    // The message could not be accepted due to some invariant being broken.
    // Note, game actions have their own set of errors. This only applies to other aspects of the protocol.
    PROTOCOL_ERROR(4),
    // An action was sent with an unexpected clientIndex, suggesting client and server being out of sync
    OUT_OF_ORDER_GAME_ACTION(5),
    // action was rejected by the rules engine because it wasn't valid for the current node.
    INVALID_GAME_ACTION_TYPE(6),
    // The server rejected the game action because it was sent by the wrong client for the current node.
    INVALID_GAME_ACTION_OWNER(7),
}

// Top-level interface for all server errors
// Sending an error code in a sealed interface is a bit unnecessary, but it does make some checks
// and output easier on the client.
@Serializable
sealed interface ServerError: ServerMessage {
    val errorCode: JervisErrorCode
    val message: String
}

@Serializable
sealed interface GameActionServerError: ServerError {
    val actionId: GameActionId
}

@Serializable
class UnknownServerError(override val message: String): ServerError {
    override val errorCode = JervisErrorCode.UNKNOWN_ERROR
}

@Serializable
class InvalidTeamServerError(override val message: String): ServerError {
    override val errorCode = JervisErrorCode.INVALID_TEAM
}

@Serializable
class ReadMessageServerError(override val message: String): ServerError {
    override val errorCode = JervisErrorCode.READ_MESSAGE_ERROR
}

@Serializable
class ProtocolErrorServerError(override val message: String): ServerError {
    override val errorCode = JervisErrorCode.PROTOCOL_ERROR
}

@Serializable
class OutOfOrderGameActionServerError(
    override val actionId: GameActionId,
    override val message: String,
): GameActionServerError {
    override val errorCode = JervisErrorCode.OUT_OF_ORDER_GAME_ACTION
}

@Serializable
class InvalidGameActionTypeServerError(
    override val actionId: GameActionId,
    override val message: String,
): GameActionServerError {
    override val errorCode = JervisErrorCode.INVALID_GAME_ACTION_TYPE
}

@Serializable
class InvalidGameActionOwnerServerError(
    override val actionId: GameActionId,
    override val message: String
): GameActionServerError {
    override val errorCode = JervisErrorCode.INVALID_GAME_ACTION_OWNER
}
