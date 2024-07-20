package dk.ilios.jervis.rules.pathfinder

import dk.ilios.jervis.model.FieldCoordinate
import dk.ilios.jervis.model.Game

/**
 * Interface encapsulating ways to calculate paths between squares on the field.
 */
interface PathFinder {

    interface DebugInformation

    sealed interface SinglePathResult
    class Success(val path: List<FieldCoordinate>, val debugInformation: DebugInformation?): SinglePathResult
    class Failure(val path: List<FieldCoordinate>, val debugInformation: DebugInformation?): SinglePathResult

    interface AllPathsResult {
        // Return a map of all known distances
        val distances: Map<FieldCoordinate, Int>
        // Returns the path from the start to the goal square. If no path exists, the path
        // that is closets is returned instead.
        fun getClosestPathTo(goal: FieldCoordinate): List<FieldCoordinate>
        // Returns the path from start to goal or `null` or no path exists.
        fun getPathTo(goal: FieldCoordinate): List<FieldCoordinate>?
    }

    /**
     * Calculates the shortest distance between two fields. If target cannot be reached, the path
     * that brings you closets is returned.
     */
    fun calculateShortestPath(
        state: Game,
        start: FieldCoordinate,
        goal: FieldCoordinate,
        includeDebugInfo: Boolean = false
    ): SinglePathResult

    /**
     * Calculates the shortest distance between the [start] and every reachable square on the field.
     *
     * A square is reachable if it can be reached without using any dice rolls.
     */
    fun calculateAllPaths(
        state: Game,
        start: FieldCoordinate
    ): AllPathsResult

    /**
     * Returns the direct path in squares between two squares on the field, start and end inclusive.
     */
    fun getStraightLine(state: Game, start: FieldCoordinate, end: FieldCoordinate): List<FieldCoordinate>
}