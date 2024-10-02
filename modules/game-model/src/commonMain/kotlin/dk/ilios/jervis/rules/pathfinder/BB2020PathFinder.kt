package dk.ilios.jervis.rules.pathfinder

import dk.ilios.jervis.model.BallState
import dk.ilios.jervis.model.Field
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.Team
import dk.ilios.jervis.model.locations.FieldCoordinate
import dk.ilios.jervis.rules.Rules
import kotlin.math.abs

class BB2020PathFinder(private val rules: Rules) : PathFinder {
    class DebugInformation(
        val fieldView: Array<Array<Int>>,
        val openSet: PriorityQueue<AStarNode>,
        val cameFrom: Map<FieldCoordinate, FieldCoordinate?>,
        val gScore: Map<FieldCoordinate, Double>,
        val currentLocation: Pair<FieldCoordinate, Int>,
    )

    data class AStarNode(val point: FieldCoordinate, val g: Double, val h: Int) : Comparable<AStarNode> {
        val f = g + h

        override fun compareTo(other: AStarNode) = f.compareTo(other.f)
    }

    data class DjikstraNode(val point: FieldCoordinate, val distanceInSteps: Int, val realDistance: Double) : Comparable<DjikstraNode> {
        override fun compareTo(other: DjikstraNode): Int {
            return realDistance.compareTo(other.realDistance)
        }
    }

    /**
     * Calculate the straight line (using squares) between two squares using Bresenham's line algorithm.
     *
     * See https://en.m.wikipedia.org/wiki/Bresenham%27s_line_algorithm
     */
    override fun getStraightLine(
        state: Game,
        start: FieldCoordinate,
        end: FieldCoordinate,
    ): List<FieldCoordinate> {
        val line = mutableListOf<FieldCoordinate>()
        var x = start.x
        var y = start.y
        val dx = abs(end.x - x)
        val sx = if (x < end.x) 1 else -1
        val dy = abs(end.y - y)
        val sy = if (y < end.y) 1 else -1
        var error = dx - dy
        while (true) {
            line.add(FieldCoordinate(x, y))
            if (x == end.x && y == end.y) break
            val e2 = 2 * error
            if (e2 >= -dy) {
                if (start.x == end.x) break
                error -= dy
                x += sx
            }
            if (e2 <= dx) {
                if (start.y == end.y) break
                error += dx
                y += sy
            }
        }
        return line
    }

    /**
     * Calculate the shortest distance between two locations using A*.
     * See https://en.wikipedia.org/wiki/A*_search_algorithm
     */
    override fun calculateShortestPath(
        state: Game,
        start: FieldCoordinate,
        goal: FieldCoordinate,
        maxMove: Int,
        includeDebugInfo: Boolean,
    ): PathFinder.SinglePathResult {
        val fieldView: Array<Array<Int>> = prepareFieldView(state.field, state.activeTeam)
        var pathState = listOf<FieldCoordinate>()

        // Locations to check. Use a priority queue to always start checking the most promising path.
        val openSet = PriorityQueue<AStarNode> { a, b -> a.compareTo(b) }
        val cameFrom = mutableMapOf<FieldCoordinate, FieldCoordinate?>()
        val gScore = mutableMapOf<FieldCoordinate, Double>().withDefault { Double.MAX_VALUE }
        // Track the closest location to the goal. Only used if goal couldn't be reached
        var closestLocation: Pair<FieldCoordinate, Int> = Pair(start, Int.MAX_VALUE)

        openSet.offer(AStarNode(start, 0.0, calculateHeuristicValue(start, goal)))
        gScore[start] = 0.0

        while (!openSet.isEmpty) {
            val currentLocation: FieldCoordinate = openSet.poll()!!.point
            if (currentLocation == goal) {
                pathState = reconstructPath(cameFrom, currentLocation, maxMove)
                break
            }
            val neighbors: List<FieldCoordinate> = currentLocation.getSurroundingCoordinates(rules, 1)
            for (neighbor in neighbors) {
                // Skip all fields that contain tackle zones or players (except the goal
                val neighborValue = fieldView[neighbor.x][neighbor.y]
                val isGoal = (neighbor == goal)
                if (
                    (isGoal && neighborValue == Int.MAX_VALUE) || // Only skip goal if occupied by another player
                    (neighborValue > 0 && !isGoal) // Skip all intermediate steps going through tackle zones.
                ) {
                    continue
                }
                val tentativeGScore = gScore.getValue(currentLocation) + currentLocation.realDistanceTo(neighbor)
                if (tentativeGScore < gScore.getValue(neighbor)) {
                    cameFrom[neighbor] = currentLocation
                    gScore[neighbor] = tentativeGScore
                    val heuristicDistance = calculateHeuristicValue(neighbor, goal)
                    closestLocation = if (heuristicDistance < closestLocation.second) Pair(neighbor, heuristicDistance) else closestLocation
                    openSet.offer(AStarNode(neighbor, tentativeGScore, heuristicDistance))
                }
            }
            pathState = reconstructPath(cameFrom, currentLocation, maxMove)
        }

        val debugInfo: DebugInformation? =
            if (includeDebugInfo) {
                DebugInformation(
                    fieldView,
                    openSet,
                    cameFrom,
                    gScore,
                    closestLocation,
                )
            } else {
                null
            }

        // If the goal location wasn't reached, instead calculate the path to the closest possible
        return if (pathState.lastOrNull() != goal) {
            pathState = reconstructPath(cameFrom, closestLocation.first, maxMove)
            PathFinder.Failure(pathState, debugInfo)
        } else {
            PathFinder.Success(pathState, debugInfo)
        }
    }

    /**
     * Calculate the shortest distance to all reachable squares using Dijkstra's algorithm.
     * See https://en.wikipedia.org/wiki/Dijkstra%27s_algorithm
     */
    override fun calculateAllPaths(
        state: Game,
        start: FieldCoordinate,
        maxMove: Int,
    ): PathFinder.AllPathsResult {
        // Prepare a primitive version of the field that contains the following values:
        // - Int.MAX if the location is occupied
        // - i > 0 is the number of tackle zones.
        // - 0 = Field is safe to move to
        val fieldView: Array<Array<Int>> = prepareFieldView(state.field, state.activeTeam)
        // Calculated distances
        val distances = mutableMapOf<FieldCoordinate, Int>().withDefault { Int.MAX_VALUE }
        // Nodes being processed
        val openSet = PriorityQueue<DjikstraNode> { a, b -> a.compareTo(b) }
        // Used to do backtracking in order to create a path
        val cameFrom = mutableMapOf<FieldCoordinate, FieldCoordinate?>()

        distances[start] = 0
        openSet.offer(DjikstraNode(start, 0, 0.0))

        while (!openSet.isEmpty) {
            val currentLocation: FieldCoordinate = openSet.poll()!!.point
            val neighbors: List<FieldCoordinate> = currentLocation.getSurroundingCoordinates(rules, 1)
            for (neighbor in neighbors) {
                val neighborValue: Int = distances.getValue(neighbor)
                // Terminal nodes can be entered, but not exited.
                val hasTackleZone = (fieldView[neighbor.x][neighbor.y] > 0)
                val hasBall = state.field[neighbor.x, neighbor.y].balls.any { it.state == BallState.ON_GROUND }
                val isTreacherousTrapdoor = false
                val isTerminalNode = hasTackleZone || hasBall || isTreacherousTrapdoor
                val tentativeDistance = distances.getValue(currentLocation) + 1

                // We found a path that is straight up more optimal.
                if (tentativeDistance < neighborValue && tentativeDistance <= maxMove) {
                    distances[neighbor] = tentativeDistance
                    cameFrom[neighbor] = currentLocation
                    val realDistance = start.realDistanceTo(neighbor)
                    if (!isTerminalNode) {
                        openSet.offer(DjikstraNode(neighbor, tentativeDistance, realDistance))
                    }
                } else if (tentativeDistance == neighborValue) {
                    // Check if we found a path that would appear more natural to players, but otherwise
                    // takes the same number of steps. This mostly means trying to find a path that is
                    // as "direct" as possible. We estimate this by combing the real distance to the start
                    // as well as the real distance to the new location. Using this heuristic will favor
                    // straight lines over diagonals, while still keeping the line from start to end as
                    // straight as possible.
                    val currentCameFromLocation: FieldCoordinate = cameFrom[neighbor]!!
                    val oldDistance = currentCameFromLocation.realDistanceTo(neighbor) + currentCameFromLocation.realDistanceTo(start)
                    val newDistance = currentLocation.realDistanceTo(neighbor) + currentLocation.realDistanceTo(start)
                    if (newDistance < oldDistance) {
                        cameFrom[neighbor] = currentLocation
                    }
                }
            }
        }

        return object : PathFinder.AllPathsResult {
            override val distances: Map<FieldCoordinate, Int> = distances

            override fun getClosestPathTo(
                goal: FieldCoordinate,
                maxMove: Int,
            ): List<FieldCoordinate> {
                if (distances.containsKey(goal)) {
                    return reconstructPath(cameFrom, goal, maxMove)
                } else {
                    // If we cannot reach the goal, we define the closet path as the one lying on a direct
                    // line from start to goal
                    val backPath = getStraightLine(state, start, goal).reversed()
                    val updatedGoal =
                        backPath.first {
                            distances.containsKey(
                                it,
                            )
                        } // Guaranteed to return a result (at worst start = goal)
                    return reconstructPath(cameFrom, updatedGoal, maxMove)
                }
            }

            override fun getPathTo(
                goal: FieldCoordinate,
                maxMove: Int,
            ): List<FieldCoordinate>? {
                return if (distances.containsKey(goal)) {
                    getClosestPathTo(goal)
                } else {
                    null
                }
            }
        }
    }

    private fun prepareFieldView(
        field: Field,
        movingTeam: Team,
    ): Array<Array<Int>> {
        // Prepare a primitive version of the field that contains the following values:
        // - Int.MAX if the location is occupied
        // - i > 0 is the number of tackle zones.
        // - 0 = Field is safe to move to

        val fieldView =
            Array(26) {
                Array(15) { 0 }
            }
        field.forEach { square ->
            if (square.isOccupied()) {
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

    private fun calculateHeuristicValue(
        start: FieldCoordinate,
        end: FieldCoordinate,
    ): Int {
        return start.distanceTo(end).toInt()
    }

    private fun reconstructPath(
        cameFrom: Map<FieldCoordinate, FieldCoordinate?>,
        currentLocation: FieldCoordinate,
        maxMove: Int,
    ): List<FieldCoordinate> {
        val path = mutableListOf(currentLocation)
        var currentPoint = currentLocation
        while (cameFrom[currentPoint] != null) {
            currentPoint = cameFrom[currentPoint]!!
            path.add(currentPoint)
        }
        path.removeLast()

        return if (path.isEmpty()) {
            path
        } else {
            try {
                path.reversed().subList(0, maxMove.coerceAtMost(path.size))
            } catch(ex: Throwable) {
                println(ex)
                TODO()
            }
        }
    }
}
