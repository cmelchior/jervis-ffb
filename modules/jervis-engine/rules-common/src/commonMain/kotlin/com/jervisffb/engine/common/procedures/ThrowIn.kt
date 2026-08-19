package com.jervisffb.engine.common.procedures

import com.jervisffb.engine.actions.D3Result
import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.actions.Dice
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.actions.RollDice
import com.jervisffb.engine.actions.TargetSquare.Companion.direction
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.SetBallLocation
import com.jervisffb.engine.commands.SetBallState
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.context.AddContext
import com.jervisffb.engine.commands.context.RemoveContext
import com.jervisffb.engine.commands.context.UpdateContext
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.commands.probabiliy.AddChanceObservation
import com.jervisffb.engine.common.commands.SetTurnOver
import com.jervisffb.engine.common.context.PuntContext
import com.jervisffb.engine.common.context.ThrowInContext
import com.jervisffb.engine.common.procedures.dicerolls.createFinalTableLookupObservation
import com.jervisffb.engine.common.reports.ReportDiceRoll
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.ParentNode
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.fsm.castDiceRoll
import com.jervisffb.engine.fsm.castDiceRollList
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.TurnOver
import com.jervisffb.engine.model.context.assertContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.context.getContextOrNull
import com.jervisffb.engine.model.locations.PitchCoordinate
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.builder.GameType
import com.jervisffb.engine.rules.builder.GameVersion
import com.jervisffb.engine.statistics.probability.observation.ChanceObservationHandler
import com.jervisffb.engine.utils.assert
import com.jervisffb.engine.utils.sum

/**
 * Resolve a Throw In after a ball went out of bounds, up until the ball is caught
 * or lands on an empty square. This includes checking for touchdowns.
 *
 * If a Throw-int triggers a turnover, this should be handled by the caller of
 * this procedure.
 *
 * This procedure supports both Standard and BB7 throw-ins. The only thing
 * that changes between the two is the distance the ball is thrown (2D6 vs.
 * 1D6+2).
 *
 * See page 51 in the BB2020 rulebook.
 * See page 73 in the BB2025 rulebook.
 * See page 13 in Spike 22.
 *
 * Developer's Commentary:
 * In BB2025, Bouncing Balls (page 34) says that:
 * "...When the ball hits the pitch, it will Bounce. When the rules tell you to
 * Bounce the ball..."
 *
 * Throw-in (page 73) does not say anything about bouncing, which would indicate
 * that the ball does indeed not bounce. However, this is a change from BB2020,
 * and also the only place when a ball no longer bounces. Catches, being knocked
 * down and kick-off all still bounce.
 *
 * As the rules also prefix Bouncing with "When the ball hits the pitch...",
 * this could indicate that removing the sentence from Throw-in was an editing
 *  mistake and not intended. At least NAF thinks so, as they have ruled that
 * the ball does indeed bounce after a Throw-in.
 *
 * This probably needs to be addressed in a FAQ, but until it does, Jervis
 * follows the NAF interpretation of this and will bounce the
 * ball after a throw-in.
 */
object ThrowIn : Procedure(), ChanceObservationHandler {
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
            return listOf(RollDice(Dice.D3, type = DiceRollType.THROWIN_DIRECTION))
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return castDiceRoll<D3Result>(action) { d3 ->
                val context = state.getContext<ThrowInContext>()
                val direction = rules.throwIn(context.outOfBoundsAt, d3)
                val ball = context.ball
                val chanceObservation = createFinalTableLookupObservation(
                    state = state,
                    team = observationTeam(state),
                    rollType = DiceRollType.THROWIN_DIRECTION,
                    dice = listOf(d3),
                    favorableOutcomes = 1,
                    possibleOutcomes = d3.max.toInt(),
                )
                return compositeCommandOf(
                    ReportDiceRoll(DiceRollType.THROWIN_DIRECTION, d3),
                    chanceObservation?.let(::AddChanceObservation),
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
            val dice = when (rules.gameType) {
                GameType.STANDARD,
                GameType.DUNGEON_BOWL,
                GameType.GUTTER_BOWL -> listOf(Dice.D6, Dice.D6)
                GameType.BB7 -> listOf(Dice.D6)
            }
            return listOf(RollDice(dice, type = DiceRollType.THROWIN_DISTANCE))
        }
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return castDiceRollList<D6Result>(action) { dice ->
                val diceDistance = calculateDiceDistance(rules, dice)
                val context = state.getContext<ThrowInContext>()
                val chanceObservation = createFinalTableLookupObservation(
                    state = state,
                    team = observationTeam(state),
                    rollType = DiceRollType.THROWIN_DISTANCE,
                    dice = dice,
                    favorableOutcomes = when (rules.gameType == GameType.BB7) {
                        true -> D6Result.SIDES - dice.single().value + 1
                        false -> D6Result.combinationsEqualToTotal(dice = dice.size, total = diceDistance)
                    },
                    possibleOutcomes = when (rules.gameType == GameType.BB7) {
                        true -> D6Result.SIDES
                        false -> dice.size * D6Result.SIDES
                    },
                )

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
                        chanceObservation?.let(::AddChanceObservation),
                        UpdateContext(context.copy(distance = dice)),
                        SetBallLocation(ball, ballPosition),
                        SetBallState.outOfBounds(ball, outOfBoundsAt),
                        GotoNode(ResolveOutOfBounds)
                    )
                } else {
                    compositeCommandOf(
                        ReportDiceRoll(DiceRollType.THROWIN_DISTANCE, dice),
                        chanceObservation?.let(::AddChanceObservation),
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

    //
    // HELPER FUNCTIONS
    //
    private fun observationTeam(state: Game): Team = state.activeTeam ?: state.kickingTeam

    private fun calculateDiceDistance(rules: Rules, dice: List<D6Result>): Int {
        return when (rules.gameType) {
            GameType.STANDARD,
            GameType.DUNGEON_BOWL,
            GameType.GUTTER_BOWL -> {
                // Throw-in is 2D6
                assert(dice.size == 2)
                dice.sum()
            }
            GameType.BB7 -> {
                // Throw-in is 1D6 + 2
                assert(dice.size == 1)
                dice.sum() + 2
            }
        }
    }

}
