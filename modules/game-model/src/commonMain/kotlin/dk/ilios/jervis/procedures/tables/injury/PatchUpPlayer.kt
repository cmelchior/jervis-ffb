package dk.ilios.jervis.procedures.tables.injury

import compositeCommandOf
import dk.ilios.jervis.actions.ActionDescriptor
import dk.ilios.jervis.actions.Cancel
import dk.ilios.jervis.actions.CancelWhenReady
import dk.ilios.jervis.actions.Confirm
import dk.ilios.jervis.actions.ConfirmWhenReady
import dk.ilios.jervis.actions.Continue
import dk.ilios.jervis.actions.ContinueWhenReady
import dk.ilios.jervis.actions.GameAction
import dk.ilios.jervis.commands.Command
import dk.ilios.jervis.commands.SetPlayerLocation
import dk.ilios.jervis.commands.SetPlayerState
import dk.ilios.jervis.commands.UseApothecary
import dk.ilios.jervis.commands.fsm.ExitProcedure
import dk.ilios.jervis.fsm.ActionNode
import dk.ilios.jervis.fsm.Node
import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.PlayerState
import dk.ilios.jervis.model.Team
import dk.ilios.jervis.model.context.assertContext
import dk.ilios.jervis.model.context.getContext
import dk.ilios.jervis.model.inducements.ApothecaryType
import dk.ilios.jervis.model.locations.DogOut
import dk.ilios.jervis.rules.Rules
import dk.ilios.jervis.rules.tables.InjuryResult
import dk.ilios.jervis.utils.INVALID_ACTION

/**
 * Procedure that handles patching up an injury after it has been rolled.
 * This procedure is responsible for handling all effects that can affect
 * the injury result.
 */
object PatchUpPlayer: Procedure() {
    override val initialNode: Node = ChooseToUseApothecary
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null
    override fun isValid(state: Game, rules: Rules) {
        state.assertContext<RiskingInjuryContext>()
    }

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
                    // TODO Figure out how to handle apothecary here
                    compositeCommandOf(
                        UseApothecary(team, team.teamApothecaries.first { it.type == ApothecaryType.STANDARD && !it.used }),
                        SetPlayerState(player, PlayerState.STUNNED), // Override whatever injury they had
                        ExitProcedure()
                    )
                }
                Cancel,
                Continue -> {
                    compositeCommandOf(
                        SetPlayerLocation(player, DogOut),
                        ExitProcedure()  // Apothecary not used, just accept the result
                    )
                }
                else -> INVALID_ACTION(action)
            }
        }
    }

    // TODO Add support for other healing affects, like Regeneration?
}
