package com.jervisffb.ui.game.state.decorators

import com.jervisffb.engine.ActionRequest
import com.jervisffb.engine.actions.Cancel
import com.jervisffb.engine.actions.CancelWhenReady
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.rules.bb2025.procedures.actions.move.JumpStep
import com.jervisffb.engine.rules.bb2025.procedures.actions.move.LeapStep
import com.jervisffb.engine.rules.bb2025.procedures.actions.move.PogoStep
import com.jervisffb.engine.rules.bb2025.procedures.actions.pass.InterceptionStep
import com.jervisffb.engine.rules.bb2025.procedures.skills.UseShadowingStep
import com.jervisffb.engine.rules.bb2025.procedures.tables.kickoff.Charge
import com.jervisffb.engine.rules.common.procedures.ResolveBallLandingOnField
import com.jervisffb.engine.rules.common.procedures.actions.move.DodgeRoll
import com.jervisffb.ui.game.UiSnapshotAccumulator
import com.jervisffb.ui.game.icons.ActionIcon
import com.jervisffb.ui.game.state.ManualActionProvider
import com.jervisffb.ui.game.view.ContextMenuOption

/**
 * Some "cancel" actions we want to display in inside the timer button
 */
object CancelDecorator : FieldActionDecorator<CancelWhenReady> {

    private val nodesForGameStatusButton = setOf(
        UseShadowingStep.CheckIfShadowingIsAvailable,
        InterceptionStep.SelectPlayerForInterception,
        DodgeRoll.ChooseToUseTackle,
        DodgeRoll.ChooseToUsePrehensileTail,
    )

    private val nodesForContextMenu = setOf(
        JumpStep.SelectTargetSquareOrCancel,
        LeapStep.SelectTargetSquareOrCancel,
        PogoStep.SelectTargetSquareOrCancel,
    )

    // We track these here until we get a better API. These are actually handled elsewhere,
    // but need to be swallowed here to avoid showing up up as an unhandled action.
    private val swallowNodes = setOf(
        Charge.SelectPlayersToActivate,
        ResolveBallLandingOnField.InactiveTeamChoosesDivingCatchPlayers,
        ResolveBallLandingOnField.ActiveTeamChoosesDivingCatchPlayers
    )

    override fun isApplicable(state: Game, request: ActionRequest): Boolean {
        val currentNode = state.stack.currentNode()
        return nodesForGameStatusButton.contains(currentNode)
            || nodesForContextMenu.contains(currentNode)
            || swallowNodes.contains(currentNode)
    }

    override fun decorate(
        actionProvider: ManualActionProvider,
        state: Game,
        descriptor: CancelWhenReady,
        owner: Team?,
        acc: UiSnapshotAccumulator
    ) {
        // Some Cancel events are actually handled elsewhere, so just swallow them here
        if (state.stack.currentNode() in swallowNodes) {
            return
        }

        val currentNode = state.stack.currentNode()
        if (currentNode in nodesForGameStatusButton) {
            // For these actions we want to have them in as the Game Status Button
            val title = when (state.stack.currentNode()) {
                UseShadowingStep.CheckIfShadowingIsAvailable -> "Do not use Shadowing"
                InterceptionStep.SelectPlayerForInterception -> "Do not intercept"
                DodgeRoll.ChooseToUseTackle -> "Do not use Tackle"
                DodgeRoll.ChooseToUsePrehensileTail -> "Do not use Prehensile Tail"
                else -> error("Unsupported node: ${state.stack.currentNode()}")
            }
            acc.updateGameStatus {
                it.copy(
                    centerBadgeText = title,
                    centerBadgeAction = { actionProvider.userActionSelected(Cancel) },
                    centerBadgeEnabled = true
                )
            }
        } else {
            // For these nodes, we want to expose it as a "cancel" context menu option
            val title = when (state.stack.currentNode()) {
                JumpStep.SelectTargetSquareOrCancel -> "Cancel Jump"
                LeapStep.SelectTargetSquareOrCancel -> "Cancel Leap"
                PogoStep.SelectTargetSquareOrCancel -> "Cancel Pogo"
                else -> error("Unsupported node: ${state.stack.currentNode()}")
            }
            val coordinates = state.activePlayer?.coordinates ?: error("Missing active player")
            acc.updateSquare(coordinates) {
                it.copy(
                    contextMenuOptions = it.contextMenuOptions.add(
                        ContextMenuOption(
                            title,
                            { actionProvider.userActionSelected(Cancel) },
                            ActionIcon.CANCEL
                        )
                    )
                )
            }
        }
    }
}
