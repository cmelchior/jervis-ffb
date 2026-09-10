package com.jervisffb.tourplay

import com.jervisffb.engine.sprites.SpriteLocation
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class TourPlayIconMappingTests {

    private val fumbblIcons = """
        #Sun Jan 05 12:40:26 CET 2025
        https\://cdn.fumbbl.com/i/1000.png=players/iconsets/human_lineman.png
        https\://cdn.fumbbl.com/i/1001=players/portraits/human_lineman.png
        https\://cdn.fumbbl.com/i/1002.png=players/iconsets/oldworldalliance_humanlineman.png
        https\://cdn.fumbbl.com/i/1003=players/portraits/oldworldalliance_humanlineman.png
        https\://cdn.fumbbl.com/i/1004.png=players/iconsets/lizardmen_kroxigor.png
        https\://cdn.fumbbl.com/i/1005=players/portraits/lizardmen_kroxigor.png
        https\://cdn.fumbbl.com/i/1006.png=players/iconsets/goblin_ooligan.png
        https\://cdn.fumbbl.com/i/1007=players/portraits/goblin_ooligan.png
        https\://cdn.fumbbl.com/i/1008.png=players/iconsets/pygmy_kroxigor.png
        https\://cdn.fumbbl.com/i/1009=players/portraits/pygmy_kroxigor.png
        # FUMBBL ships the Dark Elf lineman icon set and portrait under different names.
        https\://cdn.fumbbl.com/i/1010.png=players/iconsets/darkelf_darkelflineman.png
        https\://cdn.fumbbl.com/i/1011=players/portraits/darkelf_lineman.png
    """.trimIndent()

    private val fumbblExtraIcons = """
        # Extra icons
        https\://cdn.fumbbl.com/i/2000.png=players/iconsets/TheBlackGobbo.png
    """.trimIndent()

    private val tourPlayIconSets = """
        # Comment
        https\://cdn.fumbbl.com/i/1000.png=Human/Human Lineman
        https\://cdn.fumbbl.com/i/1002.png=Old World Alliance/Human Lineman
        https\://cdn.fumbbl.com/i/1004.png=Kroxigor
        https\://cdn.fumbbl.com/i/1006.png=Goblin/’Ooligan
        https\://cdn.fumbbl.com/i/1008.png=
        https\://cdn.fumbbl.com/i/1010.png=Dark Elf/Dark Elf Lineman
        https\://cdn.fumbbl.com/i/2000.png=The Black Gobbo
    """.trimIndent()

    private val tourPlayPortraits = """
        https\://cdn.fumbbl.com/i/1001=Human/Human Lineman
        https\://cdn.fumbbl.com/i/1003=Old World Alliance/Human Lineman
        https\://cdn.fumbbl.com/i/1005=Kroxigor
        https\://cdn.fumbbl.com/i/1007=Goblin/’Ooligan
        https\://cdn.fumbbl.com/i/1009=
        https\://cdn.fumbbl.com/i/1011=Dark Elf/Dark Elf Lineman
    """.trimIndent()

    private fun mapping(): TourPlayIconMapping = TourPlayIconMapping().apply {
        initialize(fumbblIcons, fumbblExtraIcons, tourPlayIconSets, tourPlayPortraits)
    }

    @Test
    fun lookupIconAndPortrait() {
        val icons = mapping()
        val sprite = icons.getSprite("Human", "Human Lineman")
        assertEquals(SpriteLocation.FUMBBL_INI, sprite?.type)
        assertEquals("players/iconsets/human_lineman.png", sprite?.resource)
        val portrait = icons.getPortrait("Human", "Human Lineman")
        assertEquals(SpriteLocation.FUMBBL_INI, portrait?.type)
        assertEquals("players/portraits/human_lineman.png", portrait?.resource)
    }

    @Test
    fun iconSetAndPortraitMayUseDifferentFumbblNames() {
        val icons = mapping()
        assertEquals(
            "players/iconsets/darkelf_darkelflineman.png",
            icons.getSprite("Dark Elf", "Dark Elf Lineman")?.resource,
        )
        assertEquals(
            "players/portraits/darkelf_lineman.png",
            icons.getPortrait("Dark Elf", "Dark Elf Lineman")?.resource,
        )
    }

    @Test
    fun rosterQualifiedEntryWinsOverTheBaseRoster() {
        val icons = mapping()
        assertEquals(
            "players/iconsets/oldworldalliance_humanlineman.png",
            icons.getSprite("Old World Alliance", "Human Lineman")?.resource,
        )
        assertEquals(
            "players/portraits/oldworldalliance_humanlineman.png",
            icons.getPortrait("Old World Alliance", "Human Lineman")?.resource,
        )
    }

    @Test
    fun bareEntryMatchesAnyRoster() {
        val icons = mapping()
        assertEquals("players/iconsets/lizardmen_kroxigor.png", icons.getSprite("Lizardmen", "Kroxigor")?.resource)
        assertEquals("players/iconsets/lizardmen_kroxigor.png", icons.getSprite("Skink", "Kroxigor")?.resource)
        assertEquals("players/portraits/lizardmen_kroxigor.png", icons.getPortrait("Skink", "Kroxigor")?.resource)
    }

    @Test
    fun lookupIgnoresPunctuationAndCasing() {
        val icons = mapping()
        // TourPlay uses both apostrophe variants for the same position.
        assertEquals("players/iconsets/goblin_ooligan.png", icons.getSprite("Goblin", "'Ooligan")?.resource)
        assertEquals("players/portraits/goblin_ooligan.png", icons.getPortrait("goblin", "’ooligan")?.resource)
    }

    @Test
    fun unmappedIconsAreIgnored() {
        val icons = mapping()
        // `pygmy_kroxigor` is deliberately unmapped, so it must not shadow the Lizardmen one.
        assertEquals("players/iconsets/lizardmen_kroxigor.png", icons.getSprite("Lizardmen", "Kroxigor")?.resource)
        assertEquals("players/portraits/lizardmen_kroxigor.png", icons.getPortrait("Lizardmen", "Kroxigor")?.resource)
    }

    @Test
    fun aPositionMayHaveAnIconSetButNoPortrait() {
        val icons = mapping()
        assertEquals("players/iconsets/TheBlackGobbo.png", icons.getSprite("Goblin", "The Black Gobbo")?.resource)
        assertNull(icons.getPortrait("Goblin", "The Black Gobbo"))
    }

    @Test
    fun unknownPositionsHaveNoIcon() {
        val icons = mapping()
        assertNull(icons.getSprite("Kislev", "Lineman"))
        assertNull(icons.getPortrait("Kislev", "Lineman"))
    }

    @Test
    fun uninitializedMappingHasNoIcons() {
        val icons = TourPlayIconMapping()
        assertNull(icons.getSprite("Human", "Human Lineman"))
        assertNull(icons.getPortrait("Human", "Human Lineman"))
    }
}
