package com.jervisffb.engine.rules.common.pathfinder

import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.locations.PitchCoordinate

/**
 * Interface encapsulating ways to calculate paths between squares on the field.
 */
interface PathFinder {
    sealed interface SinglePathResult {
        val path: List<PitchCoordinate>
    }

    class Success(override val path: List<PitchCoordinate>, val debugInformation: Any?) : SinglePathResult

    class Failure(override val path: List<PitchCoordinate>, val debugInformation: Any?) : SinglePathResult

    interface AllPathsResult {
        // Return a map of all known distances
        val distances: Map<PitchCoordinate, Int>

        // Returns the path from the start to the goal square. If no path exists, the path
        // that is closets is returned instead.
        fun getClosestPathTo(
            goal: PitchCoordinate,
            maxMove: Int = Int.MAX_VALUE,
        ): List<PitchCoordinate>

        // Returns the path from start to goal or `null` or no path exists.
        fun getPathTo(
            goal: PitchCoordinate,
            maxMove: Int = Int.MAX_VALUE,
        ): List<PitchCoordinate>?
    }

    /**
     * Calculates the shortest distance between two squares. If the target cannot be reached, the path
     * that brings you closets is returned.
     */
    fun calculateShortestPath(
        state: Game,
        start: PitchCoordinate,
        goal: PitchCoordinate,
        maxMove: Int,
        includeDebugInfo: Boolean = false,
    ): SinglePathResult

    /**
     * Calculates the shortest distance between the [start] and every reachable square on the pitch.
     *
     * A square is reachable if it can be reached without using any dice rolls. This means that this
     * should return paths that ends in squares with tackle zones or containing the ball, but should
     * not contain paths that move through these squares.
     */
    fun calculateAllPaths(
        state: Game,
        start: PitchCoordinate,
        maxMove: Int,
    ): AllPathsResult

    /**
     * Returns the direct path in squares between two squares on the pitch, start and end inclusive.
     */
    fun getStraightLine(
        state: Game,
        start: PitchCoordinate,
        end: PitchCoordinate,
    ): List<PitchCoordinate>
}
