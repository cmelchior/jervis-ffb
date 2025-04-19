package com.jervisffb.engine.serialize

import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.common.roster.Roster
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
data class RosterLogo(
    val large: SingleSprite?, // Should be an image 600x600px
    val small: SingleSprite?, // Should be an image 200x200px
) {
    companion object {
        val NONE: RosterLogo = RosterLogo(null, null)
    }
}

/**
 * Enum describing where a sprite is coming from.
 */
enum class SpriteLocation {
    // The sprite is included in the application bundle and is under `composeResources`.
    EMBEDDED,
    // The sprite is hosted on a remote server.
    URL,
    // The sprite is provided by FUMBBL and its remote location is defined by its ini file.
    FUMBBL_INI
}

fun normalizeFumbblIconPath(path: String): String {
    var relativePath = path
    // All fumbbl icons are in /i/*, but it looks like some of the REST APIs only return the id and not the
    // full path.
    relativePath = if (relativePath.startsWith("/")) relativePath.removeSuffix("/") else relativePath
    if (!relativePath.startsWith("i/")) {
        relativePath = "i/$relativePath"
    }
    return relativePath
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
        /**
         * Points to a sprite included in the application bundle.
         * Path is relative from `/jervis-ui/src/commonMain/composeResources/files`.
         */
        fun embedded(path: String): SingleSprite {
            return SingleSprite(SpriteLocation.EMBEDDED, path)
        }

        /**
         * Used for relative URLS like those provided by the FUMBBL Rest API. They normally
         * look like just a number "123456" or "i/123456"
         */
        fun url(url: String): SingleSprite {
            return SingleSprite(SpriteLocation.URL, url)
        }

        /**
         * Used for relative URLS like those provided by the FUMBBL Rest API. They normally
         * look like just a number "123456" or "i/123456"
         */
        fun fumbbl(path: String): SingleSprite {
            val relativePath = normalizeFumbblIconPath(path)
            return SingleSprite(SpriteLocation.URL, "https://fumbbl.com/$relativePath")
        }

        /**
         * Used for paths defined by the FUMBBL `.ini` file. Entries in that look like this:
         * `https\://cdn.fumbbl.com/i/318581=players/portraits/chaoschosen_minotaur.png` and it
         * is the later that should be used here, e.g. `players/portraits/chaoschosen_minotaur.png`
         */
        fun ini(path: String): SingleSprite {
            return SingleSprite(SpriteLocation.FUMBBL_INI, path)
        }
    }
}

@Serializable
data class SpriteSheet(
    override val type: SpriteLocation,
    override val resource: String,
    // How many variants in the spritesheet. If `null` we need to calculate it after fetching the sheet.
    // The calculation is done based on the assumption that there are 4 player images per row.
    val variants: Int? = null,
    // Which entry in the sheet to use. If `null`, one will be automatically selected
    val selectedIndex: Int? = null,
): SpriteSource {
    companion object {

        /**
         * Points to a sprite included in the application bundle.
         * Path is relative from `/jervis-ui/src/commonMain/composeResources/files`.
         */
        fun embedded(path: String, variants: Int, selectedIndex: Int? = null): SpriteSheet {
            return SpriteSheet(SpriteLocation.EMBEDDED, path, variants, selectedIndex)
        }

        /**
         * Points to a sprite hosted on a remote server.
         * Path should be a valid URL.
         */
        fun url(path: String, variants: Int? = null, selectedIndex: Int? = null): SpriteSheet {
            return SpriteSheet(SpriteLocation.URL, path, variants, selectedIndex)
        }

        /**
         * Used for relative URLS like those provided by the FUMBBL Rest API. They normally
         * look like just a number "123456" or "i/123456"
         */
        fun fumbbl(path: String, variants: Int? = null, selectedIndex: Int? = null): SpriteSheet {
            val relativePath = normalizeFumbblIconPath(path)
            return SpriteSheet(SpriteLocation.URL, "https://fumbbl.com/$relativePath", variants, selectedIndex)
        }

        /**
         * Used for paths defined by the FUMBBL `.ini` file. Entries in that looks like this:
         * `https\://cdn.fumbbl.com/i/318581=players/portraits/chaoschosen_minotaur.png` and it
         * is the later that should be used here, e.g. `players/portraits/chaoschosen_minotaur.png`
         */
        fun ini(path: String, variants: Int? = null, selectedIndex: Int? = null): SpriteSheet {
            return SpriteSheet(SpriteLocation.FUMBBL_INI, path, variants, selectedIndex)
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
