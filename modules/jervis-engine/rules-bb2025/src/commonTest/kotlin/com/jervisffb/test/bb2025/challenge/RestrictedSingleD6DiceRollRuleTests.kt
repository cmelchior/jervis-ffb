package com.jervisffb.test.bb2025.challenge

import com.jervisffb.engine.ActionRequest
import com.jervisffb.engine.GameRulesContext
import com.jervisffb.engine.actions.Dice
import com.jervisffb.engine.actions.DiceFaces
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.actions.GameActionId
import com.jervisffb.engine.actions.RollDice
import com.jervisffb.engine.bb2025.StandardBB2025Rules
import com.jervisffb.engine.bb2025.challenge.rule.RestrictedSingleD6DiceRollRule
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.policy.ActionFilterContext
import com.jervisffb.engine.rules.policy.GameRulePhase
import com.jervisffb.engine.utils.InvalidGameStateException
import com.jervisffb.test.bb2025.createDefaultHomeTeamBB2025
import com.jervisffb.test.bb2025.humanTeamAwayBB2025
import kotlin.test.Test
import kotlin.test.assertEquals

class RestrictedSingleD6DiceRollRuleTests {

    @Test
    fun restrictsAllSingleD6RollsWhenNoTypesAreSpecified() {
        val rule = RestrictedSingleD6DiceRollRule(4.d6)
        val request = rule.filterRequest(
            context(),
            ActionRequest(
                GameActionId(0),
                null,
                listOf(
                    RollDice(Dice.D6, type = DiceRollType.RUSH),
                    RollDice(Dice.D6, type = DiceRollType.LEAP),
                ),
            ),
        )

        request.actions.forEach { descriptor ->
            assertEquals(DiceFaces.of(4), (descriptor as RollDice).getAllowedFaces(0))
        }
    }

    @Test
    fun restrictsOnlySelectedTypesAndSingleD6Rolls() {
        val rule = RestrictedSingleD6DiceRollRule(4.d6, listOf(DiceRollType.RUSH, DiceRollType.LEAP))
        val request = rule.filterRequest(
            context(),
            ActionRequest(
                GameActionId(0),
                null,
                listOf(
                    RollDice(Dice.D6, type = DiceRollType.RUSH),
                    RollDice(Dice.D6, Dice.D6, type = DiceRollType.LEAP),
                    RollDice(Dice.D6, type = DiceRollType.JUMP),
                    RollDice(Dice.BLOCK, type = DiceRollType.RUSH),
                ),
            ),
        )

        assertEquals(DiceFaces.of(4), (request.actions[0] as RollDice).getAllowedFaces(0))
        assertEquals(DiceFaces.all(Dice.D6), (request.actions[1] as RollDice).getAllowedFaces(0))
        assertEquals(DiceFaces.all(Dice.D6), (request.actions[2] as RollDice).getAllowedFaces(0))
        assertEquals(DiceFaces.all(Dice.BLOCK), (request.actions[3] as RollDice).getAllowedFaces(0))
    }

    private fun context(): ActionFilterContext {
        val rules = StandardBB2025Rules()
        val state = Game(
            GameRulesContext(rules, emptyList()),
            createDefaultHomeTeamBB2025(rules),
            humanTeamAwayBB2025(rules),
        )
        return ActionFilterContext(state, TestActionNode, GameRulePhase.LIVE)
    }

    private object TestActionNode : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team? = null
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> = emptyList()
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command =
            throw InvalidGameStateException("Not used by this test")
    }
}
