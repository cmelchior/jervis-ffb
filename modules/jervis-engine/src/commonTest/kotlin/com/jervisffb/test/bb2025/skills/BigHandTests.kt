package com.jervisffb.test.bb2025.skills

import com.jervisffb.engine.actions.Cancel
import com.jervisffb.engine.actions.Confirm
import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.commands.SetPlayerLocation
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.rules.bb2025.skills.BigHand
import com.jervisffb.engine.rules.common.actions.PlayerStandardActionType
import com.jervisffb.engine.rules.common.skills.RegularTeamReroll
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.test.JervisGameBB2025Test
import com.jervisffb.test.SmartMoveTo
import com.jervisffb.test.activatePlayer
import com.jervisffb.test.defaultKickOffHomeTeam
import com.jervisffb.test.defaultPregame
import com.jervisffb.test.defaultSetup
import com.jervisffb.test.ext.rollForward
import com.jervisffb.test.pickup
import com.jervisffb.test.utils.SelectTeamReroll
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Class testing usage of the [BigHand] skill.
 */
class BigHandTests: JervisGameBB2025Test() {

    @BeforeTest
    override fun setUp() {
        super.setUp()
        state.apply {
            awayTeam["A10".playerId].apply {
                addSkill(SkillType.BIG_HAND.id())
            }
        }

        controller.rollForward(
            *defaultPregame(
                weatherRoll = DiceRollResults(6.d6, 5.d6)
            ),
            *defaultSetup(),
            *defaultKickOffHomeTeam(),
        )
    }

    @Test
    fun chooseNotToUseBigHand() {
        val player = awayTeam["A10".playerId]
        controller.rollForward(
            *activatePlayer(player, PlayerStandardActionType.MOVE),
            SmartMoveTo(17, 7),
            Cancel, // Do not use Big Hand
            *pickup(3.d6, SelectTeamReroll<RegularTeamReroll>()),
            4.d6
        )
        assertTrue(player.hasBall())
    }

    @Test
    fun workOnPickup() {
        val player = awayTeam["A10".playerId]
        controller.rollForward(
            *activatePlayer(player, PlayerStandardActionType.MOVE),
            SmartMoveTo(17, 7),
            Confirm, // Use Big Hand
            *pickup(3.d6),
        )
        assertTrue(player.hasBall())
    }

    @Test
    fun workOnSecureTheBall() {
        val player = awayTeam["A10".playerId]
        controller.rollForward(
            *activatePlayer(player, PlayerStandardActionType.SECURE_THE_BALL),
            SmartMoveTo(17, 7),
            Confirm, // Use Big Hand
            *pickup(3.d6),
        )
        assertTrue(player.hasBall())
    }

    @Test
    fun ignoreMarksOnPickup() {
        SetPlayerLocation(homeTeam["H1".playerId], FieldCoordinate(18,8)).execute(state)
        val player = awayTeam["A10".playerId]
        controller.rollForward(
            *activatePlayer(player, PlayerStandardActionType.MOVE),
            SmartMoveTo(17, 7),
            Confirm, // Use Big Hand
            *pickup(3.d6),
        )
        assertTrue(player.hasBall())
    }

    @Test
    fun ignorePouringRainOnPickup() {
        val player = awayTeam["A10".playerId]
        controller.rollForward(
            *activatePlayer(player, PlayerStandardActionType.MOVE),
            SmartMoveTo(17, 7),
            Confirm, // Use Big Hand
            *pickup(3.d6),
        )
        assertTrue(player.hasBall())
    }

    @Test
    fun ignoreMarksOnSecureTheBall() {
        val player = awayTeam["A10".playerId]
        controller.rollForward(
            *activatePlayer(player, PlayerStandardActionType.SECURE_THE_BALL)
        )
        SetPlayerLocation(homeTeam["H1".playerId], FieldCoordinate(18,8)).execute(state)
        controller.rollForward(
            SmartMoveTo(17, 7),
            Confirm, // Use Big Hand
            *pickup(3.d6),
        )
        assertTrue(player.hasBall())
    }

    @Test
    fun ignorePouringRainOnSecureTheBall() {
        val player = awayTeam["A10".playerId]
        controller.rollForward(
            *activatePlayer(player, PlayerStandardActionType.SECURE_THE_BALL),
            SmartMoveTo(17, 7),
            Confirm, // Use Big Hand
            *pickup(3.d6),
        )
        assertTrue(player.hasBall())
    }
}
