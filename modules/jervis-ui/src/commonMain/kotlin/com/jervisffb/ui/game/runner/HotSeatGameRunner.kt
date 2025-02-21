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
import com.jervisffb.ui.game.state.UiActionProvider

/**
 * Model class for Hotseat games, i.e., games where both teams are being
 * played on the same screen.
 */
class HotSeatGameRunner(
    gameRules: Rules,
    val homeTeam: Team,
    val awayTeam: Team,
) : UiGameRunner {

    override val controller: GameEngineController
    init {
        val game = Game(gameRules, homeTeam, awayTeam, Field.createForRuleset(gameRules))
        controller = GameEngineController(game)
    }
    override var state: Game = controller.state
    override var rules: Rules = controller.rules
    override val diceGenerator: DiceRollGenerator = UnsafeRandomDiceGenerator()
    override var actionProvider: UiActionProvider? = null

    override fun handleAction(action: GameAction) {
        controller.handleAction(action)
    }

    override fun getAvailableActions(): ActionRequest {
        return controller.getAvailableActions()
    }

}
