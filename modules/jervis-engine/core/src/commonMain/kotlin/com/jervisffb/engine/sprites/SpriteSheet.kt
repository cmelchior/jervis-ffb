package com.jervisffb.engine.sprites

import kotlinx.serialization.Serializable

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

        /**
         * Generate a generic player sprite sheet for a given player title.
         * It will only contain a single variant.
         */
        fun generated(playerTitle: String): SpriteSheet {
            return SpriteSheet(SpriteLocation.GENERATED, playerTitle, variants = 1, selectedIndex = 0)
        }
    }
}
