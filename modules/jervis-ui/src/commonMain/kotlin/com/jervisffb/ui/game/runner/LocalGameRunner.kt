package com.jervisffb.ui.game.runner

import com.jervisffb.engine.ActionRequest
import com.jervisffb.engine.GameEngineController
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.model.Field
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.rng.DiceRollGenerator
import com.jervisffb.engine.rng.UnsafeRandomDiceGenerator
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.StandardBB2020Rules
import com.jervisffb.ui.game.state.UiActionProvider

/**
 * Runner class for Stand-alone games that are run fully on the client.
 */
class LocalGameRunner(
    private val homeTeam: Team,
    private val awayTeam: Team,
    private val onClientAction: (Int, GameAction) -> Unit
) : UiGameRunner {
    override var actionProvider: UiActionProvider? = null
    override val controller: GameEngineController
    override val state: Game
    override val rules: Rules = StandardBB2020Rules() // Should read these from viewController
    override val diceGenerator: DiceRollGenerator = UnsafeRandomDiceGenerator()

    init {
        val game = Game(rules, homeTeam, awayTeam, Field.Companion.createForRuleset(rules))
        controller = GameEngineController(game)
        state = game
    }

    // Action from the UI
    override fun handleAction(action: GameAction) {
        controller.handleAction(action)
        // Should only send this if the event is truly from this client and not just a sync message
        val clientIndex = controller.history.last().id
        onClientAction(clientIndex, action)
    }

    override fun getAvailableActions(): ActionRequest {
        val actions = controller.getAvailableActions()
        return actions
//        // TODO We need to check ServerConfiguration to check who is responsible for actions with randomness.
//        return if (actions.team?.id == controllingTeam.id && !actions.containsActionWithRandomBehavior()) {
//            actions
//        } else {
//            // TODO How do we show a timer representing that someone else
//            //  is taking their turn?
//            ActionRequest(team = null, emptyList())
//        }
    }
}
