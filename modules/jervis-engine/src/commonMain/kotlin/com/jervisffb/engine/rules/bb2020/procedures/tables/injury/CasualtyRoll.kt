package com.jervisffb.engine.rules.bb2020.procedures.tables.injury

import com.jervisffb.engine.actions.D16Result
import com.jervisffb.engine.actions.Dice
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.actions.RollDice
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.SetContext
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.fsm.checkDiceRoll
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.context.assertContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.reports.ReportDiceRoll
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.bb2020.skills.Skill

/**
 * Implement the Casualty Roll as described on page 61 in the rulebook.
 *
 * The result is stored in [RiskingInjuryContext] and it is up
 * to the caller to determine what to do with the result.
 */
object CasualtyRoll: Procedure() {
    override val initialNode: Node = RollDie
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null
    override fun isValid(state: Game, rules: Rules) {
        state.assertContext<RiskingInjuryContext>()
    }
    object RollDie : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<RiskingInjuryContext>().player.team.otherTeam()
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> = listOf(RollDice(Dice.D16))
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return checkDiceRoll<D16Result>(action) { d16 ->
                val context = state.getContext<RiskingInjuryContext>()

                // Determine the result of casualty roll
                val result = rules.casualtyTable.roll(d16)
                val modifiers = emptyList<Skill>() // Just having skills here is not enough, we need more generic Modifier

                val updatedContext = context.copy(
                    casualtyRoll = d16,
                    casualtyResult = result,
//                    casualtyModifiers = modifiers,
                )

                compositeCommandOf(
                    ReportDiceRoll(DiceRollType.CASUALTY, d16),
                    SetContext(updatedContext),
                    ExitProcedure()
                )
            }
        }
    }
}
