package com.jervisffb.net.handlers

import com.jervisffb.engine.ActionRequest
import com.jervisffb.engine.actions.RollDice
import com.jervisffb.engine.actions.SelectRandomPlayers
import com.jervisffb.engine.actions.TossCoin
import com.jervisffb.engine.utils.containsActionWithRandomBehavior
import com.jervisffb.engine.utils.createRandomAction
import com.jervisffb.net.GameSession
import com.jervisffb.net.JervisNetworkWebSocketConnection
import com.jervisffb.net.messages.GameActionMessage
import com.jervisffb.net.messages.JervisErrorCode

class GameActionHandler(override val session: GameSession) : ClientMessageHandler<GameActionMessage>() {
    override suspend fun handleMessage(message: GameActionMessage, connection: JervisNetworkWebSocketConnection) {
        val game = session.game
        if (game == null) {
            session.out.sendError(connection, message, JervisErrorCode.INVALID_GAME_ACTION, "Game is not initialized yet. Please wait for the GameStarted event to be sent.")
            return
        }
        try {
            val game = session.game!!
            game.handleAction(message.action)
            val sender = session.getPlayerClient(connection)!!
            session.out.sendGameActionSync(sender = sender, sender.coach.id, session.game?.history?.last()?.id!!, action = message.action)

            // If the Game is set up, so the server handle all random actions, we should now roll forward creating them here.
            // TODO Add a ServerConfiguration that holds this choice
            var availableActions = game.getAvailableActions()
            while (availableActions.containsActionWithRandomBehavior()) {
                val action = createRandomAction(game.state, availableActions)
                game.handleAction(action)
                // If no producer, we just set it to the Home Team
                val producer = session.coaches.firstOrNull { it.coach == availableActions.team?.coach } ?: session.coaches.first()
                session.out.sendGameActionSync(sender = null, producer.coach.id, session.game?.history?.last()?.id!!, action = action)
                availableActions = game.getAvailableActions()
            }
        } catch (e: Exception) {
            session.out.sendError(connection, message, JervisErrorCode.INVALID_GAME_ACTION, e.stackTraceToString())
        }
    }

    fun createActionWithRandomBehavior(availableActions: ActionRequest) {
        availableActions.random().let {
            when (it) {
                is RollDice -> TODO()
                is SelectRandomPlayers -> TODO()
                TossCoin -> TODO()
                else -> error("Unsupported action: $it")
            }
        }
        // We assume that if a Node requires randomness, all legal action requires randomness as well.


    }
}
