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
    immediateMoves: Map<PitchCoordinate, PlannedMove>,
    private val paths: PathFinder.AllPathsResult?,
    val maximumPathLength: Int,
    val movesUsedBeforePath: Int,
    private val normalMovesAvailable: Int,
    private val startingRequiresDodge: Boolean,
    pathActionTail: List<GameAction> = emptyList(),
) {

    // Returns the target coordinate at every step of the path.
    val immediateMoves: Map<PitchCoordinate, PlannedMove> = immediateMoves.toMap()

    private val pathActionTail: List<GameAction> = pathActionTail.toList()

    val hasPaths: Boolean = paths
        ?.distances
        ?.values
        ?.any { it in 1..maximumPathLength }
        ?: false

    fun getClosestPathTo(goal: PitchCoordinate): List<PlannedMove> {
        if (!hasPaths) return emptyList()
        return paths!!
            .getClosestPathTo(goal, maximumPathLength)
            .mapIndexed { index, coordinate ->
                val target = TargetSquare.move(
                    coordinate = coordinate,
                    needRush = (index + 1 > normalMovesAvailable),
                    needDodge = (index == 0 && startingRequiresDodge),
                )
                PlannedMove(target, createStandardMoveAction(target.coordinate, pathActionTail))
            }
    }

    companion object {

        fun empty(movesUsedBeforePath: Int = 0): MovePlan {
            return MovePlan(
                immediateMoves = emptyMap(),
                paths = null,
                maximumPathLength = 0,
                movesUsedBeforePath = movesUsedBeforePath,
                normalMovesAvailable = 0,
                startingRequiresDodge = false,
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
