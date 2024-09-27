package dk.ilios.jervis.procedures.actions.block

import buildCompositeCommand
import compositeCommandOf
import dk.ilios.jervis.actions.ActionDescriptor
import dk.ilios.jervis.actions.EndAction
import dk.ilios.jervis.actions.EndActionWhenReady
import dk.ilios.jervis.actions.GameAction
import dk.ilios.jervis.actions.PlayerSelected
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
import dk.ilios.jervis.model.Player
import dk.ilios.jervis.model.Team
import dk.ilios.jervis.model.context.getContext
import dk.ilios.jervis.model.hasSkill
import dk.ilios.jervis.procedures.ActivatePlayerContext
import dk.ilios.jervis.rules.BlockType
import dk.ilios.jervis.rules.Rules
import dk.ilios.jervis.rules.skills.ProjectileVomit
import dk.ilios.jervis.rules.skills.Stab
import dk.ilios.jervis.utils.INVALID_ACTION
import kotlinx.serialization.Serializable

/**
 * Procedure for handling the Stab special action as described on page 86 in the rulebook
 */
@Serializable
object StabAction : Procedure() {
    override val initialNode: Node = SelectDefenderOrEndAction
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command {
        return compositeCommandOf(
            RemoveContext<BlockContext>(),
            RemoveContext<BlockActionContext>()
        )

    }

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
                    val context = BlockActionContext(
                        attacker = state.activePlayer!!,
                        defender = action.getPlayer(state),
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

    object ResolveBlock : ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command {
            val actionContext = state.getContext<BlockActionContext>()
            return SetContext(BlockContext(
                attacker = actionContext.attacker,
                defender = actionContext.defender,
                blockType = BlockType.STANDARD
            ))
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = StandardBlockStep
        override fun onExitNode(state: Game, rules: Rules): Command {
            // Regardless of the outcome of the block, the action is over
            val activeContext = state.getContext<ActivatePlayerContext>()
            val actionContext = state.getContext<BlockActionContext>()
            return buildCompositeCommand {
                if (!actionContext.aborted) {
                    add(SetContext(activeContext.copy(markActionAsUsed = true)))
                }
                add(ExitProcedure())
            }
        }
    }

    // ------------------------------------------------------------------------------------------------------------
    // HELPER FUNCTIONS

    /**
     * Return all available block types available to a given player.
     */
    fun getAvailableBlockType(player: Player, isMultipleBlock: Boolean): List<BlockType> {
        return buildList {
            BlockType.entries.forEach { type ->
                when (type) {
                    BlockType.CHAINSAW -> if (player.getSkillOrNull<ProjectileVomit>()?.used == false) add(type)
                    BlockType.MULTIPLE_BLOCK -> if (!isMultipleBlock) add(type)
                    BlockType.PROJECTILE_VOMIT -> if (player.getSkillOrNull<ProjectileVomit>()?.used == false) add(type)
                    BlockType.STAB -> if (player.hasSkill<Stab>()) add(type)
                    BlockType.STANDARD -> add(type)
                }
            }
        }
    }
}
