package com.jervisffb.test.bb2025.skills

import com.jervisffb.engine.actions.Confirm
import com.jervisffb.engine.actions.FieldSquareSelected
import com.jervisffb.engine.actions.NoRerollSelected
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.rules.bb2025.skills.CloudBurster
import com.jervisffb.engine.rules.common.actions.PlayerStandardActionType
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.test.JervisGameBB2025Test
import com.jervisffb.test.SmartMoveTo
import com.jervisffb.test.activatePlayer
import com.jervisffb.test.catch
import com.jervisffb.test.ext.rollForward
import com.jervisffb.test.moveTo
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Class testing usage of the [CloudBurster] skill.
 */
class CloudBursterTests: JervisGameBB2025Test() {

    @BeforeTest
    override fun setUp() {
        super.setUp()
        startDefaultGame()
    }

    @Test
    fun preventIntercept() {
        awayTeam["A10".playerId].addSkill(SkillType.CLOUD_BURSTER)
        controller.rollForward(
            *activatePlayer("A10", PlayerStandardActionType.PASS),
            *moveTo(17, 7),
            4.d6, // Pickup
            NoRerollSelected(),
            SmartMoveTo(12, 3),
            Confirm, // Start pass
            FieldSquareSelected(13, 9),
            6.d6, // Pass
            NoRerollSelected(),
            *catch(6.d6), // Go directly to Catch as Cloud Burster remove all, otherwise, eligible interceptors
        )
        assertTrue(awayTeam["A5".playerId].hasBall())
    }
}
