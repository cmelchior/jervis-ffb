package com.jervisffb.test.bb2025.skills

import com.jervisffb.engine.actions.Cancel
import com.jervisffb.engine.actions.Confirm
import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.actions.DirectionSelected
import com.jervisffb.engine.ext.d16
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.ext.dblock
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.model.Direction
import com.jervisffb.engine.model.PlayerState
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.locations.DogOut
import com.jervisffb.engine.model.modifiers.CasualtyModifier
import com.jervisffb.engine.rules.bb2025.skills.Decay
import com.jervisffb.engine.rules.common.actions.PlayerStandardActionType
import com.jervisffb.engine.rules.common.procedures.tables.injury.RiskingInjuryContext
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.engine.rules.common.tables.CasualtyResult
import com.jervisffb.test.JervisGameBB2025Test
import com.jervisffb.test.activatePlayer
import com.jervisffb.test.ext.rollForward
import com.jervisffb.test.standardBlock
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Class testing usage of the [Decay] trait.
 */
class DecayTests: JervisGameBB2025Test() {

    @BeforeTest
    override fun setUp() {
        super.setUp()
        startDefaultGame()
    }

    @Test
    fun applyToCasualtyRoll() {
        val defender = homeTeam["H1".playerId]
        defender.addSkill(SkillType.DECAY)
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.BLOCK),
            *standardBlock(defender, 6.dblock),
            DirectionSelected(Direction.LEFT),
            Cancel, // Do not follow up
            DiceRollResults(6.d6, 6.d6), // Armor
            DiceRollResults(4.d6, 6.d6), // Casualty
            DiceRollResults(8.d16), // Badly Hurt + 1 -> Serious Injury
        )
        val context = state.getContext<RiskingInjuryContext>()
        assertEquals(CasualtyResult.SERIOUSLY_HURT, context.casualtyResult)
        assertTrue(context.casualtyModifiers.contains(CasualtyModifier.DECAY))
        controller.rollForward(
            Cancel,  // Do not Apothecary
        )

        assertEquals(PlayerState.SERIOUSLY_HURT, defender.state)
        assertEquals(DogOut, defender.location)
    }

    @Test
    fun applyToRerolledCasualtyRoll() {
        val defender = homeTeam["H1".playerId]
        defender.addSkill(SkillType.DECAY)
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.BLOCK),
            *standardBlock(defender, 6.dblock),
            DirectionSelected(Direction.LEFT),
            Cancel, // Do not follow up
            DiceRollResults(6.d6, 6.d6), // Armor
            DiceRollResults(4.d6, 6.d6), // Casualty
            DiceRollResults(8.d16),
            Confirm, // Use Apothecary
            DiceRollResults(14.d16), // Serious Injury + 1 -> Dead
            Confirm, // Use 2nd roll
        )
        assertEquals(PlayerState.DEAD, defender.state)
        assertEquals(DogOut, defender.location)
    }
}
