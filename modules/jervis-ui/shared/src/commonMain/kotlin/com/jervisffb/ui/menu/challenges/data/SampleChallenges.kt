package com.jervisffb.ui.menu.challenges.data

import com.jervisffb.engine.bb2025.challenge.goal.BlockGoalBuilder
import com.jervisffb.engine.bb2025.challenge.goal.DebugGoalBuilder
import com.jervisffb.engine.bb2025.challenge.goal.ScoreTouchdownGoalBuilder
import com.jervisffb.engine.bb2025.challenge.modifier.BlockDiceRequired
import com.jervisffb.engine.bb2025.challenge.modifier.PerformedByPlayer
import com.jervisffb.engine.bb2025.challenge.modifier.PerformedByTeam
import com.jervisffb.engine.bb2025.challenge.rule.TeamRerollsAvailable
import com.jervisffb.engine.bb2025.challenge.rule.TurnLimit
import com.jervisffb.engine.challenge.Challenge
import com.jervisffb.engine.challenge.ChallengeBuilder
import com.jervisffb.engine.challenge.ChallengeCategory
import com.jervisffb.engine.challenge.ChallengeScore
import com.jervisffb.engine.challenge.ChallengeScoring
import com.jervisffb.engine.challenge.GoalTarget
import com.jervisffb.engine.model.ChallengeId
import com.jervisffb.engine.model.Coach
import com.jervisffb.engine.model.CoachId
import com.jervisffb.engine.serialization.JervisSerialization
import com.jervisffb.utils.getHttpClient
import com.jervisffb.utils.jervisLogger
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.Url
import io.ktor.http.encodeURLParameter
import kotlin.time.Clock
import kotlin.time.Instant

// Temporary wrapper around sample data to better be able to create it and use
// it in the ChallengeRepository.
data class ChallengePresentation(
    val challenge: Challenge,
    val communityVotes: Int,
    val userVote: Boolean,
    val userScore: ChallengeScore<*>? = null,
    val otherScores: Set<ScoreboardEntry> = emptySet(),
)

/**
 * List of (incomplete) challenges. Should only be used for testing.
 *
 * The file behind it is read once by [SampleChallenges.load] and handed in as
 * text, but it is parsed again for every challenge on purpose: `Game` rebinds a
 * `Team` to itself in its `init` block, so two challenges sharing one team pair
 * would both end up pointing at whichever game was built last.
 */
private fun ChallengeBuilder.addSampleTeam(positionJson: String) {
    val gameData = JervisSerialization.loadFromJsonContent(positionJson).getOrThrow()
    this.gameRules = gameData.game.rules
    this.homeTeam = gameData.homeTeam
    this.awayTeam = gameData.awayTeam
    this.setup.addAll(gameData.actions)
}

/**
 * Hard-coded sample challenges. Challenges are authored in code for now, but a
 * future Editor UI is expected to use the same API.
 *
 * Call [load] to feth and build them.
 */
object SampleChallenges {

    private val LOG = jervisLogger()

    // TODO Temporary. The position needs to move into the repository, or into
    //  the challenge definition itself, once challenges can be serialized.
    private val CHALLENGES_SAMPLE = Url("https://jervis.ilios.dk/proxy.php?url=${"https://jervis.ilios.dk/first-turn.jrg".encodeURLParameter()}")

    /**
     * Builds the sample challenges and returns them. This can be quite heavy
     * so call it from a background scope. Nothing is cached here. Callers
     * must cache the result.
     *
     * See [com.jervisffb.ui.menu.challenges.InMemoryChallengesRepository]
     */
    suspend fun load(): List<ChallengePresentation> {
        val positionJson = readPosition() ?: return emptyList()
        return listOf(
            ChallengePresentation(
                challenge = ChallengeBuilder(ChallengeId("one-turn-touchdown")).apply {
                    name = "One-Turn Touchdown"
                    author = "Ilios".toSampleCoach()
                    category = ChallengeCategory.ONE_TURN_TOUCHDOWNS
                    description = "The classic. Score a touchdown in a single turn using a hand-off and a " +
                        "blitz to clear the final defender."
                    addSampleTeam(positionJson)
                    scoring = ChallengeScoring.ProbabilityScoring(homeTeam!!.id)
                    goal = ScoreTouchdownGoalBuilder()
                        .addModifier(PerformedByPlayer(homeTeam!!.first()))
                        .build()
                    addRules(
                        TeamRerollsAvailable(0),
                        TurnLimit(1),
                    )
                }.build(),
                communityVotes = 63,
                userVote = true,
            ),
            ChallengePresentation(
                challenge = ChallengeBuilder(ChallengeId("two-heads-are-better")).apply {
                    name = "Two Heads Are Better"
                    author = "Ilios".toSampleCoach()
                    category = ChallengeCategory.CROWD_SURFING
                    description = "A lone Black Orc stands between your Blitzer and a clear run at the " +
                        "end zone. Knock him down and stay on your feet, without rolling a single skull."
                    addSampleTeam(positionJson)
                    scoring = ChallengeScoring.ProbabilityScoring(homeTeam!!.id)
                    goal = BlockGoalBuilder(homeTeam!!, GoalTarget.AnyPlayers(count = 1, sameTeam = false))
                        .addModifier(BlockDiceRequired(2))
                        .build()
                    addRule(TeamRerollsAvailable(1))
                    addRule(TurnLimit(1))
                }.build(),
                communityVotes = 42,
                userVote = false,
                userScore = null,
                otherScores = emptySet(),
            ),
            ChallengePresentation(
                challenge = ChallengeBuilder(ChallengeId("threading-the-needle")).apply {
                    name = "Threading the Needle"
                    author = "Nuffle".toSampleCoach()
                    category = ChallengeCategory.SCORING
                    description = "Your Thrower has the ball and a Catcher is streaking downfield. Find " +
                        "the passing lane through a crowded midfield and complete the throw."
                    addSampleTeam(positionJson)
                    goal = ScoreTouchdownGoalBuilder()
                        .addModifier(PerformedByTeam(homeTeam!!))
                        .build()
                    addRules(
                        TeamRerollsAvailable(1),
                        TurnLimit(2),
                    )
                }.build(),
                communityVotes = 18,
                userVote = true,
                otherScores = setOf(
                    completionEntry("Nuffle", Clock.System.now()),
                    completionEntry("Skittter", Clock.System.now()),
                ),
            ),
            ChallengePresentation(
                challenge = ChallengeBuilder(ChallengeId("the-great-escape")).apply {
                    name = "The Great Escape"
                    author = "Skittter".toSampleCoach()
                    category = ChallengeCategory.BLOCKING
                    description = "Your Gutter Runner is surrounded. Dodge out of three tackle zones " +
                        "and sprint into open field without hitting the turf."
                    addSampleTeam(positionJson)
                    goal = ScoreTouchdownGoalBuilder()
                        .addModifier(PerformedByTeam(homeTeam!!))
                        .build()
                    addRules(
                        TeamRerollsAvailable(0),
                        TurnLimit(1),
                    )
                }.build(),
                communityVotes = 27,
                userVote = false,
                userScore = ChallengeScore.CompletionOnly(Clock.System.now()),
                otherScores = setOf(
                    completionEntry("Nuffle"),
                    completionEntry("Skittter"),
                ),
            ),
            ChallengePresentation(
                challenge = ChallengeBuilder(ChallengeId("hold-the-line")).apply {
                    name = "Hold the Line"
                    author = "GrimIronjaw".toSampleCoach()
                    category = ChallengeCategory.BREAK_THE_CAGE
                    description = "You are up by one with the clock running down. Stall the drive, keep " +
                        "the ball safe and deny the opponent any chance to equalise."
                    addSampleTeam(positionJson)
                    goal = BlockGoalBuilder(awayTeam!!, GoalTarget.SpecificPlayer(awayTeam!!.first()))
                        .addModifier(PerformedByTeam(homeTeam!!))
                        .addModifier(BlockDiceRequired(1))
                        .build()
                    addRules(
                        TeamRerollsAvailable(0),
                        TurnLimit(1),
                    )
                }.build(),
                communityVotes = 9,
                userVote = false,
                otherScores = setOf(
                    completionEntry("Grim Ironjaw")
                ),
            ),
            ChallengePresentation(
                challenge = ChallengeBuilder(ChallengeId("crowd-surf")).apply {
                    name = "Crowd Surf"
                    author = "Nuffle".toSampleCoach()
                    category = ChallengeCategory.CROWD_SURFING
                    description = "The opposing Blitzer is standing right on the sideline. Push him " +
                        "into the crowd for a guaranteed removal."
                    addSampleTeam(positionJson)
                    goal = BlockGoalBuilder(homeTeam!!, GoalTarget.AnyPlayers(1))
                        .addModifier(BlockDiceRequired(3))
                        .build()
                    addRule(TurnLimit(1))
                    addRule(TeamRerollsAvailable(0))
                }.build(),
                communityVotes = 51,
                userVote = false,
                otherScores = setOf(
                    completionEntry("Nuffle"),
                    completionEntry("Cowhead")
                ),
            ),
            ChallengePresentation(
                challenge = ChallengeBuilder(ChallengeId("the-long-bomb")).apply {
                    name = "The Long Bomb"
                    author = "Skittter".toSampleCoach()
                    category = ChallengeCategory.SCORING
                    description = "It is all or nothing. Launch a Hail Mary pass the length of the " +
                        "pitch and bring it down for the winning score."
                    addSampleTeam(positionJson)
                    goal = ScoreTouchdownGoalBuilder()
                        .addModifier(PerformedByTeam(homeTeam!!))
                        .build()
                    addRules(
                        TeamRerollsAvailable(0),
                        TurnLimit(1),
                    )
                }.build(),
                communityVotes = 0,
                userVote = false,
            ),
            ChallengePresentation(
                challenge = ChallengeBuilder(ChallengeId("pick-and-run")).apply {
                    name = "Pick and Run"
                    author = "GrimIronjaw".toSampleCoach()
                    category = ChallengeCategory.BLOCKING
                    description = "The ball is loose inside a tackle zone. Pick it up cleanly and carry " +
                        "it to safety before the defence collapses on you."
                    addSampleTeam(positionJson)
                    goal = ScoreTouchdownGoalBuilder()
                        .addModifier(PerformedByTeam(homeTeam!!))
                        .build()
                    addRules(
                        TeamRerollsAvailable(0),
                        TurnLimit(1),
                    )
                }.build(),
                communityVotes = 33,
                userVote = false,
                otherScores = setOf(
                    completionEntry("GrimIronjaw"),
                    completionEntry("Ilios"),
                    completionEntry("Jim")
                ),
            ),
            ChallengePresentation(
                challenge = ChallengeBuilder(ChallengeId("debug")).apply {
                    name = "Demo Puzzle"
                    author = "GrimIronjaw".toSampleCoach()
                    category = ChallengeCategory.BLOCKING
                    description = "This challenge is just for testing"
                    addSampleTeam(positionJson)
                    goal = DebugGoalBuilder()
                        .addModifier(PerformedByTeam(homeTeam!!))
                        .build()
                    addRules(
                        TurnLimit(1),
                    )
                }.build(),
                communityVotes = 33,
                userVote = false,
                otherScores = setOf(
                    completionEntry("Nuffle"),
                    completionEntry("Cowhead")
                ),
            ),
        )
    }

    /**
     * Reads the shared starting position. Returns `null` if it is missing or
     * unreadable, which just means there are no sample challenges to show.
     */
    private suspend fun readPosition(): String? {
        return try {
            return getHttpClient().get(CHALLENGES_SAMPLE).bodyAsText()
        } catch (ex: Exception) {
            LOG.w { "Could not read sample challenge position $CHALLENGES_SAMPLE: $ex" }
            null
        }
    }

    private fun completionEntry(coachName: String, date: Instant = Clock.System.now()): ScoreboardEntry {
        val coach = coachName.toSampleCoach()
        return ScoreboardEntry(
            coach = coach,
            score = ChallengeScore.CompletionOnly(date)
        )
    }

    private fun String.toSampleCoach(): Coach {
        return Coach(CoachId("coach-${this.lowercase()}"), this)
    }
}
