package com.jervisffb.test.bb2025.skills

import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.actions.EndAction
import com.jervisffb.engine.actions.EndTurn
import com.jervisffb.engine.actions.FieldSquareSelected
import com.jervisffb.engine.actions.MoveType
import com.jervisffb.engine.actions.MoveTypeSelected
import com.jervisffb.engine.actions.NoRerollSelected
import com.jervisffb.engine.actions.PlayerActionSelected
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.model.PlayerState
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.rules.bb2025.skills.Titchy
import com.jervisffb.engine.rules.common.actions.PlayerStandardActionType
import com.jervisffb.engine.rules.common.skills.RegularTeamReroll
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.test.JervisGameBB2025Test
import com.jervisffb.test.ext.rollForward
import com.jervisffb.test.utils.SelectTeamReroll
import com.jervisffb.test.utils.assertCoordinates
import com.jervisffb.test.utils.assertStanding
import com.jervisffb.test.utils.hasSkill
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Class testing usage of the [Titchy] skill
 */
class TitchyTests: JervisGameBB2025Test() {

    @BeforeTest
    override fun setUp() {
        super.setUp()
        state.apply {
            // Should be on LoS
            awayTeam["A1".playerId].apply {
                addSkill(SkillType.TITCHY.id())
                strength = 1
                agility = 3
            }
            awayTeam["A5".playerId].apply {
                addSkill(SkillType.TITCHY.id())
                strength = 1
                agility = 4
            }
        }
        startDefaultGame()
    }

    @Test
    fun useTitchyOnDodge() {
        // Titchy is mandatory, so is applied automtically
        val player = state.getPlayerById("A1".playerId)
        controller.rollForward(
            PlayerSelected(player),
            PlayerActionSelected(PlayerStandardActionType.MOVE),
            MoveTypeSelected(MoveType.STANDARD),
            FieldSquareSelected(FieldCoordinate(12, 4)),
            DiceRollResults(2.d6), // Not good enough, even with Titchy
            SelectTeamReroll<RegularTeamReroll>(),
            DiceRollResults(3.d6), // Good enough, but only with Titchy
            EndAction
        )
        player.assertCoordinates(12, 4)
        player.assertStanding()
    }

    @Test
    fun dodgeIntoSquareMarkedByTitchy() {
        val player = state.getPlayerById("H5".playerId)
        controller.rollForward(
            EndTurn,
            PlayerSelected(player),
            PlayerActionSelected(PlayerStandardActionType.MOVE),
            MoveTypeSelected(MoveType.STANDARD),
            FieldSquareSelected(FieldCoordinate(13, 10)),
            DiceRollResults(3.d6), // Only good enough if square is using Titchy
            NoRerollSelected(),
            EndAction
        )
        player.assertCoordinates(13, 10)
        player.assertStanding()
    }

    @Test
    fun leavingTitchyPlayerRequiresDodge() {
        // Titchy players are still marking players, so leaving them requires a dodge
        val player = state.getPlayerById("H5".playerId)
        state.getPlayerById("A4".playerId).state = PlayerState.PRONE
        val markingPlayers = rules.getMarkingPlayers(state, homeTeam, player.coordinates)
        assertEquals(1, markingPlayers.size)
        assertTrue(markingPlayers.first().hasSkill<Titchy>())
        controller.rollForward(
            EndTurn,
            PlayerSelected(player),
            PlayerActionSelected(PlayerStandardActionType.MOVE),
            MoveTypeSelected(MoveType.STANDARD),
            FieldSquareSelected(FieldCoordinate(13, 10)),
            DiceRollResults(6.d6),
            NoRerollSelected(),
            EndAction
        )
        player.assertCoordinates(13, 10)
        player.assertStanding()
    }
}
