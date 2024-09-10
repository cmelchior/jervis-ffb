package dk.ilios.jervis.procedures.tables.injury

import compositeCommandOf
import dk.ilios.jervis.actions.ActionDescriptor
import dk.ilios.jervis.actions.D16Result
import dk.ilios.jervis.actions.Dice
import dk.ilios.jervis.actions.GameAction
import dk.ilios.jervis.actions.RollDice
import dk.ilios.jervis.commands.Command
import dk.ilios.jervis.commands.SetContext
import dk.ilios.jervis.commands.fsm.ExitProcedure
import dk.ilios.jervis.fsm.ActionNode
import dk.ilios.jervis.fsm.Node
import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.Team
import dk.ilios.jervis.model.context.assertContext
import dk.ilios.jervis.model.context.getContext
import dk.ilios.jervis.reports.ReportDiceRoll
import dk.ilios.jervis.rules.Rules
import dk.ilios.jervis.rules.skills.DiceRollType
import dk.ilios.jervis.rules.skills.Skill

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
        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> = listOf(RollDice(Dice.D16))
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return checkDiceRoll<D16Result>(action) { d16 ->
                val context = state.getContext<RiskingInjuryContext>()

                // Determine the result of casualty roll
                val result = rules.casualtyTable.roll(d16)
                val modifiers = emptyList<Skill>() // Just having skills here is not enough, we need more generic Modifier

                val updatedContext = context.copy(
                    casualtyRoll = d16,
                    casualtyResult = result,
                    casualtyModifiers = modifiers,
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
