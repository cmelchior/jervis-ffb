package com.jervisffb.net.handlers

import com.jervisffb.engine.ActionRequest
import com.jervisffb.engine.GameEngineController
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.RollDice
import com.jervisffb.engine.actions.SelectRandomPlayers
import com.jervisffb.engine.actions.TossCoin
import com.jervisffb.engine.model.CoachId
import com.jervisffb.engine.utils.containsActionWithRandomBehavior
import com.jervisffb.engine.utils.createRandomAction
import com.jervisffb.net.GameSession
import com.jervisffb.net.JervisNetworkWebSocketConnection
import com.jervisffb.net.messages.GameActionMessage
import com.jervisffb.net.messages.InternalGameActionMessage
import com.jervisffb.net.messages.JervisErrorCode
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

class GameActionHandler(override val session: GameSession) : ClientMessageHandler<GameActionMessage>() {

    var timeout: Pair<Int, Job>? = null

    override suspend fun handleMessage(message: GameActionMessage, connection: JervisNetworkWebSocketConnection?) {
        val game = session.game
        if (game == null) {
            session.out.sendError(connection, message, JervisErrorCode.INVALID_GAME_ACTION, "Game is not initialized yet. Please wait for the GameStarted event to be sent.")
            return
        }
        try {
            val expectedDeltaId = session.game!!.currentActionIndex() + 1
            if (message.clientIndex != expectedDeltaId) {
                error("Invalid clientIndex received. Expected $expectedDeltaId, but received ${message.clientIndex}.")
            }
            val coach = game.getAvailableActions().team?.coach ?: game.state.homeTeam.coach
            handleAction(session, game, coach.id, message.action, connection)
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

suspend fun handleAction(
    session: GameSession,
    game: GameEngineController,
    producer: CoachId,
    nextAction: GameAction,
    connection: JervisNetworkWebSocketConnection?
) {
    game.handleAction(nextAction)
    val sender = if (connection != null) session.getPlayerClient(connection) else null
    session.out.sendGameActionSync(sender = sender, producer, game.currentActionIndex(), action = nextAction)

    // TODO If start of turn, start the end-of-turn tracker


    // TODO If non-active teams turn, start wait-for-action tracker.

    // TODO How to handle if end-of-turn tracker triggers when waiting

    // If the Game is set up so the server handles all random actions, we should now roll forward creating them here.
    var availableActions = game.getAvailableActions()
    while (!session.gameSettings.clientSelectedDiceRolls && availableActions.containsActionWithRandomBehavior()) {
        val action = createRandomAction(game.state, availableActions)
        game.handleAction(action)
        // If no producer, we just set it to the Home Team
        val producer = session.coaches.firstOrNull { it.coach == availableActions.team?.coach } ?: session.coaches.first()
        session.out.sendGameActionSync(sender = null, producer.coach.id, game.currentActionIndex(), action = action)
        availableActions = game.getAvailableActions()
    }

    // Last, before waiting for the next action, we set up any server timeouts
    // TODO Figure out exactly which timer to use
    if (session.gameSettings.timerSettings.timersEnabled) {
        val nextIndex = game.currentActionIndex() + 1
        session.scope.launch {
            // delay(session.gameSettings.timerSettings.turnLimitSeconds)
            session.sendInternalMessage(connection, InternalGameActionMessage(nextIndex))
        }
    }
}

suspend fun rollForwardToUserAction(session: GameSession, game: GameEngineController, connection: JervisNetworkWebSocketConnection) {
    var availableActions = game.getAvailableActions()
    while (availableActions.containsActionWithRandomBehavior()) {
        val action = createRandomAction(game.state, availableActions)
        game.handleAction(action)
        // If no producer, we just set it to the Home Team
        val producer = session.coaches.firstOrNull { it.coach == availableActions.team?.coach } ?: session.coaches.first()
        session.out.sendGameActionSync(sender = null, producer.coach.id,session.game?.currentActionIndex()!!, action = action)
        availableActions = game.getAvailableActions()
    }

    // Last, before waiting for the next action, we set up any server timeouts
    // TODO Figure out exactly which timer to use
    if (session.gameSettings.timerSettings.timersEnabled) {
        val nextIndex = game.currentActionIndex() + 1
        session.scope.launch {
            // delay(session.gameSettings.timerSettings.turnLimitSeconds)
            println("TIMER: ${Clock.System.now()}.")
            // TODO Figure out how to select automated actions
            session.sendInternalMessage(connection, InternalGameActionMessage(nextIndex))
        }
    }
}

