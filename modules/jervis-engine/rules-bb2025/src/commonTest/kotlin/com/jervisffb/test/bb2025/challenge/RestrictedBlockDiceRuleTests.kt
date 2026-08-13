package com.jervisffb.test.bb2025.challenge

import com.jervisffb.engine.GameEngineController
import com.jervisffb.engine.GameRulesContext
import com.jervisffb.engine.actions.BlockDice
import com.jervisffb.engine.actions.DBlockResult
import com.jervisffb.engine.actions.Dice
import com.jervisffb.engine.actions.DiceFaces
import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.actions.RollDice
import com.jervisffb.engine.bb2025.StandardBB2025Rules
import com.jervisffb.engine.bb2025.challenge.rule.RestrictedBlockDiceRule
import com.jervisffb.engine.challenge.ChallengeRule
import com.jervisffb.engine.common.procedures.FullGame
import com.jervisffb.engine.ext.dblock
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.rules.common.actions.PlayerStandardActionType
import com.jervisffb.engine.utils.InvalidActionException
import com.jervisffb.test.activatePlayer
import com.jervisffb.test.bb2025.createDefaultHomeTeamBB2025
import com.jervisffb.test.bb2025.humanTeamAwayBB2025
import com.jervisffb.test.defaultKickOffHomeTeam
import com.jervisffb.test.defaultPregame
import com.jervisffb.test.defaultSetup
import com.jervisffb.test.ext.rollForward
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class RestrictedBlockDiceRuleTests {

    private data class Fixture(
        val state: Game,
        val controller: GameEngineController,
    )

    private fun createFixture(rule: ChallengeRule = RestrictedBlockDiceRule(3.dblock, 1)): Fixture {
        val rules = StandardBB2025Rules()
        val state = Game(
            GameRulesContext(rules, rule.policies),
            createDefaultHomeTeamBB2025(rules),
            humanTeamAwayBB2025(rules),
        )
        val controller = GameEngineController(state)
        controller.startTestMode(FullGame)
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            *defaultKickOffHomeTeam(),
            *activatePlayer("A1", PlayerStandardActionType.BLOCK),
            PlayerSelected("H1".playerId),
        )
        return Fixture(state, controller)
    }

    @Test
    fun filtersEveryBlockDieToForcedResult() {
        val (_, controller) = createFixture()
        val request = controller.getAvailableActions().get<RollDice>()

        assertEquals(DiceRollType.BLOCK, request.type)
        request.dice.indices.forEach { index ->
            assertEquals(DiceFaces.of(3), request.getAllowedFaces(index))
        }
    }

    @Test
    fun doesNotFilterBlockDiceWithDifferentCount() {
        val (_, controller) = createFixture(RestrictedBlockDiceRule(3.dblock, 2))
        val request = controller.getAvailableActions().get<RollDice>()

        request.dice.indices.forEach { index ->
            assertEquals(DiceFaces.all(request.dice[index]), request.getAllowedFaces(index))
        }
    }

    @Test
    fun rejectsResultsOtherThanForcedResult() {
        val (_, controller) = createFixture()
        val request = controller.getAvailableActions().get<RollDice>()

        assertFailsWith<InvalidActionException> {
            controller.handleAction(DiceRollResults(List(request.dice.size) { DBlockResult(1) }))
        }
        assertFailsWith<InvalidActionException> {
            controller.handleAction(DiceRollResults(List(request.dice.size) { DBlockResult(4) }))
        }
        controller.handleAction(DiceRollResults(List(request.dice.size) { DBlockResult(3) }))
    }

    @Test
    fun randomRollsRespectAllowedFaces() {
        val request = RollDice(
            Dice.BLOCK,
            Dice.BLOCK,
            type = DiceRollType.BLOCK,
            allowedFaces = listOf(DiceFaces.of(3, 4), DiceFaces.of(3, 4)),
        )

        repeat(20) {
            val roll = request.createRandom(Random(it)) as DiceRollResults
            assertTrue(roll.rolls.all { result -> result is DBlockResult && result.blockResult == BlockDice.PUSH_BACK })
        }
    }
}
