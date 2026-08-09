package com.jervisffb.ui.menu.challenges

import com.jervis.generated.SettingsKeys
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.challenge.Challenge
import com.jervisffb.engine.challenge.ChallengeRerollSelectionPolicy
import com.jervisffb.engine.challenge.ChallengeScore
import com.jervisffb.engine.challenge.ChallengeScoring
import com.jervisffb.engine.model.ChallengeId
import com.jervisffb.engine.model.Coach
import com.jervisffb.engine.model.CoachId
import com.jervisffb.engine.statistics.probability.scorer.LogicalActionPathScorer
import com.jervisffb.engine.statistics.probability.scorer.PhysicalActionPathScorer
import com.jervisffb.engine.statistics.probability.scorer.PriorityListRerollUsagePolicy
import com.jervisffb.engine.statistics.probability.scorer.ProbabilityScoreResult
import com.jervisffb.ui.SETTINGS_MANAGER
import com.jervisffb.ui.menu.challenges.data.ChallengeDetails
import com.jervisffb.ui.menu.challenges.data.ChallengePresentation
import com.jervisffb.ui.menu.challenges.data.ChallengeRow
import com.jervisffb.ui.menu.challenges.data.ChallengeUserState
import com.jervisffb.ui.menu.challenges.data.ChallengeUserState.SolvedState
import com.jervisffb.ui.menu.challenges.data.SampleChallenges
import com.jervisffb.ui.menu.challenges.data.ScoreboardEntry
import com.jervisffb.utils.jervisLogger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.Duration

/**
 * Interface wrapping how to save and load challenge data.
 */
interface ChallengeRepository {
    suspend fun initialize()
    val challengesList: Flow<List<ChallengeRow>>
    fun challengeDetails(id: ChallengeId): Flow<ChallengeDetails>
    suspend fun markAsFavorite(id: ChallengeId, favorite: Boolean)
    suspend fun vote(id: ChallengeId, voted: Boolean)
    // Records a solved challenge, keeping the coach's best result.
    suspend fun solve(id: ChallengeId, score: ChallengeScore<*>, duration: Duration, actions: List<GameAction>)
}

/**
 * In-memory challenge repository. Will not persist anything after the application has exited.
 * Should only be used for testing.
 */
class InMemoryChallengesRepository(
    private val dataSource: suspend () -> List<ChallengePresentation> = SampleChallenges::load,
) : ChallengeRepository {

    private val LOG = jervisLogger()

    // Guards `loaded`, `attempts` and the read-modify-write of `rows`.
    private val mutex = Mutex()
    private var loaded = false

    // Authored data. Written once by `initialize`, read-only afterwards.
    private var communityVotes: Map<ChallengeId, Int> = emptyMap()
    private var otherScores: Map<ChallengeId, List<ScoreboardEntry>> = emptyMap()

    // The solution the coach last submitted, kept so `solve` is not lossy. It
    // is what a backend would upload as a challenge attempt for re-scoring.
    private val attempts: MutableMap<ChallengeId, List<GameAction>> = mutableMapOf()

    // Everything the UI observes. Insertion-ordered, and a mutation replaces
    // only the row it touches, so the other rows keep their identity and
    // `challengeDetails` can filter on that alone.
    private val rows = MutableStateFlow<Map<ChallengeId, ChallengeRow>>(emptyMap())

    // Who the coach is on their own scoreboard entry. There is no login yet,
    // so this is whatever name they last played under.
    private val localCoach: Coach by lazy {
        val name = SETTINGS_MANAGER
            .getString(SettingsKeys.JERVIS_DEFAULT_CLIENT_COACH_NAME, "")
            .ifBlank { "You" }
        Coach(CoachId("coach-local"), name)
    }

    override val challengesList: Flow<List<ChallengeRow>> = rows.map { it.values.toList() }

    override suspend fun initialize() {
        mutex.withLock {
            if (loaded) return
            val challenges = dataSource()
            communityVotes = challenges.associate { it.challenge.id to it.communityVotes }
            otherScores = challenges.associate { presentation ->
                presentation.challenge.id to presentation.otherScores
                    .filter { it.score.isRanked(presentation.challenge.scoring) }
                    .sortedWith { first, second -> compareScores(first.score, second.score) }
            }
            rows.value = challenges.associate { presentation ->
                val id = presentation.challenge.id
                id to ChallengeRow(
                    data = presentation.challenge,
                    votes = presentation.communityVotes + if (presentation.userVote) 1 else 0,
                    userState = ChallengeUserState(
                        solved = solvedState(presentation.challenge, presentation.userScore),
                        score = presentation.userScore,
                        voted = presentation.userVote,
                    ),
                )
            }
            loaded = true
        }
    }

    override fun challengeDetails(id: ChallengeId): Flow<ChallengeDetails> {
        return rows
            .map { it[id] }
            .filterNotNull()
            // Identity is enough: an untouched row is the same instance after
            // a mutation elsewhere. Comparing by value would walk into
            // `Challenge`, which carries both teams.
            .distinctUntilChanged { old, new -> old === new }
            .map { row -> row.toDetails() }
    }

    override suspend fun markAsFavorite(id: ChallengeId, favorite: Boolean) {
        updateUserState(id) { it.copy(favorite = favorite) }
    }

    override suspend fun vote(id: ChallengeId, voted: Boolean) {
        updateUserState(id) { it.copy(voted = voted) }
    }

    override suspend fun solve(
        id: ChallengeId,
        score: ChallengeScore<*>,
        duration: Duration,
        actions: List<GameAction>,
    ) {
        mutex.withLock {
            attempts[id] = actions
            updateRow(id) { userState ->
                val best = bestOf(userState.score, score)
                userState.copy(
                    solved = solvedState(rows.value.getValue(id).data, best),
                    score = best
                )
            }
        }
    }

    private suspend fun updateUserState(id: ChallengeId, transform: (ChallengeUserState) -> ChallengeUserState) {
        mutex.withLock { updateRow(id, transform) }
    }

    /** Callers must hold [mutex] before calling this method */
    private fun updateRow(id: ChallengeId, transform: (ChallengeUserState) -> ChallengeUserState) {
        val current = rows.value[id] ?: run {
            // Either the repository was never initialized or this id is not one of
            // ours. Staying silent here is what hid a solve going to the wrong
            // repository instance.
            LOG.w { "No challenge with id $id. Update dropped." }
            return
        }
        val userState = transform(current.userState)
        rows.value += (
            id to current.copy(
                userState = userState,
                votes = (communityVotes[id] ?: 0) + if (userState.voted) 1 else 0,
            )
        )
    }

    private fun ChallengeRow.toDetails(): ChallengeDetails {
        return ChallengeDetails(
            data = data,
            userState = userState,
            votes = votes,
            scoreboard = scoreboard(data, userState),
        )
    }

    // The scoreboard as the coach sees it: everyone else's runs plus their own, best first.
    private fun scoreboard(challenge: Challenge, userState: ChallengeUserState): List<ScoreboardEntry> {
        val others = otherScores[challenge.id] ?: emptyList()
        val ownScore = userState.score?.takeIf { it.isRanked(challenge.scoring) } ?: return others
        return (others + ScoreboardEntry(localCoach, ownScore)).sortedWith { first, second ->
            compareScores(first.score, second.score)
        }
    }

    private fun solvedState(
        challenge: Challenge,
        score: ChallengeScore<*>?,
    ): SolvedState {
        if (score == null) return SolvedState.UNSOLVED
        if (!score.isRanked(challenge.scoring)) return SolvedState.SOLVED
        val bestOther = otherScores[challenge.id]
            ?.filter { scoresAreComparable(it.score, score) }
            ?.minWithOrNull { first, second -> compareScores(first.score, second.score) }
            ?.score
        return when (bestOther == null || compareScores(score, bestOther) <= 0) {
            true -> SolvedState.BEST_IN_CLASS
            false -> SolvedState.SOLVED
        }
    }

    /**
     * Only scores from the same mechanism can be ranked against each other, so
     * a score of a different type replaces the old one rather than competing
     * with it.
     */
    private fun bestOf(current: ChallengeScore<*>?, candidate: ChallengeScore<*>): ChallengeScore<*> {
        if (current == null || !scoresAreComparable(current, candidate)) return candidate
        return if (compareScores(candidate, current) <= 0) candidate else current
    }

    private fun compareScores(first: ChallengeScore<*>, second: ChallengeScore<*>): Int {
        return when (first) {
            is ChallengeScore.CompletionOnly if second is ChallengeScore.CompletionOnly -> {
                first.compareTo(second)
            }

            is ChallengeScore.ProbabilityScore if second is ChallengeScore.ProbabilityScore -> {
                first.compareTo(second)
            }
            else -> {
                LOG.w { "Challenge Scores are mixed for challenge: $first and $second" }
                first::class.toString().compareTo(second::class.toString())
            }
        }
    }

    private fun scoresAreComparable(first: ChallengeScore<*>, second: ChallengeScore<*>): Boolean {
        if (first::class != second::class) return false
        if (first is ChallengeScore.ProbabilityScore && second is ChallengeScore.ProbabilityScore) {
            return first.result.algorithmId == second.result.algorithmId &&
                first.result.rerollPolicyId == second.result.rerollPolicyId
        }
        return true
    }

    private fun ChallengeScore<*>.usesCurrentScoringVersion(scoring: ChallengeScoring<*>): Boolean = when (this) {
        is ChallengeScore.CompletionOnly -> scoring == ChallengeScoring.CompletionOnly
        is ChallengeScore.ProbabilityScore -> when (val policy = (scoring as? ChallengeScoring.ProbabilityScoring)?.policy) {
            is ChallengeRerollSelectionPolicy.LogicalRerollSelection ->
                result.algorithmId == LogicalActionPathScorer.algorithmId
                    && result.rerollPolicyId == PriorityListRerollUsagePolicy.id
            is ChallengeRerollSelectionPolicy.PhysicalRerollSelection ->
                result.algorithmId == PhysicalActionPathScorer.algorithmId
                    && result.rerollPolicyId == PhysicalActionPathScorer.rerollUsagePolicy.id
            null -> false
        }
    }

    private fun ChallengeScore<*>.isRanked(scoring: ChallengeScoring<*>): Boolean = when (this) {
        is ChallengeScore.CompletionOnly -> false
        is ChallengeScore.ProbabilityScore ->
            result is ProbabilityScoreResult.Scored && usesCurrentScoringVersion(scoring)
    }
}
