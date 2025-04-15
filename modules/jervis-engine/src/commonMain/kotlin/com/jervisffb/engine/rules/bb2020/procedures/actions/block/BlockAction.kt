package com.jervisffb.engine.rules.bb2020.procedures.actions.block

import com.jervisffb.engine.actions.BlockTypeSelected
import com.jervisffb.engine.actions.EndAction
import com.jervisffb.engine.actions.EndActionWhenReady
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.actions.SelectBlockType
import com.jervisffb.engine.actions.SelectPlayer
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.RemoveContext
import com.jervisffb.engine.commands.SetContext
import com.jervisffb.engine.commands.SetSkillUsed
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.ParentNode
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.fsm.checkTypeAndValue
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.PlayerState
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.context.ProcedureContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.context.getContextOrNull
import com.jervisffb.engine.model.isSkillAvailable
import com.jervisffb.engine.rules.BlockType
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.bb2020.procedures.ActivatePlayerContext
import com.jervisffb.engine.rules.bb2020.skills.BreatheFire
import com.jervisffb.engine.rules.bb2020.skills.Frenzy
import com.jervisffb.engine.rules.bb2020.skills.ProjectileVomit
import com.jervisffb.engine.rules.bb2020.skills.Stab
import com.jervisffb.engine.utils.INVALID_ACTION
import kotlinx.serialization.Serializable

/**
 * Context for a "Block Action". This context only tracks the top-level state relevant to a block action.
 * All state related to the type of block is tracked in the relevant contexts.
 */
data class BlockActionContext(
    val attacker: Player,
    val defender: Player,
    val blockType: BlockType? = null,
    val hasBlocked: Boolean = false,
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
 * 1. Have an enum defined in [com.jervisffb.rules.BlockType]
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
 * 3. It is up to [StandardBlockStep] and [MultipleBlockAction] to correctly set up the call order of these as well
 *    making sure that they have the correct context's set.
 */
@Serializable
object BlockAction : Procedure() {
    override val initialNode: Node = SelectDefenderOrEndAction
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command {
        val activatePlayerContext = state.getContext<ActivatePlayerContext>()
        val blockActionContext = state.getContextOrNull<BlockActionContext>()
        return compositeCommandOf(
            SetContext(activatePlayerContext.copy(markActionAsUsed = (blockActionContext?.hasBlocked == true))),
            RemoveContext<BlockContext>(),
            RemoveContext<BlockActionContext>()
        )
    }

    object SelectDefenderOrEndAction : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.activePlayer!!.team
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val end: List<GameActionDescriptor> = listOf(EndActionWhenReady)

            val attacker = state.activePlayer!!
            val eligibleDefenders: List<GameActionDescriptor> =
                attacker.coordinates.getSurroundingCoordinates(rules)
                    .filter { state.field[it].isOccupied() }
                    .filter { state.field[it].player!!.team != attacker.team }
                    .map { state.field[it].player!! }
                    .filter { it.state == PlayerState.STANDING }
                    .map { player -> SelectPlayer(player) }

            return end + eligibleDefenders
        }
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return when (action) {
                is EndAction -> ExitProcedure()
                is PlayerSelected -> {
                    val context = BlockActionContext(
                        attacker = state.activePlayer!!,
                        defender = action.getPlayer(state),
                    )
                    compositeCommandOf(
                        SetContext(context),
                        GotoNode(SelectBlockType),
                    )
                }
                else -> INVALID_ACTION(action)
            }
        }
    }

    object SelectBlockType : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<BlockActionContext>().attacker.team
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val attacker = state.getContext<BlockActionContext>().attacker
            val availableBlockTypes = getAvailableBlockType(attacker, true)
            return listOf(
                SelectBlockType(availableBlockTypes),
                EndActionWhenReady
            )
        }
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            val context = state.getContext<BlockActionContext>()
            return when (action) {
                is EndAction -> ExitProcedure()
                else -> {
                    checkTypeAndValue<BlockTypeSelected>(state, rules, action) { typeSelected ->
                        val type = typeSelected.type
                        compositeCommandOf(
                            SetContext(context.copy(blockType = typeSelected.type)),
                            GotoNode(ResolveBlock),
                        )
                    }
                }
            }
        }
    }

    object ResolveBlock : ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command {
            val context = state.getContext<BlockActionContext>()
            return when (context.blockType!!) {
                BlockType.BREATHE_FIRE -> TODO()
                BlockType.CHAINSAW -> TODO()
                BlockType.MULTIPLE_BLOCK -> TODO()
                BlockType.PROJECTILE_VOMIT -> TODO()
                BlockType.STAB -> TODO()
                BlockType.STANDARD -> {
                    SetContext(
                        BlockContext(
                            context.attacker,
                            context.defender,
                            isBlitzing = false
                        )
                    )
                }
            }
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure {
            val context = state.getContext<BlockActionContext>()
            return when (context.blockType!!) {
                BlockType.BREATHE_FIRE -> TODO()
                BlockType.CHAINSAW -> TODO()
                BlockType.MULTIPLE_BLOCK -> TODO()
                BlockType.PROJECTILE_VOMIT -> TODO()
                BlockType.STAB -> TODO()
                BlockType.STANDARD -> StandardBlockStep
            }
        }

        override fun onExitNode(state: Game, rules: Rules): Command {
            // If player is not standing on the field after the move, it is a turn over,
            // otherwise they are free to continue their blitz
            // TODO This approach to turn overs might not be correct, i.e. a goal
            // could have been scored after a Blitz
            val context = state.getContext<BlockActionContext>()

            // Check if Block Action was completed or not
            val removeContextCommand = when (context.blockType!!) {
                BlockType.BREATHE_FIRE -> TODO()
                BlockType.CHAINSAW -> TODO()
                BlockType.MULTIPLE_BLOCK -> TODO()
                BlockType.PROJECTILE_VOMIT -> TODO()
                BlockType.STAB -> TODO()
                BlockType.STANDARD -> RemoveContext<BlockContext>()
            }

            // Remove state required for the specific block type
            val hasBlocked = when (context.blockType) {
                BlockType.BREATHE_FIRE -> TODO()
                BlockType.CHAINSAW -> TODO()
                BlockType.MULTIPLE_BLOCK -> TODO()
                BlockType.PROJECTILE_VOMIT -> TODO()
                BlockType.STAB -> TODO()
                BlockType.STANDARD -> !state.getContext<BlockContext>().aborted
            }

            val didFollowUp = when (context.blockType) {
                BlockType.BREATHE_FIRE -> TODO()
                BlockType.CHAINSAW -> false
                BlockType.MULTIPLE_BLOCK -> false
                BlockType.PROJECTILE_VOMIT -> false
                BlockType.STAB -> false
                BlockType.STANDARD -> !state.getContext<BlockContext>().didFollowUp
            }

            // After the Push was resolved, if the target is still standing
            // and the attacker has frenzy and was able to follow up, a
            // second block is thrown
            val hasFrenzy = context.attacker.isSkillAvailable<Frenzy>()
            val isNextToTarget = (
                rules.isStanding(context.attacker) &&
                    rules.isStanding(context.defender) &&
                    context.attacker.coordinates
                        .getSurroundingCoordinates(rules, distance = 1)
                        .contains(context.defender.coordinates)
            )

            return if (hasBlocked && hasFrenzy && isNextToTarget) {
                compositeCommandOf(
                    removeContextCommand,
                    SetContext(context.copy(hasBlocked = hasBlocked)),
                    SetSkillUsed(context.attacker, context.attacker.getSkill<Frenzy>(), true),
                    GotoNode(SelectBlockType),
                )
            } else {
                compositeCommandOf(
                    removeContextCommand,
                    SetContext(context.copy(hasBlocked = hasBlocked)),
                    ExitProcedure()
                )
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
                    BlockType.BREATHE_FIRE -> if (player.isSkillAvailable<BreatheFire>()) add(type)
                    BlockType.CHAINSAW -> if (player.isSkillAvailable<ProjectileVomit>()) add(type)
                    BlockType.MULTIPLE_BLOCK -> if (!isMultipleBlock) add(type)
                    BlockType.PROJECTILE_VOMIT -> if (player.isSkillAvailable<ProjectileVomit>()) add(type)
                    BlockType.STAB -> if (player.isSkillAvailable<Stab>()) add(type)
                    BlockType.STANDARD -> add(type)
                }
            }
        }
    }
}
