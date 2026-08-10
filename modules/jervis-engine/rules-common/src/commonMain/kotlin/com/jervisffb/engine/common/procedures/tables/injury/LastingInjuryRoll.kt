package com.jervisffb.engine.common.procedures.tables.injury

import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.actions.Dice
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.actions.RollDice
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.context.UpdateContext
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.probabiliy.AddChanceObservation
import com.jervisffb.engine.common.context.RiskingInjuryContext
import com.jervisffb.engine.common.procedures.dicerolls.createFinalTableLookupObservation
import com.jervisffb.engine.common.reports.ReportDiceRoll
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.fsm.castDiceRoll
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.context.assertContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.statistics.probability.observation.ChanceObservationHandler

/**
 * Implement the lasting injury roll as described on page 61 in the rulebook.
 *
 * The result is stored in [Game.injuryRollResultContext] and it is up
 * to the caller to determine what to do with the result.
 */
object LastingInjuryRoll: Procedure(), ChanceObservationHandler {
    override val initialNode: Node = RollDie
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null
    override fun isValid(state: Game, rules: Rules) = state.assertContext<RiskingInjuryContext>()

    object RollDie : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<RiskingInjuryContext>().player.team.otherTeam()
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> = listOf(RollDice(Dice.D6))
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            val table = rules.lastingInjuryTable
            return castDiceRoll<D6Result>(action) { d6 ->
                val context = state.getContext<RiskingInjuryContext>()
                val result = table.roll(d6)
                val chanceObservation = createFinalTableLookupObservation(
                    state = state,
                    team = context.player.team.otherTeam(),
                    rollType = DiceRollType.LASTING_INJURY,
                    dice = listOf(d6),
                    favorableOutcomes = table.entries.values.count { it == result },
                    possibleOutcomes = table.entries.size,
                )
                val updatedContext = context.copy(
                    lastingInjuryRoll = d6,
                    lastingInjuryResult = result,
                    // lastingInjuryModifiers = emptyList(),
                )

                compositeCommandOf(
                    ReportDiceRoll(DiceRollType.LASTING_INJURY, d6),
                    chanceObservation?.let(::AddChanceObservation),
                    UpdateContext(updatedContext),
                    ExitProcedure()
                )
            }
        }
    }
}
