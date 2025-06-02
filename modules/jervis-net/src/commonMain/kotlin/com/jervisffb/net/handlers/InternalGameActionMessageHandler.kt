package com.jervisffb.net.handlers

import com.jervisffb.net.GameSession
import com.jervisffb.net.JervisNetworkWebSocketConnection
import com.jervisffb.net.messages.InternalGameActionMessage
import com.jervisffb.utils.jervisLogger

/**
 * Server handler for game actions that was triggered by the server.
 *
 * For now, these only happen after a timer has expired, triggering the server to generate
 * an action for the client.
 */
class InternalGameActionMessageHandler(override val session: GameSession) : ClientMessageHandler<InternalGameActionMessage>() {

    companion object {
        val LOG = jervisLogger()
    }

    override suspend fun handleMessage(message: InternalGameActionMessage, connection: JervisNetworkWebSocketConnection?) {
        val game = session.game ?: error("Game is not initialized yet.")

        val expectedClientIndex = game.history.last().id + 1
        if (message.clientIndex > expectedClientIndex) {
            LOG.e { "Received an out-of-order action. Expected ${expectedClientIndex}, but received ${message.clientIndex}." }
            error("Invalid clientIndex received. Expected ${expectedClientIndex}, but received ${message.clientIndex}.")
        } else if (message.clientIndex < expectedClientIndex) {
            // This means that the user managed to sneak in before the automated action could be processed.
            // In that case, we just ignore the automated action.
            LOG.d { "Received an out-dated action. Expected ${expectedClientIndex.toSimpleIdString()}, but received ${message.clientIndex.toSimpleIdString()}. Ignoring." }
            return
        } else {
            // Everything is fine, handle the action.
            LOG.d { "Handle internal game action (${message.clientIndex.toSimpleIdString()}): ${message.action}" }
        }

        val coach = game.getAvailableActions().team.coach
        handleAction(session, null, game, coach.id, message.action, null)
    }
}
