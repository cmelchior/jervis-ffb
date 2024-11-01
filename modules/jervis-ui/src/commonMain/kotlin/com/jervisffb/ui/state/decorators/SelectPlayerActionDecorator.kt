package com.jervisffb.ui.state.decorators

import com.jervisffb.engine.actions.PlayerActionSelected
import com.jervisffb.engine.actions.SelectPlayerAction
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.rules.PlayerAction
import com.jervisffb.engine.rules.PlayerSpecialActionType
import com.jervisffb.engine.rules.PlayerStandardActionType
import com.jervisffb.ui.UiGameSnapshot
import com.jervisffb.ui.state.UiActionProvider
import com.jervisffb.ui.view.ContextMenuOption

class SelectPlayerActionDecorator: FieldActionDecorator<SelectPlayerAction> {
    override fun decorate(actionProvider: UiActionProvider, state: Game, snapshot: UiGameSnapshot, descriptor: SelectPlayerAction) {
        // TODO Fix this, so we do not update each square multiple times
        descriptor.actions.forEach {
            handleAction(actionProvider, state, snapshot, it)
        }
    }

    private fun handleAction(actionProvider: UiActionProvider, state: Game, snapshot: UiGameSnapshot, action: PlayerAction) {
        state.activePlayer?.location?.let { location ->
            val oldData = snapshot.fieldSquares[location]!!
            snapshot.fieldSquares[location as FieldCoordinate] =
                oldData.copyAddContextMenu(
                    action.let {
                        val name = when (it.type) {
                            PlayerStandardActionType.MOVE -> "Move"
                            PlayerStandardActionType.PASS -> "Pass"
                            PlayerStandardActionType.HAND_OFF -> "Hand-off"
                            PlayerStandardActionType.BLOCK -> "Block"
                            PlayerStandardActionType.BLITZ -> "Blitz"
                            PlayerStandardActionType.FOUL -> "Foul"
                            PlayerStandardActionType.SPECIAL -> "Special"
                            PlayerStandardActionType.THROW_TEAM_MATE -> "Throw Team-mate"
                            PlayerSpecialActionType.BALL_AND_CHAIN -> "Ball & Chain"
                            PlayerSpecialActionType.BOMBARDIER -> "Bombardier"
                            PlayerSpecialActionType.BREATHE_FIRE -> "Breathe Fire"
                            PlayerSpecialActionType.CHAINSAW -> "Chainsaw"
                            PlayerSpecialActionType.HYPNOTIC_GAZE -> "Hypnotic Gaze"
                            PlayerSpecialActionType.KICK_TEAM_MATE -> "Kick Team-mate"
                            PlayerSpecialActionType.MULTIPLE_BLOCK -> "Multiple Block"
                            PlayerSpecialActionType.PROJECTILE_VOMIT -> "Projectile Vomit"
                            PlayerSpecialActionType.STAB -> "Stab"
                        }
                        ContextMenuOption(
                            title = name,
                            command = { actionProvider.userActionSelected(PlayerActionSelected(it.type)) },
                        )
                    },
                )
        } ?: error("No active player")
    }
}
