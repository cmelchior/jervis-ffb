package dk.ilios.jervis.rules.pathfinder

import dk.ilios.jervis.model.FieldCoordinate
import dk.ilios.jervis.model.Game

interface PathFinder {

    interface DebugInformation

    sealed interface Result
    class Success(val path: List<FieldCoordinate>, val debugInformation: DebugInformation?): Result
    class Failure(val path: List<FieldCoordinate>, val debugInformation: DebugInformation?): Result

    /**
     * Calculates the shortest distance between two fields. If target cannot be reached, the path
     * that brings you closets is returned.
     */
    fun calculateShortestPath(state: Game, start: FieldCoordinate, goal: FieldCoordinate, includeDebugInfo: Boolean = false): Result
}