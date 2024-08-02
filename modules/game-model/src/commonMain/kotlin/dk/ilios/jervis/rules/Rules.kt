package dk.ilios.jervis.rules

import dk.ilios.jervis.actions.D3Result
import dk.ilios.jervis.actions.D6Result
import dk.ilios.jervis.actions.D8Result
import dk.ilios.jervis.model.DiceModifier
import dk.ilios.jervis.model.FieldSquare
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.Player
import dk.ilios.jervis.model.PlayerState
import dk.ilios.jervis.model.Team
import dk.ilios.jervis.procedures.CatchModifier
import dk.ilios.jervis.procedures.bb2020.kickoff.Blitz
import dk.ilios.jervis.procedures.bb2020.kickoff.BrilliantCoaching
import dk.ilios.jervis.procedures.bb2020.kickoff.ChangingWeather
import dk.ilios.jervis.procedures.bb2020.kickoff.CheeringFans
import dk.ilios.jervis.procedures.bb2020.kickoff.GetTheRef
import dk.ilios.jervis.procedures.bb2020.kickoff.HighKick
import dk.ilios.jervis.procedures.bb2020.kickoff.OfficiousRef
import dk.ilios.jervis.procedures.bb2020.kickoff.PitchInvasion
import dk.ilios.jervis.procedures.bb2020.kickoff.QuickSnap
import dk.ilios.jervis.procedures.bb2020.kickoff.SolidDefense
import dk.ilios.jervis.procedures.bb2020.kickoff.TimeOut
import dk.ilios.jervis.rules.pathfinder.BB2020PathFinder
import dk.ilios.jervis.rules.pathfinder.PathFinder
import dk.ilios.jervis.rules.tables.CornerThrowInPosition
import dk.ilios.jervis.rules.tables.Direction
import dk.ilios.jervis.rules.tables.PrayersToNuffleTable
import dk.ilios.jervis.rules.tables.RandomDirectionTemplate
import dk.ilios.jervis.rules.tables.TableResult
import dk.ilios.jervis.rules.tables.WeatherTable
import dk.ilios.jervis.utils.INVALID_GAME_STATE

interface BloodBowl {

}

interface DungeonBowl {

}

interface BloodBowl7 {

}



/**
 * Class representing the Kick-Off Event Table on page 41 in the rulebook.
 */
object KickOffEventTable {

    private val table = mapOf(
        2 to TableResult("Get the Ref", GetTheRef),
        3 to TableResult("Time Out", TimeOut),
        4 to TableResult("Solid Defense", SolidDefense),
        5 to TableResult("High Kick", HighKick),
        6 to TableResult("Cheering Fans", CheeringFans),
        7 to TableResult("Brilliant Coaching", BrilliantCoaching),
        8 to TableResult("Changing Weather", ChangingWeather),
        9 to TableResult("Quick Snap", QuickSnap),
        10 to TableResult("Blitz", Blitz),
        11 to TableResult("Officious Ref", OfficiousRef),
        12 to TableResult("Pitch Invasion", PitchInvasion),
    )

    /**
     * Roll on the Kick-Off table and return the result.
     */
    fun roll(die1: D6Result, die2: D6Result): TableResult {
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
            return false
        }

        // Check LoS requirements
        val field = state.field
        val losIndex: Int = if (isHomeTeam) lineOfScrimmageHome else lineOfScrimmageAway
        val playersOnLos = (0u + wideZone until fieldHeight - wideZone).filter { y: UInt ->
            !field[losIndex, y.toInt()].isEmpty()
        }.size

        // If available, 3 players must be on the widezone LoS
        if (onField.size.toUInt() >= playersRequiredOnLineOfScrimmage && playersOnLos < playersRequiredOnLineOfScrimmage.toInt()) {
            return false
        }

        // If less than 3 players, all must be on the widezone LoS
        if (onField.size.toUInt() < playersRequiredOnLineOfScrimmage && onField.size != playersOnLos) {
            return false
        }

        // Max 2 players in top wide zone. They must not be on the widezone LoS.
        var count = 0
        if (isHomeTeam) {
            (0 .. lineOfScrimmageHome).forEach { x ->
                (0 until wideZone.toInt()).forEach { y ->
                    if (field[x, y].isNotEmpty()) {
                        // They must not be on the LoS
                        if (x == lineOfScrimmageHome) {
                            return false
                        }
                        count++
                    }
                }
            }
        } else {
            (fieldWidth - 1u downTo lineOfScrimmageAway.toUInt()).forEach { x ->
                (0u until wideZone).forEach { y ->
                    if (field[x.toInt(), y.toInt()].isNotEmpty()) {
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

        // TODO Max 2 players in bottom wide zone
        // TODO They must not be on the LoS
        return true
    }

    // Roll on the random direction template
    fun direction(d8: D8Result): Direction = randomDirectionTemplate.roll(d8)
    fun cornerThrowIn(corner: CornerThrowInPosition, d3: D3Result): Direction {
        return randomDirectionTemplate.roll(corner, d3)
    }

    fun isKickingOff(state: Game): Boolean {
        TODO("Not yet implemented")
    }

    /**
     * Returns whether a not a player is eligible for catching a ball that landed in his field.
     */
    fun canCatch(state: Game, player: Player): Boolean {
        return player.hasTackleZones && player.state == PlayerState.STANDING && player.location.isOnField(this)
    }

    /**
     * Return `true` if this player is able to mark other players.
     */
    fun canMark(player: Player): Boolean {
        return player.hasTackleZones
    }

    /**
     * Return `true` if the [assisting] player can assist another player against
     * [target], `false` if not.
     */
    fun canOfferAssistAgainst(assisting: Player, target: Player): Boolean {
        if (assisting.team == target.team) return false
        if (!assisting.location.isAdjacent(this, target.location)) return false
        if (!canMark(assisting)) return false
        // TODO If player has Guard, player can always assist
        return assisting.location.coordinate.getSurroundingCoordinates(this).firstOrNull {
            assisting.team.game.field[it].player?.let { adjacentPlayer ->
                adjacentPlayer.team != assisting.team && canMark(adjacentPlayer)
            } ?: false
        } == null
    }

    // Only call this method for the active team
    fun addMarkedModifiers(game: Game, activeTeam: Team, square: FieldSquare, modifiers: MutableList<DiceModifier>) {
        square.coordinates.getSurroundingCoordinates(this).forEach {
            val markingPlayer: Player? = game.field[it].player
            if (markingPlayer != null) {
                if (markingPlayer.team != activeTeam && canMark(markingPlayer)) {
                    modifiers.add(CatchModifier.MARKED)
                }
            }
        }
    }

    fun canUseTeamReroll(game: Game, player: Player): Boolean {
        if (!game.canUseTeamRerolls) return false
        if (game.activeTeam != player.team) return false
        return when(player.team.usedTeamRerollThisTurn) {
            true -> allowMultipleTeamRerollsPrTurn
            false -> true
        }
    }



    // Characteristic limits
    val moveRange: IntRange
        get() = 1 .. 9

    val strengthRange: IntRange
        get() = 1 .. 8

    val agilityRange: IntProgression
        get() = 6 downTo  1

    val passingRange: IntProgression
        get() = 6 downTo 1

    val armorValueRange: IntProgression
        get() = 11 downTo 1

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

    val prayersToNuffleTable
        get() = PrayersToNuffleTable

    val weatherTable
        get() = WeatherTable

    val teamActions: TeamActions
        get() = BB2020TeamActions()

    val allowMultipleTeamRerollsPrTurn: Boolean
        get() = true

    val pathFinder: PathFinder
        get() = BB2020PathFinder(this)

    // Blood Bowl 7
    // Total width of the field
//    val fieldWidth = 2
//    val fieldHeight = 11
//    val wideZone = 2
//    val endZone = 1
//    val lineOfScrimmage = 7
}