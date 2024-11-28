package com.jervisffb.engine.serialize

import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.model.PlayerId
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.common.roster.PositionId
import com.jervisffb.engine.rules.common.roster.Roster
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * We support 3 different kinds of save files in Jervis:
 *
 * 1. A game replay file (.jrg)
 * 2. A team file (.jrt)
 * 3. A roster file (.jrr)
 *
 * Each file is a plain json object defined by the data classes found in this
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
const val FILE_EXTENSION_GAME_FILE = "jgf"
const val FILE_EXTENSION_ROSTER_FILE = "jrf"
const val FILE_EXTENSION_TEAM_FILE = "jtf"

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
)

// Format of a Jervis Team File (.jtf)
// For stand-alone teams this should also contain a history entry
@Serializable
data class JervisTeamFile(
    val metadata: JervisMetaData,
    val roster: Roster,
    val team: Team,
    val history: GameHistory?,
    val rosterUiData: RosterSpriteData,
    val uiData: TeamSpriteData,
)

// Format of a Jervis Roster File (.jrf)
@Serializable
data class JervisRosterFile(
    val metadata: JervisMetaData,
    val roster: Roster,
    val uiData: RosterSpriteData,
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
data class TeamSpriteData(
    val teamLogo: SpriteSource?, // Either team or roster logo
    val players: Map<PlayerId, PlayerUiData>,
)

@Serializable
data class RosterSpriteData(
    val rosterLogo: SpriteSource?,
    val positions: Map<PositionId, SpriteSource>,
    val portraits: Map<PositionId, SpriteSource>,
)

enum class SpriteLocation {
    EMBEDDED,
    URL
}

@Serializable
sealed interface SpriteSource {
    val type: SpriteLocation
    val resource: String
}

@Serializable
data class SingleSprite(
    override val type: SpriteLocation,
    override val resource: String,
): SpriteSource {
    companion object {
        fun embedded(path: String): SpriteSource {
            return SingleSprite(SpriteLocation.EMBEDDED, path)
        }
        fun url(url: String): SpriteSource {
            return SingleSprite(SpriteLocation.URL, url)
        }
        fun fumbbl(path: String): SingleSprite {
            val relativePath = if (path.startsWith("/")) path.removeSuffix("/") else path
            return SingleSprite(SpriteLocation.URL, "https://fumbbl.com/$relativePath")
        }
    }
}

@Serializable
data class SpriteSheet(
    override val type: SpriteLocation,
    override val resource: String,
    val variants: Int? = null, // How many variants in the spritesheet. If `null` we need to calculate it
    val selectedIndex: Int? = null, // If
): SpriteSource {
    companion object {
        fun embedded(path: String, variants: Int, selectedIndex: Int? = null): SpriteSource {
            return SpriteSheet(SpriteLocation.EMBEDDED, path, variants, selectedIndex)
        }
        fun url(path: String, variants: Int, selectedIndex: Int? = null): SpriteSource {
            return SpriteSheet(SpriteLocation.URL, path, variants, selectedIndex)
        }
        fun fumbbl(path: String, variants: Int? = null, selectedIndex: Int? = null): SpriteSource {
            val relativePath = if (path.startsWith("/")) path.removeSuffix("/") else path
            return SpriteSheet(SpriteLocation.URL, "https://fumbbl.com/$relativePath", variants, selectedIndex)
        }
    }
}

@Serializable
data class PlayerUiData(
    val sprite: SpriteSource?,
    val portrait: SpriteSource?,

)

@Serializable
sealed interface PositionUiData

@Serializable
data class PositionSpriteSheetUiData(
    val spriteSheet: SpriteSource,
    val variants: Int,
): PositionUiData



//@Serializable
//sealed interface SpriteSource
//
//// Sprites shipped as part of the Client.
//// The path is relative to the internal `composeResources/files` directory
//@Serializable
//data class EmbeddedSpriteSource(
//    val resourcePath: String,
//) : SpriteSource
//
//
//
//
//
//@Serializable
//sealed interface PlayerSpriteSheetSource: SpriteSource {
//    val variants: Int // Number of different variants
//    val index: Int // Selected row
//}
//
//// Sprite sheets are expected to look like the FUMBBL sprite sheets, i.e.,
//// One player variant pr row.
//// Each row contains 4 players: 0: Home/Inactive, 1: Home/Active,
//// 2: Away/Inactive, 3: Away/Active.
//@Serializable
//data class EmbeddedPlayerSpriteSource(
//    val resourcePath: String,
//    override val variants: Int, // Number of different variants
//    override val index: Int, // Selected row
//) : PlayerSpriteSheetSource
//
//@Serializable
//data class HttpPlayerSpriteSource(
//    val url: String,
//    override val variants: Int, // Number of different variants
//    override val index: Int, // Selected row
//) : PlayerSpriteSheetSource {
//    companion object {
//        fun fumbblSprite(relativePath: String): HttpSpriteSource {
//            return HttpSpriteSource("https://fumbbl.com/$relativePath")
//        }
//    }
//}
//
//// Sprites are fetched over the network and cached.
//// Once cached the sprites are fetched from there. Updated sprites
//// should have a new URL
//@Serializable
//data class HttpSpriteSource(
//    val url: String,
//): SpriteSource {
//    companion object {
//        fun fumbblSprite(relativePath: String): HttpSpriteSource {
//            return HttpSpriteSource("https://fumbbl.com/$relativePath")
//        }
//    }
//}

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
