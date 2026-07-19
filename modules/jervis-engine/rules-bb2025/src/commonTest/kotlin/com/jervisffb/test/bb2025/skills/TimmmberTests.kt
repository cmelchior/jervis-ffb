package com.jervisffb.test.bb2025.skills

import com.jervisffb.engine.actions.EndAction
import com.jervisffb.engine.actions.MoveType
import com.jervisffb.engine.actions.MoveTypeSelected
import com.jervisffb.engine.actions.PlayerActionSelected
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.commands.SetPlayerLocation
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.locations.PitchCoordinate
import com.jervisffb.engine.rules.bb2025.skills.Timmmber
import com.jervisffb.engine.rules.common.actions.PlayerStandardActionType
import com.jervisffb.engine.rules.common.procedures.actions.move.StandingUpRollContext
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.engine.utils.sum
import com.jervisffb.test.JervisGameBB2025Test
import com.jervisffb.test.ext.rollForward
import com.jervisffb.test.standingUpRoll
import com.jervisffb.test.utils.assertProne
import com.jervisffb.test.utils.assertStanding
import com.jervisffb.test.utils.makeDistracted
import com.jervisffb.test.utils.putProne
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Class testing usage of the [Timmmber] skill.
 */
class TimmmberTests: JervisGameBB2025Test() {

    @BeforeTest
    override fun setUp() {
        super.setUp()
        startDefaultGame()
    }

    @Test
    fun noEffectIfMA3OrMore() {
        val player = state.getPlayerById("A10".playerId)
        player.apply {
            addSkill(SkillType.TIMMMBER)
            putProne()
        }
        controller.rollForward(
            PlayerSelected(player.id),
            PlayerActionSelected(PlayerStandardActionType.MOVE),
            MoveTypeSelected(MoveType.STAND_UP),
            EndAction
        )
        player.assertStanding()
    }

    @Test
    fun noModifiersIfNoHelpers() {
        val player = state.getPlayerById("A10".playerId)
        player.apply {
            addSkill(SkillType.TIMMMBER)
            move = 2
            movesLeft = 2
            putProne()
        }
        controller.rollForward(
            PlayerSelected(player.id),
            PlayerActionSelected(PlayerStandardActionType.MOVE),
            MoveTypeSelected(MoveType.STAND_UP),
            *standingUpRoll(3.d6)
        )
        assertNull(state.activePlayer)
        player.assertProne()
    }

    @Test
    fun openPlayersAddModifiers() {
        val player = awayTeam["A6".playerId]
        player.apply {
            addSkill(SkillType.TIMMMBER)
            move = 2
            movesLeft = 2
            putProne()
        }
        controller.rollForward(
            PlayerSelected(player.id),
            PlayerActionSelected(PlayerStandardActionType.MOVE),
            MoveTypeSelected(MoveType.STAND_UP),
            *standingUpRoll(3.d6)
        )
        assertEquals(player, state.activePlayer)
        player.assertStanding()
    }

    @Test
    fun distractedPlayersCanHelp() {
        val player = awayTeam["A6".playerId]
        player.apply {
            addSkill(SkillType.TIMMMBER)
            move = 2
            movesLeft = 2
            putProne()
        }
        awayTeam["A7".playerId].makeDistracted()
        controller.rollForward(
            PlayerSelected(player.id),
            PlayerActionSelected(PlayerStandardActionType.MOVE),
            MoveTypeSelected(MoveType.STAND_UP),
            *standingUpRoll(3.d6)
        )
        assertEquals(player, state.activePlayer)
        player.assertStanding()
    }

    @Test
    fun markedPlayersCannotHelp() {
        val player = awayTeam["A6".playerId]
        player.apply {
            addSkill(SkillType.TIMMMBER)
            move = 2
            movesLeft = 2
            putProne()
        }
        SetPlayerLocation(homeTeam["H1".playerId], PitchCoordinate(16, 1)).execute(state)
        controller.rollForward(
            PlayerSelected(player.id),
            PlayerActionSelected(PlayerStandardActionType.MOVE),
            MoveTypeSelected(MoveType.STAND_UP),
            *standingUpRoll(3.d6)
        )
        assertNull(state.activePlayer)
        player.assertProne()
    }

    @Test
    fun naturalOneFails() {
        listOf("H1", "H2", "H3", "H4").forEach {
            homeTeam[it.playerId].putProne()
        }
        val player = awayTeam["A2".playerId]
        player.apply {
            addSkill(SkillType.TIMMMBER)
            move = 2
            movesLeft = 2
            putProne()
        }
        // Move a player to help A2. We now have 3 assists
        SetPlayerLocation(awayTeam["A6".playerId], PitchCoordinate(14, 6)).execute(state)
        controller.rollForward(
            PlayerSelected(player.id),
            PlayerActionSelected(PlayerStandardActionType.MOVE),
            MoveTypeSelected(MoveType.STAND_UP),
        )
        val context = state.getContext<StandingUpRollContext>()
        assertEquals(3, context.modifiers.sum())
        controller.rollForward(
            *standingUpRoll(1.d6)
        )
        assertNull(state.activePlayer)
        player.assertProne()
    }
}
