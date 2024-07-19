package dk.ilios.jervis.rules.pathfinder

import dk.ilios.jervis.model.Field
import dk.ilios.jervis.model.FieldCoordinate
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.Team
import dk.ilios.jervis.rules.Rules
import kotlin.math.ceil
import kotlin.math.hypot
import kotlin.math.roundToInt

class BB2020PathFinder(private val rules: Rules): PathFinder {

    class DebugInformation(
        val fieldView: Array<Array<Int>>,
        val openSet: PriorityQueue<Node>,
        val cameFrom: Map<FieldCoordinate, FieldCoordinate?>,
        val gScore: Map<FieldCoordinate, Int>,
        val currentLocation: Pair<FieldCoordinate, Int>
    ): PathFinder.DebugInformation

    data class Node(val point: FieldCoordinate, val g: Int, val h: Int) : Comparable<Node> {
        val f = g + h
        override fun compareTo(other: Node) = f.compareTo(other.f)
    }

    /**
     * Calculate the shortest distance between two locations using A*.
     *
     * See https://en.wikipedia.org/wiki/A*_search_algorithm
     */
    override fun calculateShortestPath(state: Game, start: FieldCoordinate, goal: FieldCoordinate, includeDebugInfo: Boolean): PathFinder.Result {
        // Prepare a primitive version of the field that contains the following values:
        // - Int.MAX if the location is occupied
        // - i > 0 is the number of tackle zones.
        // - 0 = Field is safe to move to
        val fieldView: Array<Array<Int>> = prepareFieldView(state.field, state.activeTeam)
        var pathState = listOf<FieldCoordinate>()

        // Locations to check. Use a priority queue to always start checking the most promising path.
        val openSet = PriorityQueue<Node> { a, b -> a.compareTo(b) }
        val cameFrom = mutableMapOf<FieldCoordinate, FieldCoordinate?>()
        val gScore = mutableMapOf<FieldCoordinate, Int>().withDefault { Int.MAX_VALUE }
        // Track the closest location to the goal. Only used if goal couldn't be reached
        var closestLocation: Pair<FieldCoordinate, Int> = Pair(start, Int.MAX_VALUE)

        openSet.offer(Node(start, 0, calculateHeuristicValue(start, goal)))
        gScore[start] = 0

        while (!openSet.isEmpty) {
            val currentLocation: FieldCoordinate = openSet.poll()!!.point
            if (currentLocation == goal) {
                pathState = reconstructPath(cameFrom, currentLocation)
                break
            }
            val neighbors: List<FieldCoordinate> = currentLocation.getSurroundingCoordinates(rules, 1u)
            for (neighbor in neighbors) {
                // Skip all fields that contain tackle zones or players (except the goal
                val neighborValue = fieldView[neighbor.x][neighbor.y]
                val isGoal = (neighbor == goal)
                if (
                    (isGoal && neighborValue == Int.MAX_VALUE) // Only skip goal if occupied by another player
                    || (neighborValue > 0 && !isGoal) // Skip all intermediate steps going through tackle zones.
                    ) continue
                val tentativeGScore = gScore.getValue(currentLocation) + 1 // TODO Why + 1?
                if (tentativeGScore < gScore.getValue(neighbor)) {
                    cameFrom[neighbor] = currentLocation
                    gScore[neighbor] = tentativeGScore
                    val heuristicDistance = calculateHeuristicValue(neighbor, goal)
                    closestLocation = if (heuristicDistance < closestLocation.second) Pair(neighbor, heuristicDistance) else closestLocation
                    openSet.offer(Node(neighbor, tentativeGScore, heuristicDistance))
                }

            }
            pathState = reconstructPath(cameFrom, currentLocation)
        }

        val debugInfo: DebugInformation? = if (includeDebugInfo) {
            DebugInformation(
                fieldView,
                openSet,
                cameFrom,
                gScore,
                closestLocation
            )
        } else {
            null
        }

        // If the goal location wasn't reached, instead calculate the path to the closest possible
        return if (pathState.lastOrNull() != goal) {
            pathState = reconstructPath(cameFrom, closestLocation.first)
            PathFinder.Failure(pathState, debugInfo)
        } else {
            PathFinder.Success(pathState, debugInfo)
        }
    }

    private fun prepareFieldView(field: Field, movingTeam: Team): Array<Array<Int>> {
        val fieldView = Array(26) {
            Array(15) { 0 }
        }
        field.forEach { square ->
            if (square.isNotEmpty()) {
                // Location contains a player. Mark this field and all adjacent fields as blocked
                // if the player is an opponent.
                fieldView[square.x][square.y] = Int.MAX_VALUE
                if (square.player?.team != movingTeam && square.player?.hasTackleZones == true) {
                    square.coordinates.getSurroundingCoordinates(rules).forEach { neighbor ->
                        if (fieldView[neighbor.x][neighbor.y] < Int.MAX_VALUE) {
                            fieldView[neighbor.x][neighbor.y] += 1
                        }
                    }
                }
            }
            // TODO Other things, end zone detection, trapdoors? Anything else dangerous?
        }
        return fieldView
    }

    private fun calculateHeuristicValue(start: FieldCoordinate, end: FieldCoordinate): Int {
        val a = (end.x - start.x).toDouble()
        val b = (end.y - start.y).toDouble()
        return ceil(hypot(a, b)).roundToInt()
    }

    private fun reconstructPath(cameFrom: Map<FieldCoordinate, FieldCoordinate?>, currentLocation: FieldCoordinate): List<FieldCoordinate> {
        val path = mutableListOf(currentLocation)
        var currentPoint = currentLocation
        while (cameFrom[currentPoint] != null) {
            currentPoint = cameFrom[currentPoint]!!
            path.add(currentPoint)
        }
        return path.reversed()
    }

}