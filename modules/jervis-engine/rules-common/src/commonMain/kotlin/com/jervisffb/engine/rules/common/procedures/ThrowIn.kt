package com.jervisffb.engine.rules.common.procedures

import com.jervisffb.engine.actions.D3Result
import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.actions.Dice
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.actions.RollDice
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.SetBallLocation
import com.jervisffb.engine.commands.SetBallState
import com.jervisffb.engine.commands.SetTurnOver
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.context.AddContext
import com.jervisffb.engine.commands.context.RemoveContext
import com.jervisffb.engine.commands.context.UpdateContext
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.ParentNode
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.fsm.castDiceRoll
import com.jervisffb.engine.fsm.castDiceRollList
import com.jervisffb.engine.model.Ball
import com.jervisffb.engine.model.Direction
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.TurnOver
import com.jervisffb.engine.model.context.ProcedureContext
import com.jervisffb.engine.model.context.PuntContext
import com.jervisffb.engine.model.context.assertContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.context.getContextOrNull
import com.jervisffb.engine.model.locations.PitchCoordinate
import com.jervisffb.engine.reports.ReportDiceRoll
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.builder.GameVersion
import com.jervisffb.engine.utils.assert
import com.jervisffb.engine.utils.sum

data class ThrowInContext(
    val ball: Ball,
    val outOfBoundsAt: PitchCoordinate,
    val directionRoll: D3Result? = null,
    val direction: Direction? = null,
    val distance: List<D6Result> = emptyList(),
): ProcedureContext

/**
 * Resolve a Throw In after a ball went out of bounds, up until the ball is caught
 * or lands on an empty square. This includes checking for touchdowns.
 *
 * If a Throw-int triggers a turnover, this should be handled by the caller of
 * this procedure.
 *
 * See page 51 in the BB2020 rulebook.
 * See page 73 in the BB2025 rulebook.
 *
 * Developer's Commentary:
 * In BB2025, Bouncing Balls (page 34 in the BB2025 rulebook says that:
 * "...When the ball hits the pitch, it will Bounce. When the rules tell you to
 * Bounce the ball..."
 *
 * Throw-in (page 73) does not say anything about bouncing, which would indicate
 * that the ball does indeed not bounce. However, this is a change from BB2020,
 * and also the only place when a ball no longer bounce, catches, being knocked
 * down and kick-off all still bounce.
 *
 * As the rules also prefix Bouncing with "When the ball hits the pitch...",
 * this could indicate that removing the sentence from Throw-in was an editing
 * mistake, and not intended. At least NAF things so, as they have ruled that
 * the ball does indeed bounce after a Throw-in.
 *
 * This probably needs to be addressed in a FAQ, but until it does, Jervis
 * follows the NAF interpretation of this and will bounce the
 * ball after a throw-in.
 */
object ThrowIn : Procedure() {
    override val initialNode: Node = RollDirection
    override fun onEnterProcedure(state: Game, rules: Rules): Command? {
        // When punting, if the ball leaves the pitch for any reason, it is a turnover.
        // For now, we just mark it here, so we can respond later
        val puntContext = state.getContextOrNull<PuntContext>()
        return when (puntContext != null) {
            true -> SetTurnOver(TurnOver.STANDARD)
            false -> null
        }
    }
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null
    override fun isValid(state: Game, rules: Rules) {
        state.assertContext<ThrowInContext>()
        state.currentBallOrNull() ?: error("Missing current ball")
    }

    object RollDirection : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team? = null

        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            return listOf(RollDice(Dice.D3))
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return castDiceRoll<D3Result>(action) { d3 ->
                val context = state.getContext<ThrowInContext>()
                val direction = rules.throwIn(context.outOfBoundsAt, d3)
                val ball = context.ball
                return compositeCommandOf(
                    ReportDiceRoll(DiceRollType.THROWIN_DIRECTION, d3),
                    UpdateContext(context.copy(
                        directionRoll =  d3,
                        direction = direction,
                    )),
                    SetBallState.thrownIn(ball),
                    GotoNode(RollDistance)
                )
            }
        }
    }

    object RollDistance : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team? = null
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            return listOf(RollDice(Dice.D6, Dice.D6))
        }
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return castDiceRollList<D6Result>(action) { dice ->
                assert(dice.size == 2)
                val context = state.getContext<ThrowInContext>()
                val diceDistance = dice.sum()

                // Move the ball the entire distance until it either goes out of bounds again
                // or hit an empty location
                val direction = context.direction!!
                val ball = context.ball
                var ballPosition = context.outOfBoundsAt
                var outOfBoundsAt: PitchCoordinate? = null


                // In BB2024, the first square is counted as 0, while in BB2025, it is counted as 1
                val travelDistance = when (rules.baseVersion) {
                    GameVersion.BB2020 -> 1 .. diceDistance
                    GameVersion.BB2025 -> 2 .. diceDistance
                }

                for (d in travelDistance) {
                    val start = ballPosition
                    ballPosition = start.move(direction, 1)
                    if (ballPosition.isOutOfBounds(rules)) {
                        outOfBoundsAt = start
                        break
                    }
                }

                return if (outOfBoundsAt != null) {
                    compositeCommandOf(
                        ReportDiceRoll(DiceRollType.THROWIN_DISTANCE, dice),
                        UpdateContext(context.copy(distance = dice)),
                        SetBallLocation(ball, ballPosition),
                        SetBallState.outOfBounds(ball, outOfBoundsAt),
                        GotoNode(ResolveOutOfBounds)
                    )
                } else {
                    compositeCommandOf(
                        ReportDiceRoll(DiceRollType.THROWIN_DISTANCE, dice),
                        UpdateContext(context.copy(distance = dice)),
                        SetBallLocation(ball, ballPosition),
                        GotoNode(ResolveLandOnPitch)
                    )
                }
            }
        }
    }

    object ResolveOutOfBounds : ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command? {
            // Replace the current throw in context
            // TODO Does this ruin reporting logging?
            val oldContext = state.getContext<ThrowInContext>()
            return AddContext(ThrowInContext(oldContext.ball, oldContext.ball.outOfBoundsAt!!))
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = ThrowIn
        override fun onExitNode(state: Game, rules: Rules): Command {
            return compositeCommandOf(
                RemoveContext<ThrowInContext>(),
                ExitProcedure()
            )
        }
    }

    object ResolveLandOnPitch : ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = ResolveBallLandingOnPitch
        override fun onExitNode(state: Game, rules: Rules): Command {
            return ExitProcedure()
        }
    }
}
