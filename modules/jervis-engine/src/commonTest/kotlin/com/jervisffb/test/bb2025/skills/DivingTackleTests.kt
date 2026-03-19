package com.jervisffb.test.bb2025.skills

import com.jervisffb.engine.actions.Cancel
import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.actions.EndAction
import com.jervisffb.engine.actions.FieldSquareSelected
import com.jervisffb.engine.actions.MoveType
import com.jervisffb.engine.actions.MoveTypeSelected
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.rules.bb2025.skills.DivingTackle
import com.jervisffb.engine.rules.common.actions.PlayerStandardActionType
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.test.JervisGameBB2025Test
import com.jervisffb.test.activatePlayer
import com.jervisffb.test.dodge
import com.jervisffb.test.ext.rollForward
import com.jervisffb.test.jump
import com.jervisffb.test.leap
import com.jervisffb.test.moveTo
import com.jervisffb.test.utils.assertCoordinates
import com.jervisffb.test.utils.assertProne
import com.jervisffb.test.utils.assertStanding
import com.jervisffb.test.utils.putProne
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Class testing usage of the [DivingTackle] skill.
 */
class DivingTackleTests: JervisGameBB2025Test() {

    @BeforeTest
    override fun setUp() {
        super.setUp()
        startDefaultGame()
    }

    @Test
    fun usedAfterOtherModifiersAndRerolls() {
        val mover = awayTeam["A1".playerId]
        val tackler = homeTeam["H1".playerId]
        tackler.addSkill(SkillType.DIVING_TACKLE)
        assertEquals(3, mover.agility)
        controller.rollForward(
            *activatePlayer(mover, PlayerStandardActionType.MOVE),
            *moveTo(14, 5),
            *dodge(4.d6),
            PlayerSelected(tackler),
            DiceRollResults(1.d6, 1.d6)
        )
        assertNull(state.activePlayer)
        assertEquals(homeTeam, state.activeTeam)
        assertEquals(FieldCoordinate(14, 5), mover.coordinates)
        mover.assertProne()
    }

    @Test
    fun useIsOptional() {
        val mover = awayTeam["A1".playerId]
        val tackler = homeTeam["H1".playerId]
        tackler.addSkill(SkillType.DIVING_TACKLE)
        assertEquals(3, mover.agility)
        controller.rollForward(
            *activatePlayer(mover, PlayerStandardActionType.MOVE),
            *moveTo(14, 5),
            *dodge(4.d6),
            Cancel, // Do not use Diving Tackle
            EndAction
        )
        assertNull(state.activePlayer)
        assertEquals(awayTeam, state.activeTeam)
        assertEquals(FieldCoordinate(14, 5), mover.coordinates)
        mover.assertStanding()
    }

    @Test
    fun onlyOnePlayerCanUseIt() {
        val mover = awayTeam["A1".playerId]
        val tackler1 = homeTeam["H1".playerId]
        tackler1.addSkill(SkillType.DIVING_TACKLE)
        val tackler2 = homeTeam["H1".playerId]
        tackler2.addSkill(SkillType.DIVING_TACKLE)
        assertEquals(3, mover.agility)
        controller.rollForward(
            *activatePlayer(mover, PlayerStandardActionType.MOVE),
            *moveTo(14, 5),
            *dodge(4.d6),
            PlayerSelected(tackler2),
            DiceRollResults(1.d6, 1.d6),
        )
        assertNull(state.activePlayer)
        assertEquals(homeTeam, state.activeTeam)
        assertEquals(FieldCoordinate(14, 5), mover.coordinates)
        mover.assertProne()
    }

    @Test
    fun leapDoesNotConsiderDivingTackleModifier() {
        val leapingPlayer = state.getPlayerById("A1".playerId)
        leapingPlayer.addSkill(SkillType.LEAP)
        homeTeam["H1".playerId].putProne()
        homeTeam["H2".playerId].addSkill(SkillType.DIVING_TACKLE)
        assertEquals(3, leapingPlayer.agility)
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.MOVE),
            MoveTypeSelected(MoveType.LEAP),
            FieldSquareSelected(11, 4),
            *leap(4.d6), // -1 Modifiers from leaving, no to enter, so should succeed (Leap modifier cannot be used)
            PlayerSelected("H2".playerId), // Use Diving Tackle (causing leap to fail)
            DiceRollResults(1.d6, 1.d6),
        )
        assertNull(state.activePlayer)
        leapingPlayer.assertProne()
        leapingPlayer.assertCoordinates(11, 4)
    }

    @Test
    fun workOnJump() {
        val leapingPlayer = state.getPlayerById("A1".playerId)
        homeTeam["H1".playerId].putProne()
        homeTeam["H2".playerId].addSkill(SkillType.DIVING_TACKLE)
        assertEquals(3, leapingPlayer.agility)
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.MOVE),
            MoveTypeSelected(MoveType.JUMP),
            FieldSquareSelected(11, 4),
            *jump(4.d6), // -1 Modifiers from leaving, no to enter, so should succeed
            PlayerSelected("H2".playerId), // Use Diving Tackle (causing Jump to fail)
            DiceRollResults(1.d6, 1.d6),
        )
        assertNull(state.activePlayer)
        leapingPlayer.assertProne()
        leapingPlayer.assertCoordinates(11, 4)
    }
}
