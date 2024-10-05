package dk.ilios.jervis.procedures.actions.foul

import buildCompositeCommand
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
import dk.ilios.jervis.model.PlayerState
import dk.ilios.jervis.model.Team
import dk.ilios.jervis.model.TurnOver
import dk.ilios.jervis.model.context.MoveContext
import dk.ilios.jervis.model.context.getContext
import dk.ilios.jervis.procedures.ActivatePlayerContext
import dk.ilios.jervis.procedures.actions.move.ResolveMoveTypeStep
import dk.ilios.jervis.procedures.actions.move.calculateMoveTypesAvailable
import dk.ilios.jervis.procedures.getSetPlayerRushesCommand
import dk.ilios.jervis.reports.ReportFoulResult
import dk.ilios.jervis.rules.Rules
import dk.ilios.jervis.utils.INVALID_ACTION
import dk.ilios.jervis.utils.INVALID_GAME_STATE
import kotlinx.serialization.Serializable


/**
 * Procedure for controlling a player's Foul action.
 *
 * FUMBBL does not follow the rulebook and allow the fouler to wait with
 * selecting the victim until they are going to perform the foul
 */
@Serializable
object FumbblFoulAction : Procedure() {
    override val initialNode: Node = MoveOrFoulOrEndAction
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

    object MoveOrFoulOrEndAction : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<FoulContext>().fouler.team
        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
            val options = mutableListOf<ActionDescriptor>()

            // Find possible move types
            options.addAll(calculateMoveTypesAvailable(state.activePlayer!!, rules))

            // Check if adjacent to target of the Blitz
            val foulContext= state.getContext<FoulContext>()
            val fouler = foulContext.fouler
            if (foulContext.hasMoved) {
                options.add(EndActionWhenReady)
            } else {
                options.add(DeselectPlayer(fouler))
            }
            val availableTargetPlayers = fouler.team.otherTeam().filter {
                // You cannot foul your own players, so no need to check for STUNNED_OWN_TURN
                it.location.isOnField(rules) && (it.state == PlayerState.PRONE || it.state == PlayerState.STUNNED)
            }.map {
                SelectPlayer(it)
            }
            options.addAll(availableTargetPlayers)

            // End action before the foul
            options.add(EndActionWhenReady)
            return options
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            val context = state.getContext<FoulContext>()
            return when (action) {
                EndAction -> ExitProcedure()
                is PlayerDeselected -> ExitProcedure()
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
            return buildCompositeCommand {
                if (moveContext.hasMoved) {
                    add(SetContext(context.copy(hasMoved = true)))
                }
                if (!context.fouler.isStanding(rules)) {
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

