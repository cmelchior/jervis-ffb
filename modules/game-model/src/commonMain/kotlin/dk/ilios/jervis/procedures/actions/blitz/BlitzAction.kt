package dk.ilios.jervis.procedures.actions.blitz

import compositeCommandOf
import dk.ilios.jervis.actions.ActionDescriptor
import dk.ilios.jervis.actions.DeselectPlayer
import dk.ilios.jervis.actions.EndAction
import dk.ilios.jervis.actions.EndActionWhenReady
import dk.ilios.jervis.actions.GameAction
import dk.ilios.jervis.actions.MoveTypeSelected
import dk.ilios.jervis.actions.PlayerDeselected
import dk.ilios.jervis.actions.PlayerSelected
import dk.ilios.jervis.actions.SelectPlayer
import dk.ilios.jervis.commands.Command
import dk.ilios.jervis.commands.ExitProcedure
import dk.ilios.jervis.commands.GotoNode
import dk.ilios.jervis.commands.SetAvailableActions
import dk.ilios.jervis.commands.SetOldContext
import dk.ilios.jervis.commands.SetTurnOver
import dk.ilios.jervis.fsm.ActionNode
import dk.ilios.jervis.fsm.Node
import dk.ilios.jervis.fsm.ParentNode
import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.Player
import dk.ilios.jervis.model.PlayerState
import dk.ilios.jervis.model.context.MoveContext
import dk.ilios.jervis.model.context.ProcedureContext
import dk.ilios.jervis.procedures.actions.block.BlockContext
import dk.ilios.jervis.procedures.actions.block.BlockStep
import dk.ilios.jervis.procedures.actions.move.MoveTypeSelectorStep
import dk.ilios.jervis.procedures.actions.move.calculateMoveTypesAvailable
import dk.ilios.jervis.procedures.getSetPlayerRushesCommand
import dk.ilios.jervis.reports.ReportActionEnded
import dk.ilios.jervis.rules.PlayerActionType
import dk.ilios.jervis.rules.Rules
import dk.ilios.jervis.utils.INVALID_ACTION
import dk.ilios.jervis.utils.INVALID_GAME_STATE
import kotlinx.serialization.Serializable


data class BlitzContext(
    val attacker: Player,
    val defender: Player? = null,
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

    override fun onEnterProcedure(
        state: Game,
        rules: Rules,
    ): Command {
        val player = state.activePlayer ?: INVALID_GAME_STATE("No active player")
        return compositeCommandOf(
            getSetPlayerRushesCommand(rules, player),
            SetOldContext(Game::blitzContext, BlitzContext(player))
        )
    }

    override fun onExitProcedure(
        state: Game,
        rules: Rules,
    ): Command {
        val context = state.blitzContext!!
        val player = state.activePlayer!!
        return compositeCommandOf(
            SetOldContext(Game::blitzContext, null),
            if (context.hasBlocked || context.hasMoved) {
                val team = state.activeTeam
                SetAvailableActions(team, PlayerActionType.FOUL, team.turnData.blitzActions - 1)
            } else {
                null
            },
            ReportActionEnded(player, state.activePlayerAction!!)
        )
    }

    object SelectTargetOrCancel : ActionNode() {
        override fun getAvailableActions(
            state: Game,
            rules: Rules,
        ): List<ActionDescriptor> {
            val attacker = state.blitzContext!!.attacker
            val availableTargetPlayers = attacker.team.otherTeam().filter {
                it.location.isOnField(rules) && it.state == PlayerState.STANDING
            }.map {
                SelectPlayer(it)
            }
            return availableTargetPlayers + listOf(DeselectPlayer(attacker))
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return when (action) {
                PlayerDeselected -> ExitProcedure()
                is PlayerSelected -> {
                    val context = state.blitzContext!!
                    compositeCommandOf(
                        SetOldContext(Game::blitzContext, context.copy(defender = action.player)),
                        GotoNode(MoveOrBlockOrEndAction)
                    )
                }

                else -> INVALID_ACTION(action)
            }
        }
    }

    object MoveOrBlockOrEndAction : ActionNode() {
        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
            val context = blitzContext(state)
            val options = mutableListOf<ActionDescriptor>()

            // Find possible move types
            options.addAll(calculateMoveTypesAvailable(state.activePlayer!!, rules))

            // Check if adjacent to target of the Blitz
            if (context.attacker.location.isAdjacent(rules, context.defender!!.location)) {
                options.add(SelectPlayer(context.defender))
            }

            // End action before the block
            // As soon as a target is selected, you can no longer cancel the action
            // (Ideally this should be allowed until you take the first move)
            options.add(EndActionWhenReady)

            return options
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            val context = state.blitzContext!!
            return when (action) {
                EndAction -> ExitProcedure()
                is MoveTypeSelected -> {
                    val moveContext = MoveContext(context.attacker, action.moveType)
                    compositeCommandOf(
                        SetOldContext(Game::blitzContext, context.copy(hasMoved = true)),
                        SetOldContext(Game::moveContext, moveContext),
                        GotoNode(ResolveMove)
                    )
                }

                is PlayerSelected -> {
                    val blockContext = BlockContext(
                        attacker = context.attacker,
                        defender = action.player,
                        isBlitzing = true
                    )
                    compositeCommandOf(
                        SetOldContext(Game::blitzContext, context.copy(hasBlocked = true)),
                        SetOldContext(Game::blockContext, blockContext),
                        GotoNode(ResolveBlock)
                    )
                }

                else -> INVALID_ACTION(action)
            }
        }
    }

    object ResolveMove : ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = MoveTypeSelectorStep
        override fun onExitNode(state: Game, rules: Rules): Command {
            // If player is not standing on the field after the move, it is a turn over,
            // otherwise they are free to continue their blitz
            val context = state.blitzContext!!
            return if (!context.attacker.isStanding(rules)) {
                compositeCommandOf(
                    SetTurnOver(true),
                    ExitProcedure()
                )
            } else {
                GotoNode(if (context.hasBlocked) RemainingMovesOrEndAction else MoveOrBlockOrEndAction)
            }
        }
    }

    object ResolveBlock : ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = BlockStep
        override fun onExitNode(state: Game, rules: Rules): Command {
            // If player is not standing on the field after the move, it is a turn over,
            // otherwise they are free to continue their blitz
            val context = state.blitzContext!!
            return if (!context.attacker.isStanding(rules)) {
                compositeCommandOf(
                    SetTurnOver(true),
                    ExitProcedure()
                )
            } else {
                GotoNode(RemainingMovesOrEndAction)
            }
        }
    }

    object RemainingMovesOrEndAction : ActionNode() {
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
            val context = state.blitzContext!!
            return when (action) {
                EndAction -> ExitProcedure()
                is MoveTypeSelected -> {
                    val moveContext = MoveContext(context.attacker, action.moveType)
                    compositeCommandOf(
                        SetOldContext(Game::blitzContext, context.copy(hasMoved = true)),
                        SetOldContext(Game::moveContext, moveContext),
                        GotoNode(ResolveMove)
                    )
                }

                else -> INVALID_ACTION(action)
            }
        }
    }

    private fun blitzContext(state: Game): BlitzContext {
        val context = state.blitzContext!!
        return context
    }
}
