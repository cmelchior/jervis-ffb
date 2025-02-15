package com.jervisffb.test

import com.jervisffb.engine.model.Coach
import com.jervisffb.engine.model.CoachId
import com.jervisffb.engine.model.Field
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.PlayerId
import com.jervisffb.engine.model.PlayerNo
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.teamBuilder
import com.jervisffb.test.utils.HUMAN_BLITZER
import com.jervisffb.test.utils.HUMAN_CATCHER
import com.jervisffb.test.utils.HUMAN_LINEMAN
import com.jervisffb.test.utils.HUMAN_TEAM
import com.jervisffb.test.utils.HUMAN_THROWER

fun getDefaultTestSetup(rules: Rules): Game {
    val team1: Team =
        teamBuilder(rules, HUMAN_TEAM) {
            coach = Coach(CoachId("home-coach"), "HomeCoach")
            name = "HomeTeam"
            addPlayer(PlayerId("H1"), "Lineman-1", PlayerNo(1), HUMAN_LINEMAN)
            addPlayer(PlayerId("H2"), "Lineman-2", PlayerNo(2), HUMAN_LINEMAN)
            addPlayer(PlayerId("H3"), "Lineman-3", PlayerNo(3), HUMAN_LINEMAN)
            addPlayer(PlayerId("H4"), "Lineman-4", PlayerNo(4), HUMAN_LINEMAN)
            addPlayer(PlayerId("H5"), "Thrower-1", PlayerNo(5), HUMAN_THROWER)
            addPlayer(PlayerId("H6"), "Catcher-1", PlayerNo(6), HUMAN_CATCHER)
            addPlayer(PlayerId("H7"), "Catcher-2", PlayerNo(7), HUMAN_CATCHER)
            addPlayer(PlayerId("H8"), "Blitzer-1", PlayerNo(8), HUMAN_BLITZER)
            addPlayer(PlayerId("H9"), "Blitzer-2", PlayerNo(9), HUMAN_BLITZER)
            addPlayer(PlayerId("H10"), "Blitzer-3", PlayerNo(10), HUMAN_BLITZER)
            addPlayer(PlayerId("H11"), "Blitzer-4", PlayerNo(11), HUMAN_BLITZER)
            reRolls = 4
            apothecaries = 1
        }
    val p1 = team1
    val p2 = team1
    val field = Field.createForRuleset(rules)
    return Game(rules, p1, p2, field)
}

