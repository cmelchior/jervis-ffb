package com.jervisffb.engine.rules.bb2020.procedures

import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.SetBallLocation
import com.jervisffb.engine.commands.SetBallState
import com.jervisffb.engine.commands.buildCompositeCommand
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.context.RemoveContext
import com.jervisffb.engine.commands.context.SetContext
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.ParentNode
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.BallState
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.context.CatchRollContext
import com.jervisffb.engine.model.context.PassingInterferenceContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.context.getContextOrNull
import com.jervisffb.engine.model.modifiers.CatchModifier
import com.jervisffb.engine.model.modifiers.DiceModifier
import com.jervisffb.engine.reports.ReportCatch
import com.jervisffb.engine.reports.ReportInterception
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.bb2020.procedures.actions.move.ScoringATouchDownContext
import com.jervisffb.engine.rules.bb2020.procedures.actions.move.ScoringATouchdown
import com.jervisffb.engine.rules.bb2020.tables.Weather
import com.jervisffb.engine.utils.INVALID_GAME_STATE

/**
 * Resolve a player attempting to catch the ball as described on page 51 in the rulebook.
 *
 * This procedure assumes that the parent already checked if the cath is valid
 * in the first place.
 */
object Catch : Procedure() {
    override val initialNode: Node = RollToCatch
    override fun onEnterProcedure(state: Game, rules: Rules): Command {
        // Determine target and modifiers for the Catch roll
        val ball = state.currentBall()
        val catchingPlayer = state.field[ball.location].player!!
        val diceRollTarget = catchingPlayer.agility
        val modifiers = mutableListOf<DiceModifier>()
        val ballStateModifier = when (ball.state) {
            BallState.BOUNCING -> CatchModifier.BOUNCING
            BallState.DEVIATING -> CatchModifier.DEVIATED
            BallState.SCATTERED -> CatchModifier.SCATTERED
            BallState.THROW_IN -> CatchModifier.THROW_IN
            BallState.DEFLECTED -> CatchModifier.CONVERT_DEFLECTION
            else -> null
        }
        if (ballStateModifier != null) modifiers.add(ballStateModifier)

        // Add marked modifiers for field
        rules.addMarkedModifiers(
            state,
            catchingPlayer.team,
            ball.location,
            modifiers,
            CatchModifier.MARKED
        )

        // Check the weather
        if (state.weather == Weather.POURING_RAIN) {
            modifiers.add(CatchModifier.POURING_RAIN)
        }

        // TODO Check for disturbing presence.

        val rollContext = CatchRollContext(catchingPlayer, diceRollTarget, modifiers)
        return SetContext(rollContext)
    }
    override fun isValid(state: Game, rules: Rules) {
        super.isValid(state, rules)
        // Check that this is only called on a standing player with tackle zones
        val ballLocation = state.currentBall().location
        if (state.field[ballLocation].player == null) {
            INVALID_GAME_STATE("No player available to catch the ball at: $ballLocation")
        }
        if (!rules.canCatch(state, state.field[ballLocation].player!!)) {
            INVALID_GAME_STATE("Player is not eligible for catching the ball at: $ballLocation")
        }
    }
    override fun onExitProcedure(state: Game, rules: Rules): Command {
        return compositeCommandOf(
            RemoveContext<CatchRollContext>(),
        )
    }

    object RollToCatch : ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = CatchRoll
        override fun onExitNode(state: Game, rules: Rules): Command {
            val context = state.getContext<CatchRollContext>()
            val passingInterferenceContext = state.getContextOrNull<PassingInterferenceContext>()
            val roll = context.roll!!
            val ball = state.currentBall()
            return if (context.isSuccess) {
                buildCompositeCommand {
                    addAll(
                        SetBallState.carried(ball, context.catchingPlayer),
                        ReportCatch(context.catchingPlayer, context.target, context.modifiers, roll.result, true),
                    )
                    if (ball.state == BallState.DEFLECTED) {
                        addAll(
                            SetContext(passingInterferenceContext!!.copy(didIntercept = true)),
                            ReportInterception(context.catchingPlayer, true)
                        )
                    }
                    add(GotoNode(CheckForTouchDown))
                }
            } else {
                buildCompositeCommand {
                    val newBallState = when (ball.state) {
                        BallState.DEFLECTED -> SetBallState.scattered(ball)
                        else -> SetBallState.bouncing(ball)
                    }
                    addAll(
                        newBallState,
                        ReportCatch(context.catchingPlayer, context.target, context.modifiers, roll.result, false)
                    )
                    if (ball.state == BallState.DEFLECTED) {
                        add(ReportInterception(context.catchingPlayer, false))
                    }
                    add(GotoNode(CatchFailed))
                }
            }
        }
    }

    object CatchFailed : ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command? {
            val ball = state.currentBall()
            return when (ball.state) {
                BallState.SCATTERED -> {
                    val scatterContext = ScatterRollContext(
                        ball = ball,
                        from = ball.location
                    )
                    SetContext(scatterContext)
                }
                else -> null
            }
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure {
            return if (state.currentBall().state == BallState.SCATTERED) {
                ScatterRoll
            } else {
                Bounce
            }
        }
        override fun onExitNode(state: Game, rules: Rules): Command {
            return compositeCommandOf(
                if (state.currentBall().state == BallState.SCATTERED) {
                    GotoNode(ResolveScatteredBallLanding)
                } else {
                    ExitProcedure()
                }
            )
        }
    }

    // The ball scattered after a failed catch attempt, now it needs to land.
    // This can either be on the ground, on a player or result in a throw-in because
    // it scattered out of bounds
    object ResolveScatteredBallLanding: ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command? {
            val scatterContext = state.getContext<ScatterRollContext>()
            val ball = scatterContext.ball
            val landOutOfBounds = (scatterContext.outOfBoundsAt != null)
            val landsOnCatchingPlayer = scatterContext.landsAt?.let {
                state.field[it].player?.let { player -> rules.canCatch(state, player) }
            } ?: false
            return when {
                landOutOfBounds -> {
                    compositeCommandOf(
                        RemoveContext<ScatterRollContext>(),
                        SetBallState.outOfBounds(ball, scatterContext.outOfBoundsAt),
                        SetContext(ThrowInContext(
                            ball = ball,
                            outOfBoundsAt = scatterContext.outOfBoundsAt,
                        )),
                    )
                }
                landsOnCatchingPlayer -> {
                    compositeCommandOf(
                        RemoveContext<ScatterRollContext>(),
                        SetBallState.scattered(ball),
                        SetBallLocation(ball, scatterContext.landsAt)
                    )
                }
                !landsOnCatchingPlayer -> {
                    compositeCommandOf(
                        RemoveContext<ScatterRollContext>(),
                        SetBallState.bouncing(ball),
                        SetBallLocation(ball, scatterContext.landsAt!!)
                    )
                }
                else -> INVALID_GAME_STATE("Unexpected game state")
            }
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure {
            return when (state.currentBall().state) {
                BallState.BOUNCING -> Bounce
                BallState.SCATTERED -> Catch
                BallState.OUT_OF_BOUNDS -> ThrowIn
                else -> INVALID_GAME_STATE("Unexpected ball state: ${state.currentBall().state}")
            }
        }
        override fun onExitNode(state: Game, rules: Rules): Command {
            return compositeCommandOf(
                RemoveContext<ThrowInContext>(),
                ExitProcedure()
            )
        }
    }

    // If the catch succeeded, then we need to check if the player has a touchdown.
    object CheckForTouchDown : ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command {
            val context = state.getContext<CatchRollContext>()
            return SetContext(ScoringATouchDownContext(context.catchingPlayer))
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = ScoringATouchdown
        override fun onExitNode(state: Game, rules: Rules): Command {
            return ExitProcedure()
        }
    }
}
