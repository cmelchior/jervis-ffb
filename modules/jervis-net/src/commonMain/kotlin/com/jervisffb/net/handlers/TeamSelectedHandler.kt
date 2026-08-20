package com.jervisffb.net.handlers

import com.jervisffb.engine.serialization.SerializedTeam
import com.jervisffb.net.GameSession
import com.jervisffb.net.JervisNetworkWebSocketConnection
import com.jervisffb.net.messages.InvalidTeamServerError
import com.jervisffb.net.messages.P2PTeamInfo
import com.jervisffb.net.messages.ProtocolErrorServerError
import com.jervisffb.net.messages.TeamSelectedMessage

class TeamSelectedHandler(override val session: GameSession) : ClientMessageHandler<TeamSelectedMessage>() {
    override suspend fun handleMessage(message: TeamSelectedMessage, connection: JervisNetworkWebSocketConnection?) {
        // Save selected team for the given client
        val team = (message.team as P2PTeamInfo).team
        val client = connection?.let { session.getPlayerClient(it) }
        if (client == null) {
            session.out.sendError(
                connection,
                ProtocolErrorServerError("Connection is not allowed to select a team.")
            )
            return
        } else {
            // The engine cannot represent a team playing against itself, and `Game.init` reports an
            // invalid game state if it happens. Reject the selection here, so a Client whose team
            // list is out of sync gets an error.
            val teamTakenByOtherCoach = session.coaches
                .filter { otherClient -> otherClient !== client }
                .any { otherClient -> otherClient.team?.id == team.id }
            if (teamTakenByOtherCoach) {
                session.out.sendError(
                    connection,
                    InvalidTeamServerError("'${team.name}' has already been selected by the other coach.")
                )
                return
            }
            // TODO This is a temp fix for getting the correct team refs. Should probably be done by serialization instead.
            client.team = SerializedTeam.deserialize(session.gameSettings.gameRules, team, client.coach)
//            team.forEach { it.team = team }
//            team.notifyDogoutChange()
//            team.coach = client.coach
//            client.team = team
        }

        val isHomeTeam = (client == session.host)
        session.out.sendTeamJoined(isHomeTeam, team, client.coach)

        // Check if all players have selected their teams, in that case continue with accepting the game
        session.requestGameStartIfAllTeamsSelected()
    }
}
