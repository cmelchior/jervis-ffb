package com.jervisffb.test.bb2025.skills

import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.rules.bb2025.skills.CloudBurster
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.test.JervisGameBB2025Test
import kotlin.test.BeforeTest
import kotlin.test.Test

/**
 * Class testing usage of the [CloudBurster] skill.
 */
class CloudBursterTests: JervisGameBB2025Test() {

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
    fun preventIntercept() {

    }
}
