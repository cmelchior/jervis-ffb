package com.jervisffb.net.handlers

import com.jervisffb.net.GameSession
import com.jervisffb.net.JervisExitCode
import com.jervisffb.net.JervisNetworkWebSocketConnection
import com.jervisffb.net.JoinedP2PClient
import com.jervisffb.net.JoinedP2PCoach
import com.jervisffb.net.JoinedP2PHost
import com.jervisffb.net.messages.AcceptGameMessage
import com.jervisffb.net.messages.GameState
import com.jervisffb.net.messages.P2PClientState
import com.jervisffb.net.messages.P2PHostState
import com.jervisffb.net.messages.ProtocolErrorServerError

class AcceptGameHandler(override val session: GameSession) : ClientMessageHandler<AcceptGameMessage>() {
    override suspend fun handleMessage(message: AcceptGameMessage, connection: JervisNetworkWebSocketConnection?) {
        // If all players have accepted the game, it will start "for real", sending a notification
        // to all connected clients so they can initiate their respective Game Engines.
        if (connection == null) error("Missing connection for message: $message")
        session.getPlayerClient(connection)?.let { joinedClient ->
            if (!assertInvariants(joinedClient, connection)) return

            // If accepted, start the game for real
            if (message.startGame) {
                joinedClient.hasAcceptedGame = true
                if (session.isReadyToStart()) {
                    session.startGame()
                    session.out.sendGameReady(session.gameId)
                    session.hostState = P2PHostState.RUN_GAME
                    session.clientState = P2PClientState.RUN_GAME
                    session.out.sendHostStateUpdate(session.hostState)
                    session.out.sendClientStateUpdate(session.clientState)
                }
            } else {
                // If game was rejected, behavior depends on who rejected it
                // Client:
                //     - Client: Disconnects from server and gets sent back to Join screen.
                //     - Host: Gets moved back to Waiting for Opponent screen with an error message
                // Host:
                //     - Client: Gets disconnected and moved back to Join screen with an error message.
                //     - Host: Shuts down game. Moves back to "Configure" screen.
                //

                // But we also need to reset accepted state for everyone. Mostly for the Host in case
                // they reuse the session for another Client.
                session.coaches.forEach { coach -> coach.hasAcceptedGame = false}
                when (joinedClient) {
                    is JoinedP2PClient -> {
                        session.state = GameState.JOINING
                        session.hostState = P2PHostState.WAIT_FOR_CLIENT
                        session.out.sendHostStateUpdate(session.hostState, "${joinedClient.coach.name} did not accept the game.")
                        session.clientState = P2PClientState.JOIN_SERVER
                        session.out.sendClientStateUpdate(session.clientState, "${joinedClient.coach.name} did not accept the game.")
                        joinedClient.disconnect(JervisExitCode.GAME_NOT_ACCEPTED, "Game '${session.gameId.value}' was rejected by ${joinedClient.coach.name}.")
                    }
                    is JoinedP2PHost -> {
                        session.state = GameState.CLOSING
                        session.clientState = P2PClientState.JOIN_SERVER
                        // Sending the state change to the client means it gets the "same" disconnect event twice.
                        // session.out.sendClientStateUpdate(P2PClientState.JOIN_SERVER, "${joinedClient.coach.name} did not accept the game.")
                        session.hostState = P2PHostState.SETUP_GAME
                        session.out.sendHostStateUpdate(session.hostState, "${joinedClient.coach.name} did not accept the game.")
                        session.shutdownGame(JervisExitCode.GAME_NOT_ACCEPTED, "Game '${session.gameId.value}' was rejected by ${joinedClient.coach.name}")
                    }
                }
            }
        } ?: session.out.sendError(
            connection,
            ProtocolErrorServerError("Spectator clients cannot start games: $message"),
        )
    }

    // Check invariants for this handler and send errors to the sender if relevant
    // Returns `true` if no variants are broken.
    private suspend fun assertInvariants(joinedClient: JoinedP2PCoach, connection: JervisNetworkWebSocketConnection): Boolean {
        // Ignore message if game has already been started
        if (joinedClient.hasAcceptedGame) {
            session.out.sendError(
                connection,
                ProtocolErrorServerError("Coach has already accepted the game."),
            )
            return false
        }

        // Ignore message if game isn't in a state where this message makes sense
        if (session.state != GameState.STARTING) {
            session.out.sendError(
                connection,
                ProtocolErrorServerError("Game are in a state that doesn't allow starting: ${session.state}."),
            )
            return false
        }

        return true
    }
}
