package dk.ilios.jervis.procedures.actions.block

import compositeCommandOf
import dk.ilios.jervis.actions.ActionDescriptor
import dk.ilios.jervis.actions.BlockTypeSelected
import dk.ilios.jervis.actions.DeselectPlayer
import dk.ilios.jervis.actions.EndAction
import dk.ilios.jervis.actions.EndActionWhenReady
import dk.ilios.jervis.actions.GameAction
import dk.ilios.jervis.actions.PlayerDeselected
import dk.ilios.jervis.actions.PlayerSelected
import dk.ilios.jervis.actions.SelectBlockType
import dk.ilios.jervis.actions.SelectPlayer
import dk.ilios.jervis.commands.Command
import dk.ilios.jervis.commands.RemoveContext
import dk.ilios.jervis.commands.SetContext
import dk.ilios.jervis.commands.fsm.ExitProcedure
import dk.ilios.jervis.commands.fsm.GotoNode
import dk.ilios.jervis.fsm.ActionNode
import dk.ilios.jervis.fsm.Node
import dk.ilios.jervis.fsm.ParentNode
import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.Team
import dk.ilios.jervis.model.context.getContext
import dk.ilios.jervis.model.hasSkill
import dk.ilios.jervis.procedures.ActivatePlayerContext
import dk.ilios.jervis.rules.BlockType
import dk.ilios.jervis.rules.Rules
import dk.ilios.jervis.rules.skills.MultipleBlock
import dk.ilios.jervis.rules.skills.Stab
import dk.ilios.jervis.utils.INVALID_ACTION
import kotlinx.serialization.Serializable

/**
 * Procedure for controlling a player's Block action.
 *
 * See page 56 in the rulebook.
 */
@Serializable
object BlockAction : Procedure() {
    override val initialNode: Node = SelectDefenderOrEndAction
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null

    object SelectDefenderOrEndAction : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.activePlayer!!.team

        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
            val end: List<ActionDescriptor> = listOf(EndActionWhenReady)

            val attacker = state.activePlayer!!
            val eligibleDefenders: List<ActionDescriptor> =
                attacker.coordinates.getSurroundingCoordinates(rules)
                    .filter { state.field[it].isOccupied() }
                    .filter { state.field[it].player!!.team != attacker.team }
                    .map { SelectPlayer(state.field[it].player!!) }

            return end + eligibleDefenders
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return when (action) {
                EndAction -> {
                    val activeContext = state.getContext<ActivatePlayerContext>()
                    compositeCommandOf(
                        SetContext(activeContext.copy(markActionAsUsed = false)),
                        ExitProcedure()
                    )
                }
                is PlayerSelected -> {
                    val context =
                        BlockContext(
                            attacker = state.activePlayer!!,
                            defender = action.getPlayer(state),
                            isBlitzing = false
                        )
                    compositeCommandOf(
                        SetContext(context),
                        GotoNode(ResolveBlock),
                    )
                }
                else -> INVALID_ACTION(action)
            }
        }
    }

    object SelectBlockTypeOrUndoPlayerSelection : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<BlockContext>().attacker.team
        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
            val context = state.getContext<BlockContext>()
            val attacker = context.attacker
            val availableBlockTypes = buildList<BlockType> {
                add(BlockType.STANDARD)
                if (attacker.hasSkill<Stab>()) {
                    add(BlockType.STAB)
                }
//                if (attacker.getSkillOrNull<ChainSaw>().used != false) {
//                    add(BlockActionType.CHAINSAW)
//                }
                if (attacker.getSkillOrNull<MultipleBlock>()?.used == false) {
                    add(BlockType.MULTIPLE_BLOCK)
                }
//                if (attacker.getSkillOrNull<ProjectileVomit>()?.used == false) {
//                    add(BlockActionType.PROJECTILE_VOMIT)
//                }
            }

            return listOf(
                DeselectPlayer(context.attacker),
                SelectBlockType(availableBlockTypes)
            )
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return when (action) {
                is PlayerDeselected -> {
                    compositeCommandOf(
                        RemoveContext<BlockContext>(),
                        GotoNode(SelectDefenderOrEndAction)
                    )
                }
                else -> {
                    checkTypeAndValue<BlockTypeSelected>(state, rules, action, this) { blockType ->
                        val context = state.getContext<BlockContext>()
                        compositeCommandOf(
                            SetContext(context.copy(blockType = blockType.type)),
                            GotoNode(ResolveBlock)
                        )
                    }
                }
            }
        }
    }


    object ResolveBlock : ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure {
            val context = state.getContext<BlockContext>()
            return when (context.blockType!!) {
                BlockType.CHAINSAW -> TODO()
                BlockType.MULTIPLE_BLOCK -> MultipleBlockStep
                BlockType.PROJECTILE_VOMIT -> TODO()
                BlockType.STAB -> TODO()
                BlockType.STANDARD -> BlockStep
            }
        }
        override fun onExitNode(state: Game, rules: Rules): Command {
            // Regardless of the outcome of the block, the action is over
            val activeContext = state.getContext<ActivatePlayerContext>()
            return compositeCommandOf(
                SetContext(activeContext.copy(markActionAsUsed = true)), // TODO Sub procedures should be able to communicate if used or not
                ExitProcedure()
            )
        }
    }
}
