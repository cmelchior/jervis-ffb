package com.jervisffb.engine.rules.bb2020.procedures.tables.injury

import compositeCommandOf
import com.jervisffb.engine.actions.ActionDescriptor
import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.actions.Dice
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.SetContext
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.actions.RollDice
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.context.assertContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.modifiers.DiceModifier
import com.jervisffb.engine.reports.ReportDiceRoll
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.bb2020.skills.DiceRollType

/**
 * Implement the injury roll as described on page 60 in the rulebook.
 *
 * The result is stored in [Game.injuryRollResultContext] and it is up
 * to the caller to determine what to do with the result.
 *
 * TODO Note, Mighty Blow specifically say "When an opposition player is knocked
 *  down" (page 80) and "Pushed into the Crows" (page 58) says "A player that
 *  is pushed into the crowd is immediately removed from play". So this would
 *  mean that any effect that requires a "Knocked Down" player doesn't apply.
 */
object InjuryRoll: Procedure() {
    override val initialNode: Node = RollDice
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null
    override fun isValid(state: Game, rules: Rules) = state.assertContext<RiskingInjuryContext>()

    object RollDice : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<RiskingInjuryContext>().player.team.otherTeam()
        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> = listOf(RollDice(Dice.D6, Dice.D6))

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return checkDiceRoll<D6Result, D6Result>(action) { die1, die2 ->
                val context = state.getContext<RiskingInjuryContext>()

                // Determine result of injury roll
                // TODO This logic needs to be expanded to support things like Mighty Blow and others.
                val roll = listOf(die1, die2)
                val modifiers = emptyList<DiceModifier>()
                val result = rules.injuryTable.roll(die1, die2, modifiers.sumOf { it.modifier })

                val updatedContext = context.copy(
                    injuryRoll = roll,
                    injuryResult = result,
                    injuryModifiers = modifiers,
                )

                compositeCommandOf(
                    ReportDiceRoll(DiceRollType.INJURY, roll),
                    SetContext(updatedContext),
                    ExitProcedure()
                )
            }
        }
    }
}
