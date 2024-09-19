package dk.ilios.jervis.procedures

import compositeCommandOf
import dk.ilios.jervis.actions.ActionDescriptor
import dk.ilios.jervis.actions.D6Result
import dk.ilios.jervis.actions.Dice
import dk.ilios.jervis.actions.DiceResults
import dk.ilios.jervis.actions.GameAction
import dk.ilios.jervis.actions.PlayerSelected
import dk.ilios.jervis.actions.RollDice
import dk.ilios.jervis.actions.SelectPlayer
import dk.ilios.jervis.commands.Command
import dk.ilios.jervis.commands.RemoveContext
import dk.ilios.jervis.commands.SetBallState
import dk.ilios.jervis.commands.SetContext
import dk.ilios.jervis.commands.fsm.ExitProcedure
import dk.ilios.jervis.commands.fsm.GotoNode
import dk.ilios.jervis.fsm.ActionNode
import dk.ilios.jervis.fsm.ComputationNode
import dk.ilios.jervis.fsm.Node
import dk.ilios.jervis.fsm.ParentNode
import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.model.BallState
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.PlayerState
import dk.ilios.jervis.model.Team
import dk.ilios.jervis.model.context.ProcedureContext
import dk.ilios.jervis.model.context.getContext
import dk.ilios.jervis.model.locations.FieldCoordinate
import dk.ilios.jervis.reports.ReportDiceRoll
import dk.ilios.jervis.reports.ReportTouchback
import dk.ilios.jervis.rules.Rules
import dk.ilios.jervis.rules.skills.DiceRollType
import dk.ilios.jervis.rules.tables.TableResult

data class KickOffEventContext(
    val roll: DiceResults,
    val result: TableResult,
    val scatterBallBeforeLanding: Boolean = false // If Changing Weather rolled Perfect Conditions
): ProcedureContext

/**
 * Run the Kick-Off Event as well as the results of the ball coming back to the field.
 *
 * See page 41 in the rulebook.
 */
object TheKickOffEvent : Procedure() {
    override val initialNode: Node = RollForKickOffEvent
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null

    object RollForKickOffEvent : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.kickingTeam

        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
            return listOf(RollDice(Dice.D6, Dice.D6))
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return checkDiceRoll<D6Result, D6Result>(action) { firstD6, secondD6 ->
                val result: TableResult = rules.kickOffEventTable.roll(firstD6, secondD6)
                compositeCommandOf(
                    ReportDiceRoll(DiceRollType.KICK_OFF_TABLE, listOf(firstD6, secondD6)),
                    SetContext(KickOffEventContext(roll = DiceResults(firstD6, secondD6), result = result)),
                    GotoNode(ResolveKickOffTableEvent),
                )
            }
        }
    }

    object ResolveKickOffTableEvent : ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure {
            return state.getContext<KickOffEventContext>().result.procedure
        }
        override fun onExitNode(state: Game, rules: Rules): Command {
            val context = state.getContext<KickOffEventContext>()
            return if (context.scatterBallBeforeLanding) {
                compositeCommandOf(
                    SetBallState.scattered(),
                    RemoveContext<KickOffEventContext>(),
                    GotoNode(ScatterBallBeforeLanding)
                )
            } else {
                compositeCommandOf(
                    RemoveContext<KickOffEventContext>(),
                    GotoNode(WhatGoesUpMustComeDown)
                )
            }
        }
    }

    /**
     * The ball scatters further while high in the air, before coming down.
     * Should only happen if Changing Weather (on the Kick-off Event Table) rolled Perfect
     * Conditions.
     */
    object ScatterBallBeforeLanding : ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command {
            return SetContext(ScatterRollContext(from = state.ball.location))
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = Scatter
        override fun onExitNode(state: Game, rules: Rules): Command {
            return compositeCommandOf(
                RemoveContext<ScatterRollContext>(),
                GotoNode(WhatGoesUpMustComeDown)
            )
        }
    }

    /**
     * Resolve the "What goes up, must come down" step.
     * See page 41 in the rulebook.
     */
    object WhatGoesUpMustComeDown : ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            // If out-of-bounds, award touch back
            // If on an empty square, bounce
            // if landing on a player, they must/can(?) attempt to catch it
            // TODO Ball lands again, and can either be caught, will bounce or result in a
            //  touchback
            val ballLocation: FieldCoordinate = state.ball.location
            val outOfBounds =
                state.ball.state == BallState.OUT_OF_BOUNDS ||
                    (state.kickingTeam.isHomeTeam() && ballLocation.isOnHomeSide(rules)) ||
                    (state.kickingTeam.isAwayTeam() && ballLocation.isOnAwaySide(rules))
            return if (outOfBounds) {
                GotoNode(TouchBack)
            } else {
                compositeCommandOf(
                    GotoNode(ResolveBallLanding(ballLocation)),
                )
            }
        }
    }

    // Move this logic to its own procedure. It will be needed when blocking, throwing and otherwise.
    class ResolveBallLanding(private val location: FieldCoordinate) : ParentNode() {
        var isFieldEmpty: Boolean = true
        var canCatch: Boolean = false

        override fun onEnterNode(state: Game, rules: Rules): Command? {
            state.abortIfBallOutOfBounds = true // TODO Wrong way to do this
            isFieldEmpty = state.field[location].player != null
            canCatch = state.field[location].player?.let { rules.canCatch(state, it) } ?: false
            // If field is empty or the player cannot catch the ball, the ball is now
            // bouncing rather than deviating.
            return if (!canCatch) {
                SetBallState.bouncing()
            } else {
                null
            }
        }

        override fun getChildProcedure(state: Game, rules: Rules): Procedure {
            return if (canCatch) Catch else Bounce
        }

        override fun onExitNode(state: Game, rules: Rules): Command {
            state.abortIfBallOutOfBounds = false
            return if (state.ball.state == BallState.OUT_OF_BOUNDS) {
                GotoNode(TouchBack)
            } else {
                ExitProcedure()
            }
        }
    }

    object TouchBack : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.receivingTeam

        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
            // TODO Handle no valid players, so it will bounce
            return state.receivingTeam.filter {
                it.hasTackleZones && it.state == PlayerState.STANDING && it.location.isOnField(rules)
            }.map {
                SelectPlayer(it)
            }
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return checkType<PlayerSelected>(action) {
                return compositeCommandOf(
                    SetBallState.carried(it.getPlayer(state)),
                    ReportTouchback(it.getPlayer(state)),
                    ExitProcedure(),
                )
            }
        }
    }
}
