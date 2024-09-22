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
import dk.ilios.jervis.model.context.ProcedureContext
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
 * Context for a "Block Action". This context only tracks the top-level state relevant to a block action.
 * All state related to the type of block is tracked in the relevant contexts.
 */
data class BlockActionContext(
    val attacker: Player,
    val defender: Player,
    val blockType: BlockType? = null,
    val aborted: Boolean = false,
): ProcedureContext

/**
 * Procedure for controlling a player's Standard Block action. Multiple Block, Stab, Projectile Vomit etc. have
 * their own actions.
 *
 * See page 56 in the rulebook.
 *
 * Developer's Commentary:
 * A block action consists of quite a few steps, and because Multiple Block require us to run these in lock-step,
 * it means we need to split them up into multiple procedures so we can switch context after each step.
 *
 * This means that this complexity also bleeds into normal single blocks, at least if we want to avoid duplicating
 * the logic.
 *
 * For that reason, any action that is either a "block action" or a "special action" that can replace a block, it must
 * fulfill the following requirements:
 *
 * 1. Have an enum defined in [dk.ilios.jervis.rules.BlockType]
 *
 * 2. It must split its behavior into sub-procedures that cover the following phases:
 *    a. Select Modifiers (e.g. assists, Horns, Dauntless)
 *    b. Roll block dice or dice that isn't injury/armour rolls, e.g. Projectile Vomit roll to see who is hit.
 *    c. Select type of reroll or keep the result.
 *    d. Reroll dice using the selected reroll.
 *    e. For blocks with multiple dice you have to choose the final result.
 *    f. Apply the final result (multiple blocks also affect injury rolls, but this is handled in RiskingInjuryRoll)
 *    g. Handle injuries
 *
 * 3. It is up to [StandardBlockStep] and [MultipleBlockStep] to correctly set up the call order of these as well
 *    making sure that they have the correct context's set.
 */
@Serializable
object BlockAction : Procedure() {
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
