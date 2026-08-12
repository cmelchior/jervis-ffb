package com.jervisffb.engine.common.procedures.tables.injury

import com.jervisffb.engine.actions.D16Result
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
import com.jervisffb.engine.common.modifiers.CasualtyModifier
import com.jervisffb.engine.common.procedures.dicerolls.createFinalAtLeastObservation
import com.jervisffb.engine.common.reports.ReportDiceRoll
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.fsm.castDiceRoll
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.context.assertContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.isSkillAvailable
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.engine.statistics.probability.observation.ChanceObservationHandler
import kotlinx.collections.immutable.toPersistentList

/**
 * Implement the Casualty Roll as described on page 61 in the rulebook.
 *
 * The result is stored in [com.jervisffb.engine.common.context.RiskingInjuryContext] and it is up
 * to the caller to determine what to do with the result.
 */
object CasualtyRoll: Procedure(), ChanceObservationHandler {
    override val initialNode: Node = RollDie
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null
    override fun isValid(state: Game, rules: Rules) {
        state.assertContext<RiskingInjuryContext>()
    }
    object RollDie : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<RiskingInjuryContext>().player.team.otherTeam()
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> = listOf(RollDice(Dice.D16, type = DiceRollType.CASUALTY))
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return castDiceRoll<D16Result>(action) { d16 ->
                val context = state.getContext<RiskingInjuryContext>()
                val player = context.player

                // Determine the result of casualty roll
                val modifiers = buildList {
                    if (player.isSkillAvailable(SkillType.DECAY)) {
                        add(CasualtyModifier.DECAY)
                    }
                }
                val result = rules.casualtyTable.roll(d16, modifiers)

                // Since Casualty modifiers are applied after the roll, we cannot treat this as a
                // table lookup; instead we need to treat it as an X+ roll, even though it is too
                // optimistic.
                val chanceObservation = createFinalAtLeastObservation(
                    state = state,
                    team = player.team.otherTeam(),
                    rollType = DiceRollType.CASUALTY,
                    die = d16,
                )

                val updatedContext = context.copy(
                    casualtyRoll = d16,
                    casualtyModifiers = modifiers.toPersistentList(),
                    casualtyResult = result,
                )

                compositeCommandOf(
                    ReportDiceRoll(DiceRollType.CASUALTY, d16),
                    chanceObservation?.let(::AddChanceObservation),
                    UpdateContext(updatedContext),
                    ExitProcedure()
                )
            }
        }
    }
}
