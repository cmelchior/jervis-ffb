package dk.ilios.jervis.rules

import dk.ilios.jervis.actions.D3Result
import dk.ilios.jervis.actions.D6Result
import dk.ilios.jervis.actions.D8Result
import dk.ilios.jervis.actions.RollDice
import dk.ilios.jervis.model.FieldCoordinate
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.Location
import dk.ilios.jervis.model.Player
import dk.ilios.jervis.model.PlayerState
import dk.ilios.jervis.procedures.GameDrive
import dk.ilios.jervis.utils.INVALID_ACTION
import dk.ilios.jervis.utils.INVALID_GAME_STATE
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

interface BloodBowl {

}

interface DungeonBowl {

}

interface BloodBowl7 {

}

/**
 * A vector describing the result of a roll using the Random Direction Template.
 *
 * The [xModifier] and [yModifier] are the delta that needs to be applied to a
 * [FieldCoordinate] in order to move it 1 square in the desired direction.
 */
data class Direction(val xModifier: Int, val yModifier: Int)

/**
 *  Determines in what corner the Random Direction Template is placed.
 *  "Top" is defined as the direction towards 0 on the x-axis for the [FieldCoordinate]
 *  "Left" is defined as the direction towards 0 on the y-axis for the [FieldCoordinate]
 */
enum class CornerThrowInPosition(val rotateDegrees: Int) {
    TOP_LEFT(135),
    TOP_RIGHT(-135),
    BOTTOM_RIGHT(-45),
    BOTTOM_LEFT(45)
}

/**
 * Class representing the Random Direction Template.
 * See page 20 in the rulebook
 */
object RandomDirectionTemplate {

    // Order of numbers from top and clockwise around the template
//    private val order = listOf(2, 3, 5, 8, 7, 6, 4, 1)
    private val results = mapOf(
        1 to Direction(-1, -1),
        2 to Direction(0, -1),
        3 to Direction(1, -1),
        4 to Direction(-1, 0),
        5 to Direction(1, 0),
        6 to Direction(-1, 1),
        7 to Direction(0, 1),
        8 to Direction(1, 1)
    )

    /**
     * When the template is placed on the field (and not in a corner), roll
     * a D8 to determine the direction the object is moving in.
     */
    fun roll(roll: D8Result): Direction {
        return results[roll.result] ?:throw IllegalArgumentException("Only values between [1, 8] is allowed: ${roll.result}")
    }

    /**
     * When the template is placed in a corner, it needs to be rotated so only the
     * values 1-3 are visible. Once done, roll the D3 in order to determine the
     * direction.
     */
    fun roll(corner: CornerThrowInPosition, d3: D3Result): Direction {
        return rotateVector(results[d3.result]!!, corner.rotateDegrees)
    }

    private fun rotateVector(vector: Direction, angleDegrees: Int): Direction {
        // Use the Rotation Matrix to rotate the coordinates
        val angleRadians = angleDegrees * PI / 180.0
        val cosTheta = cos(angleRadians)
        val sinTheta = sin(angleRadians)
        val x: Double = vector.xModifier * cosTheta - vector.yModifier * sinTheta
        val y = vector.xModifier * sinTheta + vector.yModifier * cosTheta
        return Direction(x.roundToInt(), y.roundToInt())
    }
}

enum class KickOffEvent {
    GET_THE_REF,
    TIME_OUT,
    SOLID_DEFENSE,
    HIGH_KICK,
    CHEERING_FANS,
    BRILLIANT_COACHING,
    CHANGING_WEATHER,
    QUICK_SNAP,
    BLITZ,
    OFFICIOUS_REF,
    PITCH_INVASION
}

/**
 * Class representing the Kick-Off Event Table on page 41 in the rulebook.
 */
object KickOffEventTable {

    private val table = mapOf(
        2 to KickOffEvent.GET_THE_REF,
        3 to KickOffEvent.TIME_OUT,
        4 to KickOffEvent.SOLID_DEFENSE,
        5 to KickOffEvent.HIGH_KICK,
        6 to KickOffEvent.CHEERING_FANS,
        7 to KickOffEvent.BRILLIANT_COACHING,
        8 to KickOffEvent.CHANGING_WEATHER,
        9 to KickOffEvent.QUICK_SNAP,
        10 to KickOffEvent.BLITZ,
        11 to KickOffEvent.OFFICIOUS_REF,
        12 to KickOffEvent.PITCH_INVASION,
    )

    /**
     * Roll on the table and return the result.
     */
    fun roll(die1: D6Result, die2: D6Result): KickOffEvent {
        val result = die1.result + die2.result
        return table[result] ?: INVALID_GAME_STATE("$result was not found in the Kick-Off Event Table.")
    }
}

interface Rules {

    fun isValidSetup(state: Game): Boolean {
        val team = state.activeTeam
        val isHomeTeam = team.isHomeTeam()
        val inReserve: List<Player> = team.filter { it.state == PlayerState.STANDING && !it.location.isOnField(this) }
        val onField: List<Player> = team.filter { it.state == PlayerState.STANDING && it.location.isOnField(this) }
        val totalAvailablePlayers: UInt = inReserve.size.toUInt() + onField.size.toUInt()

        // If below 11 players, all players must be fielded
        if (totalAvailablePlayers < maxPlayersOnField && inReserve.isNotEmpty()) {
            return false
        }
        // Otherwise 11 players must be on the field
        if (onField.size.toUInt() != maxPlayersOnField) {
            println("${onField.size.toUInt()}, $maxPlayersOnField")
            return false
        }

        // 3 players must be on LoS, or if less than 3 players, all must be on LoS
        val field = state.field
        val losIndex: Int = if (isHomeTeam) lineOfScrimmageHome else lineOfScrimmageAway
        val playersOnLos = (0u + wideZone until fieldHeight - wideZone).filter { y: UInt ->
            !field[losIndex, y.toInt()].isEmpty()
        }.size
        if (onField.size.toUInt() < playersRequiredOnLineOfScrimmage && onField.size != playersOnLos) {
            return false
        }
        if (onField.size.toUInt() >= playersRequiredOnLineOfScrimmage && playersOnLos != playersRequiredOnLineOfScrimmage.toInt()) {
            return false
        }

        // Max 2 players in top wide zone
        var count = 0
        if (isHomeTeam) {
            (0 until lineOfScrimmageHome).forEach { x ->
                (0 until wideZone.toInt()).forEach { y ->
                    if (!field[x, y].isEmpty()) {
                        // They must not be on the LoS
                        if (x == (lineOfScrimmageHome - 1)) {
                            return false
                        }
                        count++
                    }
                }
            }
        } else {
            (fieldWidth - 1u until lineOfScrimmageAway.toUInt()).forEach { x ->
                (0u until wideZone).forEach { y ->
                    if (!field[x.toInt(), y.toInt()].isEmpty()) {
                        // They must not be on the LoS
                        if (x == lineOfScrimmageAway.toUInt()) {
                            return false
                        }
                        count++
                    }
                }
            }
        }
        if (count > maxPlayersInWideZone.toInt()) {
            return false
        }

        // Max 2 players in each wide zone
        // They must not be on the LoS
        // TODO
        return true
    }

    // Roll on the random direction template
    fun randomDirection(d8: D8Result): Direction = randomDirectionTemplate.roll(d8)
    fun cornerThrowIn(corner: CornerThrowInPosition, d3: D3Result): Direction {
        return randomDirectionTemplate.roll(corner, d3)
    }

    // Game length setup

    val halfsPrGame: UInt
        get() = 2u

    val turnsPrHalf: UInt
        get() = 8u

    // Field description

    // Total width of the field
    val fieldWidth: UInt
        get() = 26u

    // Total height of the field
    val fieldHeight: UInt
        get() = 15u

    // Height of the Wide Zone at the top and bottom of the field
    val wideZone: UInt
        get() = 4u

    // Width of the End Zone at each end of the field
    val endZone: UInt
        get() = 1u

    // X-coordinates for the line of scrimmage for the home team
    val lineOfScrimmageHome: Int
        get() = 12

    // X-coordinate for the line of scrimmage for the away team
    val lineOfScrimmageAway: Int
        get() = 13

    val playersRequiredOnLineOfScrimmage: UInt
        get() = 3u

    val maxPlayersInWideZone: UInt
        get() = 2u

    val maxPlayersOnField: UInt
        get() = 11u

    val randomDirectionTemplate
        get() = RandomDirectionTemplate

    val kickOffEventTable
        get() = KickOffEventTable

    // Blood Bowl 7
    // Total width of the field
//    val fieldWidth = 2
//    val fieldHeight = 11
//    val wideZone = 2
//    val endZone = 1
//    val lineOfScrimmage = 7
}