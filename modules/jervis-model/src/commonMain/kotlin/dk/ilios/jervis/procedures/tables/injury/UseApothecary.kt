package dk.ilios.jervis.procedures.tables.injury

import compositeCommandOf
import dk.ilios.jervis.actions.ActionDescriptor
import dk.ilios.jervis.actions.Cancel
import dk.ilios.jervis.actions.CancelWhenReady
import dk.ilios.jervis.actions.Confirm
import dk.ilios.jervis.actions.ConfirmWhenReady
import dk.ilios.jervis.actions.Continue
import dk.ilios.jervis.actions.ContinueWhenReady
import dk.ilios.jervis.actions.D16Result
import dk.ilios.jervis.actions.D6Result
import dk.ilios.jervis.actions.Dice
import dk.ilios.jervis.actions.GameAction
import dk.ilios.jervis.actions.RollDice
import dk.ilios.jervis.commands.Command
import dk.ilios.jervis.commands.SetApothecaryUsed
import dk.ilios.jervis.commands.SetContext
import dk.ilios.jervis.commands.SetPlayerLocation
import dk.ilios.jervis.commands.fsm.ExitProcedure
import dk.ilios.jervis.commands.fsm.GotoNode
import dk.ilios.jervis.fsm.ActionNode
import dk.ilios.jervis.fsm.Node
import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.Team
import dk.ilios.jervis.model.context.assertContext
import dk.ilios.jervis.model.context.getContext
import dk.ilios.jervis.model.inducements.ApothecaryType
import dk.ilios.jervis.model.locations.DogOut
import dk.ilios.jervis.reports.ReportApothecaryUsed
import dk.ilios.jervis.reports.ReportDiceRoll
import dk.ilios.jervis.rules.Rules
import dk.ilios.jervis.rules.skills.DiceRollType
import dk.ilios.jervis.rules.tables.CasualtyResult
import dk.ilios.jervis.rules.tables.InjuryResult
import dk.ilios.jervis.utils.INVALID_ACTION

/**
 * Procedure for using an apothecary as described on page 62 in the rulebook.
 * The result of using the apothecary is stored in [RiskingInjuryContext]
 */
object UseApothecary: Procedure() {
    override val initialNode: Node = ChooseToUseApothecary
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null
    override fun isValid(state: Game, rules: Rules) {
        state.assertContext<RiskingInjuryContext>()
    }

    // TODO Change this to select the type of apothecary instead?
    object ChooseToUseApothecary: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<RiskingInjuryContext>().player.team
        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
            val context = state.getContext<RiskingInjuryContext>()
            val hasApothecary = context.player.team.teamApothecaries.count { it.type == ApothecaryType.STANDARD && !it.used } > 0
            return when (hasApothecary) {
                true -> listOf(ConfirmWhenReady, CancelWhenReady)
                false -> listOf(ContinueWhenReady)
            }
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            val context = state.getContext<RiskingInjuryContext>()
            val player = context.player
            val team = player.team
            val moveToDogOut = context.armourBroken && context.injuryResult != InjuryResult.STUNNED

            return when (action) {
                Confirm -> {
                    val apothecary = team.teamApothecaries.first { it.type == ApothecaryType.STANDARD && !it.used }
                    compositeCommandOf(
                        SetApothecaryUsed(team, apothecary, true),
                        ReportApothecaryUsed(team, apothecary),
                        SetContext(context.copy(apothecaryUsed = apothecary)),
                        if (context.injuryResult == InjuryResult.KO) ExitProcedure() else GotoNode(ApothecaryCasualtyReRoll)
                    )
                }
                Cancel,
                Continue -> {
                    compositeCommandOf(
                        SetContext(context.copy(
                            finalCasualtyResult = context.casualtyResult,
                            finalLastingInjury = context.lastingInjuryResult,
                        )),
                        SetPlayerLocation(player, DogOut),
                        ExitProcedure()  // Apothecary not used, just accept the result
                    )
                }
                else -> INVALID_ACTION(action)
            }
        }
    }

    /**
     * Just to make it easier, we roll both on the Casualty
     */
    object ApothecaryCasualtyReRoll: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<RiskingInjuryContext>().player.team
        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
            return listOf(RollDice(Dice.D16))
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return checkDiceRoll<D16Result>(action) { d16 ->
                val context = state.getContext<RiskingInjuryContext>()
                val result = rules.casualtyTable.roll(d16)
                compositeCommandOf(
                    ReportDiceRoll(DiceRollType.CASUALTY, d16),
                    SetContext(context.copy(
                        apothecaryCasualtyRoll = d16,
                        apothecaryCasualtyResult = result)
                    ),
                    if (result == CasualtyResult.LASTING_INJURY) GotoNode(ApothecaryLastingInjuryReroll) else GotoNode(SelectInjury)
                )
            }
        }
    }

    object ApothecaryLastingInjuryReroll: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<RiskingInjuryContext>().player.team
        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
            return listOf(RollDice(Dice.D6))
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return checkDiceRoll<D6Result>(action) { d6 ->
                val context = state.getContext<RiskingInjuryContext>()
                val result = rules.lastingInjuryTable.roll(d6)
                compositeCommandOf(
                    ReportDiceRoll(DiceRollType.LASTING_INJURY, d6),
                    SetContext(context.copy(apothecaryLastingInjuryRoll = d6, apothecaryLastingInjuryResult = result)),
                    GotoNode(SelectInjury),
                )
            }
        }
    }

    object SelectInjury: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<RiskingInjuryContext>().player.team
        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
            // Treat confirm as choosing the rerolled result, cancel as keeping the original result
            return listOf(ConfirmWhenReady, CancelWhenReady)
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            val context = state.getContext<RiskingInjuryContext>()
            val updatedContext = when (action) {
                Confirm -> {
                    context.copy(
                        finalCasualtyResult = context.apothecaryCasualtyResult,
                        finalLastingInjury = context.apothecaryLastingInjuryResult,
                    )
                }
                Cancel -> {
                    context.copy(
                        finalCasualtyResult = context.casualtyResult,
                        finalLastingInjury = context.lastingInjuryResult,
                    )
                }
                else -> INVALID_ACTION(action)
            }
            return compositeCommandOf(
                SetContext(updatedContext),
                ExitProcedure()
            )
        }
    }
}
