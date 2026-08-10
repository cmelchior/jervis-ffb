package com.jervisffb.test.bb2025.propability

import com.jervisffb.engine.actions.BlockDice
import com.jervisffb.engine.actions.Confirm
import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.actions.NoRerollSelected
import com.jervisffb.engine.actions.PlayerActionSelected
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.actions.Undo
import com.jervisffb.engine.bb2025.challenge.goal.BlockGoalBuilder
import com.jervisffb.engine.bb2025.procedures.rerolls.StandardTeamReroll
import com.jervisffb.engine.challenge.ChallengeBuilder
import com.jervisffb.engine.challenge.ChallengeCategory
import com.jervisffb.engine.challenge.ChallengeOutcome
import com.jervisffb.engine.challenge.ChallengeScore
import com.jervisffb.engine.challenge.ChallengeScoring
import com.jervisffb.engine.challenge.ChallengeTracker
import com.jervisffb.engine.challenge.GoalTarget
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.ext.dblock
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.model.ChallengeId
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.rules.common.actions.PlayerStandardActionType
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.engine.statistics.probability.ProbabilityTracker
import com.jervisffb.engine.statistics.probability.Surprisal
import com.jervisffb.engine.statistics.probability.event.ActionPathEvent
import com.jervisffb.engine.statistics.probability.event.PhysicalRollRole
import com.jervisffb.engine.statistics.probability.event.RerollCategory
import com.jervisffb.engine.statistics.probability.observation.ChanceObservation
import com.jervisffb.engine.statistics.probability.observation.ChanceRerollSourceKind
import com.jervisffb.engine.statistics.probability.scorer.LogicalActionPathScorer
import com.jervisffb.engine.statistics.probability.scorer.PhysicalActionPathScorer
import com.jervisffb.engine.statistics.probability.scorer.ProbabilityScoreResult
import com.jervisffb.test.JervisGameBB2025Test
import com.jervisffb.test.SmartMoveTo
import com.jervisffb.test.activatePlayer
import com.jervisffb.test.ext.rollForward
import com.jervisffb.test.moveTo
import com.jervisffb.test.standardBlock
import com.jervisffb.test.utils.TeamRerollSelected
import com.jervisffb.test.utils.putProne
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class LogicalActionPathLedgerInstrumentationTests : JervisGameBB2025Test() {
    @BeforeTest
    override fun setUp() {
        setupDefaultGame(collectMetadata = true)
        state.collectChanceData = false
        startDefaultGame()
        state.collectChanceData = true
    }

    @Test
    fun trackingIsOptIn() {
        state.collectChanceData = false
        controller.rollForward(
            PlayerSelected("A1".playerId),
            PlayerActionSelected(PlayerStandardActionType.MOVE),
            *moveTo(12, 4),
            6.d6,
        )

        assertTrue(trackedObservations().isEmpty())
        assertTrue(controller.getDelta().chanceObservations.isEmpty())
    }

    @Test
    fun rerolledD6RecordsPhysicalRelationshipAndNormalizesToFinalValue() {
        controller.rollForward(
            PlayerSelected("A1".playerId),
            PlayerActionSelected(PlayerStandardActionType.MOVE),
            *moveTo(12, 4),
            1.d6,
            TeamRerollSelected<StandardTeamReroll>(),
            4.d6,
        )

        val observations = trackedDiceRolls()
        assertEquals(2, observations.size)
        val initial = observations[0]
        val reroll = observations[1]
        assertEquals(DiceRollType.DODGE, initial.rollType)
        assertEquals(1, (initial.dice.single().result as D6Result).value)
        assertEquals(initial.index, reroll.rerolledRollIndex)
        assertEquals(4, (reroll.dice.single().result as D6Result).value)
        assertTrue(reroll.success == true)
        assertTrue(initial.rerollOptions.any { it.source.kind == ChanceRerollSourceKind.TEAM_REROLL })

        val event = assertIs<ActionPathEvent.Logical>(logicalScore().events.single())
        assertEquals(4, assertIs<D6Result>(event.results.single()).value)
        assertTrue(assertIs<ActionPathEvent.Resolution.Dice>(event.resolution).isSuccess)
        assertTrue(event.recoveries.any { it.resource.category == RerollCategory.TEAM_REROLL })

        controller.handleAction(Undo)
        val rewound = trackedDiceRolls().single()
        assertEquals(1, (rewound.dice.single().result as D6Result).value)
        assertTrue(!rewound.finalized)
    }

    @Test
    fun acceptedArmourRollRecordsAndFinalizesEntireDicePool() {
        val target = state.getPlayerById("H1".playerId)
        target.putProne()

        controller.rollForward(
            *activatePlayer("A6", PlayerStandardActionType.FOUL),
            SmartMoveTo(13, 4),
            PlayerSelected(target),
            DiceRollResults(2.d6, 2.d6),
        )

        val observation = trackedDiceRolls().single { it.rollType == DiceRollType.ARMOUR }
        assertEquals(listOf(2, 2), observation.dice.map { assertIs<D6Result>(it.result).value })
        assertTrue(observation.dice.all { it.dieId != null })
        assertEquals(false, observation.success)
        assertTrue(observation.rerollOptions.isEmpty())
        assertTrue(observation.finalized)
    }

    @Test
    fun successfulArmourRollSnapshotsLoneFoulerForAlternativeFailureBranch() {
        val fouler = state.getPlayerById("A6".playerId)
        fouler.addSkill(SkillType.LONE_FOULER)
        val target = state.getPlayerById("H1".playerId)
        target.putProne()

        controller.rollForward(
            *activatePlayer(fouler, PlayerStandardActionType.FOUL),
            SmartMoveTo(13, 4),
            PlayerSelected(target),
            DiceRollResults(4.d6, 5.d6),
        )

        val observation = trackedDiceRolls().single { it.rollType == DiceRollType.ARMOUR }
        assertEquals(true, observation.success)
        val loneFoulerOption = observation.rerollOptions.single {
            it.source.skillId?.type == SkillType.LONE_FOULER
        }
        assertTrue(!loneFoulerOption.appliesOnSuccess)
        assertTrue(loneFoulerOption.appliesOnFailure)
        assertTrue(!loneFoulerOption.currentlyAvailable)
        assertEquals(null, observation.selectedReroll)
        assertTrue(observation.finalized)
    }

    @Test
    fun loneFoulerRecordsFailureOnlyPoolRerollAndReplacementRoll() {
        val fouler = state.getPlayerById("A6".playerId)
        fouler.addSkill(SkillType.LONE_FOULER)
        val target = state.getPlayerById("H1".playerId)
        target.putProne()

        controller.rollForward(
            *activatePlayer(fouler, PlayerStandardActionType.FOUL),
            SmartMoveTo(13, 4),
            PlayerSelected(target),
            DiceRollResults(2.d6, 2.d6),
            Confirm,
            DiceRollResults(5.d6, 6.d6),
        )

        val observations = trackedDiceRolls().filter { it.rollType == DiceRollType.ARMOUR }
        assertEquals(2, observations.size)
        val initial = observations[0]
        val reroll = observations[1]

        assertEquals(listOf(2, 2), initial.dice.map { assertIs<D6Result>(it.result).value })
        assertEquals(false, initial.success)
        assertTrue(initial.finalized)
        val loneFoulerOption = initial.rerollOptions.single {
            it.source.skillId?.type == SkillType.LONE_FOULER
        }
        assertTrue(!loneFoulerOption.appliesOnSuccess)
        assertTrue(loneFoulerOption.appliesOnFailure)
        assertTrue(loneFoulerOption.currentlyAvailable)
        assertEquals(initial.dice.map { it.id }.toSet(), loneFoulerOption.resultIds.toSet())
        assertEquals(loneFoulerOption.source.id, initial.selectedReroll?.sourceId)
        assertEquals(initial.dice.map { it.id }.toSet(), initial.selectedReroll?.resultIds?.toSet())
        assertEquals(true, initial.selectedReroll?.allowed)

        assertEquals(initial.index, reroll.rerolledRollIndex)
        assertEquals(listOf(5, 6), reroll.dice.map { assertIs<D6Result>(it.result).value })
        assertEquals(initial.dice.map { it.dieId }, reroll.dice.map { it.dieId })
        assertEquals(true, reroll.success)
        assertTrue(reroll.finalized)
    }

    @Test
    fun standardBlockRecordsRawPoolAndSelectedResult() {
        val attacker = state.getPlayerById("A1".playerId)
        val defender = state.getPlayerById("H1".playerId)

        controller.rollForward(
            *activatePlayer(attacker, PlayerStandardActionType.BLOCK),
            *standardBlock(defender, 5.dblock),
        )

        val observation = trackedDiceRolls().single()
        assertEquals(DiceRollType.BLOCK, observation.rollType)
        assertEquals(1, observation.dice.size)
        assertEquals(observation.dice.single().id, observation.selectedResultIds.single())

        val event = assertIs<ActionPathEvent.Logical>(logicalScore().events.single())
        val resolution = assertIs<ActionPathEvent.Resolution.Block>(event.resolution)
        assertEquals(BlockDice.STUMBLE, resolution.selectedFace)
        assertEquals(1, event.results.size)
        assertEquals(attacker.team.id, event.owner)
    }

    @Test
    fun challengeCompletingOnBlockRollIgnoresUnfinalizedBlock() {
        val attacker = state.getPlayerById("A1".playerId)
        val defender = state.getPlayerById("H1".playerId)
        val challenge = ChallengeBuilder(ChallengeId("unfinished-terminal-block")).apply {
            name = "Unfinished terminal block"
            description = "Complete when the target is blocked."
            category = ChallengeCategory.BLOCKING
            gameRules = rules
            homeTeam = this@LogicalActionPathLedgerInstrumentationTests.homeTeam
            awayTeam = this@LogicalActionPathLedgerInstrumentationTests.awayTeam
            goal = BlockGoalBuilder(awayTeam!!, GoalTarget.SpecificPlayer(defender)).build()
            scoring = ChallengeScoring.ProbabilityScoring(awayTeam!!.id)
        }.build()
        val tracker = ChallengeTracker(challenge).also { it.initialize(state, controller.statistics!!) }
        val actions = activatePlayer(attacker, PlayerStandardActionType.BLOCK) +
            arrayOf(PlayerSelected(defender.id), 5.dblock)

        var outcome = ChallengeOutcome.IN_PROGRESS
        actions.forEach { action ->
            controller.handleAction(action)
            outcome = tracker.evaluate(state, controller.getDelta())
        }

        assertEquals(ChallengeOutcome.COMPLETED, outcome)
        assertTrue(!trackedDiceRolls().single().finalized)
        val challengeScore = assertIs<ChallengeScore.ProbabilityScore>(tracker.score)
        val probabilityScore = assertIs<ProbabilityScoreResult.Scored>(challengeScore.result)
        assertTrue(probabilityScore.events.isEmpty())
        assertEquals(1.0, probabilityScore.successProbability.value)
        assertEquals(Surprisal.ZERO, probabilityScore.surprisal)
    }

    @Test
    fun hybridScorerClassifiesAnAcceptedRawD6() {
        controller.rollForward(
            PlayerSelected("A1".playerId),
            PlayerActionSelected(PlayerStandardActionType.MOVE),
            *moveTo(12, 4),
            2.d6,
            NoRerollSelected(),
        )

        val raw = trackedDiceRolls().single()
        assertEquals(null, raw.rerolledRollIndex)
        assertEquals(null, raw.selectedReroll)
        assertTrue(raw.finalized)

        val event = assertIs<ActionPathEvent.Physical>(physicalScore().events.single())
        assertEquals(2, assertIs<D6Result>(event.results.single()).value)
        assertEquals(PhysicalRollRole.PRIMARY, event.role)
        assertEquals(null, event.actualRecovery)
        assertTrue(event.recoveries.any { it.resource.category == RerollCategory.TEAM_REROLL })
    }

    @Test
    fun hybridScorerClassifiesActualRerollChronologically() {
        controller.rollForward(
            PlayerSelected("A1".playerId),
            PlayerActionSelected(PlayerStandardActionType.MOVE),
            *moveTo(12, 4),
            2.d6,
            TeamRerollSelected<StandardTeamReroll>(),
            4.d6,
        )

        val events = physicalScore().events
        assertEquals(2, events.size)
        val initial = assertIs<ActionPathEvent.Physical>(events[0])
        val reroll = assertIs<ActionPathEvent.Physical>(events[1])
        assertEquals(2, assertIs<D6Result>(initial.results.single()).value)
        assertEquals(RerollCategory.TEAM_REROLL, initial.actualRecovery?.resource?.category)
        assertTrue(initial.recoveries.isEmpty())
        assertEquals(4, assertIs<D6Result>(reroll.results.single()).value)
        assertEquals(PhysicalRollRole.REROLL, reroll.role)
        assertEquals(initial.index, reroll.traceRootIndex)
    }

    private fun logicalScore(): ProbabilityScoreResult.Scored = assertIs(
        LogicalActionPathScorer.score(
            state.rules,
            trackedObservations(),
            state.awayTeam.id,
        ),
    )

    private fun physicalScore(): ProbabilityScoreResult.Scored = assertIs(
        PhysicalActionPathScorer.score(
            state.rules,
            trackedObservations(),
            state.awayTeam.id,
        ),
    )

    private fun trackedDiceRolls(): List<ChanceObservation.DiceRoll> =
        trackedObservations().filterIsInstance<ChanceObservation.DiceRoll>()

    private fun trackedObservations(): List<ChanceObservation> {
        val tracker = ProbabilityTracker()
        controller.history.forEach(tracker::handleAction)
        return tracker.observations
    }
}
