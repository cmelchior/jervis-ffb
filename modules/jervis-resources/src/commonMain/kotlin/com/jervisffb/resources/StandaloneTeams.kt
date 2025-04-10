package com.jervisffb.resources

import LIZARDMEN_TEAM
import SAURUS_BLOCKERS
import SKINK_RUNNER_LINEMEN
import com.jervisffb.engine.model.PlayerId
import com.jervisffb.engine.model.PlayerNo
import com.jervisffb.engine.rules.StandardBB2020Rules
import com.jervisffb.engine.serialize.FILE_FORMAT_VERSION
import com.jervisffb.engine.serialize.JervisMetaData
import com.jervisffb.engine.serialize.buildTeamFile
import com.jervisffb.engine.teamBuilder

// List of default starter team rosters
// This is primarely usd by Standalone Mode
object StandaloneTeams {
    private val rules = StandardBB2020Rules()
    val defaultTeams = mapOf(
        "human-starter-team.jrt" to buildTeamFile {
            metadata = JervisMetaData(FILE_FORMAT_VERSION)
            roster = HUMAN_TEAM
            team = teamBuilder(rules, HUMAN_TEAM) {
                name = "Human Starter Team #1"
                addPlayer(PlayerId("Hu1"), "Ogre-1", PlayerNo(1), OGRE)
                addPlayer(PlayerId("Hu2"), "Blitzer-2", PlayerNo(2), HUMAN_BLITZER)
                addPlayer(PlayerId("Hu3"), "Blitzer-3", PlayerNo(3), HUMAN_BLITZER)
                addPlayer(PlayerId("Hu4"), "Blitzer-4", PlayerNo(4), HUMAN_BLITZER)
                addPlayer(PlayerId("Hu5"), "Blitzer-5", PlayerNo(5), HUMAN_BLITZER)
                addPlayer(PlayerId("Hu6"), "Thrower-6", PlayerNo(6), HUMAN_THROWER)
                addPlayer(PlayerId("Hu7"), "Catcher-7", PlayerNo(7), HUMAN_CATCHER)
                addPlayer(PlayerId("Hu8"), "Catcher-8", PlayerNo(8), HUMAN_CATCHER)
                addPlayer(PlayerId("Hu9"), "Lineman-9", PlayerNo(9), HUMAN_LINEMAN)
                addPlayer(PlayerId("Hu10"), "Lineman-10", PlayerNo(10), HUMAN_LINEMAN)
                addPlayer(PlayerId("Hu11"), "Lineman-11", PlayerNo(11), HUMAN_LINEMAN)
                rerolls = 3
                apothecaries = 0
                dedicatedFans = 1
                teamValue = 1_000_000
            }
            history = null
        },

        "lizardmen-starter-team.jrt" to buildTeamFile {
            metadata = JervisMetaData(FILE_FORMAT_VERSION)
            roster = LIZARDMEN_TEAM
            team = teamBuilder(rules, LIZARDMEN_TEAM) {
                name = "Lizardmen Starter Team #1"
                addPlayer(PlayerId("Li1"), "Skink-1", PlayerNo(1), SKINK_RUNNER_LINEMEN)
                addPlayer(PlayerId("Li2"), "Skink-2", PlayerNo(2), SKINK_RUNNER_LINEMEN)
                addPlayer(PlayerId("Li3"), "Skink-3", PlayerNo(3), SKINK_RUNNER_LINEMEN)
                addPlayer(PlayerId("Li4"), "Skink-4", PlayerNo(4), SKINK_RUNNER_LINEMEN)
                addPlayer(PlayerId("Li5"), "Skink-5", PlayerNo(5), SKINK_RUNNER_LINEMEN)
                addPlayer(PlayerId("Li6"), "Saurus-6", PlayerNo(6), SAURUS_BLOCKERS)
                addPlayer(PlayerId("Li7"), "Saurus-7", PlayerNo(7), SAURUS_BLOCKERS)
                addPlayer(PlayerId("Li8"), "Saurus-8", PlayerNo(8), SAURUS_BLOCKERS)
                addPlayer(PlayerId("Li9"), "Saurus-9", PlayerNo(9), SAURUS_BLOCKERS)
                addPlayer(PlayerId("Li10"), "Saurus-10", PlayerNo(10), SAURUS_BLOCKERS)
                addPlayer(PlayerId("Li11"), "Saurus-11", PlayerNo(11), SAURUS_BLOCKERS)
                rerolls = 2
                apothecaries = 1
                dedicatedFans = 0
                teamValue = 1_000_000
            }
            history = null
        },

        "skaven-starter-team.jrt" to buildTeamFile {
            metadata = JervisMetaData(FILE_FORMAT_VERSION)
            roster = SKAVEN_TEAM
            team = teamBuilder(rules, SKAVEN_TEAM) {
                name = "Skaven Starter Team #1"
                addPlayer(PlayerId("Sk1"), "Blitzer-1", PlayerNo(1), SKAVEN_BLITZER)
                addPlayer(PlayerId("Sk2"), "Blitzer-2", PlayerNo(2), SKAVEN_BLITZER)
                addPlayer(PlayerId("Sk3"), "GutterRunner-3", PlayerNo(3), GUTTER_RUNNER)
                addPlayer(PlayerId("Sk4"), "GutterRunner-4", PlayerNo(4), GUTTER_RUNNER)
                addPlayer(PlayerId("Sk5"), "GutterRunner-5", PlayerNo(5), GUTTER_RUNNER)
                addPlayer(PlayerId("Sk6"), "Thrower-6", PlayerNo(6), SKAVEN_THROWER)
                addPlayer(PlayerId("Sk7"), "Lineman-7", PlayerNo(7), SKAVEN_LINEMAN)
                addPlayer(PlayerId("Sk8"), "Lineman-8", PlayerNo(8), SKAVEN_LINEMAN)
                addPlayer(PlayerId("Sk9"), "Lineman-9", PlayerNo(9), SKAVEN_LINEMAN)
                addPlayer(PlayerId("Sk10"), "Lineman-10", PlayerNo(10), SKAVEN_LINEMAN)
                addPlayer(PlayerId("Sk11"), "Lineman-11", PlayerNo(11), SKAVEN_LINEMAN)
                rerolls = 3
                apothecaries = 1
                dedicatedFans = 0
                teamValue = 970_000
            }
            history = null
        },

        "khorne-starter-team.jrt" to buildTeamFile {
            metadata = JervisMetaData(FILE_FORMAT_VERSION)
            roster = KHORNE_TEAM
            team = teamBuilder(rules, KHORNE_TEAM) {
                name = "Khorne Starter Team #1"
                addPlayer(PlayerId("Kh1"), "Lineman-1", PlayerNo(1), BLOODBORN_MARAUDER_LINEMEN)
                addPlayer(PlayerId("Kh2"), "Lineman-2", PlayerNo(2), BLOODBORN_MARAUDER_LINEMEN)
                addPlayer(PlayerId("Kh3"), "Lineman-3", PlayerNo(3), BLOODBORN_MARAUDER_LINEMEN)
                addPlayer(PlayerId("Kh4"), "Lineman-4", PlayerNo(4), BLOODBORN_MARAUDER_LINEMEN)
                addPlayer(PlayerId("Kh5"), "Lineman-5", PlayerNo(5), BLOODBORN_MARAUDER_LINEMEN)
                addPlayer(PlayerId("Kh6"), "Lineman-6", PlayerNo(6), BLOODBORN_MARAUDER_LINEMEN)
                addPlayer(PlayerId("Kh7"), "Khorngor-7", PlayerNo(7), KHORNGORS)
                addPlayer(PlayerId("Kh8"), "Khorngor-8", PlayerNo(8), KHORNGORS)
                addPlayer(PlayerId("Kh9"), "Bloodseeker-9", PlayerNo(9), BLOODSEEKERS)
                addPlayer(PlayerId("Kh10"), "Bloodseeker-10", PlayerNo(10), BLOODSEEKERS)
                addPlayer(PlayerId("Kh11"), "Bloodspawn-11", PlayerNo(11), BLOODSPAWN)
                rerolls = 3
                apothecaries = 0
                dedicatedFans = 0
                teamValue = 1_000_000
            }
            history = null
        },
    )

}
