package com.jervisffb.net.handlers

import com.jervisffb.engine.GameEngineController
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.model.CoachId
import com.jervisffb.engine.utils.containsActionWithRandomBehavior
import com.jervisffb.engine.utils.createRandomAction
import com.jervisffb.net.GameSession
import com.jervisffb.net.JervisNetworkWebSocketConnection
import com.jervisffb.net.JoinedP2PCoach
import com.jervisffb.net.messages.InternalGameActionMessage

/**
 * Handle a user's action and roll forward to the next state that requires input from
 * connected clients.
 *
 * All [ClientMessageHandler] implementations that react to [GameAction]s should go
 * through this method.
 */
suspend fun handleAction(
    session: GameSession,
    client: JoinedP2PCoach?, // Might be `null` for internal actions
    game: GameEngineController,
    producer: CoachId,
    action: GameAction,
    connection: JervisNetworkWebSocketConnection?
) {
    session.timer.stopTimer()
    game.handleAction(action)
    client?.resetErrorsSeen()
    session.timer.startNextAction(session.game!!) { id ->
        // It should be safe to do this here, because even if this by accident gets triggered
        // after the state has moved on, the outdated id will cause it to be filtered out.
        session.timer.getOutOfTimeAction(game.state, session.game!!.getAvailableActions() )
        val msg = InternalGameActionMessage(id, action)
        session.sendInternalMessage(null, msg)
    }
    val sender = if (connection != null) session.getPlayerClient(connection) else null
    session.out.sendGameActionSync(
        sender = sender,
        producer = producer,
        index = game.currentActionIndex(),
        action = action,
    )
    val actions = game.getAvailableActions()
    session.out.sendGameTimerSync(actions.team.coach.id, game.nextActionIndex(), session.timer.getDeadlineForNextAction())
    rollForwardToUserAction(session, game)
}

/**
 * Roll the game forward to the first action that cannot be generated on the server, but must be created
 * on one of the connected clients.
 *
 * Note, this method shares a lot of logic with [handleAction].
 */
suspend fun rollForwardToUserAction(session: GameSession, game: GameEngineController) {
    var serverMustCreateAction = checkIfServerMustCreateAction(session, game)
    while (serverMustCreateAction) {
        val availableActions = game.getAvailableActions()
        val action = createRandomAction(game.state, availableActions, session.random)
        session.timer.stopTimer()
        game.handleAction(action)
        // Consider exactly what needs to be taken into account when tracking time.
        // For now, we ignore everything on the server, and just start the timer, when we
        // start waiting for client actions and stop when we are ready to handle the user's
        // action. This will allow for a little drift, especially on the incoming side as we
        // handle events from a single event queue, but it seems fine for a first implementation.
        session.timer.startNextAction(session.game!!) { id->
            // It should be safe to do this here, because even if this by accident gets triggered
            // after the state has moved on, the outdated id will cause it to be filtered out.
            session.timer.getOutOfTimeAction(game.state, session.game!!.getAvailableActions() )
            val msg = InternalGameActionMessage(id, action)
            session.sendInternalMessage(null, msg)
        }

        // If no producer, we just set it to the Home Team
        val producer = session.coaches.firstOrNull { it.coach == availableActions.team.coach } ?: session.coaches.first()
        session.out.sendGameActionSync(
            sender = null,
            producer.coach.id,
            session.game?.currentActionIndex()!!,
            action = action,
        )
        session.out.sendGameTimerSync(
            game.getAvailableActions().team.coach.id,
            game.nextActionIndex(),
            session.timer.getDeadlineForNextAction()
        )
        serverMustCreateAction = checkIfServerMustCreateAction(session, game)
    }
}

// Check if we are in a state where the server must create the action rather than delegating it to
// a client.
private fun checkIfServerMustCreateAction(
    session: GameSession,
    game: GameEngineController
): Boolean {
    val outOfTime = session.timer.isOutOfTime(game)
    val availableActions = game.getAvailableActions()
    val serverRandomActions = !session.gameSettings.clientSelectedDiceRolls && availableActions.containsActionWithRandomBehavior()
    return outOfTime || serverRandomActions
}
