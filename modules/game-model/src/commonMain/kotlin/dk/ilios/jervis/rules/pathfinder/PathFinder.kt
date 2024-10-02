package dk.ilios.jervis.rules.pathfinder

import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.locations.FieldCoordinate

/**
 * Interface encapsulating ways to calculate paths between squares on the field.
 */
interface PathFinder {
    sealed interface SinglePathResult {
        val path: List<FieldCoordinate>
    }

    class Success(override val path: List<FieldCoordinate>, val debugInformation: Any?) : SinglePathResult

    class Failure(override val path: List<FieldCoordinate>, val debugInformation: Any?) : SinglePathResult

    interface AllPathsResult {
        // Return a map of all known distances
        val distances: Map<FieldCoordinate, Int>

        // Returns the path from the start to the goal square. If no path exists, the path
        // that is closets is returned instead.
        fun getClosestPathTo(
            goal: FieldCoordinate,
            maxMove: Int = Int.MAX_VALUE,
        ): List<FieldCoordinate>

        // Returns the path from start to goal or `null` or no path exists.
        fun getPathTo(
            goal: FieldCoordinate,
            maxMove: Int = Int.MAX_VALUE,
        ): List<FieldCoordinate>?
    }

    /**
     * Calculates the shortest distance between two fields. If target cannot be reached, the path
     * that brings you closets is returned.
     */
    fun calculateShortestPath(
        state: Game,
        start: FieldCoordinate,
        goal: FieldCoordinate,
        maxMove: Int,
        includeDebugInfo: Boolean = false,
    ): SinglePathResult

    /**
     * Calculates the shortest distance between the [start] and every reachable square on the field.
     *
     * A square is reachable if it can be reached without using any dice rolls. This means that this
     * should return paths that ends in squares with tackle zones or containing the ball, but should
     * not contain paths that move through these squares.
     */
    fun calculateAllPaths(
        state: Game,
        start: FieldCoordinate,
        maxMove: Int,
    ): AllPathsResult

    /**
     * Returns the direct path in squares between two squares on the field, start and end inclusive.
     */
    fun getStraightLine(
        state: Game,
        start: FieldCoordinate,
        end: FieldCoordinate,
    ): List<FieldCoordinate>
}
