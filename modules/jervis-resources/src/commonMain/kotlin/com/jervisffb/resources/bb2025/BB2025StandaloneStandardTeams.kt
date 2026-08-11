package com.jervisffb.resources.bb2025

import com.jervisffb.engine.bb2025.StandardBB2025Rules
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.ext.playerNo
import com.jervisffb.engine.serialization.FILE_FORMAT_VERSION
import com.jervisffb.engine.serialization.JervisMetaData
import com.jervisffb.engine.serialization.buildTeamFile
import com.jervisffb.engine.teamBuilder

// The list of default starter team rosters.
// This issued by Standalone Mode.
//
// Builds follow the recommended BB2025 starter rosters from FUMBBL:
// https://fumbbl.com/help:BB25RaceStrategy
object BB2025StandaloneStandardTeams {
    private val rules = StandardBB2025Rules()
    val defaultTeams = mapOf(
        "amazon-starter-team-bb2025.jrt" to buildTeamFile {
            metadata = JervisMetaData(FILE_FORMAT_VERSION)
            roster = AMAZON_TEAM_BB2025
            team = teamBuilder(rules, AMAZON_TEAM_BB2025) {
                name = "Amazon Starter Team #1"
                addPlayer("Am1".playerId, "Blocker-1", 1.playerNo, AMAZON_BLOCKER)
                addPlayer("Am2".playerId, "Blocker-2", 2.playerNo, AMAZON_BLOCKER)
                addPlayer("Am3".playerId, "Blitzer-3", 3.playerNo, AMAZON_BLITZER)
                addPlayer("Am4".playerId, "Blitzer-4", 4.playerNo, AMAZON_BLITZER)
                addPlayer("Am5".playerId, "Linewoman-5", 5.playerNo, AMAZON_LINEMAN)
                addPlayer("Am6".playerId, "Linewoman-6", 6.playerNo, AMAZON_LINEMAN)
                addPlayer("Am7".playerId, "Linewoman-7", 7.playerNo, AMAZON_LINEMAN)
                addPlayer("Am8".playerId, "Linewoman-8", 8.playerNo, AMAZON_LINEMAN)
                addPlayer("Am9".playerId, "Linewoman-9", 9.playerNo, AMAZON_LINEMAN)
                addPlayer("Am10".playerId, "Linewoman-10", 10.playerNo, AMAZON_LINEMAN)
                addPlayer("Am11".playerId, "Linewoman-11", 11.playerNo, AMAZON_LINEMAN)
                rerolls = 3
                apothecaries = 1
                dedicatedFans = 2
                teamValue = 980_000
            }
            history = null
        },

        "human-starter-team-bb2025.jrt" to buildTeamFile {
            metadata = JervisMetaData(FILE_FORMAT_VERSION)
            roster = HUMAN_TEAM_BB2025
            team = teamBuilder(rules, HUMAN_TEAM_BB2025) {
                name = "Human Starter Team #1"
                addPlayer("Hu1".playerId, "Ogre-1", 1.playerNo, OGRE)
                addPlayer("Hu2".playerId, "Blitzer-2", 2.playerNo, HUMAN_BLITZER)
                addPlayer("Hu3".playerId, "Blitzer-3", 3.playerNo, HUMAN_BLITZER)
                addPlayer("Hu4".playerId, "Catcher-4", 4.playerNo, HUMAN_CATCHER)
                addPlayer("Hu5".playerId, "Catcher-5", 5.playerNo, HUMAN_CATCHER)
                addPlayer("Hu6".playerId, "Halfling-6", 6.playerNo, HALFLING_HOPEFUL)
                addPlayer("Hu7".playerId, "Lineman-7", 7.playerNo, HUMAN_LINEMAN)
                addPlayer("Hu8".playerId, "Lineman-8", 8.playerNo, HUMAN_LINEMAN)
                addPlayer("Hu9".playerId, "Lineman-9", 9.playerNo, HUMAN_LINEMAN)
                addPlayer("Hu10".playerId, "Lineman-10", 10.playerNo, HUMAN_LINEMAN)
                addPlayer("Hu11".playerId, "Lineman-11", 11.playerNo, HUMAN_LINEMAN)
                addPlayer("Hu12".playerId, "Lineman-12", 12.playerNo, HUMAN_LINEMAN)
                rerolls = 3
                apothecaries = 1
                dedicatedFans = 2
                teamValue = 990_000
            }
            history = null
        },

        "lizardmen-starter-team-bb2025.jrt" to buildTeamFile {
            metadata = JervisMetaData(FILE_FORMAT_VERSION)
            roster = LIZARDMEN_TEAM_BB2025
            team = teamBuilder(rules, LIZARDMEN_TEAM_BB2025) {
                name = "Lizardmen Starter Team #1"
                addPlayer("Li7".playerId, "Kroxigor-1", 1.playerNo, KROXIGOR)
                addPlayer("Li1".playerId, "Saurus-2", 2.playerNo, SAURUS_BLOCKERS)
                addPlayer("Li2".playerId, "Saurus-3", 3.playerNo, SAURUS_BLOCKERS)
                addPlayer("Li3".playerId, "Saurus-4", 4.playerNo, SAURUS_BLOCKERS)
                addPlayer("Li4".playerId, "Saurus-5", 5.playerNo, SAURUS_BLOCKERS)
                addPlayer("Li5".playerId, "Saurus-6", 6.playerNo, SAURUS_BLOCKERS)
                addPlayer("Li6".playerId, "Saurus-7", 7.playerNo, SAURUS_BLOCKERS)
                addPlayer("Li8".playerId, "Skink-8", 8.playerNo, SKINK_RUNNER_LINEMEN)
                addPlayer("Li9".playerId, "Skink-9", 9.playerNo, SKINK_RUNNER_LINEMEN)
                addPlayer("Li10".playerId, "Skink-10", 10.playerNo, SKINK_RUNNER_LINEMEN)
                addPlayer("Li11".playerId, "Chameleon-11", 11.playerNo, CHAMELEON_SKINKS)
                rerolls = 1
                apothecaries = 0
                dedicatedFans = 1
                teamValue = 1_000_000
            }
            history = null
        },

        "nurgle-starter-team-bb2025.jrt" to buildTeamFile {
            metadata = JervisMetaData(FILE_FORMAT_VERSION)
            roster = NURGLE_TEAM_BB2025
            team = teamBuilder(rules, NURGLE_TEAM_BB2025) {
                name = "Nurgle Starter Team #1"
                addPlayer("Nu1".playerId, "Rotter-1", 1.playerNo, ROTTER_LINEMEN)
                addPlayer("Nu2".playerId, "Rotter-2", 2.playerNo, ROTTER_LINEMEN)
                addPlayer("Nu3".playerId, "Rotter-3", 3.playerNo, ROTTER_LINEMEN)
                addPlayer("Nu4".playerId, "Rotter-4", 4.playerNo, ROTTER_LINEMEN)
                addPlayer("Nu5".playerId, "Bloater-5", 5.playerNo, BLOATERS)
                addPlayer("Nu6".playerId, "Bloater-6", 6.playerNo, BLOATERS)
                addPlayer("Nu7".playerId, "Bloater-7", 7.playerNo, BLOATERS)
                addPlayer("Nu8".playerId, "Bloater-8", 8.playerNo, BLOATERS)
                addPlayer("Nu9".playerId, "Pestigor-9", 9.playerNo, PESTIGORS)
                addPlayer("Nu10".playerId, "Pestigor-10", 10.playerNo, PESTIGORS)
                addPlayer("Nu11".playerId, "Rotspawn-11", 11.playerNo, ROTSPAWN)
                rerolls = 2
                apothecaries = 0
                teamValue = 1_000_000
            }
            history = null
        },

        "skaven-starter-team-bb2025.jrt" to buildTeamFile {
            metadata = JervisMetaData(FILE_FORMAT_VERSION)
            roster = SKAVEN_TEAM_BB2025
            team = teamBuilder(rules, SKAVEN_TEAM_BB2025) {
                name = "Skaven Starter Team #1"
                addPlayer("Sk1".playerId, "RatOgre-1", 1.playerNo, RAT_OGRE)
                addPlayer("Sk2".playerId, "Blitzer-2", 2.playerNo, SKAVEN_BLITZER)
                addPlayer("Sk3".playerId, "Blitzer-3", 3.playerNo, SKAVEN_BLITZER)
                addPlayer("Sk4".playerId, "GutterRunner-4", 4.playerNo, GUTTER_RUNNER)
                addPlayer("Sk5".playerId, "GutterRunner-5", 5.playerNo, GUTTER_RUNNER)
                addPlayer("Sk6".playerId, "Thrower-6", 6.playerNo, SKAVEN_THROWER)
                addPlayer("Sk7".playerId, "Lineman-7", 7.playerNo, SKAVEN_LINEMAN)
                addPlayer("Sk8".playerId, "Lineman-8", 8.playerNo, SKAVEN_LINEMAN)
                addPlayer("Sk9".playerId, "Lineman-9", 9.playerNo, SKAVEN_LINEMAN)
                addPlayer("Sk10".playerId, "Lineman-10", 10.playerNo, SKAVEN_LINEMAN)
                addPlayer("Sk11".playerId, "Lineman-11", 11.playerNo, SKAVEN_LINEMAN)
                rerolls = 3
                apothecaries = 0
                dedicatedFans = 2
                teamValue = 980_000
            }
            history = null
        },

        "khorne-starter-team-bb2025.jrt" to buildTeamFile {
            metadata = JervisMetaData(FILE_FORMAT_VERSION)
            roster = KHORNE_TEAM_BB2025
            team = teamBuilder(rules, KHORNE_TEAM_BB2025) {
                name = "Khorne Starter Team #1"
                addPlayer("Kh1".playerId, "Bloodspawn-1", 1.playerNo, BLOODSPAWN)
                addPlayer("Kh2".playerId, "Bloodseeker-2", 2.playerNo, BLOODSEEKERS)
                addPlayer("Kh3".playerId, "Bloodseeker-3", 3.playerNo, BLOODSEEKERS)
                addPlayer("Kh4".playerId, "Bloodseeker-4", 4.playerNo, BLOODSEEKERS)
                addPlayer("Kh5".playerId, "Bloodseeker-5", 5.playerNo, BLOODSEEKERS)
                addPlayer("Kh6".playerId, "Khorngor-6", 6.playerNo, KHORNGORS)
                addPlayer("Kh7".playerId, "Khorngor-7", 7.playerNo, KHORNGORS)
                addPlayer("Kh8".playerId, "Marauder-8", 8.playerNo, BLOODBORN_MARAUDER_LINEMEN)
                addPlayer("Kh9".playerId, "Marauder-9", 9.playerNo, BLOODBORN_MARAUDER_LINEMEN)
                addPlayer("Kh10".playerId, "Marauder-10", 10.playerNo, BLOODBORN_MARAUDER_LINEMEN)
                addPlayer("Kh11".playerId, "Marauder-11", 11.playerNo, BLOODBORN_MARAUDER_LINEMEN)
                rerolls = 1
                apothecaries = 0
                dedicatedFans = 2
                teamValue = 980_000
            }
            history = null
        },

        "dwarf-starter-team-bb2025.jrt" to buildTeamFile {
            metadata = JervisMetaData(FILE_FORMAT_VERSION)
            roster = DWARF_TEAM_BB2025
            team = teamBuilder(rules, DWARF_TEAM_BB2025) {
                name = "Dwarf Starter Team #1"
                addPlayer("Dw1".playerId, "Blitzer-1", 1.playerNo, DWARF_BLITZER)
                addPlayer("Dw2".playerId, "Blitzer-2", 2.playerNo, DWARF_BLITZER)
                addPlayer("Dw3".playerId, "Runner-3", 3.playerNo, DWARF_RUNNER)
                addPlayer("Dw4".playerId, "TrollSlayer-4", 4.playerNo, TROLL_SLAYER)
                addPlayer("Dw5".playerId, "Lineman-5", 5.playerNo, DWARF_LINEMAN)
                addPlayer("Dw6".playerId, "Lineman-6", 6.playerNo, DWARF_LINEMAN)
                addPlayer("Dw7".playerId, "Lineman-7", 7.playerNo, DWARF_LINEMAN)
                addPlayer("Dw8".playerId, "Lineman-8", 8.playerNo, DWARF_LINEMAN)
                addPlayer("Dw9".playerId, "Lineman-9", 9.playerNo, DWARF_LINEMAN)
                addPlayer("Dw10".playerId, "Lineman-10", 10.playerNo, DWARF_LINEMAN)
                addPlayer("Dw11".playerId, "Lineman-11", 11.playerNo, DWARF_LINEMAN)
                rerolls = 2
                apothecaries = 0
                dedicatedFans = 2
                teamValue = 985_000
            }
            history = null
        },

        "tomb-kings-starter-team-bb2025.jrt" to buildTeamFile {
            metadata = JervisMetaData(FILE_FORMAT_VERSION)
            roster = TOMB_KINGS_TEAM_BB2025
            team = teamBuilder(rules, TOMB_KINGS_TEAM_BB2025) {
                name = "Tomb Kings Starter Team #1"
                addPlayer("Tk1".playerId, "TombGuardian-1", 1.playerNo, TOMB_GUARDIANS)
                addPlayer("Tk2".playerId, "TombGuardian-2", 2.playerNo, TOMB_GUARDIANS)
                addPlayer("Tk3".playerId, "TombGuardian-3", 3.playerNo, TOMB_GUARDIANS)
                addPlayer("Tk4".playerId, "TombGuardian-4", 4.playerNo, TOMB_GUARDIANS)
                addPlayer("Tk5".playerId, "Blitzer-5", 5.playerNo, TOMB_KINGS_BLITZERS)
                addPlayer("Tk6".playerId, "Blitzer-6", 6.playerNo, TOMB_KINGS_BLITZERS)
                addPlayer("Tk7".playerId, "Thrower-7", 7.playerNo, TOMB_KINGS_THROWERS)
                addPlayer("Tk8".playerId, "Skeleton-8", 8.playerNo, SKELETON_LINEMEN)
                addPlayer("Tk9".playerId, "Skeleton-9", 9.playerNo, SKELETON_LINEMEN)
                addPlayer("Tk10".playerId, "Skeleton-10", 10.playerNo, SKELETON_LINEMEN)
                addPlayer("Tk11".playerId, "Skeleton-11", 11.playerNo, SKELETON_LINEMEN)
                rerolls = 2
                apothecaries = 0
                dedicatedFans = 2
                teamValue = 975_000
            }
            history = null
        },
    )

}
