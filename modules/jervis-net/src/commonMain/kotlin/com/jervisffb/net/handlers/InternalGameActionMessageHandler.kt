package com.jervisffb.net.handlers

import com.jervisffb.engine.utils.createRandomAction
import com.jervisffb.net.GameSession
import com.jervisffb.net.JervisNetworkWebSocketConnection
import com.jervisffb.net.messages.InternalGameActionMessage
import com.jervisffb.utils.jervisLogger

/**
 * Handler for internal game actions. These usually occur if a time-out occurs.
 */
class InternalGameActionMessageHandler(override val session: GameSession) : ClientMessageHandler<InternalGameActionMessage>() {

    companion object {
        val LOG = jervisLogger()
    }

    override suspend fun handleMessage(message: InternalGameActionMessage, connection: JervisNetworkWebSocketConnection?) {
        val game = session.game
        if (game == null) {
            error("Game is not initialized yet.")
        }

        val expectedClientIndex = game.history.last().id + 1

        if (message.clientIndex > expectedClientIndex) {
            LOG.e { "Received an out-of-order action. Expected ${expectedClientIndex}, but received ${message.clientIndex}." }
            error("Invalid clientIndex received. Expected ${expectedClientIndex}, but received ${message.clientIndex}.")
        } else if (message.clientIndex < expectedClientIndex) {
            // This means that the user managed to sneak in before the automated action could be processed.
            // Just ignore it
            LOG.d { "Received an out-dated action. Expected ${expectedClientIndex}, but received ${message.clientIndex}. Ignoring." }
            return
        } else {
            // Everything is fine
            LOG.d { "Handle internal game action (${message.clientIndex}): ${message.action}" }
        }

        val coach = game.getAvailableActions().team?.coach ?: game.state.homeTeam.coach

        // If the action wasn't pre-selected, create one now.
        // TODO Figure out how to do this
        val nextAction = createRandomAction(game.state, game.getAvailableActions())
        handleAction(session, game, coach.id, nextAction, null)
    }
}
