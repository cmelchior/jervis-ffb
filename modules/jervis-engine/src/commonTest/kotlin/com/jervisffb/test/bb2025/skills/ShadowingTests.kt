package com.jervisffb.test.bb2025.skills

import com.jervisffb.engine.actions.EndAction
import com.jervisffb.engine.actions.EndTurn
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.actions.SelectPlayer
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.ext.playerNo
import com.jervisffb.engine.model.PlayerNo
import com.jervisffb.engine.rules.bb2025.skills.Shadowing
import com.jervisffb.engine.rules.common.actions.PlayerStandardActionType
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.test.JervisGameBB2025Test
import com.jervisffb.test.activatePlayer
import com.jervisffb.test.dodge
import com.jervisffb.test.ext.rollForward
import com.jervisffb.test.moveTo
import com.jervisffb.test.utils.assertCoordinates
import com.jervisffb.test.utils.makeDistracted
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Class testing usage of the [Shadowing] skill.
 * Technically, you could reroll a shadowing roll, but since both Team Rerolls
 * and Pro don't work during the opponent turn, these cannot be used, so no
 * known reroll type exists.
 */
class ShadowingTests: JervisGameBB2025Test() {

    @BeforeTest
    override fun setUp() {
        super.setUp()
        state.apply {
            // Should be on LoS
            homeTeam["H1".playerId].apply {
                addSkill(SkillType.SHADOWING.id())
                baseMove = 1
                move = 1
            }
        }
        startDefaultGame()
    }

    @Test
    fun canOnlyUseShadowingDuringOpponentTurn() {
        state.awayTeam[4.playerNo].apply {
            addSkill(SkillType.SHADOWING.id())
        }
        val activePlayer = awayTeam[5.playerNo]
        controller.rollForward(
            *activatePlayer(activePlayer, PlayerStandardActionType.MOVE),
            *moveTo(14, 9),
            *dodge(6.d6),
            EndAction // Shadowing does not trigger for the teams own players
        )
        activePlayer.assertCoordinates(14, 9)
        assertEquals(awayTeam, state.activeTeam)
    }

    @Test
    fun cannotUseShadowingIfDistracted() {
        val shadowingPlayer = homeTeam[1.playerNo]
        val activePlayer = awayTeam[1.playerNo]
        shadowingPlayer.makeDistracted()

        controller.rollForward(
            *activatePlayer(activePlayer, PlayerStandardActionType.MOVE),
            *moveTo(14, 5), // Move away from shadowing player
            *dodge(6.d6),
            EndAction,
            EndTurn
        )
        activePlayer.assertCoordinates(14, 5)
        assertEquals(homeTeam, state.activeTeam)
    }

    @Test
    fun onlyOnePlayerCanUseShadowing() {
        val shadowingPlayer = homeTeam[PlayerNo(2)]
        val movingPlayer = awayTeam[PlayerNo(1)]

        shadowingPlayer.addSkill(SkillType.SHADOWING)

        controller.rollForward(
            *activatePlayer(movingPlayer, PlayerStandardActionType.MOVE),
            *moveTo(14, 5), // Move away from both shadowing players
            *dodge(6.d6),
        )
        assertEquals(2, controller.getAvailableActions().get<SelectPlayer>().players.size)
        controller.rollForward(
            PlayerSelected(shadowingPlayer),
            4.d6, // Shadowing roll
            EndAction,
            EndTurn
        )
        assertEquals("H2".playerId, state.pitch[13, 5].player?.id)
        movingPlayer.assertCoordinates(14, 5)
        assertEquals(homeTeam, state.activeTeam)
    }

    @Test
    fun failShadowingOn3OrLess()  {
        val shadowingPlayer = homeTeam[1.playerNo]
        val activePlayer = awayTeam[1.playerNo]
        controller.rollForward(
            *activatePlayer(activePlayer, PlayerStandardActionType.MOVE),
            *moveTo(14, 5),
            *dodge(6.d6),
            PlayerSelected(shadowingPlayer),
            3.d6, // Shadowing
            EndAction
        )
        shadowingPlayer.assertCoordinates(12, 5)
        activePlayer.assertCoordinates(14, 5)
        assertEquals(awayTeam, state.activeTeam)
    }

    @Test
    fun successfulShadowingMovesShadowingPlayer() {
        val shadowingPlayer = homeTeam[1.playerNo]
        val activePlayer = awayTeam[1.playerNo]
        controller.rollForward(
            *activatePlayer(activePlayer, PlayerStandardActionType.MOVE),
            *moveTo(14, 5),
            *dodge(6.d6),
            PlayerSelected(shadowingPlayer),
            4.d6, // Shadowing
            EndAction
        )
        shadowingPlayer.assertCoordinates(13, 5)
        activePlayer.assertCoordinates(14, 5)
        assertEquals(awayTeam, state.activeTeam)
    }

    @Test
    fun canOnlyShadowUpToMATimes() {
        val shadowingPlayer = homeTeam[1.playerNo]
        val activePlayer = awayTeam[1.playerNo]
        controller.rollForward(
            *activatePlayer(activePlayer, PlayerStandardActionType.MOVE),
            *moveTo(14, 5),
            *dodge(6.d6),
            PlayerSelected(shadowingPlayer),
            4.d6, // 1st Shadowing
            *moveTo(15, 5),
            *dodge(6.d6), // The shadowing player has no more move and cannot follow
            EndAction
        )
        shadowingPlayer.assertCoordinates(13, 5)
        activePlayer.assertCoordinates(15, 5)
        assertEquals(awayTeam, state.activeTeam)
    }
}
