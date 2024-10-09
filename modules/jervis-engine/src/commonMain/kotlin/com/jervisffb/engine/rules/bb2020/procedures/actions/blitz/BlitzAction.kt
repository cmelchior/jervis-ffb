package com.jervisffb.engine.rules.bb2020.procedures.actions.blitz

import com.jervisffb.engine.actions.ActionDescriptor
import com.jervisffb.engine.actions.BlockTypeSelected
import com.jervisffb.engine.actions.DeselectPlayer
import com.jervisffb.engine.actions.EndAction
import com.jervisffb.engine.actions.EndActionWhenReady
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.MoveTypeSelected
import com.jervisffb.engine.actions.PlayerDeselected
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.actions.SelectBlockType
import com.jervisffb.engine.actions.SelectPlayer
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.RemoveContext
import com.jervisffb.engine.commands.SetContext
import com.jervisffb.engine.commands.SetPlayerMoveLeft
import com.jervisffb.engine.commands.SetSkillUsed
import com.jervisffb.engine.commands.SetTurnOver
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.ComputationNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.ParentNode
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.PlayerState
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.TurnOver
import com.jervisffb.engine.model.context.MoveContext
import com.jervisffb.engine.model.context.ProcedureContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.isSkillAvailable
import com.jervisffb.engine.rules.BlockType
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.bb2020.procedures.ActivatePlayerContext
import com.jervisffb.engine.rules.bb2020.procedures.actions.block.BlockAction
import com.jervisffb.engine.rules.bb2020.procedures.actions.block.BlockContext
import com.jervisffb.engine.rules.bb2020.procedures.actions.block.StandardBlockStep
import com.jervisffb.engine.rules.bb2020.procedures.actions.move.ResolveMoveTypeStep
import com.jervisffb.engine.rules.bb2020.procedures.actions.move.RushRoll
import com.jervisffb.engine.rules.bb2020.procedures.actions.move.calculateMoveTypesAvailable
import com.jervisffb.engine.rules.bb2020.procedures.getSetPlayerRushesCommand
import com.jervisffb.engine.rules.bb2020.procedures.tables.injury.FallingOver
import com.jervisffb.engine.rules.bb2020.procedures.tables.injury.RiskingInjuryContext
import com.jervisffb.engine.rules.bb2020.procedures.tables.injury.RiskingInjuryMode
import com.jervisffb.engine.rules.bb2020.skills.Frenzy
import com.jervisffb.engine.utils.INVALID_ACTION
import com.jervisffb.engine.utils.INVALID_GAME_STATE
import compositeCommandOf
import kotlinx.serialization.Serializable

data class BlitzContext(
    val attacker: Player,
    val defender: Player? = null,
    val blockType: BlockType? = null,
    val hasMoved: Boolean = false,
    val hasBlocked: Boolean = false,
    val didFollowUp: Boolean = false,
) : ProcedureContext

/**
 * Procedure for controlling a player's Blitz action.
 *
 * See page 43 in the rulebook.
 */
@Serializable
object BlitzAction : Procedure() {
    override val initialNode: Node = SelectTargetOrCancel
    override fun onEnterProcedure(state: Game, rules: Rules): Command {
        val player = state.activePlayer!!
        return compositeCommandOf(
            getSetPlayerRushesCommand(rules, player),
            SetContext(BlitzContext(player))
        )
    }
    override fun onExitProcedure(state: Game, rules: Rules): Command {
        val activateContext = state.getContext<ActivatePlayerContext>()
        val blitzContext = state.getContext<BlitzContext>()
        return compositeCommandOf(
            RemoveContext<BlitzContext>(),
            if (blitzContext.hasBlocked || blitzContext.hasMoved) {
                SetContext(activateContext.copy(markActionAsUsed = true))
            } else {
                SetContext(activateContext.copy(markActionAsUsed = false))
            }
        )
    }
    override fun isValid(state: Game, rules: Rules) {
        state.activePlayer ?: INVALID_GAME_STATE("No active player")
    }

    object SelectTargetOrCancel : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<BlitzContext>().attacker.team

        override fun getAvailableActions(
            state: Game,
            rules: Rules,
        ): List<ActionDescriptor> {
            val attacker = state.getContext<BlitzContext>().attacker
            val availableTargetPlayers = attacker.team.otherTeam().filter {
                it.location.isOnField(rules) && it.state == PlayerState.STANDING
            }.map {
                SelectPlayer(it)
            }
            return availableTargetPlayers + listOf(DeselectPlayer(attacker))
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return when (action) {
                is PlayerDeselected -> ExitProcedure()
                is PlayerSelected -> {
                    val context = state.getContext<BlitzContext>()
                    compositeCommandOf(
                        SetContext(context.copy(defender = action.getPlayer(state))),
                        GotoNode(MoveOrBlockOrEndAction)
                    )
                }

                else -> INVALID_ACTION(action)
            }
        }
    }

    object MoveOrBlockOrEndAction : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<BlitzContext>().attacker.team

        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
            val context = state.getContext<BlitzContext>()
            val blitzer = context.attacker
            val options = mutableListOf<ActionDescriptor>()

            // Find possible move types
            options.addAll(calculateMoveTypesAvailable(blitzer, rules))

            // Check if adjacent to target of the Blitz
            val hasMovesLeft = blitzer.movesLeft + blitzer.rushesLeft > 0
            if (context.attacker.location.isAdjacent(rules, context.defender!!.location) && hasMovesLeft) {
                options.add(SelectPlayer(context.defender))
            }

            // End action before the block
            // As soon as a target is selected, you can no longer cancel the action
            // (Ideally this should be allowed until you take the first move)
            options.add(EndActionWhenReady)

            return options
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            val context = state.getContext<BlitzContext>()
            return when (action) {
                EndAction -> ExitProcedure()
                is MoveTypeSelected -> {
                    val moveContext = MoveContext(context.attacker, action.moveType)
                    compositeCommandOf(
                        SetContext(moveContext),
                        GotoNode(ResolveMove)
                    )
                }

                is PlayerSelected -> {
                    val blockContext = BlockContext(
                        attacker = context.attacker,
                        defender = action.getPlayer(state),
                        isBlitzing = true
                    )
                    compositeCommandOf(
                        SetContext(context.copy(hasBlocked = true)),
                        SetContext(blockContext),
                        GotoNode(SelectBlockType)
                    )
                }

                else -> INVALID_ACTION(action)
            }
        }
    }

    object ResolveMove : ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = ResolveMoveTypeStep
        override fun onExitNode(state: Game, rules: Rules): Command {
            // If player is not standing on the field after the move, it is a turn over,
            // otherwise they are free to continue their blitz
            // TODO This is wrong. It is only a turnover if the player was Knocked Down
            //  or went prone with the Ball. Need to rework this.
            //  This logic will probably also override scoring turn overs
            val moveContext = state.getContext<MoveContext>()
            val blitzContext = state.getContext<BlitzContext>()
            return if (!blitzContext.attacker.isStanding(rules)) {
                compositeCommandOf(
                    if (moveContext.hasMoved) SetContext(blitzContext.copy(hasMoved = true)) else null,
                    RemoveContext<MoveContext>(),
                    SetTurnOver(TurnOver.STANDARD),
                    ExitProcedure()
                )
            } else {
                compositeCommandOf(
                    if (moveContext.hasMoved) SetContext(blitzContext.copy(hasMoved = true)) else null,
                    RemoveContext<MoveContext>(),
                    GotoNode(if (blitzContext.hasBlocked) RemainingMovesOrEndAction else MoveOrBlockOrEndAction)
                )
            }
        }
    }

    object SelectBlockType : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<BlitzContext>().attacker.team
        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
            val attacker = state.getContext<BlitzContext>().attacker
            val availableBlockTypes = BlockAction.getAvailableBlockType(attacker, true)
            return availableBlockTypes.map {
                SelectBlockType(it)
            } + DeselectPlayer(attacker)
        }
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            val context = state.getContext<BlitzContext>()
            return when (action) {
                is PlayerDeselected -> {
                    GotoNode(MoveOrBlockOrEndAction)
                }
                else -> {
                    checkTypeAndValue<BlockTypeSelected>(state, rules, action) { typeSelected ->
                        val type = typeSelected.type
                        compositeCommandOf(
                            SetContext(context.copy(blockType = typeSelected.type)),
                            GotoNode(UseMoveToBlock),
                        )
                    }
                }
            }
        }
    }

    object UseMoveToBlock : ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            val context = state.getContext<BlitzContext>()
            val player = context.attacker
            return if (player.movesLeft > 0) {
                compositeCommandOf(
                    SetPlayerMoveLeft(state.activePlayer!!, state.activePlayer!!.movesLeft - 1),
                    GotoNode(ResolveBlock)
                )
            } else if (player.rushesLeft > 0) {
                compositeCommandOf(
                    GotoNode(RushBeforeBlock)
                )
            } else {
                INVALID_GAME_STATE("Player has no moves left: $context.attacker")
            }
        }
    }

    // TODO
    object RushBeforeBlock : ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = RushRoll
        override fun onExitNode(state: Game, rules: Rules): Command {
            TODO("Not yet implemented")
        }
    }

    // TODO
    object ResolveFallingOverBeforeBlock: ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command {
            val context = state.getContext<MoveContext>()
            return SetContext(RiskingInjuryContext(context.player, mode = RiskingInjuryMode.FALLING_OVER))
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = FallingOver
        override fun onExitNode(state: Game, rules: Rules): Command {
            // Regardless of the outcome, the player's action ends in a turnover
            return compositeCommandOf(
                ExitProcedure()
            )
        }
    }

    object ResolveBlock : ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command {
            val context = state.getContext<BlitzContext>()
            return when (context.blockType!!) {
                BlockType.CHAINSAW -> TODO()
                BlockType.MULTIPLE_BLOCK -> TODO()
                BlockType.PROJECTILE_VOMIT -> TODO()
                BlockType.STAB -> TODO()
                BlockType.STANDARD -> {
                    SetContext(BlockContext(
                        context.attacker,
                        context.defender!!,
                        isBlitzing = true
                    ))
                }
            }
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure {
            val context = state.getContext<BlitzContext>()
            return when (context.blockType!!) {
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
            val context = state.getContext<BlitzContext>()

            // Check if Block Action was completed or not
            val removeContextCommand = when (context.blockType!!) {
                BlockType.CHAINSAW -> TODO()
                BlockType.MULTIPLE_BLOCK -> TODO()
                BlockType.PROJECTILE_VOMIT -> TODO()
                BlockType.STAB -> TODO()
                BlockType.STANDARD -> RemoveContext<BlockContext>()
            }

            // Remove state required for the specific block type
            val hasBlocked = when (context.blockType) {
                BlockType.CHAINSAW -> TODO()
                BlockType.MULTIPLE_BLOCK -> TODO()
                BlockType.PROJECTILE_VOMIT -> TODO()
                BlockType.STAB -> TODO()
                BlockType.STANDARD -> !state.getContext<BlockContext>().aborted
            }

            // After the Push was resolved, if the target is still standing
            // and the attacker has frenzy and was able to follow up, a
            // second block is thrown
            val hasFrenzy = context.attacker.isSkillAvailable<Frenzy>()
            val isNextToTarget = rules.isStanding(context.defender!!) && context.attacker.coordinates
                .getSurroundingCoordinates(rules, distance = 1)
                .contains(context.defender.coordinates)

            return if (state.isTurnOver()) {
                return compositeCommandOf(
                    removeContextCommand,
                    ExitProcedure()
                )
            } else if (hasBlocked && hasFrenzy && isNextToTarget) {
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
                    GotoNode(RemainingMovesOrEndAction)
                )
            }
        }
    }

    object RemainingMovesOrEndAction : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<BlitzContext>().attacker.team

        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
            val options = mutableListOf<ActionDescriptor>()
            // Find possible move types
            options.addAll(calculateMoveTypesAvailable(state.activePlayer!!, rules))
            // End action before the block
            // As soon as a target is selected, you can no longer cancel the action
            // (Ideally this should be allowed until you take the first move)
            options.add(EndActionWhenReady)
            return options
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            val context = state.getContext<BlitzContext>()
            return when (action) {
                EndAction -> ExitProcedure()
                is MoveTypeSelected -> {
                    val moveContext = MoveContext(context.attacker, action.moveType)
                    compositeCommandOf(
                        SetContext(context.copy(hasMoved = true)),
                        SetContext(moveContext),
                        GotoNode(ResolveMove)
                    )
                }

                else -> INVALID_ACTION(action)
            }
        }
    }
}
