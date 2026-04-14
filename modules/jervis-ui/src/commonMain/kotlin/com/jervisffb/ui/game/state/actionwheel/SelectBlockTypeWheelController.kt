package com.jervisffb.ui.game.state.actionwheel

import com.jervisffb.engine.ActionRequest
import com.jervisffb.engine.actions.BlockTypeSelected
import com.jervisffb.engine.actions.SelectBlockType
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.locations.PitchCoordinate
import com.jervisffb.engine.rules.common.actions.BlockType
import com.jervisffb.ui.game.UiSnapshotAccumulator
import com.jervisffb.ui.game.dialogs.wheel.ActionButtonData
import com.jervisffb.ui.game.dialogs.wheel.ButtonId
import com.jervisffb.ui.game.dialogs.wheel.ButtonLayoutMode
import com.jervisffb.ui.game.dialogs.wheel.MenuExpandMode
import com.jervisffb.ui.game.icons.ActionIcon
import com.jervisffb.ui.game.state.UiActionProvider
import com.jervisffb.ui.game.view.ActionWheelUiStateData
import com.jervisffb.ui.menu.LocalPitchDataWrapper

/**
 * Select a player action after selecting the player.
 */
object SelectBlockTypeWheelController : ActionWheelDialogController() {
    override val nodes: Set<Node> = setOf(
        com.jervisffb.engine.rules.bb2020.procedures.actions.block.BlockAction.SelectBlockType,
        com.jervisffb.engine.rules.bb2025.procedures.actions.block.BlockAction.SelectBlockType,
        com.jervisffb.engine.rules.common.procedures.actions.blitz.BlitzAction.SelectBlockType
    )

    override fun getActionWheelCenter(state: Game): PitchCoordinate {
        return state.activePlayerOrThrow().coordinates
    }

    override fun onDecorateActions(
        acc: UiSnapshotAccumulator,
        provider: UiActionProvider,
        actions: ActionRequest,
        sharedData: LocalPitchDataWrapper
    ) {
        val state = acc.game

        val wheelOptions = actions.get<SelectBlockType>().types.map {
            val id = ButtonId("[BlockType] ${it.name}")
            createActionOption(id, acc.game, provider, it)
        }.toMutableList()

        val wheelState = ActionWheelUiStateData(
            center = getActionWheelCenter(acc.game),
            bottomItems = wheelOptions,
            bottomExpandMode = MenuExpandMode.FanOut(spread = 360f),
            bottomAnimationType = ButtonLayoutMode.EXPEND_NEW_SUBMENU,
            onDismiss = null,
            hideWhenClickOutside = false
        )
        acc.addActionWheelEvent(wheelState)
    }

    // Temporary work-around while transition from PitchDecorator api
    private fun createActionOption(
        id: ButtonId,
        state: Game,
        provider: UiActionProvider,
        blockType: BlockType
    ): ActionButtonData {

        val (title, icon) = when (blockType) {
            BlockType.BREATHE_FIRE -> "Breathe Fire" to ActionIcon.BREATHE_FIRE
            BlockType.CHAINSAW ->  "Chainsaw" to ActionIcon.CHAINSAW
            BlockType.MULTIPLE_BLOCK ->  "Multiple Block" to ActionIcon.MULTIPLE_BLOCK
            BlockType.PROJECTILE_VOMIT ->  "Projectile Vomit" to ActionIcon.PROJECTILE_VOMIT
            BlockType.STAB ->  "Stab" to ActionIcon.STAB
            BlockType.STANDARD ->  "Block" to ActionIcon.BLOCK
        }
        return ActionButtonData(
            id = id,
            label = { title },
            icon = icon,
            action = { provider.userActionSelected(BlockTypeSelected(blockType)) },
            enabled = true,
        )
    }
}
