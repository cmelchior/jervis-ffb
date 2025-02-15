package com.jervisffb.ui.screen.p2p.client

import com.jervisffb.engine.ActionRequest
import com.jervisffb.engine.GameEngineController
import com.jervisffb.engine.GameRunner
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionId
import com.jervisffb.engine.model.Field
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.rng.DiceRollGenerator
import com.jervisffb.engine.rng.UnsafeRandomDiceGenerator
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.StandardBB2020Rules
import com.jervisffb.engine.serialize.JervisSerialization
import com.jervisffb.ui.screen.p2p.P2PClientGameController

/**
 * Runner class for P2P Games or Hosted Games where this runner is only
 * responsible for one of the teams.
 *
 * It forward handling to [P2PClientGameController]
 */
class SingleTeamNetworkGameRunner(
    private val viewController: P2PClientGameController,
    private val onClientAction: (GameAction) -> Unit
) : GameRunner {
    override val controller: GameEngineController
    override val state: Game
    override val rules: Rules = StandardBB2020Rules() // Should read these from viewController
    override val diceGenerator: DiceRollGenerator = UnsafeRandomDiceGenerator()

    init {
        val homeTeam = JervisSerialization.fixTeamRefs(viewController.homeTeam.value!!)
        val awayTeam = JervisSerialization.fixTeamRefs(viewController.awayTeam.value!!)
        val game = Game(rules, homeTeam, awayTeam, Field.createForRuleset(rules))
        controller = GameEngineController(game)
        state = game
    }

    // Action from the UI
    override fun handleAction(action: GameAction) {
        controller.handleAction(action)
        onClientAction(action)
    }

    fun handleNetworkAction(action: GameAction) {
        controller.handleAction(action)
    }

    override fun handleAction(id: GameActionId, action: GameAction) {
        TODO()
    }
    override fun getAvailableActions(): ActionRequest {
        return controller.getAvailableActions()
    }
}
