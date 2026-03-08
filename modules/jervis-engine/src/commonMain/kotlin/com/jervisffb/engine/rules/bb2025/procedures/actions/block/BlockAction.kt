package com.jervisffb.engine.rules.bb2025.procedures.actions.block

import com.jervisffb.engine.actions.BlockTypeSelected
import com.jervisffb.engine.actions.EndAction
import com.jervisffb.engine.actions.EndActionWhenReady
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.actions.SelectPlayer
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.SetSkillUsed
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.context.RemoveContext
import com.jervisffb.engine.commands.context.SetContext
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.ParentNode
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.fsm.castAction
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.PlayerState
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.context.ActivatePlayerContext
import com.jervisffb.engine.model.context.BlockActionContext
import com.jervisffb.engine.model.context.BlockContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.context.getContextOrNull
import com.jervisffb.engine.model.isSkillAvailable
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.bb2025.procedures.actions.block.singleblock.SingleStandardBlockStep
import com.jervisffb.engine.rules.common.actions.BlockType
import com.jervisffb.engine.rules.common.procedures.actions.block.BreatheFireContext
import com.jervisffb.engine.rules.common.procedures.actions.block.BreatheFireStep
import com.jervisffb.engine.rules.common.procedures.actions.block.ProjectileVomitContext
import com.jervisffb.engine.rules.common.procedures.actions.block.ProjectileVomitStep
import com.jervisffb.engine.rules.common.procedures.actions.block.StabContext
import com.jervisffb.engine.rules.common.procedures.actions.block.StabStep
import com.jervisffb.engine.rules.common.procedures.getResetPlayerTemporaryModifiersCommands
import com.jervisffb.engine.rules.common.skills.Duration
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.engine.utils.INVALID_ACTION

/**
 * Procedure for controlling a player's Standard Block action. Multiple Block,
 * Stab, Projectile Vomit, etc. have their own actions.
 *
 * See page XXX in the rulebook.
 *
 * Developer's Commentary:
 * A block action consists of quite a few steps, and because Multiple Block
 * requires us to run these in lock-step, it means we need to split them up
 * into multiple procedures so we can switch context after each step.
 *
 * This split looks different in BB2020 and BB2025. See [com.jervisffb.engine.rules.bb2020.procedures.actions.block.StandardBlockStep]
 * for more information about BB2020 and [com.jervisffb.engine.rules.bb2025.procedures.actions.block.singleblock.SingleStandardBlockStep] for BB2025.
 */
object BlockAction : Procedure() {
    override val initialNode: Node = SelectDefenderOrEndAction
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command {
        val activatePlayerContext = state.getContext<ActivatePlayerContext>()
        val blockActionContext = state.getContextOrNull<BlockActionContext>()
        return compositeCommandOf(
            SetContext(activatePlayerContext.copy(markActionAsUsed = (blockActionContext?.hasBlocked == true))),
            RemoveContext<BlockContext>(),
            RemoveContext<BlockActionContext>(),
            *getResetPlayerTemporaryModifiersCommands(state, rules, activatePlayerContext.player, Duration.END_OF_ACTION),
        )
    }

    object SelectDefenderOrEndAction : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.activePlayer!!.team
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val end: List<GameActionDescriptor> = listOf(EndActionWhenReady)

            val attacker = state.activePlayer!!
            val eligibleDefenders: List<GameActionDescriptor> =
                attacker.coordinates.getSurroundingCoordinates(rules)
                    .mapNotNull { state.field[it].player }
                    .filter { it.state == PlayerState.STANDING }
                    .filter { it.team != attacker.team }
                    .let { listOf(SelectPlayer.fromPlayers(it)) }

            return end + eligibleDefenders
        }
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return when (action) {
                is EndAction -> ExitProcedure()
                is PlayerSelected -> {
                    val context = BlockActionContext(
                        attacker = state.activePlayer!!,
                        defender = action.getPlayer(state),
                        blockType = BlockType.STANDARD,
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

    object SelectBlockType : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<BlockActionContext>().attacker.team
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val attacker = state.getContext<BlockActionContext>().attacker
            val availableBlockTypes = getAvailableBlockType(attacker, true)
            return listOf(
                com.jervisffb.engine.actions.SelectBlockType(availableBlockTypes),
                EndActionWhenReady
            )
        }
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            val context = state.getContext<BlockActionContext>()
            return when (action) {
                is EndAction -> ExitProcedure()
                else -> {
                    castAction<BlockTypeSelected>(action) { typeSelected ->
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
            return when (context.blockType) {
                BlockType.CHAINSAW -> TODO()
                BlockType.MULTIPLE_BLOCK -> TODO()
                BlockType.BREATHE_FIRE -> {
                    SetContext(
                        BreatheFireContext(
                            attacker = context.attacker,
                            defender = context.defender,
                        )
                    )
                }
                BlockType.PROJECTILE_VOMIT -> {
                    SetContext(
                        ProjectileVomitContext(
                            attacker = context.attacker,
                            defender = context.defender,
                        )
                    )
                }
                BlockType.STAB -> {
                    SetContext(
                        StabContext(
                            attacker = context.attacker,
                            defender = context.defender,
                        )
                    )
                }
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
            return when (context.blockType) {
                BlockType.CHAINSAW -> TODO()
                BlockType.MULTIPLE_BLOCK -> TODO()
                BlockType.BREATHE_FIRE -> BreatheFireStep
                BlockType.PROJECTILE_VOMIT -> ProjectileVomitStep
                BlockType.STAB -> StabStep
                BlockType.STANDARD -> SingleStandardBlockStep
            }
        }

        override fun onExitNode(state: Game, rules: Rules): Command {
            // If player is not standing on the field after the move, it is a turn over,
            // otherwise they are free to continue their blitz
            // TODO This approach to turn overs might not be correct, i.e. a goal
            // could have been scored after a Blitz
            val context = state.getContext<BlockActionContext>()

            // Check if Block Action was completed or not
            val removeContextCommand = when (context.blockType) {
                BlockType.CHAINSAW -> TODO()
                BlockType.MULTIPLE_BLOCK -> TODO()
                BlockType.BREATHE_FIRE -> RemoveContext<BreatheFireContext>()
                BlockType.PROJECTILE_VOMIT -> RemoveContext<ProjectileVomitContext>()
                BlockType.STAB -> RemoveContext<StabContext>()
                BlockType.STANDARD -> RemoveContext<BlockContext>()
            }

            // Remove state required for the specific block type
            val hasBlocked = when (context.blockType) {
                BlockType.CHAINSAW -> TODO()
                BlockType.MULTIPLE_BLOCK -> TODO()
                BlockType.BREATHE_FIRE -> (state.getContext<BreatheFireContext>().result != null)
                BlockType.PROJECTILE_VOMIT -> (state.getContext<ProjectileVomitContext>().injuryResult != null)
                BlockType.STAB -> (state.getContext<StabContext>().stabResult != null)
                BlockType.STANDARD -> !state.getContext<BlockContext>().aborted
            }

            // After the Push was resolved, if the target is still standing
            // and the attacker has frenzy and was able to follow up, a
            // second block is thrown
            // TODO Should only trigger if a player was Pushed Back
            val hasFrenzy = context.attacker.isSkillAvailable(SkillType.FRENZY)
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
                    SetSkillUsed(context.attacker, context.attacker.getSkill(SkillType.FRENZY), true),
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
                    BlockType.BREATHE_FIRE -> if (player.isSkillAvailable(SkillType.BREATHE_FIRE)) add(type)
                    BlockType.CHAINSAW -> if (player.isSkillAvailable(SkillType.CHAINSAW)) add(type)
                    BlockType.MULTIPLE_BLOCK -> if (!isMultipleBlock) add(type)
                    BlockType.PROJECTILE_VOMIT -> if (player.isSkillAvailable(SkillType.PROJECTILE_VOMIT)) add(type)
                    BlockType.STAB -> if (player.isSkillAvailable(SkillType.STAB)) add(type)
                    BlockType.STANDARD -> add(type)
                }
            }
        }
    }
}
