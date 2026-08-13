package com.jervisffb.engine.rules.common.planner

import com.jervisffb.engine.actions.CompositeGameAction
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.MoveType
import com.jervisffb.engine.actions.MoveTypeSelected
import com.jervisffb.engine.actions.PitchSquareSelected
import com.jervisffb.engine.actions.TargetSquare
import com.jervisffb.engine.model.locations.PitchCoordinate
import com.jervisffb.engine.rules.common.pathfinder.PathFinder

/**
 * Encapsulates legal moves for a movement decision.
 *
 * The underlying path search remains private, so callers cannot accidentally
 * construct actions for coordinates that were not approved by the planner.
 */
class MovePlan(
    private val pathStart: PitchCoordinate? = null,
    immediateMoves: Map<PitchCoordinate, PlannedMove>,
    private val paths: PathFinder.AllPathsResult?,
    val maximumPathLength: Int,
    val movesUsedBeforePath: Int,
    private val normalMovesAvailable: Int,
    private val requiresDodgeAt: (PitchCoordinate) -> Boolean = { false },
    pathActionTail: List<GameAction> = emptyList(),
) {
    // Map of "start -> planned move" at every step of the path.
    val neighborMoves: Map<PitchCoordinate, PlannedMove> = immediateMoves.toMap()

    private val pathActionTail: List<GameAction> = pathActionTail.toList()

    val hasPaths: Boolean = paths
        ?.distances
        ?.values
        ?.any { it in 1..maximumPathLength }
        ?: false

    /**
     * Returns a copy using the supplied immediate moves while retaining this plan's paths.
     *
     * This allows callers to only display the closest available squares, while still making it possible
     * to query for path previews on longer paths.
     */
    fun withImmediateMoves(immediateMoves: Map<PitchCoordinate, PlannedMove>): MovePlan = MovePlan(
        pathStart = pathStart,
        immediateMoves = immediateMoves.toMap(),
        paths = paths,
        maximumPathLength = maximumPathLength,
        movesUsedBeforePath = movesUsedBeforePath,
        normalMovesAvailable = normalMovesAvailable,
        requiresDodgeAt = requiresDodgeAt,
        pathActionTail = pathActionTail,
    )

    fun getClosestPathTo(goal: PitchCoordinate): List<PlannedMove> {
        if (!hasPaths) return emptyList()
        val path = paths!!.getClosestPathTo(goal, maximumPathLength)
        return path
            .mapIndexed { index, coordinate ->
                val previousCoordinate = path.getOrNull(index - 1) ?: pathStart
                val target = TargetSquare.move(
                    coordinate = coordinate,
                    needRush = (index + 1 > normalMovesAvailable),
                    needDodge = previousCoordinate?.let(requiresDodgeAt) ?: false,
                )
                PlannedMove(target, createStandardMoveAction(target.coordinate, pathActionTail))
            }
    }

    companion object {

        fun empty(movesUsedBeforePath: Int = 0): MovePlan {
            return MovePlan(
                pathStart = null,
                immediateMoves = emptyMap(),
                paths = null,
                maximumPathLength = 0,
                movesUsedBeforePath = movesUsedBeforePath,
                normalMovesAvailable = 0,
            )
        }

        fun createStandardMoveAction(
            coordinate: PitchCoordinate,
            // Any additional actions to execute after the move.
            // Currently only used to decline Fumblerooski usage.
            tail: List<GameAction> = emptyList(),
        ): CompositeGameAction {
            return CompositeGameAction(
                buildList {
                    add(MoveTypeSelected(MoveType.STANDARD))
                    add(PitchSquareSelected(coordinate))
                    addAll(tail)
                },
            )
        }
    }
}
