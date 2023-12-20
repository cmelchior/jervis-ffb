package dk.ilios.jervis.model

import dk.ilios.jervis.rules.roster.Roster
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

class Team(name: String, roster: Roster, coach: Coach) {

    // Fixed Team data, identifying the team
    val id: String = ""
    val name: String = name
    val coach: Coach = coach
    val roster: Roster = roster
    val players = TeamPlayers(this)

//    val dogout


//    val race: String
//    val race: String
    // Variable team data that might change during the game
    var reRolls: Int = 0
    var apothecaries: Int = 0
    var cheerLeaders: Int = 0
    var assistentCoaches: Int = 0
    var fanFactor: Int = 0
    var teamValue: Int = 0
    var treasury: Int = 0
    var dedicatedFans: Int = 0

    // Special team state that needs to be tracked for the given period
    lateinit var game: Game
    lateinit var halfData: TeamHalfData
    lateinit var driveData: TeamDriveData
    lateinit var turnData: TeamTurnData

    // Must be called before using this class.
    // Used to break circular reference between Team and Game instances
    fun setGameReference(game: Game) {
        halfData = TeamHalfData(game)
        driveData = TeamDriveData(game)
        turnData = TeamTurnData(game)
        this.game = game
    }

    fun otherTeam(): Team {
        if (game.homeTeam == this) {
            return game.awayTeam
        } else {
            return game.homeTeam
        }
    }
}
