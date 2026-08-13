package com.jervisffb.ui.menu.challenges.data

import com.jervisffb.engine.bb2025.challenge.goal.BlockGoalBuilder
import com.jervisffb.engine.bb2025.challenge.goal.ScoreTouchdownGoalBuilder
import com.jervisffb.engine.bb2025.challenge.modifier.BlockDiceRequired
import com.jervisffb.engine.bb2025.challenge.modifier.PerformedByTeam
import com.jervisffb.engine.bb2025.challenge.rule.MoveTypesAvailable
import com.jervisffb.engine.bb2025.challenge.rule.RestrictedBlockDiceRule
import com.jervisffb.engine.bb2025.challenge.rule.RestrictedSingleD6DiceRollRule
import com.jervisffb.engine.bb2025.challenge.rule.TeamRerollsAvailable
import com.jervisffb.engine.bb2025.challenge.rule.TurnLimit
import com.jervisffb.engine.challenge.Challenge
import com.jervisffb.engine.challenge.ChallengeBuilder
import com.jervisffb.engine.challenge.ChallengeCategory
import com.jervisffb.engine.challenge.ChallengeScore
import com.jervisffb.engine.challenge.ChallengeScoring
import com.jervisffb.engine.challenge.GoalTarget
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.ext.dblock
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.model.ChallengeId
import com.jervisffb.engine.model.Coach
import com.jervisffb.engine.model.CoachId
import com.jervisffb.engine.rules.builder.DiceRollOwner
import com.jervisffb.engine.rules.builder.UndoActionBehavior
import com.jervisffb.engine.serialization.GameFileData
import com.jervisffb.engine.serialization.JervisSerialization
import com.jervisffb.utils.getHttpClient
import com.jervisffb.utils.jervisLogger
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.Url

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
 * List of sample challenges. Should only be used for testing.
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

private suspend fun loadSampleChallenge(url: String): GameFileData {
    val url = Url(url)
    val json = getHttpClient().get(url).bodyAsText()
    return JervisSerialization.loadFromJsonContent(json).getOrThrow()
}

private fun ChallengeBuilder.addGameData(data: GameFileData) {
    this.gameRules = data.game.rules.toBuilder().run {
        diceRollsOwner = DiceRollOwner.ROLL_ON_CLIENT
        undoActionBehavior = UndoActionBehavior.ALLOWED
        allowPlayerEditsDuringGame = false
        build()
    }
    this.homeTeam = data.homeTeam
    this.awayTeam = data.awayTeam
    this.setup.addAll(data.actions)
}

/**
 * Hard-coded sample challenges. Challenges are authored in code for now, but a
 * future Editor UI is expected to use the same API.
 *
 * Call [load] to feth and build them.
 */
object SampleChallenges {

    private val LOG = jervisLogger()

    private val challengeData: List<Pair<String, (GameFileData) -> ChallengePresentation>> = listOf(
        "https://jervis.ilios.dk/uploads/challenges/v1/ma8_vs_wide_los.jrg" to { data: GameFileData ->
            ChallengePresentation(
                challenge = ChallengeBuilder(ChallengeId("ma8_vs_wide_los")).apply {
                    name = "MA8 vs. Wide LOS (Easy)"
                    author = "KFoged".toSampleCoach()
                    category = ChallengeCategory.ONE_TURN_TOUCHDOWNS
                    description = """
                        This is a setup as high elves vs a wide LOS and backline defense.
                        
                        Original Source: https://fumbbl.com/help:OTTTestingInClient
                    """.trimIndent()
                    addGameData(data)
                    scoring = ChallengeScoring.ProbabilityScoring(homeTeam!!.id)
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
            )
        },

        "https://jervis.ilios.dk/uploads/challenges/v1/ma9_vs_push_denial.jrg" to { data ->
            ChallengePresentation(
                challenge = ChallengeBuilder(ChallengeId("ma9_vs_push_denial")).apply {
                    name = "MA9 vs. Push Denial (Difficult)"
                    author = "KFoged".toSampleCoach()
                    category = ChallengeCategory.ONE_TURN_TOUCHDOWNS
                    description = """
                        This advanced setup intended when the LOS (line of scrimmage) does not allow easy conventional pushes. It will take advantage of one straggler on the side to push in to you side stepper in order to get in range.
                        
                        Original Source: https://fumbbl.com/help:OTTTestingInClient
                    """.trimIndent()
                    addGameData(data)
                    scoring = ChallengeScoring.ProbabilityScoring(homeTeam!!.id)
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
            )
        },

        "https://jervis.ilios.dk/uploads/challenges/v1/short_way_around.jrg" to { data ->
            ChallengePresentation(
                challenge = ChallengeBuilder(ChallengeId("dodge-this")).apply {
                    name = "The Short Way Around"
                    author = "Sp00keh".toSampleCoach()
                    category = ChallengeCategory.BLOCKING
                    description = """
                            How can dwarves get a hit on the amazon carrier, without dodging or rushing?
                            
                            Original Source: 145 Club on Discord
                    """.trimIndent()
                    addGameData(data)
                    scoring = ChallengeScoring.ProbabilityScoring(awayTeam!!.id)
                    goal = BlockGoalBuilder(awayTeam!!, GoalTarget.SpecificPlayer(homeTeam!!["Am3".playerId]))
                        .addModifier(BlockDiceRequired(1))
                        .build()
                    addRules(
                        MoveTypesAvailable(dodge = false, rush = false),
                        RestrictedSingleD6DiceRollRule(forcedResult = 2.d6, rollTypes = null),
                        TeamRerollsAvailable(0),
                        TurnLimit(1)
                    )
                }.build(),
                communityVotes = 0,
                userVote = false,
            )
        },
        "https://jervis.ilios.dk/uploads/challenges/v1/prevent_the_td.jrg" to { data ->
            ChallengePresentation(
                challenge = ChallengeBuilder(ChallengeId("prevent-the-td")).apply {
                    name = "Prevent the TD!"
                    author = "Blood Bowl Tactics".toSampleCoach()
                    category = ChallengeCategory.BLOCKING
                    description = """
                        During a tournament match at the Old World Football Tournament League (OFTL), the game between Mardaed’s Bawl Players and Kal_Durak’s Regnat Rattus came down to this play. Mardaed’s Bawl Players were able to stall and score as the kicking team during the first half.  The stall left the Bawl Players short-handed, but they were still able to position the ball carrier on a break-away run, 2 spaces from scoring, AND more importantly, too far from the stormvermin to blitz.

                        You are the Skaven coach.  2D Blitz the ball carrier!  Can you ball like Kal?
                        
                        Original Source: https://bbtactics.com/blood-bowl-challenge-002/
                        
                    """.trimIndent()
                    addGameData(data)
                    scoring = ChallengeScoring.ProbabilityScoring(homeTeam!!.id)
                    goal = BlockGoalBuilder(homeTeam!!, GoalTarget.SpecificPlayer(awayTeam!!["He5".playerId]))
                        .addModifier(PerformedByTeam(homeTeam!!))
                        .addModifier(BlockDiceRequired(2))
                        .build()
                    addRules(
                        RestrictedBlockDiceRule(6.dblock, RestrictedBlockDiceRule.RollType.ATTACKER_CHOOSES),
                        RestrictedBlockDiceRule(1.dblock, RestrictedBlockDiceRule.RollType.SINGLE_DIE),
                        RestrictedBlockDiceRule(1.dblock, RestrictedBlockDiceRule.RollType.OPPONENT_CHOOSES),
                        RestrictedSingleD6DiceRollRule(forcedResult = 2.d6, rollTypes = null),
                        TurnLimit(1),
                    )
                }.build(),
                communityVotes = 0,
                userVote = false,
            )
        },
    )

    /**
     * Builds the sample challenges and returns them. This can be quite heavy
     * so call it from a background scope. Nothing is cached here. Callers
     * must cache the result.
     *
     * See [com.jervisffb.ui.menu.challenges.InMemoryChallengesRepository]
     */
    suspend fun load(): List<ChallengePresentation> {
        return challengeData.mapNotNull { (url, challengeFactory) ->
            try {
                val gameData = loadSampleChallenge(url)
                challengeFactory(gameData)
            } catch (ex: Throwable) {
                LOG.w(ex) { "Failed to load sample challenge: $url" }
                null
            }
        }
    }

    private fun String.toSampleCoach(): Coach {
        return Coach(CoachId("coach-${this.lowercase()}"), this)
    }
}
