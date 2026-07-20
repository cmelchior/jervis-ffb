package com.jervisffb.engine.serialization

import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.model.PlayerNo
import com.jervisffb.engine.model.SetupId
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.TeamId
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.builder.GameType
import com.jervisffb.engine.rules.common.roster.Roster
import com.jervisffb.engine.sprites.SpriteSource
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * We support 3 different kinds of save files in Jervis:
 *
 * 1. A game file. Can either be used as a replay or to restart a game (.jrg)
 * 2. A team file (.jrt)
 * 3. A roster file (.jrr)
 *
 * Each file is a plain JSON object defined by the data classes found in this
 * file.
 *
 * The format of these files is allowed to change, but all of them _must_ have
 * a "metadata" object containing a "fileFormat" property defining how the
 * rest of the file is read.
 *
 * Versions:
 * - 1: Initial version
 */
const val FILE_FORMAT_VERSION = 1
const val FILE_EXTENSION_GAME_FILE = "jrg"
const val FILE_EXTENSION_ROSTER_FILE = "jrr"
const val FILE_EXTENSION_TEAM_FILE = "jrt"
const val FILE_EXTENSION_SETUP_FILE = "jrs"

@Serializable
data class JervisMetaData(
    // The name of this property must never change, and this number should
    // be incremented every time the file format is changed.
    val fileFormat: Int,
)

// Format of a Jervis Game File (.jgf)
@Serializable
data class JervisGameFile(
    val metadata: JervisMetaData,
    val configuration: JervisConfiguration,
    val game: JervisGameData,
    // Optional debug information
    val debugInfo: JervisDebugInfo? = null
)

// Format of a Jervis Team File (.jtf)
// For stand-alone teams this should also contain a history entry
@Serializable
data class JervisTeamFile(
    val metadata: JervisMetaData,
    val team: SerializedTeam,
    val history: GameHistory?,
) {
    val roster: Roster = team.roster
}

// Format of a Jervis Roster File (.jrf)
@Serializable
data class JervisRosterFile(
    val metadata: JervisMetaData,
    val roster: Roster,
)

// A relative coordinate that will differ based on the game type:
// - Standard/BB7/Gutter Bowl: Y-axis is from a teams Line of Scrimmage towards
//   their own end-zone. Y=0 is the top of the pitch (which is in a horizontal position)
// - Dungeon Bowl: Y-axis is the back of the End-zone tile facing the
//   entrance. Y=0 is (top-left) for an End Zone tile with the entrance
//   to the south. Rotate this axis with the end zone.
@Serializable
data class RelativeCoordinate(val y: Int, val dist: Int)

// Format of a Jervis Setup File (.jrs)
@Serializable
data class JervisSetupFile(
    val metadata: JervisMetaData,
    // Unique identifier for this setup
    val id: SetupId,
    // Name of the setup
    val name: String,
    // Which game type is this setup for
    val gameType: GameType,
    // If set, this setup is specific to the defined team, otherwise
    // it is "global", i.e. any team can use it.
    val team: TeamId?,
    // Map between player number and their setup coordinate. Coordinates
    // will be interpreted depending on the game type.
    val formation: Map<PlayerNo, RelativeCoordinate>
)

// Just dummy for now. This needs to be fleshed out.
@Serializable
data class GameHistory(
    val games: List<GameEntry>
)
@Serializable
data class GameEntry(
    val date: String,
    val homeTeam: String,
    val homeTeamRoster: String,
    val awayTeam: String,
    val awayTeamRoster: String,
    val homeScore: Int,
    val awayScore: Int,
)

@Serializable
data class PlayerUiData(
    val sprite: SpriteSource?,
    val portrait: SpriteSource?,

)

// Class encapsulating all rules, teams and other game configurations that are user defined.
@Serializable
data class JervisConfiguration(
    val rules: Rules,
)

/**
 * Class encapsulating the actual game state and all actions
 */
@Serializable
data class JervisGameData(
    val homeTeam: JsonElement,
    val awayTeam: JsonElement,
    val actions: List<GameAction>,
)

/**
 * Class encapsulating debug information about the game file.
 */
@Serializable
data class JervisDebugInfo(
    // Information about the platform running the Client
    val platform: String,
    // Information about the client
    val client: String,
    // Which Git Commit was used to build the Client
    val gitCommit: String,
    // List of errors that are relevant for debugging. This contains `Throwable.stackTraceToString()`
    val errorList: List<String>
)

/**
 * Converts a [Team] into a [JervisTeamFile], but all UI data will be empty.
 * This is mostly used for testing
 */
fun Team.createTeamFile(): JervisTeamFile {
    return buildTeamFile {
        metadata = JervisMetaData(FILE_FORMAT_VERSION)
        team = this@createTeamFile
        roster = this@createTeamFile.roster
    }
}
