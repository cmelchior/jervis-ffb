package com.jervisffb.test.bb2025.inducements

import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.model.inducements.Bribe
import com.jervisffb.engine.rules.common.actions.PlayerStandardActionType
import com.jervisffb.engine.rules.common.procedures.SetupTeam
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.test.JervisGameBB2025Test
import com.jervisffb.test.SmartMoveTo
import com.jervisffb.test.activatePlayer
import com.jervisffb.test.argueTheCall
import com.jervisffb.test.ext.rollForward
import com.jervisffb.test.skipTurns
import com.jervisffb.test.useBribe
import com.jervisffb.test.utils.assertActive
import com.jervisffb.test.utils.assertActiveTeam
import com.jervisffb.test.utils.assertNoActivePlayer
import com.jervisffb.test.utils.assertReserves
import com.jervisffb.test.utils.assertStanding
import com.jervisffb.test.utils.putProne
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Class testing the functionality of the [Bribe] Inducement.
 */
class BribeTests: JervisGameBB2025Test() {

    @BeforeTest
    override fun setUp() {
        super.setUp()
        awayTeam["A1".playerId].addSkill(SkillType.SECRET_WEAPON)
        startDefaultGame()

        // Until we get proper Inducements Support, manually add Bribes
        awayTeam.bribes.add(Bribe())
    }

    @Test
    fun decideAfterArgueTheCall() {
        val target = homeTeam["H1".playerId]
        target.putProne()

        controller.rollForward(
            *activatePlayer("A6", PlayerStandardActionType.FOUL),
            SmartMoveTo(13, 4),
            PlayerSelected(target), // Start foul
            DiceRollResults(2.d6, 2.d6), // Roll double -> Sent off
            argueTheCall(true),
            6.d6, // Roll "Well If Yout Put It Like That"
            useBribe(true),
            2.d6, // Bribe succeed
        )
        state.assertActiveTeam(awayTeam)
        state.assertNoActivePlayer()
    }

    @Test
    fun canBribeWhenChoosingNotToArgueTheCall() {
        val target = homeTeam["H1".playerId]
        target.putProne()

        controller.rollForward(
            *activatePlayer("A6", PlayerStandardActionType.FOUL),
            SmartMoveTo(13, 4),
            PlayerSelected(target), // Start foul
            DiceRollResults(2.d6, 2.d6), // Roll double -> Sent off
            argueTheCall(false),
            useBribe(true),
            2.d6, // Bribe succeed
        )
        state.assertActiveTeam(awayTeam)
        state.assertNoActivePlayer()
    }

    @Test
    fun succeedOn2Plus() {
        val target = homeTeam["H1".playerId]
        target.putProne()

        controller.rollForward(
            *activatePlayer("A6", PlayerStandardActionType.FOUL),
            SmartMoveTo(13, 4),
            PlayerSelected(target), // Start foul
            DiceRollResults(2.d6, 2.d6), // Roll double -> Sent off
            argueTheCall(true),
            2.d6, // Roll I Don't Care
            useBribe(true),
            2.d6, // Bribe succeed
        )
        assertTrue(state.awayTeam.bribes.single().used)
        assertFalse(awayTeam.coachBanned)
        state.assertActiveTeam(awayTeam)
        state.assertNoActivePlayer()
    }

    @Test
    fun failOn1() {
        val target = homeTeam["H1".playerId]
        target.putProne()

        controller.rollForward(
            *activatePlayer("A6", PlayerStandardActionType.FOUL),
            SmartMoveTo(13, 4),
            PlayerSelected(target), // Start foul
            DiceRollResults(2.d6, 2.d6), // Roll double -> Sent off
            argueTheCall(true),
            2.d6, // Roll I Don't Care
            useBribe(true),
            1.d6, // Bribe fail
        )
        assertTrue(state.awayTeam.bribes.single().used)
        assertFalse(awayTeam.coachBanned)
        state.assertActiveTeam(homeTeam)
        state.assertNoActivePlayer()
    }

    @Test
    fun coachAlwaysBanned() {
        val fouler = awayTeam["A6".playerId]
        val target = homeTeam["H1".playerId]
        target.putProne()
        controller.rollForward(
            *activatePlayer(fouler, PlayerStandardActionType.FOUL),
            SmartMoveTo(13, 4),
            PlayerSelected(target), // Start foul
            DiceRollResults(2.d6, 2.d6), // Roll double -> Sent off
            argueTheCall(true),
            1.d6, // Roll You're Outta Here
            useBribe(true),
            2.d6, // Bribe succeed
        )
        assertTrue(state.awayTeam.bribes.single().used)
        assertTrue(awayTeam.coachBanned)
        state.assertActiveTeam(awayTeam)
    }

    @Test
    fun noTurnoverOnBribe() {
        val target = homeTeam["H1".playerId]
        target.putProne()
        val fouler = awayTeam["A6".playerId]
        fouler.addSkill(SkillType.QUICK_FOUL)
        controller.rollForward(
            *activatePlayer(fouler, PlayerStandardActionType.FOUL),
            SmartMoveTo(13, 4),
            PlayerSelected(target), // Start foul
            DiceRollResults(2.d6, 2.d6), // Roll double -> Sent off
            argueTheCall(true),
            2.d6,
            useBribe(true),
            2.d6, // Bribe succeed
        )
        assertTrue(state.awayTeam.bribes.single().used)
        state.assertActiveTeam(awayTeam)
        fouler.assertActive()
    }

    @Test
    fun canBribeSecretWeapon() {
        controller.rollForward(
            *skipTurns(16),
            PlayerSelected("A1".playerId), // Send-off player with Secret Weapon
            argueTheCall(false),
            useBribe(true),
            2.d6,
        )
        awayTeam["A1".playerId].assertReserves()
        assertEquals(SetupTeam.SelectPlayerOrEndSetup, controller.currentNode())
    }

    // Behavior clarified in Designer's Commentary May 2026
    @Test
    fun canBribeIfCoachIsBanned() {
        val fouler = awayTeam["A6".playerId]
        awayTeam.coachBanned = true
        val target = homeTeam["H1".playerId]
        target.putProne()
        controller.rollForward(
            *activatePlayer(fouler, PlayerStandardActionType.FOUL),
            SmartMoveTo(13, 4),
            PlayerSelected(target), // Start foul
            DiceRollResults(2.d6, 2.d6), // Roll double -> Sent off
            useBribe(true),
            2.d6, // Bribe succeed
        )
        assertTrue(state.awayTeam.bribes.single().used)
        assertTrue(awayTeam.coachBanned)
        state.assertActiveTeam(awayTeam)
        state.assertNoActivePlayer()
        fouler.assertStanding()
    }
}
