package com.jervisffb.engine.sprites

import kotlinx.serialization.Serializable

@Serializable
data class SingleSprite(
    /** Where can this sprite be found? */
    override val type: SpriteLocation,
    /** The sprite identifier. Its format will depend on [type] */
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
