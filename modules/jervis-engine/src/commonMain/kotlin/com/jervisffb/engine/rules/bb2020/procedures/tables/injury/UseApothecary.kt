package com.jervisffb.engine.rules.bb2020.procedures.tables.injury

import com.jervisffb.engine.actions.Cancel
import com.jervisffb.engine.actions.CancelWhenReady
import com.jervisffb.engine.actions.Confirm
import com.jervisffb.engine.actions.ConfirmWhenReady
import com.jervisffb.engine.actions.Continue
import com.jervisffb.engine.actions.ContinueWhenReady
import com.jervisffb.engine.actions.D16Result
import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.actions.Dice
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.actions.RollDice
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.SetApothecaryUsed
import com.jervisffb.engine.commands.SetContext
import com.jervisffb.engine.commands.SetPlayerLocation
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.fsm.checkDiceRoll
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.context.assertContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.inducements.ApothecaryType
import com.jervisffb.engine.model.locations.DogOut
import com.jervisffb.engine.reports.ReportApothecaryUsed
import com.jervisffb.engine.reports.ReportDiceRoll
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.rules.bb2020.tables.CasualtyResult
import com.jervisffb.engine.rules.bb2020.tables.InjuryResult
import com.jervisffb.engine.utils.INVALID_ACTION

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
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
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
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
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
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
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
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
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
