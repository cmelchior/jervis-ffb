package dk.ilios.bowlbot.model

import kotlin.properties.Delegates

class TeamHalfData(private val game: Game) {
    var totalRerolls: Int = 0
    var usedRerolls: Int = 0
}

class TeamDriveData(private val game: Game) {
    // Team related data
}

class TeamTurnData(private val game: Game) {
    var currentTurn by Delegates.observable(0) { prop, old, new ->
        game.gameFlow.tryEmit(game)
    }
}

class Team(name: String, private val game: Game) {
    val id: String = ""
    val name: String = name

    val halfData = TeamHalfData(game)
    val driveData = TeamDriveData(game)
    val turnData = TeamTurnData(game)

//    val id: String
//    val name: String
//    val race: String
//    val reRolls: Int
//    val apothecaries: Int
//    val cheerLeaders: Int
//    val assistentCoaches: Int
//    val coach: String
//    val fanFactor: Int
//    val teamValue: Int
//    val division: String
//    val treasury: Int
//    val dedicatedFans: Int
//    val players: List<Player>
//    val baseIconPath: String
//    val logoUrl: String
//
//    private val specialRules: Set<com.fumbbl.ffb.model.SpecialRule>? = null
//    private val fRosterId: String? = null
//    private val fRoster: com.fumbbl.ffb.model.Roster? = null
//    private val fInducementSet: com.fumbbl.ffb.model.InducementSet? = null
//    @Transient
//    private val fPlayerById: Map<String, com.fumbbl.ffb.model.Player<*>>? = null
//    @Transient
//    private val fPlayerByNr: Map<Int, com.fumbbl.ffb.model.Player<*>>? = null
//    @Transient
//    private val currentGameId: Long = 0

    fun otherTeam(): Team {
        if (game.homeTeam == this) {
            return game.awayTeam
        } else {
            return game.homeTeam
        }
    }
}
