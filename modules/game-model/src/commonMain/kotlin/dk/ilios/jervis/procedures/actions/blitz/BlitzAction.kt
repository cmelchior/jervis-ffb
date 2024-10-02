package dk.ilios.jervis.procedures.actions.blitz

import compositeCommandOf
import dk.ilios.jervis.actions.ActionDescriptor
import dk.ilios.jervis.actions.BlockTypeSelected
import dk.ilios.jervis.actions.DeselectPlayer
import dk.ilios.jervis.actions.EndAction
import dk.ilios.jervis.actions.EndActionWhenReady
import dk.ilios.jervis.actions.GameAction
import dk.ilios.jervis.actions.MoveTypeSelected
import dk.ilios.jervis.actions.PlayerDeselected
import dk.ilios.jervis.actions.PlayerSelected
import dk.ilios.jervis.actions.SelectBlockType
import dk.ilios.jervis.actions.SelectPlayer
import dk.ilios.jervis.commands.Command
import dk.ilios.jervis.commands.RemoveContext
import dk.ilios.jervis.commands.SetContext
import dk.ilios.jervis.commands.SetTurnOver
import dk.ilios.jervis.commands.fsm.ExitProcedure
import dk.ilios.jervis.commands.fsm.GotoNode
import dk.ilios.jervis.fsm.ActionNode
import dk.ilios.jervis.fsm.Node
import dk.ilios.jervis.fsm.ParentNode
import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.Player
import dk.ilios.jervis.model.PlayerState
import dk.ilios.jervis.model.Team
import dk.ilios.jervis.model.TurnOver
import dk.ilios.jervis.model.context.MoveContext
import dk.ilios.jervis.model.context.ProcedureContext
import dk.ilios.jervis.model.context.getContext
import dk.ilios.jervis.procedures.ActivatePlayerContext
import dk.ilios.jervis.procedures.actions.block.BlockAction
import dk.ilios.jervis.procedures.actions.block.BlockContext
import dk.ilios.jervis.procedures.actions.block.StandardBlockStep
import dk.ilios.jervis.procedures.actions.move.ResolveMoveTypeStep
import dk.ilios.jervis.procedures.actions.move.calculateMoveTypesAvailable
import dk.ilios.jervis.procedures.getSetPlayerRushesCommand
import dk.ilios.jervis.rules.BlockType
import dk.ilios.jervis.rules.Rules
import dk.ilios.jervis.utils.INVALID_ACTION
import dk.ilios.jervis.utils.INVALID_GAME_STATE
import kotlinx.serialization.Serializable

data class BlitzContext(
    val attacker: Player,
    val defender: Player? = null,
    val blockType: BlockType? = null,
    val hasMoved: Boolean = false,
    val hasBlocked: Boolean = false,
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
                            GotoNode(ResolveBlock),
                        )
                    }
                }
            }
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

            return if (!rules.isStanding(context.attacker)) {
                compositeCommandOf(
                    removeContextCommand,
                    SetContext(context.copy(hasBlocked = hasBlocked)),
                    SetTurnOver(TurnOver.STANDARD),
                    ExitProcedure()
                )
            } else {
                compositeCommandOf(
                    removeContextCommand,
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
