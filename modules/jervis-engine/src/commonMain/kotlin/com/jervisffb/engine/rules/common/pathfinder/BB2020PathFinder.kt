package com.jervisffb.engine.rules.common.pathfinder

import com.jervisffb.engine.model.BallState
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Pitch
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.locations.PitchCoordinate
import com.jervisffb.engine.rules.Rules
import kotlinx.serialization.Serializable
import kotlin.math.abs

@Serializable
class BB2020PathFinder() : PathFinder {
    class DebugInformation(
        val pitchView: Array<Array<Int>>,
        val openSet: PriorityQueue<AStarNode>,
        val cameFrom: Map<PitchCoordinate, PitchCoordinate?>,
        val gScore: Map<PitchCoordinate, Double>,
        val currentLocation: Pair<PitchCoordinate, Int>,
    )

    data class AStarNode(
        val point: PitchCoordinate,
        val g: Double,
        val h: Int,
    ) : Comparable<AStarNode> {
        val f = g + h
        override fun compareTo(other: AStarNode) = f.compareTo(other.f)
    }

    data class DjikstraNode(val point: PitchCoordinate, val distanceInSteps: Int, val realDistance: Double) : Comparable<DjikstraNode> {
        override fun compareTo(other: DjikstraNode): Int {
            return realDistance.compareTo(other.realDistance)
        }
    }

    /**
     * Calculate the straight line (using squares) between two squares using Bresenham's line algorithm.
     * See https://en.m.wikipedia.org/wiki/Bresenham%27s_line_algorithm
     */
    override fun getStraightLine(
        state: Game,
        start: PitchCoordinate,
        end: PitchCoordinate,
    ): List<PitchCoordinate> {
        val line = mutableListOf<PitchCoordinate>()
        var x = start.x
        var y = start.y
        val dx = abs(end.x - x)
        val sx = if (x < end.x) 1 else -1
        val dy = abs(end.y - y)
        val sy = if (y < end.y) 1 else -1
        var error = dx - dy
        while (true) {
            line.add(PitchCoordinate(x, y))
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
        start: PitchCoordinate,
        goal: PitchCoordinate,
        maxMove: Int,
        includeDebugInfo: Boolean,
    ): PathFinder.SinglePathResult {
        val pitchView: Array<Array<Int>> = preparePitchView(state.rules,state.pitch, state.activeTeamOrThrow())
        var pathState = listOf<PitchCoordinate>()

        // Locations to check. Use a priority queue to always start checking the most promising path.
        val openSet = PriorityQueue<AStarNode> { a, b -> a.compareTo(b) }
        val cameFrom = mutableMapOf<PitchCoordinate, PitchCoordinate?>()
        val gScore = mutableMapOf<PitchCoordinate, Double>().withDefault { Double.MAX_VALUE }
        // Track the closest location to the goal. Only used if goal couldn't be reached
        var closestLocation: Pair<PitchCoordinate, Int> = Pair(start, Int.MAX_VALUE)

        openSet.offer(AStarNode(start, 0.0, calculateHeuristicValue(start, goal)))
        gScore[start] = 0.0

        while (!openSet.isEmpty) {
            val currentNode = openSet.poll()!!
            val currentLocation: PitchCoordinate = currentNode.point
            if (currentLocation == goal) {
                pathState = reconstructPath(cameFrom, currentLocation, maxMove)
                break
            }
            val neighbors: List<PitchCoordinate> = currentLocation.getSurroundingCoordinates(state.rules, 1)
            for (neighbor in neighbors) {
                // We do not allow any path to go through a square that either contains Tackle Zones
                // or the Ball (anything that might require a dice roll), but we allow the path
                // to terminate there.
                val neighborValue = pitchView[neighbor.x][neighbor.y]
                val hasBall = state.pitch[neighbor].balls.any { it.state == BallState.ON_GROUND }
                val inTackleZone = (neighborValue > 0 && neighborValue < Int.MAX_VALUE)
                val isTerminalNode = hasBall || inTackleZone

                // Skip all squares containing a player
                if (neighborValue  == Int.MAX_VALUE) {
                    continue
                }
                val tentativeGScore = gScore.getValue(currentLocation) + currentLocation.realDistanceTo(neighbor)
                if (tentativeGScore < gScore.getValue(neighbor)) {
                    cameFrom[neighbor] = currentLocation
                    gScore[neighbor] = tentativeGScore
                    val heuristicDistance = calculateHeuristicValue(neighbor, goal)
                    closestLocation = if (heuristicDistance < closestLocation.second) Pair(neighbor, heuristicDistance) else closestLocation
                    if (!isTerminalNode) {
                        openSet.offer(AStarNode(neighbor, tentativeGScore, heuristicDistance))
                    }
                }
            }
            pathState = reconstructPath(cameFrom, currentLocation, maxMove)
        }

        val debugInfo: DebugInformation? =
            if (includeDebugInfo) {
                DebugInformation(
                    pitchView,
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
        start: PitchCoordinate,
        maxMove: Int,
    ): PathFinder.AllPathsResult {
        // Prepare a primitive version of the pitch that contains the following values:
        // - Int.MAX if the square is occupied
        // - i > 0 is the number of tackle zones.
        // - 0 = Square is safe to move to
        val pitchView: Array<Array<Int>> = preparePitchView(state.rules,state.pitch, state.activeTeamOrThrow())
        // Calculated distances
        val distances = mutableMapOf<PitchCoordinate, Int>().withDefault { Int.MAX_VALUE }
        // Nodes being processed
        val openSet = PriorityQueue<DjikstraNode> { a, b -> a.compareTo(b) }
        // Used to do backtracking in order to create a path
        val cameFrom = mutableMapOf<PitchCoordinate, PitchCoordinate?>()

        distances[start] = 0
        openSet.offer(DjikstraNode(start, 0, 0.0))

        while (!openSet.isEmpty) {
            val currentLocation: PitchCoordinate = openSet.poll()!!.point
            val neighbors: List<PitchCoordinate> = currentLocation.getSurroundingCoordinates(state.rules, 1)
            for (neighbor in neighbors) {
                val neighborValue: Int = distances.getValue(neighbor)

                // Skip all squares containing a player
                if (pitchView[neighbor.x][neighbor.y] == Int.MAX_VALUE) {
                    continue
                }

                // Terminal nodes can be entered, but not exited.
                val hasTackleZone = (pitchView[neighbor.x][neighbor.y] > 0)
                val hasBall = state.pitch[neighbor.x, neighbor.y].balls.any { it.state == BallState.ON_GROUND }
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
                    val currentCameFromLocation: PitchCoordinate = cameFrom[neighbor]!!
                    val oldDistance = currentCameFromLocation.realDistanceTo(neighbor) + currentCameFromLocation.realDistanceTo(start)
                    val newDistance = currentLocation.realDistanceTo(neighbor) + currentLocation.realDistanceTo(start)
                    if (newDistance < oldDistance) {
                        cameFrom[neighbor] = currentLocation
                    }
                }
            }
        }

        return object : PathFinder.AllPathsResult {
            override val distances: Map<PitchCoordinate, Int> = distances

            override fun getClosestPathTo(
                goal: PitchCoordinate,
                maxMove: Int,
            ): List<PitchCoordinate> {
                if (maxMove < 0) throw IllegalArgumentException("Illegal max move: $maxMove")
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
                goal: PitchCoordinate,
                maxMove: Int,
            ): List<PitchCoordinate>? {
                return if (distances.containsKey(goal)) {
                    getClosestPathTo(goal)
                } else {
                    null
                }
            }
        }
    }

    private fun preparePitchView(
        rules: Rules,
        pitch: Pitch,
        movingTeam: Team,
    ): Array<Array<Int>> {
        // Prepare a primitive version of the pitch that contains the following values:
        // - Int.MAX if the square is occupied
        // - i > 0 is the number of tackle zones.
        // - 0 = square is safe to move to
        val pitchView =
            Array(26) {
                Array(15) { 0 }
            }
        pitch.forEach { square ->
            if (square.isOccupied()) {
                // Location contains a player. Mark this square and all adjacent squares as blocked
                // if the player is an opponent.
                pitchView[square.x][square.y] = Int.MAX_VALUE
                if (square.player?.team != movingTeam && square.player?.hasTackleZones == true) {
                    square.coordinates.getSurroundingCoordinates(rules).forEach { neighbor ->
                        if (pitchView[neighbor.x][neighbor.y] < Int.MAX_VALUE) {
                            pitchView[neighbor.x][neighbor.y] += 1
                        }
                    }
                }
            }
            // TODO Other things, end zone detection, trapdoors? Anything else dangerous?
        }
        return pitchView
    }

    private fun calculateHeuristicValue(
        start: PitchCoordinate,
        end: PitchCoordinate,
    ): Int {
        return start.distanceTo(end)
    }

    private fun reconstructPath(
        cameFrom: Map<PitchCoordinate, PitchCoordinate?>,
        currentLocation: PitchCoordinate,
        maxMove: Int,
    ): List<PitchCoordinate> {
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
            } catch (ex: Throwable) {
                println(ex)
                TODO()
            }
        }
    }
}
