package com.jervisffb.test.bb2025.skills

import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.actions.DirectionSelected
import com.jervisffb.engine.actions.EndAction
import com.jervisffb.engine.actions.NoRerollSelected
import com.jervisffb.engine.actions.PassTypeSelected
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.actions.SelectPlayerAction
import com.jervisffb.engine.actions.SelectRerollOption
import com.jervisffb.engine.ext.d3
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.ext.d8
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.model.Availability
import com.jervisffb.engine.model.Direction
import com.jervisffb.engine.model.locations.PitchCoordinate
import com.jervisffb.engine.rules.bb2025.skills.Kick
import com.jervisffb.engine.rules.bb2025.skills.Punt
import com.jervisffb.engine.rules.common.actions.PassType
import com.jervisffb.engine.rules.common.actions.PlayerSpecialActionType
import com.jervisffb.engine.rules.common.actions.PlayerStandardActionType
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.test.JervisGameBB2025Test
import com.jervisffb.test.activatePlayer
import com.jervisffb.test.bounce
import com.jervisffb.test.catch
import com.jervisffb.test.ext.rollForward
import com.jervisffb.test.giveBallToPlayer
import com.jervisffb.test.movePlayerTo
import com.jervisffb.test.puntDirection
import com.jervisffb.test.puntDistance
import com.jervisffb.test.throwIn
import com.jervisffb.test.utils.SelectSkillReroll
import com.jervisffb.test.utils.assertActive
import com.jervisffb.test.utils.assertCoordinates
import com.jervisffb.test.utils.assertNoActivePlayer
import com.jervisffb.test.utils.assertNoTurnOver
import com.jervisffb.test.utils.putProne
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Class testing usage of the [Punt] skill
 */
class PuntTests : JervisGameBB2025Test() {

    @BeforeTest
    override fun setUp() {
        super.setUp()
        startDefaultGame()
    }

    @Test
    fun puntIsAvailableAsSpecialAction() {
        val punter = awayTeam["A10".playerId]
        punter.addSkill(SkillType.PUNT)
        giveBallToPlayer(punter)

        controller.rollForward(PlayerSelected(punter.id))
        val playerActions = controller.getAvailableActions().get<SelectPlayerAction>().actions
        assertTrue(playerActions.any { it.type == PlayerSpecialActionType.PUNT })
    }

    @Test
    fun puntIsAvailableWhenNotStartingWithBall() {
        val player = awayTeam["A10".playerId]
        player.addSkill(SkillType.PUNT)

        controller.rollForward(PlayerSelected(player.id))
        val playerActions = controller.getAvailableActions().get<SelectPlayerAction>().actions
        assertTrue(playerActions.any { it.type == PlayerSpecialActionType.PUNT })
    }

    @Test
    fun puntIsNotAvailableWithoutSkill() {
        val player = awayTeam["A10".playerId]
        controller.rollForward(PlayerSelected(player.id))
        val playerActions = controller.getAvailableActions().get<SelectPlayerAction>().actions
        assertFalse(playerActions.any { it.type == PlayerSpecialActionType.PUNT })
    }

    @Test
    fun onlyOnePuntPerTeamTurn() {
        val punter1 = awayTeam["A10".playerId]
        punter1.addSkill(SkillType.PUNT)
        giveBallToPlayer(punter1)

        val punter2 = awayTeam["A3".playerId]
        punter2.addSkill(SkillType.PUNT)

        controller.rollForward(
            *activatePlayer(punter1, PlayerSpecialActionType.PUNT),
            PassTypeSelected(PassType.PUNT),
            DirectionSelected(Direction.LEFT),
            *puntDirection(2.d3),
            *puntDistance(3.d6),
            *catch(6.d6),
            PlayerSelected(punter2),
        )

        assertTrue(punter2.hasBall())
        val actions = controller.getAvailableActions().get<SelectPlayerAction>().actions
        assertFalse(actions.any { it.type == PlayerSpecialActionType.PUNT })
    }

    @Test
    fun landsOnEmptySquare() {
        val punter = awayTeam["A10".playerId]
        punter.addSkill(SkillType.PUNT)
        giveBallToPlayer(punter)

        controller.rollForward(
            *activatePlayer(punter, PlayerSpecialActionType.PUNT),
            PassTypeSelected(PassType.PUNT),
            DirectionSelected(Direction.RIGHT),
            *puntDirection(2.d3),
            *puntDistance(2.d6),
            bounce(5.d8)
        )
        state.assertNoActivePlayer()
        state.assertNoTurnOver()
        assertEquals(Availability.HAS_ACTIVATED, punter.available)
        state.singleBall().assertCoordinates(19, 7)
    }

    @Test
    fun goesOutOfBounds() {
        val punter = awayTeam["A6".playerId]
        punter.addSkill(SkillType.PUNT)
        giveBallToPlayer(punter)

        controller.rollForward(
            *activatePlayer(punter, PlayerSpecialActionType.PUNT),
            PassTypeSelected(PassType.PUNT),
            DirectionSelected(Direction.UP),
            *puntDirection(2.d3),
            *puntDistance(6.d6),
            throwIn(2.d3),
            DiceRollResults(2.d6, 2.d6),
            bounce(1.d8),
        )
        state.assertNoActivePlayer()
        homeTeam.assertActive()
        state.singleBall().assertCoordinates(13, 2)
    }

    @Test
    fun caughtByOppositionCausesTurnover() {
        val punter = awayTeam["A10".playerId]
        punter.addSkill(SkillType.PUNT)
        giveBallToPlayer(punter)

        val catcher = homeTeam["H10".playerId]
        catcher.addSkill(SkillType.CATCH)
        assertEquals(3, catcher.agility)
        controller.rollForward(
            *activatePlayer(punter, PlayerSpecialActionType.PUNT),
            PassTypeSelected(PassType.PUNT),
            DirectionSelected(Direction.LEFT),
            *puntDirection(2.d3),
            *puntDistance(6.d6), // Punt to (10,7)
            bounce(4.d8),
            3.d6,
            SelectSkillReroll(SkillType.CATCH),
            4.d6
        )
        state.assertNoActivePlayer()
        homeTeam.assertActive()
        assertTrue(catcher.hasBall())
    }

    // It is still a turnover if the Punt ends up in the crowd at any stage while resolving it.
    @Test
    fun turnoverIfThrownIntoCrowdAsSecondaryEffect() {
        // Punt ball to opponent, who fails to catch it.
        // It bounces out-of-bounds and gets thrown in to
        // a player on the punting team who  catches it
        val punter = awayTeam["A1".playerId]
        movePlayerTo(punter, PitchCoordinate(14, 1))
        punter.addSkill(SkillType.PUNT)
        giveBallToPlayer(punter)
        val homeCatcher = homeTeam["H10".playerId]
        movePlayerTo(homeCatcher, PitchCoordinate(13, 0))
        val awayCatcher = awayTeam["A2".playerId]

        controller.rollForward(
            *activatePlayer(punter, PlayerSpecialActionType.PUNT),
            PassTypeSelected(PassType.PUNT),
            DirectionSelected(Direction.LEFT),
            *puntDirection(3.d3),
            *puntDistance(1.d6),
            *catch(1.d6, reroll = null),
            bounce(3.d8),
            throwIn(2.d3),
            DiceRollResults(3.d6, 4.d6),
            *catch(6.d6),
        )
        assertTrue(awayCatcher.hasBall())
        state.assertNoActivePlayer()
        homeTeam.assertActive()
    }

    @Test
    fun rerollDirectionWithKick() {
        val punter = awayTeam["A10".playerId].apply {
            addSkill(SkillType.PUNT)
            addSkill(SkillType.KICK)
        }
        giveBallToPlayer(punter)

        controller.rollForward(
            *activatePlayer(punter, PlayerSpecialActionType.PUNT),
            PassTypeSelected(PassType.PUNT),
            DirectionSelected(Direction.RIGHT),
            1.d3
        )

        var actions = controller.getAvailableActions().get<SelectRerollOption>().options
        assertTrue(actions.any { state.getRerollSourceById(it.rerollId) is Kick })

        controller.rollForward(
            SelectSkillReroll(SkillType.KICK),
            2.d3,
            1.d6,
        )

        // Cannot use Kick to reroll distance if used to reroll Kick
        actions = controller.getAvailableActions().get<SelectRerollOption>().options
        assertFalse(actions.any { state.getRerollSourceById(it.rerollId) is Kick })

        controller.rollForward(
            NoRerollSelected(),
            2.d8,
        )

        awayTeam.assertActive()
        state.assertNoActivePlayer()
        state.singleBall().assertCoordinates(17, 6)
    }

    @Test
    fun rerollDistanceWithKick() {
        val punter = awayTeam["A10".playerId].apply {
            addSkill(SkillType.PUNT)
            addSkill(SkillType.KICK)
        }
        giveBallToPlayer(punter)

        controller.rollForward(
            *activatePlayer(punter, PlayerSpecialActionType.PUNT),
            PassTypeSelected(PassType.PUNT),
            DirectionSelected(Direction.RIGHT),
            *puntDirection(1.d3),
            *puntDistance(1.d6, SelectSkillReroll(SkillType.KICK)),
            2.d6,
            bounce(2.d8)
        )

        awayTeam.assertActive()
        state.assertNoActivePlayer()
        state.singleBall().assertCoordinates(18, 4)
    }

    @Test
    fun puntFromProne() {
        val punter = awayTeam["A10".playerId].apply {
            addSkill(SkillType.PUNT)
            putProne()
        }

        controller.rollForward(
            PlayerSelected(punter)
        )
        val actions = controller.getAvailableActions().get<SelectPlayerAction>().actions
        assertTrue(actions.any { it.type == PlayerSpecialActionType.PUNT })
    }

    @Test
    fun canPuntAndPassInTheSameTurn() {
        val punter = awayTeam["A10".playerId]
        punter.addSkill(SkillType.PUNT)
        giveBallToPlayer(punter)
        val thrower = awayTeam["A9".playerId]

        controller.rollForward(
            *activatePlayer(punter, PlayerSpecialActionType.PUNT),
            PassTypeSelected(PassType.PUNT),
            DirectionSelected(Direction.RIGHT),
            *puntDirection(2.d3),
            *puntDistance(2.d6),
            bounce(5.d8),
            *activatePlayer(thrower, PlayerStandardActionType.PASS),
            EndAction
        )
        state.assertNoActivePlayer()
    }
}
