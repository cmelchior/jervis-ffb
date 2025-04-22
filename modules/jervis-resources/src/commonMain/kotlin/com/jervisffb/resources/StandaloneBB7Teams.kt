package com.jervisffb.resources

import com.jervisffb.engine.model.PlayerId
import com.jervisffb.engine.model.PlayerNo
import com.jervisffb.engine.rules.BB72020Rules
import com.jervisffb.engine.rules.builder.GameType
import com.jervisffb.engine.serialize.FILE_FORMAT_VERSION
import com.jervisffb.engine.serialize.JervisMetaData
import com.jervisffb.engine.serialize.buildTeamFile
import com.jervisffb.engine.teamBuilder

// The List of default starter team rosters for BB7.
// This is primarily used by Standalone Mode
object StandaloneBB7Teams {
    private val rules = BB72020Rules()
    val defaultTeams = mapOf(
        "amazon-bb7-starter-team.jrt" to buildTeamFile {
            metadata = JervisMetaData(FILE_FORMAT_VERSION)
            roster = AMAZON_TEAM
            team = teamBuilder(rules, AMAZON_TEAM) {
                name = "Amazon Starter Team #1"
                type = GameType.BB7
                addPlayer(PlayerId("Am-bb7-1"), "Blitzer-1", PlayerNo(1), AMAZON_BLITZER)
                addPlayer(PlayerId("Am-bb7-2"), "Blitzer-2", PlayerNo(2), AMAZON_BLITZER)
                addPlayer(PlayerId("Am-bb7-3"), "Blocker-3", PlayerNo(3), AMAZON_BLOCKER)
                addPlayer(PlayerId("Am-bb7-4"), "Blocker-4", PlayerNo(4), AMAZON_BLOCKER)
                addPlayer(PlayerId("Am-bb7-5"), "Linewoman-5", PlayerNo(5), AMAZON_LINEMAN)
                addPlayer(PlayerId("Am-bb7-6"), "Linewoman-6", PlayerNo(6), AMAZON_LINEMAN)
                addPlayer(PlayerId("Am-bb7-7"), "Linewoman-7", PlayerNo(7), AMAZON_LINEMAN)
                addPlayer(PlayerId("Am-bb7-8"), "Linewoman-8", PlayerNo(8), AMAZON_LINEMAN)
                rerolls = 1
                apothecaries = 0
                dedicatedFans = 0
                teamValue = 585_000
            }
            history = null
        },

        "orc-bb7-starter-team.jrt" to buildTeamFile {
            metadata = JervisMetaData(FILE_FORMAT_VERSION)
            roster = ORC_TEAM
            team = teamBuilder(rules, ORC_TEAM) {
                name = "Orc Starter Team #1"
                type = GameType.BB7
                addPlayer(PlayerId("Orc-bb7-1"), "Blitzer-1", PlayerNo(1), ORC_BLITZER)
                addPlayer(PlayerId("Orc-bb7-2"), "Blitzer-2", PlayerNo(2), ORC_BLITZER)
                addPlayer(PlayerId("Orc-bb7-3"), "Blocker-3", PlayerNo(3), BIG_UN_BLOCKERS)
                addPlayer(PlayerId("Orc-bb7-4"), "Thrower-4", PlayerNo(4), ORC_THROWER)
                addPlayer(PlayerId("Orc-bb7-5"), "Lineman-5", PlayerNo(5), ORC_LINEMEN)
                addPlayer(PlayerId("Orc-bb7-6"), "Lineman-6", PlayerNo(6), ORC_LINEMEN)
                addPlayer(PlayerId("Orc-bb7-7"), "Lineman-7", PlayerNo(7), ORC_LINEMEN)
                rerolls = 1
                apothecaries = 0
                dedicatedFans = 0
                teamValue = 585_000
            }
            history = null
        },
    )
}
