package com.jervisffb.engine.rules.bb2020.procedures.tables.injury

import com.jervisffb.engine.actions.Cancel
import com.jervisffb.engine.actions.CancelWhenReady
import com.jervisffb.engine.actions.Confirm
import com.jervisffb.engine.actions.ConfirmWhenReady
import com.jervisffb.engine.actions.Continue
import com.jervisffb.engine.actions.ContinueWhenReady
import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.actions.Dice
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.actions.RollDice
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.SetApothecaryUsed
import com.jervisffb.engine.commands.SetPlayerLocation
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.context.SetContext
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
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.common.tables.InjuryResult
import com.jervisffb.engine.utils.INVALID_ACTION
import kotlinx.serialization.Serializable

/**
 * Procedure for using an apothecary as described on page 95 in Death Zone.
 * The result of using the apothecary is stored in [RiskingInjuryContext]
 *
 * Developer's Commentary:
 * This procedure has a lot of overlap with the [UseBB11Apothecary] procedure.
 * There might be a better way to keep them in sync; for now, it is a manual process.
 */
@Serializable
object UseBB7Apothecary: Procedure() {
    override val initialNode: Node = ChooseToUseApothecary
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null
    override fun isValid(state: Game, rules: Rules) {
        state.assertContext<RiskingInjuryContext>()
    }

    // TODO Change this to select the type of apothecary instead?
    // TODO Apothecary can have restrictions on the kind of players it works on
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

            return when (action) {
                Confirm -> {
                    val apothecary = team.teamApothecaries.first { it.type == ApothecaryType.STANDARD && !it.used }
                    compositeCommandOf(
                        SetApothecaryUsed(team, apothecary, true),
                        ReportApothecaryUsed(team, apothecary),
                        SetContext(context.copy(apothecaryUsed = apothecary)),
                        if (context.injuryResult == InjuryResult.KO) ExitProcedure() else GotoNode(ApothecaryInjuryReroll)
                    )
                }
                Cancel,
                Continue -> {
                    compositeCommandOf(
                        SetPlayerLocation(player, DogOut),
                        ExitProcedure()  // Apothecary isn't used. Accept the first result
                    )
                }
                else -> INVALID_ACTION(action)
            }
        }
    }

    object ApothecaryInjuryReroll: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<RiskingInjuryContext>().player.team
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            return listOf(RollDice(Dice.D6))
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return checkDiceRoll<D6Result>(action) { d6 ->
                val context = state.getContext<RiskingInjuryContext>()
                val success = (d6.value >= 4)
                compositeCommandOf(
                    ReportDiceRoll(DiceRollType.BB7_APOTHECARY, d6),
                    SetContext(context.copy(
                        apothecaryInjuryRoll = d6,
                        apothecaryInjuryRollSuccess = success
                    )),
                    ExitProcedure(),
                )
            }
        }
    }
}
