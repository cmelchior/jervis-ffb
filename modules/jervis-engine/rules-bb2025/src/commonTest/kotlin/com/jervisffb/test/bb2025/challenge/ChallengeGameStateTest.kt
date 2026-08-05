package com.jervisffb.test.bb2025.challenge

import com.jervisffb.engine.bb2025.StandardBB2025Rules
import com.jervisffb.engine.bb2025.challenge.goal.DebugGoalBuilder
import com.jervisffb.engine.bb2025.challenge.rule.TeamRerollsAvailable
import com.jervisffb.engine.bb2025.procedures.rerolls.LeaderTeamReroll
import com.jervisffb.engine.challenge.Challenge
import com.jervisffb.engine.challenge.ChallengeBuilder
import com.jervisffb.engine.challenge.ChallengeCategory
import com.jervisffb.engine.model.ChallengeId
import com.jervisffb.test.bb2025.createDefaultHomeTeamBB2025
import com.jervisffb.test.bb2025.humanTeamAwayBB2025
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotSame

/**
 * Tests that a challenge hands every game its own teams.
 *
 * Sharing them used to carry state between attempts. The visible symptom was
 * team re-rolls piling up, because a Leader re-roll is added at every start of
 * half and nothing reset the list between games.
 */
class ChallengeGameStateTest {

    private fun buildChallenge(rerolls: Int = 0): Challenge {
        val rules = StandardBB2025Rules()
        return ChallengeBuilder(ChallengeId("fresh-teams")).apply {
            name = "Fresh Teams"
            description = "Every attempt starts from the same position."
            category = ChallengeCategory.BLOCKING
            gameRules = rules
            homeTeam = createDefaultHomeTeamBB2025(rules)
            awayTeam = humanTeamAwayBB2025(rules)
            goal = DebugGoalBuilder().build()
            addRule(TeamRerollsAvailable(rerolls))
        }.build()
    }

    @Test
    fun createGame_buildsItsOwnTeams() {
        val challenge = buildChallenge()
        val first = challenge.createGame()
        val second = challenge.createGame()

        assertNotSame(first.state.homeTeam, second.state.homeTeam)
        assertNotSame(first.state.awayTeam, second.state.awayTeam)
        assertNotSame(first.state.homeTeam.first(), second.state.homeTeam.first())

        // The team references in `Challenge` are a template for the details page,
        // and must never end up in a `GameEngineController`.
        assertNotSame(challenge.homeTeam, first.state.homeTeam)
        assertNotSame(challenge.awayTeam, first.state.awayTeam)
    }

    @Test
    fun createGame_doesNotCarryRerollsBetweenGames() {
        val challenge = buildChallenge(rerolls = 1)
        val first = challenge.createGame()
        val expected = first.state.homeTeam.availableRerollCount

        // Stands in for SetupTeam, which appends a Leader re-roll at the start of
        // a half. With teams shared, this is what accumulated on every attempt.
        first.state.homeTeam.rerolls.add(LeaderTeamReroll(first.state.homeTeam.id))
        assertEquals(expected + 1, first.state.homeTeam.availableRerollCount)

        val second = challenge.createGame()
        assertEquals(expected, second.state.homeTeam.availableRerollCount)
    }

    @Test
    fun teamRerollsAvailable_setsTheStartingAllowance() {
        // The roster these teams are built from has 4 re-rolls; the rule decides
        // what the challenge actually starts with.
        assertEquals(4, createDefaultHomeTeamBB2025(StandardBB2025Rules()).availableRerollCount)
        assertEquals(4, buildChallenge(rerolls = 0).createGame().state.homeTeam.availableRerollCount)

        assertEquals(0, buildChallenge(rerolls = 0).createGame().run {
            this.startManualMode()
            this.state.homeTeam.availableRerollCount
        })
        assertEquals(2, buildChallenge(rerolls = 2).createGame().run {
            this.startManualMode()
            this.state.homeTeam.availableRerollCount
        })
        assertEquals(2, buildChallenge(rerolls = 2).createGame().run {
            this.startManualMode()
            this.state.homeTeam.availableRerollCount
        })
    }
}
