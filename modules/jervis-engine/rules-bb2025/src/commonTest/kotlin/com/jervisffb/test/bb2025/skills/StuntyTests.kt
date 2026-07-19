package com.jervisffb.test.bb2025.skills

import com.jervisffb.engine.actions.Cancel
import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.actions.DirectionSelected
import com.jervisffb.engine.actions.PassTypeSelected
import com.jervisffb.engine.actions.PitchSquareSelected
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.actions.Undo
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.ext.dblock
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.model.Direction
import com.jervisffb.engine.model.PlayerDogoutState
import com.jervisffb.engine.model.context.DodgeRollContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.locations.PitchCoordinate
import com.jervisffb.engine.model.modifiers.MarkedModifier
import com.jervisffb.engine.rules.bb2025.skills.Stunty
import com.jervisffb.engine.rules.common.actions.PassType
import com.jervisffb.engine.rules.common.actions.PlayerStandardActionType
import com.jervisffb.engine.rules.common.procedures.actions.pass.PassContext
import com.jervisffb.engine.rules.common.procedures.actions.pass.PassingType
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.test.JervisGameBB2025Test
import com.jervisffb.test.SmartMoveTo
import com.jervisffb.test.activatePlayer
import com.jervisffb.test.dodge
import com.jervisffb.test.ext.rollForward
import com.jervisffb.test.moveTo
import com.jervisffb.test.pickup
import com.jervisffb.test.standardBlock
import com.jervisffb.test.throwBall
import com.jervisffb.test.utils.assertCoordinates
import com.jervisffb.test.utils.assertStanding
import com.jervisffb.test.utils.makeDistracted
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Class testing usage of the [Stunty] skill.
 */
class StuntyTests: JervisGameBB2025Test() {

    @BeforeTest
    override fun setUp() {
        super.setUp()
        startDefaultGame()
    }

    @Test
    fun ignoreMarkedModifiersOnDodge() {
        val player = awayTeam["A1".playerId]
        player.addSkill(SkillType.STUNTY)
        controller.rollForward(
            *activatePlayer(player, PlayerStandardActionType.MOVE),
        )
        assertTrue(rules.isMarked(player))
        controller.rollForward(
            *moveTo(12, 4),
        )
        assertTrue(rules.isMarked(player))
        assertTrue(state.getContext<DodgeRollContext>().rollModifiers.none { it is MarkedModifier })
        assertEquals(3, player.agility)
        controller.rollForward(
            *dodge(3.d6),
        )
        assertEquals(PitchCoordinate(12, 4), player.coordinates)
        player.assertStanding()
    }

    @Test
    fun extraModifierOnIntercept() {
        controller.rollForward(
            *activatePlayer("A10", PlayerStandardActionType.PASS),
            *moveTo(17, 7),
            *pickup(4.d6),
            SmartMoveTo(12, 3),
            PassTypeSelected(PassType.STANDARD),
            PitchSquareSelected(13, 9),
            *throwBall(6.d6),
        )
        assertEquals(PassingType.ACCURATE, state.getContext<PassContext>().passingResult)
        homeTeam["H2".playerId].apply {
            agility = 1 // Make interceptor super human
            addSkill(SkillType.STUNTY)
        }
        awayTeam["A1".playerId].makeDistracted() // Remove mark
        awayTeam["A2".playerId].makeDistracted() // Remove mark
        awayTeam["A3".playerId].makeDistracted() // Remove mark
        controller.rollForward(
            PlayerSelected("H2".playerId), // Select Interceptor
            4.d6, // Intercept (with -4 modifier) - Will fail
        )
        assertFalse(state.getContext<PassContext>().intercept!!.didIntercept)
        controller.rollForward(
            Undo,
            5.d6, // Intercept - Will succeed
        )
        assertEquals(homeTeam, state.activeTeam)
        assertTrue(homeTeam["H2".playerId].hasBall())
    }

    @Test
    fun useStuntyInjuryTable() {
        val attacker = state.getPlayerById("A1".playerId)
        val defender = state.getPlayerById("H1".playerId)
        defender.addSkill(SkillType.STUNTY)
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.BLOCK),
            *standardBlock("H1", 6.dblock),
            DirectionSelected(Direction.DOWN_LEFT),
            Cancel, // Do not follow up
            DiceRollResults(6.d6, 6.d6),
            DiceRollResults(1.d6, 6.d6), // KO on Stunty Injury Table
            Cancel, // Do not use Apothecary
        )
        assertNull(state.activePlayer)
        attacker.assertCoordinates(13, 5)
        attacker.assertStanding()
        assertEquals(PlayerDogoutState.KNOCKED_OUT, defender.state)
    }
}
