package com.jervisffb.ui.game.state.decorators

import com.jervis.generated.SettingsKeys
import com.jervisffb.engine.actions.Cancel
import com.jervisffb.engine.actions.Confirm
import com.jervisffb.engine.actions.MoveType
import com.jervisffb.engine.actions.MoveTypeSelected
import com.jervisffb.engine.actions.SelectMoveType
import com.jervisffb.engine.common.context.ActivatePlayerContext
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.isSkillAvailable
import com.jervisffb.engine.model.locations.OnPitchLocation
import com.jervisffb.engine.model.locations.PitchCoordinate
import com.jervisffb.engine.rules.common.actions.PlayerStandardActionType
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.ui.SETTINGS_MANAGER
import com.jervisffb.ui.game.UiSnapshotAccumulator
import com.jervisffb.ui.game.icons.ActionIcon
import com.jervisffb.ui.game.model.UiAction
import com.jervisffb.ui.game.state.ManualActionProvider
import com.jervisffb.ui.game.state.QueuedActionsResult
import com.jervisffb.ui.game.view.SimpleContextMenuOption
import com.jervisffb.ui.game.view.ToggleContextMenuOption

object SelectMoveTypeDecorator: PitchActionDecorator<SelectMoveType> {

    // Actions we allow to skip manually selecting Stand Up
    val eligibleActions = setOf(
        PlayerStandardActionType.MOVE,
        PlayerStandardActionType.FOUL,
        PlayerStandardActionType.PASS,
        PlayerStandardActionType.BLITZ,
        PlayerStandardActionType.HAND_OFF,
        PlayerStandardActionType.THROW_TEAM_MATE
    )

    override fun decorate(
        actionProvider: ManualActionProvider,
        state: Game,
        descriptor: SelectMoveType,
        owner: Team?,
        acc: UiSnapshotAccumulator
    ) {
        val hasLeapAndJump = descriptor.types.contains(MoveType.LEAP) && descriptor.types.contains(MoveType.JUMP)
        val hideJump = hasLeapAndJump && SETTINGS_MANAGER.getBoolean(SettingsKeys.JERVIS_UI_HIDE_JUMP_IF_LEAP_IS_AVAILABLE_VALUE, false)
        descriptor.types.forEach {
            when (it) {
                MoveType.JUMP -> {
                    if (!hideJump) handleType(actionProvider, state, acc, it)
                }
                else -> handleType(actionProvider, state, acc, it)
            }
        }
    }

    private fun moveTypeAction(actionProvider: ManualActionProvider, type: MoveType): UiAction {
        val action = MoveTypeSelected(type)
        return UiAction(action) { actionProvider.userActionSelected(action) }
    }

    private fun handleType(actionProvider: ManualActionProvider, state: Game, acc: UiSnapshotAccumulator, type: MoveType) {
        val player = state.activePlayer ?: error("No active player")
        val activeLocation = player.location as PitchCoordinate

        // For move selection, some types of moves we want to display on the pitch
        // others should be a specific action that must be selected.
        // On-pitch moves are shortcutting the Rules engine, so we need to account for that as well
        when (type) {
            MoveType.JUMP -> {
                acc.updateSquare(activeLocation) {
                    it.copy(
                        contextMenuOptions = it.contextMenuOptions.add(
                            SimpleContextMenuOption(
                                "Jump",
                                moveTypeAction(actionProvider, MoveType.JUMP),
                                ActionIcon.JUMP
                            )
                        )
                    )
                }
            }

            MoveType.LEAP -> {
                acc.updateSquare(activeLocation) {
                    it.copy(
                        contextMenuOptions = it.contextMenuOptions.add(
                            SimpleContextMenuOption(
                                "Leap",
                                moveTypeAction(actionProvider, MoveType.LEAP),
                                ActionIcon.LEAP
                            )
                        )
                    )
                }
            }

            MoveType.POGO -> {
                acc.updateSquare(activeLocation) {
                    it.copy(
                        contextMenuOptions = it.contextMenuOptions.add(
                            SimpleContextMenuOption(
                                "Pogo",
                                moveTypeAction(actionProvider, MoveType.POGO),
                                ActionIcon.LEAP
                            )
                        )
                    )
                }
            }

            MoveType.STANDARD -> {
                val movePlan = state.rulesContext.actionPlanner.createMovePlan(state, player)
                val neighborMovePlan = state.rulesContext.actionPlanner.createMovePlan(
                    state,
                    player,
                    includeDodges = true,
                    includeRushes = true,
                )
                val displayPlan = movePlan.withImmediateMoves(neighborMovePlan.neighborMoves)
                acc.movePlan = displayPlan

                displayPlan.neighborMoves.forEach { (coordinate, plannedMove) ->
                    acc.updateSquare(coordinate) {
                        it.copy(
                            selectedAction = UiAction(plannedMove.action) {
                                actionProvider.userActionSelected(plannedMove.action)
                            },
                            requiresRoll = plannedMove.requiresRoll,
                        )
                    }
                }

                if (player.isSkillAvailable(SkillType.FUMBLEROOSKI) && player.hasBall()) {
                    // If player has Fumblerooski, they can enable it before-hand here
                    acc.updateSquare(player.coordinates) {
                        // Both commands only vary with which player is toggling Fumblerooski;
                        // everything else they touch is read live at click time. See [UiAction].
                        val useFumblerooski = ToggleContextMenuOption.ContextData(
                            "Use Fumblerooski when moving next",
                            ActionIcon.FUMBLEROOSKI_USE,
                            UiAction(Pair("useFumblerooski", player.id)) {
                                val uiController = acc.uiController
                                // We need to reset the UI decoration at the correct place when Undo'ing actions
                                uiController.uiDecorations.registerUndo(uiController.gameController.currentActionIndex()) {
                                    actionProvider.nextFumblerooskiCommand(player, null)
                                }
                                actionProvider.nextFumblerooskiCommand(player, Confirm)
                            },
                        )
                        val cancelFumblerooski = ToggleContextMenuOption.ContextData(
                            "Cancel Fumblerooski on next move",
                            ActionIcon.FUMBLEROOSKI_CANCEL,
                            UiAction(Pair("cancelFumblerooski", player.id)) {
                                actionProvider.nextFumblerooskiCommand(player, Cancel)
                            },
                        )
                        it.copy(
                            contextMenuOptions = it.contextMenuOptions.add(
                                ToggleContextMenuOption(
                                    useFumblerooski
                                )  {
                                    val isFumblerooskiEnabled = (actionProvider.nextFumblerooskiCommand == Confirm)
                                    when (isFumblerooskiEnabled) {
                                        true -> cancelFumblerooski
                                        false -> useFumblerooski
                                    }
                                }
                            )
                        )
                    }
                }
            }

            MoveType.STAND_UP -> {
                // Add Standing Up Action to the context menu.
                acc.updateSquare(activeLocation) {
                    it.copy(
                        contextMenuOptions = it.contextMenuOptions.add(
                            SimpleContextMenuOption(
                                "Stand-Up",
                                moveTypeAction(actionProvider, MoveType.STAND_UP),
                                ActionIcon.STAND_UP
                            )
                        )
                    )
                }
                addStandUpAndMoveOptions(actionProvider, state, player, activeLocation, acc)
            }
        }
    }

    // Add UI options allowing the Coach to skip manually selecting Stand Up
    // and then move. Instead, players can move directly. This should only
    // be available if it is free to stand up.
    private fun addStandUpAndMoveOptions(
        actionProvider: ManualActionProvider,
        state: Game,
        player: Player,
        activeLocation: OnPitchLocation,
        acc: UiSnapshotAccumulator
    ) {
        // For Standing Up, we make it easier for the player depending
        // on their Action. So if there is a move part of their
        // current action, we calculate what they can do after standing
        // up and allow the player to go directly that.
        val action = state.getContext<ActivatePlayerContext>().declaredAction?.type
        if (!eligibleActions.contains(action)) return

        val movePlan = state.rulesContext.actionPlanner.createMovePlan(state, player)
        val immediateMovePlan = state.rulesContext.actionPlanner.createMovePlan(
            state,
            player,
            includeDodges = true,
            includeRushes = true,
        )
        val displayPlan = movePlan.withImmediateMoves(immediateMovePlan.neighborMoves)
        acc.movePlan = displayPlan
        displayPlan.neighborMoves.forEach { (coordinate, plannedMove) ->
            acc.updateSquare(coordinate) {
                it.copy(
                    selectedAction = UiAction(Pair("standUpThenMoveTo", coordinate)) {
                        actionProvider.registerQueuedActionGenerator { controller ->
                            val availableActions = controller.getAvailableActions()
                            val canMove = availableActions.contains(MoveType.STANDARD)
                            if (canMove) {
                                val currentPlan = controller.state.rulesContext.actionPlanner.createMovePlan(
                                    controller.state,
                                    player,
                                    includeDodges = true,
                                    includeRushes = true,
                                )
                                currentPlan.neighborMoves[coordinate]
                                    ?.action
                                    ?.let(::QueuedActionsResult)
                            } else {
                                null
                            }
                        }
                        actionProvider.userActionSelected(MoveTypeSelected(MoveType.STAND_UP))
                    },
                    requiresRoll = plannedMove.requiresRoll,
                )
            }
        }
    }
}
