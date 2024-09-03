package dk.ilios.jervis

import dk.ilios.jervis.model.Coach
import dk.ilios.jervis.model.CoachId
import dk.ilios.jervis.model.Field
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.PlayerId
import dk.ilios.jervis.model.PlayerNo
import dk.ilios.jervis.model.Team
import dk.ilios.jervis.rules.Rules
import dk.ilios.jervis.rules.roster.bb2020.HumanTeam

fun getDefaultTestSetup(rules: Rules): Game {
    val team1: Team =
        teamBuilder(rules, HumanTeam) {
            coach = Coach(CoachId("home-coach"), "HomeCoach")
            name = "HomeTeam"
            addPlayer(PlayerId("H1"), "Lineman-1", PlayerNo(1), HumanTeam.LINEMAN)
            addPlayer(PlayerId("H2"), "Lineman-2", PlayerNo(2), HumanTeam.LINEMAN)
            addPlayer(PlayerId("H3"), "Lineman-3", PlayerNo(3), HumanTeam.LINEMAN)
            addPlayer(PlayerId("H4"), "Lineman-4", PlayerNo(4), HumanTeam.LINEMAN)
            addPlayer(PlayerId("H5"), "Thrower-1", PlayerNo(5), HumanTeam.THROWER)
            addPlayer(PlayerId("H6"), "Catcher-1", PlayerNo(6), HumanTeam.CATCHER)
            addPlayer(PlayerId("H7"), "Catcher-2", PlayerNo(7), HumanTeam.CATCHER)
            addPlayer(PlayerId("H8"), "Blitzer-1", PlayerNo(8), HumanTeam.BLITZER)
            addPlayer(PlayerId("H9"), "Blitzer-2", PlayerNo(9), HumanTeam.BLITZER)
            addPlayer(PlayerId("H10"), "Blitzer-3", PlayerNo(10), HumanTeam.BLITZER)
            addPlayer(PlayerId("H11"), "Blitzer-4", PlayerNo(11), HumanTeam.BLITZER)
            reRolls = 4
            apothecaries = 1
        }
    val p1 = team1
    val p2 = team1
    val field = Field.createForRuleset(rules)
    return Game(p1, p2, field)
}

