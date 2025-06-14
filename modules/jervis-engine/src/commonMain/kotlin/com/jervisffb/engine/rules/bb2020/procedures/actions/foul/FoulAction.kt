package com.jervisffb.engine.rules.bb2020.procedures.actions.foul

import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.actions.EndAction
import com.jervisffb.engine.actions.EndActionWhenReady
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.actions.MoveTypeSelected
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.actions.SelectPlayer
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.context.RemoveContext
import com.jervisffb.engine.commands.context.SetContext
import com.jervisffb.engine.commands.SetTurnOver
import com.jervisffb.engine.commands.buildCompositeCommand
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
import com.jervisffb.engine.model.TurnOver
import com.jervisffb.engine.model.context.MoveContext
import com.jervisffb.engine.model.context.ProcedureContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.reports.ReportFoulResult
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.bb2020.procedures.ActivatePlayerContext
import com.jervisffb.engine.rules.bb2020.procedures.actions.move.ResolveMoveTypeStep
import com.jervisffb.engine.rules.bb2020.procedures.calculateMoveTypesAvailable
import com.jervisffb.engine.rules.bb2020.procedures.getSetPlayerRushesCommand
import com.jervisffb.engine.rules.bb2020.procedures.tables.injury.RiskingInjuryContext
import com.jervisffb.engine.rules.bb2020.tables.ArgueTheCallResult
import com.jervisffb.engine.utils.INVALID_ACTION
import com.jervisffb.engine.utils.INVALID_GAME_STATE
import com.jervisffb.engine.utils.addIfNotNull
import kotlinx.serialization.Serializable


data class FoulContext(
    val fouler: Player,
    val victim: Player? = null,
    val foulAssists: Int = 0,
    val defensiveAssists: Int = 0,
    val injuryRoll: RiskingInjuryContext? = null,
    val hasMoved: Boolean = false,
    val hasFouled: Boolean = false,
    val spottedByTheRef: Boolean = false,
    val argueTheCall: Boolean = false,
    val argueTheCallRoll: D6Result? = null,
    val argueTheCallResult: ArgueTheCallResult? = null
) : ProcedureContext

/**
 * Procedure for controlling a player's Blitz action.
 *
 * See page 63 in the rulebook.
 */
@Serializable
object FoulAction : Procedure() {
    override val initialNode: Node = SelectFoulTargetOrCancel
    override fun onEnterProcedure(state: Game, rules: Rules): Command {
        val player = state.activePlayer ?: INVALID_GAME_STATE("No active player")
        return compositeCommandOf(
            getSetPlayerRushesCommand(rules, player),
            SetContext(FoulContext(player))
        )
    }
    override fun onExitProcedure(state: Game, rules: Rules): Command {
        val context = state.getContext<FoulContext>()
        val activePlayerContext = state.getContext<ActivatePlayerContext>()
        return compositeCommandOf(
            if (context.victim != null) ReportFoulResult(context) else null,
            RemoveContext<FoulContext>(),
            SetContext(
                activePlayerContext.copy(
                    markActionAsUsed = context.hasFouled || context.hasMoved
                )
            )
        )
    }

    object SelectFoulTargetOrCancel : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<FoulContext>().fouler.team
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val fouler = state.getContext<FoulContext>().fouler
            val availableTargetPlayers = fouler.team.otherTeam().filter {
                // You cannot foul your own players, so no need to check for STUNNED_OWN_TURN
                it.location.isOnField(rules) && (it.state == PlayerState.PRONE || it.state == PlayerState.STUNNED)
            }.map {
                SelectPlayer(it)
            }
            return availableTargetPlayers + listOf(EndActionWhenReady)
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return when (action) {
                is EndAction -> ExitProcedure()
                is PlayerSelected -> {
                    checkTypeAndValue<PlayerSelected>(state, action) {
                        val context = state.getContext<FoulContext>()
                        compositeCommandOf(
                            SetContext(context.copy(victim = action.getPlayer(state))),
                            GotoNode(MoveOrFoulOrEndAction)
                        )
                    }
                }
                else -> INVALID_ACTION(action)
            }
        }
    }

    object MoveOrFoulOrEndAction : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<FoulContext>().fouler.team
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val context = state.getContext<FoulContext>()
            val options = mutableListOf<GameActionDescriptor>()

            // Find possible move types
            options.addIfNotNull(calculateMoveTypesAvailable(state, state.activePlayer!!))

            // Check if adjacent to target of the Blitz
            if (context.fouler.location.isAdjacent(rules, context.victim!!.location)) {
                options.add(SelectPlayer(context.victim))
            }

            // End action before the block
            // As soon as a target is selected, you can no longer cancel the action
            // (Ideally this should be allowed until you take the first move)
            options.add(EndActionWhenReady)

            return options
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            val context = state.getContext<FoulContext>()
            return when (action) {
                EndAction -> ExitProcedure()
                is MoveTypeSelected -> {
                    val moveContext = MoveContext(context.fouler, action.moveType)
                    compositeCommandOf(
                        SetContext(context.copy(hasMoved = true)),
                        SetContext(moveContext),
                        GotoNode(ResolveMove)
                    )
                }
                is PlayerSelected -> {
                    val foulContext = state.getContext<FoulContext>()
                    compositeCommandOf(
                        SetContext(foulContext.copy(victim = action.getPlayer(state))),
                        GotoNode(ResolveFoul)
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
            val moveContext = state.getContext<MoveContext>()
            val context = state.getContext<FoulContext>()
            val endNow = state.endActionImmediately()
            return buildCompositeCommand {
                if (moveContext.hasMoved) {
                    add(SetContext(context.copy(hasMoved = true)))
                }
                if (endNow) {
                    add(ExitProcedure())
                } else if (!rules.isStanding(context.fouler)) {
                    add(SetTurnOver(TurnOver.STANDARD))
                    add(ExitProcedure())
                } else {
                    add(GotoNode(MoveOrFoulOrEndAction))
                }
            }
        }
    }

    object ResolveFoul : ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = FoulStep
        override fun onExitNode(state: Game, rules: Rules): Command {
            // The result of the foul is handled in FoulStep, so just end the action here.
            return ExitProcedure()
        }
    }
}

