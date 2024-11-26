package com.jervisffb.resources

import com.jervisffb.engine.rules.bb2020.roster.BLOODBORN_MARAUDER_LINEMEN
import com.jervisffb.engine.rules.bb2020.roster.BLOODSEEKERS
import com.jervisffb.engine.rules.bb2020.roster.BLOODSPAWN
import com.jervisffb.engine.rules.bb2020.roster.BULL_CENTAUR_BLITZERS
import com.jervisffb.engine.rules.bb2020.roster.CHAMELEON_SKINKS
import com.jervisffb.engine.rules.bb2020.roster.CHAOS_DWARF_BLOCKERS
import com.jervisffb.engine.rules.bb2020.roster.CHAOS_DWARF_TEAM
import com.jervisffb.engine.rules.bb2020.roster.ELVEN_BLITZER
import com.jervisffb.engine.rules.bb2020.roster.ELVEN_CATCHER
import com.jervisffb.engine.rules.bb2020.roster.ELVEN_LINEMAN
import com.jervisffb.engine.rules.bb2020.roster.ELVEN_THROWER
import com.jervisffb.engine.rules.bb2020.roster.ELVEN_UNION_TEAM
import com.jervisffb.engine.rules.bb2020.roster.ENSLAVED_MINOTAUR
import com.jervisffb.engine.rules.bb2020.roster.GUTTER_RUNNER
import com.jervisffb.engine.rules.bb2020.roster.HALFLING_HOPEFUL
import com.jervisffb.engine.rules.bb2020.roster.HOBGOBLIN_LINEMEN
import com.jervisffb.engine.rules.bb2020.roster.HUMAN_BLITZER
import com.jervisffb.engine.rules.bb2020.roster.HUMAN_CATCHER
import com.jervisffb.engine.rules.bb2020.roster.HUMAN_LINEMAN
import com.jervisffb.engine.rules.bb2020.roster.HUMAN_TEAM
import com.jervisffb.engine.rules.bb2020.roster.HUMAN_THROWER
import com.jervisffb.engine.rules.bb2020.roster.KHORNE_TEAM
import com.jervisffb.engine.rules.bb2020.roster.KHORNGORS
import com.jervisffb.engine.rules.bb2020.roster.KROXIGOR
import com.jervisffb.engine.rules.bb2020.roster.LIZARDMEN_TEAM
import com.jervisffb.engine.rules.bb2020.roster.OGRE
import com.jervisffb.engine.rules.bb2020.roster.RAT_OGRE
import com.jervisffb.engine.rules.bb2020.roster.SAURUS_BLOCKERS
import com.jervisffb.engine.rules.bb2020.roster.SKAVEN_BLITZER
import com.jervisffb.engine.rules.bb2020.roster.SKAVEN_LINEMAN
import com.jervisffb.engine.rules.bb2020.roster.SKAVEN_TEAM
import com.jervisffb.engine.rules.bb2020.roster.SKAVEN_THROWER
import com.jervisffb.engine.rules.bb2020.roster.SKINK_RUNNER_LINEMEN
import com.jervisffb.engine.serialize.FILE_FORMAT_VERSION
import com.jervisffb.engine.serialize.JervisMetaData
import com.jervisffb.engine.serialize.JervisRosterFile
import com.jervisffb.engine.serialize.RosterSpriteData
import com.jervisffb.engine.serialize.SingleSprite
import com.jervisffb.engine.serialize.SpriteSheet

object StandaloneRosters {
    const val iconRootPath = "icons/cached/players/iconsets"
    const val portraitRootPath = "icons/cached/players/portraits"
    val defaultRosters = mapOf(
        "human-roster.jrr" to JervisRosterFile(
            metadata = JervisMetaData(fileFormat = FILE_FORMAT_VERSION),
            roster = HUMAN_TEAM,
            uiData = RosterSpriteData(
                rosterLogo = SingleSprite.embedded("roster/logo/roster_logo_human.png"),
                positions = mapOf(
                    HUMAN_LINEMAN.id to SpriteSheet.embedded(
                        "$iconRootPath/human_lineman.png",
                        8
                    ),
                    HUMAN_THROWER.id to SpriteSheet.embedded(
                        "$iconRootPath/human_thrower.png",
                        2
                    ),
                    HUMAN_CATCHER.id to SpriteSheet.embedded(
                        "$iconRootPath/human_catcher.png",
                        4
                    ),
                    HUMAN_BLITZER.id to SpriteSheet.embedded(
                        "$iconRootPath/human_blitzer.png",
                        4
                    ),
                    HALFLING_HOPEFUL.id to SpriteSheet.embedded(
                        "$iconRootPath/human_halflinghopeful.png",
                        8
                    ),
                    OGRE.id to SpriteSheet.embedded(
                        "$iconRootPath/human_ogre.png",
                        8
                    ),
                ),
                portraits = mapOf(
                    HUMAN_LINEMAN.id to SingleSprite.embedded("$portraitRootPath/human_lineman.png"),
                    HUMAN_THROWER.id to SingleSprite.embedded("$portraitRootPath/human_thrower.png"),
                    HUMAN_CATCHER.id to SingleSprite.embedded("$portraitRootPath/human_catcher.png"),
                    HUMAN_BLITZER.id to SingleSprite.embedded("$portraitRootPath/human_blitzer.png"),
                    HALFLING_HOPEFUL.id to SingleSprite.embedded("$portraitRootPath/human_halflinghopeful.png"),
                    OGRE.id to SingleSprite.embedded("$portraitRootPath/human_ogre.png"),
                )
            )
        ),

        "chaos-dwarf-roster.jrr" to JervisRosterFile(
            metadata = JervisMetaData(fileFormat = FILE_FORMAT_VERSION),
            roster = CHAOS_DWARF_TEAM,
            uiData = RosterSpriteData(
                rosterLogo = SingleSprite.embedded("roster/logo/roster_logo_chaos_dwarf.png"),
                positions = mapOf(
                    HOBGOBLIN_LINEMEN.id to SpriteSheet.embedded(
                        "$iconRootPath/chaosdwarf_hobgoblinlineman.png",
                        10
                    ),
                    CHAOS_DWARF_BLOCKERS.id to SpriteSheet.embedded(
                        "$iconRootPath/chaosdwarf_chaosdwarfblocker.png",
                        6
                    ),
                    BULL_CENTAUR_BLITZERS.id to SpriteSheet.embedded(
                        "$iconRootPath/chaosdwarf_bullcentaurblitzer.png",
                        2
                    ),
                    ENSLAVED_MINOTAUR.id to SpriteSheet.embedded(
                        "$iconRootPath/chaosdwarf_enslavedminotaur.png",
                        1
                    ),
                ),
                portraits = mapOf(
                    HOBGOBLIN_LINEMEN.id to SingleSprite.embedded("$portraitRootPath/chaosdwarf_hobgoblinlineman.png"),
                    CHAOS_DWARF_BLOCKERS.id to SingleSprite.embedded("$portraitRootPath/chaosdwarf_chaosdwarfblocker.png"),
                    BULL_CENTAUR_BLITZERS.id to SingleSprite.embedded("$portraitRootPath/chaosdwarf_bullcentaurblitzer.png"),
                    ENSLAVED_MINOTAUR.id to SingleSprite.embedded("$portraitRootPath/chaosdwarf_enslavedminotaur.png"),
                )
            )
        ),

        "khorne-roster.jrr" to JervisRosterFile(
            metadata = JervisMetaData(fileFormat = FILE_FORMAT_VERSION),
            roster = KHORNE_TEAM,
            uiData = RosterSpriteData(
                rosterLogo = SingleSprite.embedded("roster/logo/roster_logo_khorne.png"),
                positions = mapOf(
                    BLOODBORN_MARAUDER_LINEMEN.id to SpriteSheet.embedded(
                        "$iconRootPath/khorne_bloodbornmarauderlineman.png",
                        7
                    ),
                    KHORNGORS.id to SpriteSheet.embedded(
                        "$iconRootPath/khorne_khorngor.png",
                        4
                    ),
                    BLOODSEEKERS.id to SpriteSheet.embedded(
                        "$iconRootPath/khorne_bloodseeker.png",
                        4
                    ),
                    BLOODSPAWN.id to SpriteSheet.embedded(
                        "$iconRootPath/khorne_bloodspawn.png",
                        1
                    ),
                ),
                portraits = mapOf(
                    BLOODBORN_MARAUDER_LINEMEN.id to SingleSprite.embedded("$portraitRootPath/khorne_bloodbornmarauderlineman.png"),
                    KHORNGORS.id to SingleSprite.embedded("$portraitRootPath/khorne_khorngor.png"),
                    BLOODSEEKERS.id to SingleSprite.embedded("$portraitRootPath/khorne_bloodseeker.png"),
                    BLOODSPAWN.id to SingleSprite.embedded("$portraitRootPath/khorne_bloodspawn.png"),
                )
            ),
        ),

        "elven-union-roster.jrr" to JervisRosterFile(
            metadata = JervisMetaData(fileFormat = FILE_FORMAT_VERSION),
            roster = ELVEN_UNION_TEAM,
            uiData = RosterSpriteData(
                rosterLogo = SingleSprite.embedded("roster/logo/roster_logo_elven_union.png"),
                positions = mapOf(
                    ELVEN_LINEMAN.id to SpriteSheet.embedded(
                        "$iconRootPath/elvenunion_lineman.png",
                        8
                    ),
                    ELVEN_CATCHER.id to SpriteSheet.embedded(
                        "$iconRootPath/elvenunion_catcher.png",
                        4
                    ),
                    ELVEN_THROWER.id to SpriteSheet.embedded(
                        "$iconRootPath/elvenunion_thrower.png",
                        2
                    ),
                    ELVEN_BLITZER.id to SpriteSheet.embedded(
                        "$iconRootPath/elvenunion_blitzer.png",
                        2
                    ),
                ),
                portraits = mapOf(
                    ELVEN_LINEMAN.id to SingleSprite.embedded("$portraitRootPath/elvenunion_lineman.png"),
                    ELVEN_CATCHER.id to SingleSprite.embedded("$portraitRootPath/elvenunion_catcher.png"),
                    ELVEN_THROWER.id to SingleSprite.embedded("$portraitRootPath/elvenunion_thrower.png"),
                    ELVEN_BLITZER.id to SingleSprite.embedded("$portraitRootPath/elvenunion_blitzer.png"),
                )
            )
        ),

        "skaven-roster.jrr" to JervisRosterFile(
            metadata = JervisMetaData(fileFormat = FILE_FORMAT_VERSION),
            roster = SKAVEN_TEAM,
            uiData = RosterSpriteData(
                rosterLogo = SingleSprite.embedded("roster/logo/roster_logo_skaven.png"),
                positions = mapOf(
                    SKAVEN_LINEMAN.id to SpriteSheet.embedded(
                        "$iconRootPath/skaven_lineman.png",
                        9
                    ),
                    SKAVEN_THROWER.id to SpriteSheet.embedded(
                        "$iconRootPath/skaven_thrower.png",
                        2
                    ),
                    GUTTER_RUNNER.id to SpriteSheet.embedded(
                        "$iconRootPath/skaven_thrower.png",
                        4
                    ),
                    SKAVEN_BLITZER.id to SpriteSheet.embedded(
                        "$iconRootPath/skaven_blitzer.png",
                        2
                    ),
                    RAT_OGRE.id to SpriteSheet.embedded(
                        "$iconRootPath/skaven_ratogre.png",
                        1
                    ),
                ),
                portraits = mapOf(
                    SKAVEN_LINEMAN.id to SingleSprite.embedded("$portraitRootPath/skaven_lineman.png"),
                    SKAVEN_THROWER.id to SingleSprite.embedded("$portraitRootPath/skaven_thrower.png"),
                    GUTTER_RUNNER.id to SingleSprite.embedded("$portraitRootPath/skaven_gutterrunner.png"),
                    SKAVEN_BLITZER.id to SingleSprite.embedded("$portraitRootPath/skaven_blitzer.png"),
                    RAT_OGRE.id to SingleSprite.embedded("$portraitRootPath/skaven_ratogre.png"),
                )
            )
        ),

        "lizardmen-roster.jrr" to JervisRosterFile(
            metadata = JervisMetaData(fileFormat = FILE_FORMAT_VERSION),
            roster = LIZARDMEN_TEAM,
            uiData = RosterSpriteData(
                rosterLogo = SingleSprite.embedded("roster/logo/roster_logo_lizardmen.png"),
                positions = mapOf(
                    SKINK_RUNNER_LINEMEN.id to SpriteSheet.embedded(
                        "$iconRootPath/lizardmen_skinkrunnerlineman.png",
                        6
                    ),
                    CHAMELEON_SKINKS.id to SpriteSheet.embedded(
                        "$iconRootPath/lizardmen_chameleonskink.png",
                        2
                    ),
                    SAURUS_BLOCKERS.id to SpriteSheet.embedded(
                        "$iconRootPath/lizardmen_saurusblocker.png",
                        6
                    ),
                    KROXIGOR.id to SpriteSheet.embedded(
                        "$iconRootPath/lizardmen_kroxigor.png",
                        1
                    ),
                ),
                portraits = mapOf(
                    SKINK_RUNNER_LINEMEN.id to SingleSprite.embedded("$portraitRootPath/lizardmen_skinkrunner.png"),
                    CHAMELEON_SKINKS.id to SingleSprite.embedded("$portraitRootPath/lizardmen_chameleonskink.png"),
                    SAURUS_BLOCKERS.id to SingleSprite.embedded("$portraitRootPath/lizardmen_saurusblocker.png"),
                    KROXIGOR.id to SingleSprite.embedded("$portraitRootPath/lizardmen_kroxigor.png"),
                )
            )
        ),
    )
}
