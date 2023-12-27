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

    fun isValidSetupForKicking(state: Game): Boolean {
        val team = state.kickingTeam
        val isHometeam = team.isHomeTeam()
        val inReserve: List<Player> = team.players.filter { it.state == PlayerState.STANDING }
        val onField: List<Player> = team.players.filter { it.state == PlayerState.STANDING  }
        val totalAvailablePlayers: Int = inReserve.size + onField.size

        // If below 11 players, all players must be fielded
        if (totalAvailablePlayers < 11 && inReserve.isNotEmpty()) {
            return false
        }

        // Otherwise 11 players must be on the field
        if (onField.size != 11) {
            return false
        }

        // 3 players must be on LoS, or if less than 3 players, all must be on LoS
        val field = state.field
        val losIndex: UInt = if (isHometeam) (lineOfScrimmageHome - 1u) else (lineOfScrimmageAway - 1u)
        val playersOnLos = (0u + wideZone until fieldHeight - wideZone).filter { y: UInt ->
            !field[losIndex.toInt(), y.toInt()].isEmpty()
        }.size
        if (onField.size < 3 && onField.size != playersOnLos) {
            return false
        }
        if (onField.size >= 3 && playersOnLos != 3) {
            return false
        }

        // Max 2 players in top wide zone
        var count = 0
        if (isHometeam) {
            (0 until lineOfScrimmageHome.toInt()).forEach { x ->
                (0 until wideZone.toInt()).forEach { y ->
                    if (!field[x, y].isEmpty()) {
                        // They must not be on the LoS
                        if (x == (lineOfScrimmageHome.toInt() - 1)) {
                            return false
                        }
                        count++
                    }
                }
            }
        } else {
            (fieldWidth - 1u until lineOfScrimmageAway).forEach { x ->
                (0u until wideZone).forEach { y ->
                    if (!field[x.toInt(), y.toInt()].isEmpty()) {
                        // They must not be on the LoS
                        if (x == (lineOfScrimmageAway - 1u)) {
                            return false
                        }
                        count++
                    }
                }
            }
        }
        if (count > 2) {
            return false
        }

        // Max 2 players in each wide zone
        // They must not be on the LoS
        // TODO
        return true
    }

    fun isValidSetupForReceiving(): Boolean {
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

    // From end of field (including endZone)
    val lineOfScrimmageHome: UInt
        get() = 13u

    // From end of field (including endZone)
    val lineOfScrimmageAway: UInt
        get() = 14u

    // Blood Bowl 7
    // Total width of the field
//    val fieldWidth = 2
//    val fieldHeight = 11
//    val wideZone = 2
//    val endZone = 1
//    val lineOfScrimmage = 7
}