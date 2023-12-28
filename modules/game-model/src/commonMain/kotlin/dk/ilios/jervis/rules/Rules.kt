package dk.ilios.jervis.rules

import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.Player
import dk.ilios.jervis.model.PlayerState

interface BloodBowl {

}

interface DungeonBowl {

}

interface BloodBowl7 {

}


interface Rules {

    fun isValidSetup(state: Game): Boolean {
        val team = state.activeTeam
        val isHomeTeam = team.isHomeTeam()
        val inReserve: List<Player> = team.filter { it.state == PlayerState.STANDING && !it.location.isOnField() }
        val onField: List<Player> = team.filter { it.state == PlayerState.STANDING && it.location.isOnField() }
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

    // Blood Bowl 7
    // Total width of the field
//    val fieldWidth = 2
//    val fieldHeight = 11
//    val wideZone = 2
//    val endZone = 1
//    val lineOfScrimmage = 7
}