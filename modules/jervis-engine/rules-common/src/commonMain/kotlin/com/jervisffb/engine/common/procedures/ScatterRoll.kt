package com.jervisffb.engine.common.procedures

import com.jervisffb.engine.actions.D8Result
import com.jervisffb.engine.actions.Dice
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.actions.RollDice
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.context.UpdateContext
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.probabiliy.AddChanceObservation
import com.jervisffb.engine.common.context.ScatterRollContext
import com.jervisffb.engine.common.procedures.dicerolls.createFinalTableLookupObservation
import com.jervisffb.engine.common.reports.ReportDiceRoll
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.fsm.castDiceRollList
import com.jervisffb.engine.model.BallState
import com.jervisffb.engine.model.Direction
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.context.assertContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.locations.PitchCoordinate
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.statistics.probability.observation.ChanceObservationHandler
import com.jervisffb.engine.utils.INVALID_GAME_STATE
import com.jervisffb.engine.utils.assert
import kotlin.math.pow

// To avoid having to re-calulate scatter options every time we want
// to create a ChanceObservation for it, we instead cache the result here.
// While this will be retained for the lifetime of the app, it seems worth
// it to reduce the computational overhead during games.
// Cache size per direction template: 8 * 8 * 8 = 512 objects
object ScatterCache {

    data class ScatterOffset(val x: Int, val y: Int)

    private data class ScatterPath(val steps: List<ScatterOffset>)

    private val cache = mutableMapOf<List<Direction>, List<ScatterPath>>()

    /**
     * Return how many different scatter combinations (3D8) produce the selected
     * scatter result. When the ball leaves the pitch, only the square it leaves
     * from is used to identify the result; the out-of-bounds landing coordinate
     * itself is not relevant.
     */
    fun getCombinations(
        start: PitchCoordinate,
        landingAt: PitchCoordinate,
        outOfBoundsAt: PitchCoordinate?,
        rules: Rules,
    ): Int {
        val targetOffset = if (outOfBoundsAt == null) {
            ScatterOffset(landingAt.x - start.x, landingAt.y - start.y)
        } else {
            ScatterOffset(outOfBoundsAt.x - start.x, outOfBoundsAt.y - start.y)
        }
        return getPaths(rules).count { path ->
            var previousOffset = ScatterOffset(0, 0)
            var outOfBoundsAtOffset: ScatterOffset? = null
            for (step in path.steps) {
                val location = PitchCoordinate(start.x + step.x, start.y + step.y)
                if (location.isOutOfBounds(rules)) {
                    outOfBoundsAtOffset = previousOffset
                    break
                }
                previousOffset = step
            }
            when (outOfBoundsAt) {
                null -> outOfBoundsAtOffset == null && path.steps.last() == targetOffset
                else -> outOfBoundsAtOffset == targetOffset
            }
        }
    }

    private fun getPaths(rules: Rules): List<ScatterPath> {
        val directions = D8Result.allOptions().map(rules::direction)
        return cache.getOrPut(directions) {
            buildList {
                for (first in D8Result.allOptions()) {
                    for (second in D8Result.allOptions()) {
                        for (third in D8Result.allOptions()) {
                            val rolls = listOf(first, second, third)
                            var offset = ScatterOffset(0, 0)
                            val steps = rolls.map { roll ->
                                val direction = directions[roll.value - 1]
                                offset = ScatterOffset(
                                    offset.x + direction.xModifier,
                                    offset.y + direction.yModifier,
                                )
                                offset
                            }
                            add(ScatterPath(steps))
                        }
                    }
                }
            }
        }
    }
}


/**
 * Resolve a Scatter.
 *
 * Do not try to land the ball or update its location after the scatter, this is left
 * up to the caller of this procedure.
 *
 * See page 25 in the rulebook.
 */
object ScatterRoll : Procedure(), ChanceObservationHandler {
    override val initialNode: Node = RollDice
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null
    override fun isValid(state: Game, rules: Rules) {
        state.assertContext<ScatterRollContext>()
        state.currentBallOrNull()?.let {
            if (it.state != BallState.SCATTERED) {
                INVALID_GAME_STATE("Ball is not scattered, but ${it.state}")
            }
        }
    }

    object RollDice : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team? = null

        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            return listOf(RollDice(Dice.D8, Dice.D8, Dice.D8))
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return castDiceRollList<D8Result>(action) { dice: List<D8Result> ->
                assert(dice.size == 3)
                val context = state.getContext<ScatterRollContext>()
                var scatteredLocation = context.from
                var outOfBoundsAt: PitchCoordinate? = null
                for (diceResult in dice) {
                    val startLocation = scatteredLocation
                    scatteredLocation = scatteredLocation.move(rules.direction(diceResult), 1)
                    if (scatteredLocation.isOutOfBounds(rules)) {
                        outOfBoundsAt = startLocation
                        break
                    }
                }
                val chanceObservation = createFinalTableLookupObservation(
                    state = state,
                    team = state.activeTeam ?: state.kickingTeam,
                    rollType = DiceRollType.SCATTER,
                    dice = dice,
                    favorableOutcomes = ScatterCache.getCombinations(
                        start = context.from,
                        landingAt = scatteredLocation,
                        outOfBoundsAt = outOfBoundsAt,
                        rules = rules,
                    ),
                    possibleOutcomes = D8Result.SIDES.toDouble().pow(dice.size).toInt()
                )
                @Suppress("DATA_CLASS_INVISIBLE_COPY_USAGE_WARNING")
                compositeCommandOf(
                    ReportDiceRoll(DiceRollType.SCATTER, dice),
                    chanceObservation?.let(::AddChanceObservation),
                    UpdateContext(
                        state.getContext<ScatterRollContext>().copy(
                            scatterRoll = dice,
                            landsAt = scatteredLocation,
                            outOfBoundsAt = outOfBoundsAt,
                        )
                    ),
                    ExitProcedure()
                )
            }
        }
    }
}
