package com.jervisffb.test.bb2025.skills

import com.jervisffb.engine.actions.Confirm
import com.jervisffb.engine.actions.PassTypeSelected
import com.jervisffb.engine.actions.PitchSquareSelected
import com.jervisffb.engine.commands.SetBallLocation
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.model.PitchSquare
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.rules.bb2025.skills.NervesOfSteel
import com.jervisffb.engine.rules.common.actions.PassType
import com.jervisffb.engine.rules.common.actions.PlayerStandardActionType
import com.jervisffb.engine.rules.common.procedures.actions.pass.PassContext
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.test.JervisGameBB2025Test
import com.jervisffb.test.SmartMoveTo
import com.jervisffb.test.activatePlayer
import com.jervisffb.test.catch
import com.jervisffb.test.dodge
import com.jervisffb.test.ext.rollForward
import com.jervisffb.test.moveTo
import com.jervisffb.test.pickup
import com.jervisffb.test.throwBall
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Class testing usage of the [NervesOfSteel] skill.
 */
class NervesOfSteelTests: JervisGameBB2025Test() {

    @BeforeTest
    override fun setUp() {
        super.setUp()
        startDefaultGame()
    }

    @Test
    fun worksOnCatch() {
        awayTeam["A1".playerId].addSkill(SkillType.NERVES_OF_STEEL)
        controller.rollForward(
            *activatePlayer("A7", PlayerStandardActionType.PASS),
            SmartMoveTo(17, 7),
            *pickup(6.d6),
            PassTypeSelected(PassType.STANDARD),
            PitchSquareSelected(13, 5),
            *throwBall(6.d6),
            Confirm, // Use Nerves of Steel
            *catch(3.d6) // Only works if Nerves of Steel cancels all tackle zones (2).
        )
        assertTrue(awayTeam["A1".playerId].hasBall())
    }


    @Test
    fun worksOnPass() {
        SetBallLocation(state.singleBall(), PitchSquare(13, 4)).execute(state) // one TZ
        awayTeam["A1".playerId].addSkill(SkillType.NERVES_OF_STEEL)
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.PASS),
            *moveTo(13, 4),
            *dodge(6.d6),
            *pickup(6.d6),
            PassTypeSelected(PassType.STANDARD),
            PitchSquareSelected(14, 1),
            Confirm, // Use Nerves of Steel
        )
        assertTrue(state.getContext<PassContext>().passingModifiers.none { it.description == "Marked" })
        controller.rollForward(
            *throwBall(4.d6), // Should be enough when using Nerves of Steel
            *catch(6.d6),
        )

        assertTrue(awayTeam["A6".playerId].hasBall())
    }
}
