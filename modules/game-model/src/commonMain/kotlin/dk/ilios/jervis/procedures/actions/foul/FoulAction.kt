package dk.ilios.jervis.procedures.actions.foul

import compositeCommandOf
import dk.ilios.jervis.actions.ActionDescriptor
import dk.ilios.jervis.actions.D6Result
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
import dk.ilios.jervis.commands.SetContext
import dk.ilios.jervis.commands.SetTurnOver
import dk.ilios.jervis.fsm.ActionNode
import dk.ilios.jervis.fsm.Node
import dk.ilios.jervis.fsm.ParentNode
import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.Player
import dk.ilios.jervis.model.PlayerState
import dk.ilios.jervis.model.ProcedureContext
import dk.ilios.jervis.procedures.actions.move.MoveContext
import dk.ilios.jervis.procedures.actions.move.MoveTypeSelectorStep
import dk.ilios.jervis.procedures.actions.move.calculateMoveTypesAvailable
import dk.ilios.jervis.procedures.injury.RiskingInjuryRollContext
import dk.ilios.jervis.reports.ReportActionEnded
import dk.ilios.jervis.reports.ReportFoulResult
import dk.ilios.jervis.rules.PlayerActionType
import dk.ilios.jervis.rules.Rules
import dk.ilios.jervis.rules.tables.ArgueTheCallResult
import dk.ilios.jervis.utils.INVALID_ACTION
import kotlinx.serialization.Serializable


data class FoulContext(
    val fouler: Player,
    val victim: Player? = null,
    val foulAssists: Int = 0,
    val defensiveAssists: Int = 0,
    val injuryRoll: RiskingInjuryRollContext? = null,
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

    override fun onEnterProcedure(
        state: Game,
        rules: Rules,
    ): Command = SetContext(Game::foulContext, FoulContext(state.activePlayer!!))

    override fun onExitProcedure(
        state: Game,
        rules: Rules,
    ): Command {
        val context = state.foulContext!!
        return compositeCommandOf(
            if (context.victim != null) ReportFoulResult(context) else null,
            SetContext(Game::foulContext, null),
            if (context.hasFouled || context.hasMoved) {
                val team = state.activeTeam
                SetAvailableActions(team, PlayerActionType.FOUL, team.turnData.foulActions - 1)
            } else {
                null
            },
            ReportActionEnded(state.activePlayer!!, state.activePlayerAction!!)
        )
    }

    object SelectFoulTargetOrCancel : ActionNode() {
        override fun getAvailableActions(
            state: Game,
            rules: Rules,
        ): List<ActionDescriptor> {
            val fouler = state.foulContext!!.fouler
            val availableTargetPlayers = fouler.team.otherTeam().filter {
                it.location.isOnField(rules) && (it.state == PlayerState.PRONE || it.state == PlayerState.STUNNED)
            }.map {
                SelectPlayer(it)
            }
            return availableTargetPlayers + listOf(DeselectPlayer(fouler))
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return when (action) {
                PlayerDeselected -> ExitProcedure()
                is PlayerSelected -> {
                    val context = state.foulContext!!
                    compositeCommandOf(
                        SetContext(Game::foulContext, context.copy(victim = action.player)),
                        GotoNode(MoveOrFoulOrEndAction)
                    )
                }

                else -> INVALID_ACTION(action)
            }
        }
    }

    object MoveOrFoulOrEndAction : ActionNode() {
        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
            val context = state.foulContext!!
            val options = mutableListOf<ActionDescriptor>()

            // Find possible move types
            options.addAll(calculateMoveTypesAvailable(state.activePlayer!!, rules))

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
            val context = state.foulContext!!
            return when (action) {
                EndAction -> ExitProcedure()
                is MoveTypeSelected -> {
                    val moveContext = MoveContext(context.fouler, action.moveType)
                    compositeCommandOf(
                        SetContext(Game::foulContext, context.copy(hasMoved = true)),
                        SetContext(Game::moveContext, moveContext),
                        GotoNode(ResolveMove)
                    )
                }
                is PlayerSelected -> {
                    val foulContext = state.foulContext!!
                    compositeCommandOf(
                        SetContext(Game::foulContext, foulContext.copy(victim = action.player)),
                        GotoNode(ResolveFoul)
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
            val context = state.foulContext!!
            return if (!context.fouler.isStanding(rules)) {
                compositeCommandOf(
                    SetTurnOver(true),
                    ExitProcedure()
                )
            } else {
                GotoNode(MoveOrFoulOrEndAction)
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
