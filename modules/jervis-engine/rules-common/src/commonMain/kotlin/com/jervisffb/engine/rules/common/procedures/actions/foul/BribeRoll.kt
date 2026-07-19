package com.jervisffb.engine.rules.common.procedures.actions.foul

import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.actions.Dice
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.actions.RollDice
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.context.UpdateContext
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.fsm.castDiceRoll
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.context.assertContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.reports.ReportDiceRoll
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.common.procedures.D6DieRoll

/**
 * Implement the Bribe roll. The result is stored in [BeingSentOffContext] and
 * it is up to the caller to determine what to do with the result.
 *
 * No known effect allows you to reroll the Bribe Roll. Team Rerolls explicitly
 * disallows it, and Pro only works on rolls that are done "on behalf" of the
 * player, which this is not.
 *
 * See page 144 in the BB20205 rulebook.
 */
object BribeRoll: Procedure() {
    override val initialNode: Node = RollDie
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null
    override fun isValid(state: Game, rules: Rules) = state.assertContext<BeingSentOffContext>()

    object RollDie: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<BeingSentOffContext>().player.team
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            return listOf(RollDice(Dice.D6))
        }
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return castDiceRoll<D6Result>(action) { d6 ->
                val context = state.getContext<BeingSentOffContext>()
                return compositeCommandOf(
                    ReportDiceRoll(DiceRollType.BRIBE, d6),
                    UpdateContext(context.copy(bribeRoll = D6DieRoll.create(state, d6))),
                    ExitProcedure()
                )
            }
        }
    }
}
