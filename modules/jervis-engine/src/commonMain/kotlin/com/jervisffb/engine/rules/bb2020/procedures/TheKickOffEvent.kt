package com.jervisffb.engine.rules.bb2020.procedures

import com.jervisffb.engine.actions.ActionDescriptor
import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.actions.Dice
import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.actions.RollDice
import com.jervisffb.engine.actions.SelectPlayer
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.RemoveContext
import com.jervisffb.engine.commands.SetBallState
import com.jervisffb.engine.commands.SetContext
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.ComputationNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.ParentNode
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.fsm.checkDiceRoll
import com.jervisffb.engine.fsm.checkType
import com.jervisffb.engine.model.BallState
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.PlayerState
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.context.ProcedureContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.reports.ReportDiceRoll
import com.jervisffb.engine.reports.ReportTouchback
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.bb2020.skills.DiceRollType
import com.jervisffb.engine.rules.bb2020.tables.TableResult
import com.jervisffb.engine.commands.compositeCommandOf

data class KickOffEventContext(
    val roll: DiceRollResults,
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
                    SetContext(KickOffEventContext(roll = DiceRollResults(firstD6, secondD6), result = result)),
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
                    SetBallState.scattered(state.singleBall()),
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
            val ball = state.singleBall()
            return SetContext(
                ScatterRollContext(
                ball = ball,
                from = ball.location,
            )
            )
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
            val ball = state.singleBall()
            val ballLocation: FieldCoordinate = ball.location
            val outOfBounds =
                ball.state == BallState.OUT_OF_BOUNDS ||
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
                SetBallState.bouncing(state.singleBall())
            } else {
                null
            }
        }

        override fun getChildProcedure(state: Game, rules: Rules): Procedure {
            return if (canCatch) Catch else Bounce
        }

        override fun onExitNode(state: Game, rules: Rules): Command {
            state.abortIfBallOutOfBounds = false
            return if (state.singleBall().state == BallState.OUT_OF_BOUNDS) {
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
                    SetBallState.carried(state.singleBall(), it.getPlayer(state)),
                    ReportTouchback(it.getPlayer(state)),
                    ExitProcedure(),
                )
            }
        }
    }
}
