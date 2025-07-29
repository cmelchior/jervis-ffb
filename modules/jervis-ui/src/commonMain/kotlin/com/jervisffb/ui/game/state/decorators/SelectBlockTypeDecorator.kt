package com.jervisffb.ui.game.state.decorators

import com.jervisffb.engine.actions.BlockTypeSelected
import com.jervisffb.engine.actions.SelectBlockType
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.context.MultipleBlockContext
import com.jervisffb.engine.model.context.getContextOrNull
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.rules.BlockType
import com.jervisffb.engine.rules.bb2020.procedures.actions.blitz.BlitzActionContext
import com.jervisffb.engine.rules.bb2020.procedures.actions.block.BlockActionContext
import com.jervisffb.ui.game.UiGameSnapshot
import com.jervisffb.ui.game.icons.ActionIcon
import com.jervisffb.ui.game.state.ManualActionProvider
import com.jervisffb.ui.game.view.ContextMenuOption

// When a target for the block has been selected, show the context menu for the kind of block
// to perform.
class SelectBlockTypeDecorator: FieldActionDecorator<SelectBlockType> {
    override fun decorate(actionProvider: ManualActionProvider, state: Game, snapshot: UiGameSnapshot, descriptor: SelectBlockType) {
        val blockContext = state.getContextOrNull<BlockActionContext>()
        val blitzContext = state.getContextOrNull<BlitzActionContext>()
        val multipleBlockContext = state.getContextOrNull<MultipleBlockContext>()
        val defender = blockContext?.defender
            ?: blitzContext?.defender
            ?: multipleBlockContext?.getActiveDefender()
            ?: error("Could not find defender")

        val activeLocation = defender.location as FieldCoordinate // Missing Giant support

        descriptor.types.forEach { type ->
            when (type) {
                BlockType.BREATHE_FIRE -> TODO()
                BlockType.CHAINSAW -> TODO()
                BlockType.PROJECTILE_VOMIT -> TODO()
                BlockType.STAB -> TODO()
                BlockType.MULTIPLE_BLOCK,
                BlockType.STANDARD -> {
                    val activeSquare = snapshot.fieldSquares[activeLocation] ?: error("Could not find square: $activeLocation")
                    snapshot.fieldSquares[activeLocation] = activeSquare.copyAddContextMenu(
                        item = ContextMenuOption(
                            "Block",
                            { actionProvider.userActionSelected(BlockTypeSelected(BlockType.STANDARD)) },
                            ActionIcon.BLOCK
                        ),
                        showContextMenu = true
                    )
                }
            }
        }
    }
}
