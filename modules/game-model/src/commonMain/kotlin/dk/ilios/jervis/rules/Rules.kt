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
        val inReserve: List<Player> = state.kickingTeam.players.filter { it.state == PlayerState.RESERVE }
        val onField: List<Player> = state.kickingTeam.players.filter { it.state == PlayerState.STANDING  }
        val totalAvailablePlayers: Int = inReserve.size + onField.size

        // If below 11 players, all players must be fielded
        if (totalAvailablePlayers < 11 && inReserve.isNotEmpty()) {
            return false
        }

        // 3 players must be on LoS
        // TODO

        // Max 2 players in each wide zone
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
    val lineOfScrimmage: UInt
        get() = 13u

    // Blood Bowl 7
    // Total width of the field
//    val fieldWidth = 2
//    val fieldHeight = 11
//    val wideZone = 2
//    val endZone = 1
//    val lineOfScrimmage = 7
}