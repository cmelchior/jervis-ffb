package com.jervisffb.tourplay

import com.jervisffb.engine.sprites.SingleSprite
import com.jervisffb.engine.sprites.SpriteSheet

/**
 * Maps TourPlay positions onto the FUMBBL player icons Jervis should render
 * them with.
 *
 * TourPlay does not expose usable player artwork of its own, so teams imported
 * from TourPlay borrow the FUMBBL icons instead. The mapping is spread across
 * two files:
 *
 * - `files/tourplay/icons-iconsets.ini` contains player sprites.
 * - `files/tourplay/icons-portraits.ini` contains player portraits.
 *
 * FUMBBL keeps both kinds in one file, separated only by their `players/iconsets/`
 * or `players/portraits/` path, and does not always name the two consistently.
 * The Dark Elf Lineman icon set is `darkelf_darkelflineman` while its portrait
 * is `darkelf_lineman`. Jervis therefore keeps one TourPlay file per kind, so a
 * position can be mapped independently on each side.
 *
 * Positions are written either as `<Roster>/<Position>` or as a bare
 * `<Position>`, where the bare form matches any roster and acts as the fallback
 * for the qualified form.
 */
class TourPlayIconMapping {

    private val sprites: MutableMap<String, SpriteSheet> = mutableMapOf()
    private val portraits: MutableMap<String, SingleSprite> = mutableMapOf()

    /**
     * Populate the mapping from the raw content of the files described in the
     * class documentation. Until this is called, all lookups return `null` and
     * imported teams fall back to generated placeholder sprites.
     */
    fun initialize(
        fumbblIcons: String,
        fumbblExtraIcons: String,
        tourPlayIconSets: String,
        tourPlayPortraits: String,
    ) {
        val fumbblPaths = mutableMapOf<String, String>()
        forEachEntry(fumbblIcons) { url, path -> fumbblPaths[url] = path }
        forEachEntry(fumbblExtraIcons) { url, path -> fumbblPaths[url] = path }

        forEachMapping(tourPlayIconSets, fumbblPaths) { key, path -> sprites[key] = SpriteSheet.ini(path) }
        forEachMapping(tourPlayPortraits, fumbblPaths) { key, path -> portraits[key] = SingleSprite.ini(path) }
    }

    /**
     * Returns the icon set to use for a TourPlay position, or `null` if Jervis
     * has no FUMBBL icon for it.
     */
    fun getSprite(roster: String, position: String): SpriteSheet? = lookup(sprites, roster, position)

    /**
     * Returns the portrait to use for a TourPlay position, or `null` if Jervis
     * has no FUMBBL portrait for it.
     */
    fun getPortrait(roster: String, position: String): SingleSprite? = lookup(portraits, roster, position)

    private fun <T> lookup(icons: Map<String, T>, roster: String, position: String): T? {
        return icons[keyOf(roster, position)] ?: icons[keyOf(position)]
    }

    private inline fun forEachMapping(
        file: String,
        fumbblPaths: Map<String, String>,
        onMapping: (key: String, path: String) -> Unit,
    ) {
        forEachEntry(file) { url, positions ->
            val path = fumbblPaths[url]
            if (path != null) {
                positions
                    .split(",")
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
                    .forEach { position -> onMapping(keyOf(position), path) }
            }
        }
    }

    private fun keyOf(entry: String): String {
        val separator = entry.indexOf('/')
        return when (separator) {
            -1 -> normalize(entry)
            else -> keyOf(entry.substring(0, separator), entry.substring(separator + 1))
        }
    }

    private fun keyOf(roster: String, position: String): String = "${normalize(roster)}/${normalize(position)}"

    // TourPlay is inconsistent about spacing, hyphens and the two apostrophe variants
    // (e.g. both `Boa Kon'ssstriktr` and `Boa Kon’ssstriktr` exist), so keys are compared
    // on letters and digits only.
    private fun normalize(name: String): String = name.filter { it.isLetterOrDigit() }.lowercase()

    private inline fun forEachEntry(file: String, onEntry: (url: String, value: String) -> Unit) {
        file.lineSequence().forEach { line ->
            val entry = line.trim()
            if (entry.isNotEmpty() && !entry.startsWith("#")) {
                val separator = entry.indexOf('=')
                if (separator != -1) {
                    onEntry(entry.substring(0, separator), entry.substring(separator + 1))
                }
            }
        }
    }
}
