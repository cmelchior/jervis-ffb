package com.jervisffb.net.handlers

import com.jervisffb.net.GameSession
import com.jervisffb.net.JervisNetworkWebSocketConnection
import com.jervisffb.net.messages.GameState
import com.jervisffb.net.messages.JervisErrorCode
import com.jervisffb.net.messages.P2PClientState
import com.jervisffb.net.messages.P2PHostState
import com.jervisffb.net.messages.P2PTeamInfo
import com.jervisffb.net.messages.TeamSelectedMessage

class TeamSelectedHandler(override val session: GameSession) : ClientMessageHandler<TeamSelectedMessage>() {
    override suspend fun handleMessage(message: TeamSelectedMessage, connection: JervisNetworkWebSocketConnection) {
        // Save selected team for the given client
        val team = (message.team as P2PTeamInfo).team
        val client = session.getPlayerClient(connection)
        if (client == null) {
            session.out.sendError(connection, message, JervisErrorCode.PROTOCOL_ERROR, "Connection is not allowed to select a team.")
            return
        } else {
            // TODO This is a temp fix for getting the correct team refs. Should probably be done by serialization instead.
            team.forEach { it.team = team }
            team.notifyDogoutChange()
            team.coach = client.coach
            client.team = team
        }

        val isHomeTeam = (client == session.host)
        session.out.sendTeamJoined(isHomeTeam, team)

        // Check if all players have selected their teams, in that case continue with accepting the game
        when (session.coaches.count { it.team != null}) {
            0, 1 -> session.state = GameState.JOINING
            2 -> {
                session.state = GameState.STARTING
                session.out.sendStartingGameRequest(session.gameId, session.coaches.map { it.team!! })
                session.hostState = P2PHostState.ACCEPT_GAME
                session.clientState = P2PClientState.ACCEPT_GAME
                session.out.sendHostStateUpdate(session.hostState)
                session.out.sendClientStateUpdate(session.clientState)
            }
        }
    }
}
