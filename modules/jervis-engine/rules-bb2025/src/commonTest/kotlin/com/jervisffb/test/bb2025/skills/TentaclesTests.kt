package com.jervisffb.test.bb2025.skills

import com.jervisffb.engine.actions.Cancel
import com.jervisffb.engine.actions.Confirm
import com.jervisffb.engine.actions.DirectionSelected
import com.jervisffb.engine.actions.EndAction
import com.jervisffb.engine.actions.EndTurn
import com.jervisffb.engine.actions.PitchSquareSelected
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.ext.dblock
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.model.Direction
import com.jervisffb.engine.rules.bb2025.skills.Tentacles
import com.jervisffb.engine.rules.common.actions.PlayerStandardActionType
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.test.JervisGameBB2025Test
import com.jervisffb.test.activatePlayer
import com.jervisffb.test.dodge
import com.jervisffb.test.ext.rollForward
import com.jervisffb.test.jumpTo
import com.jervisffb.test.leapTo
import com.jervisffb.test.moveTo
import com.jervisffb.test.pogoRoll
import com.jervisffb.test.pogoTo
import com.jervisffb.test.shadowPlayer
import com.jervisffb.test.standardBlock
import com.jervisffb.test.tentaclePlayer
import com.jervisffb.test.utils.assertActive
import com.jervisffb.test.utils.assertCoordinates
import com.jervisffb.test.utils.assertNoActivePlayer
import com.jervisffb.test.utils.assertStanding
import com.jervisffb.test.utils.makeDistracted
import com.jervisffb.test.utils.putProne
import kotlin.test.BeforeTest
import kotlin.test.Test

/**
 * Class testing usage of the [Tentacles] skill.
 */
class TentaclesTests: JervisGameBB2025Test() {

    @BeforeTest
    override fun setUp() {
        super.setUp()
        startDefaultGame()
    }

    @Test
    fun workOnDodge() {
        val tentacles = homeTeam["H1".playerId].apply {
            addSkill(SkillType.TENTACLES)
        }
        val mover = awayTeam["A1".playerId]
        controller.rollForward(
            *activatePlayer(mover, PlayerStandardActionType.MOVE),
            *moveTo(14, 5),
            *tentaclePlayer(tentacles, 6.d6),
        )
        state.assertNoActivePlayer()
        awayTeam.assertActive()
        mover.assertStanding()
        mover.assertCoordinates(13, 5)
    }

    @Test
    fun workOnJump() {
        homeTeam["H2".playerId].putProne()
        val tentacles = homeTeam["H1".playerId].apply {
            addSkill(SkillType.TENTACLES)
        }
        val mover = awayTeam["A1".playerId]
        controller.rollForward(
            *activatePlayer(mover, PlayerStandardActionType.MOVE),
            *jumpTo(11, 6),
            *tentaclePlayer(tentacles, 6.d6),
        )
        state.assertNoActivePlayer()
        awayTeam.assertActive()
        mover.assertStanding()
        mover.assertCoordinates(13, 5)
    }

    @Test
    fun workOnLeap() {
        val tentacles = homeTeam["H1".playerId].apply {
            addSkill(SkillType.TENTACLES)
        }
        val mover = awayTeam["A1".playerId].apply {
            addSkill(SkillType.LEAP)
        }
        controller.rollForward(
            *activatePlayer(mover, PlayerStandardActionType.MOVE),
            *leapTo(15, 5),
            *tentaclePlayer(tentacles, 6.d6),
        )
        state.assertNoActivePlayer()
        awayTeam.assertActive()
        mover.assertStanding()
        mover.assertCoordinates(13, 5)
    }


    @Test
    fun onlyOnePlayerCanUseTentacles() {
        val tentacles1 = homeTeam["H1".playerId].apply {
            addSkill(SkillType.TENTACLES)
        }
        val tentacles2 = homeTeam["H2".playerId].apply {
            addSkill(SkillType.TENTACLES)
        }
        val mover = awayTeam["A1".playerId]
        controller.rollForward(
            *activatePlayer(mover, PlayerStandardActionType.MOVE),
            *moveTo(14, 5),
            *tentaclePlayer(tentacles2, 6.d6),
        )
        state.assertNoActivePlayer()
        awayTeam.assertActive()
        mover.assertStanding()
        mover.assertCoordinates(13, 5)
    }

    @Test
    fun moveIfTentaclesAreNotUsed() {
        homeTeam["H1".playerId].apply {
            addSkill(SkillType.TENTACLES)
        }
        val mover = awayTeam["A1".playerId]
        controller.rollForward(
            *activatePlayer(mover, PlayerStandardActionType.MOVE),
            *moveTo(14, 5),
            Cancel,
            *dodge(6.d6),
            EndAction,
        )
        state.assertNoActivePlayer()
        awayTeam.assertActive()
        mover.assertStanding()
        mover.assertCoordinates(14, 5)
    }

    @Test
    fun doesNotWorkOnPogo() {
        homeTeam["H1".playerId].apply {
            addSkill(SkillType.TENTACLES)
        }
        val mover = awayTeam["A1".playerId].apply {
            addSkill(SkillType.POGO_STICK)
        }
        controller.rollForward(
            *activatePlayer(mover, PlayerStandardActionType.MOVE),
            *pogoTo(15, 5),
            *pogoRoll(6.d6),
            EndAction
        )
        state.assertNoActivePlayer()
        awayTeam.assertActive()
        mover.assertStanding()
        mover.assertCoordinates(15, 5)
    }

    @Test
    fun doesNotWorkOnShadowing() {
        homeTeam["H1".playerId].apply {
            addSkill(SkillType.TENTACLES)
        }
        val mover = homeTeam["H2".playerId]
        val shadower = awayTeam["A1".playerId].apply {
            addSkill(SkillType.SHADOWING)
        }
        controller.rollForward(
            EndTurn,
            *activatePlayer(mover, PlayerStandardActionType.MOVE),
            *moveTo(11, 6),
            *dodge(5.d6),
            *shadowPlayer(shadower, 6.d6),
            EndAction
        )
        state.assertNoActivePlayer()
        homeTeam.assertActive()
        mover.assertStanding()
        mover.assertCoordinates(11, 6)
        shadower.assertStanding()
        shadower.assertCoordinates(12, 6)
    }

    @Test
    fun doesNotWorkOnHitAndRun() {
        val attacker = state.getPlayerById("A1".playerId)
        attacker.addSkill(SkillType.HIT_AND_RUN)
        val defender = homeTeam["H1".playerId]
        homeTeam["H2".playerId].addSkill(SkillType.TENTACLES)
        controller.rollForward(
            *activatePlayer(attacker, PlayerStandardActionType.BLOCK),
            *standardBlock(defender, 3.dblock),
            DirectionSelected(Direction.LEFT),
            Cancel, // Do not follow up
            Confirm, // Use Hit and Run
            PitchSquareSelected(14, 6)
        )
        state.assertNoActivePlayer()
        attacker.assertCoordinates(14, 6)
    }

    @Test
    fun doesNotWorkWhenDistracted() {
        homeTeam["H1".playerId].apply {
            addSkill(SkillType.TENTACLES)
            makeDistracted()
        }
        val mover = awayTeam["A1".playerId]
        controller.rollForward(
            *activatePlayer(mover, PlayerStandardActionType.MOVE),
            *moveTo(14, 5),
            *dodge(6.d6),
            EndAction,
        )
        state.assertNoActivePlayer()
        awayTeam.assertActive()
        mover.assertStanding()
        mover.assertCoordinates(14, 5)
    }

    @Test
    fun need6PlusToSucceed() {
        val tentacles = homeTeam["H1".playerId].apply {
            addSkill(SkillType.TENTACLES)
            strength = 5
        }
        val mover = awayTeam["A1".playerId]
        controller.rollForward(
            *activatePlayer(mover, PlayerStandardActionType.MOVE),
            *moveTo(14, 5),
            *tentaclePlayer(tentacles, 4.d6),
        )
        state.assertNoActivePlayer()
        awayTeam.assertActive()
        mover.assertStanding()
        mover.assertCoordinates(13, 5)
    }

    @Test
    fun natural6IsSuccess() {
        val tentacles = homeTeam["H1".playerId].apply {
            addSkill(SkillType.TENTACLES)
            strength = 1
        }
        val mover = awayTeam["A1".playerId].apply {
            strength = 9
        }
        controller.rollForward(
            *activatePlayer(mover, PlayerStandardActionType.MOVE),
            *moveTo(14, 5),
            *tentaclePlayer(tentacles, 6.d6),
        )
        state.assertNoActivePlayer()
        awayTeam.assertActive()
        mover.assertStanding()
        mover.assertCoordinates(13, 5)
    }

    @Test
    fun lessThan6IsFailure() {
        val tentacles = homeTeam["H1".playerId].apply {
            addSkill(SkillType.TENTACLES)
        }
        val mover = awayTeam["A1".playerId]
        controller.rollForward(
            *activatePlayer(mover, PlayerStandardActionType.MOVE),
            *moveTo(14, 5),
            *tentaclePlayer(tentacles, 2.d6),
            *dodge(6.d6),
            EndAction,
        )
        state.assertNoActivePlayer()
        awayTeam.assertActive()
        mover.assertStanding()
        mover.assertCoordinates(14, 5)
    }
}
