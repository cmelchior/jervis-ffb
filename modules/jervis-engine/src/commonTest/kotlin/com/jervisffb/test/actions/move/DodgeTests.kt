package com.jervisffb.test.actions.move

import com.jervisffb.test.JervisGameTest
import kotlin.test.BeforeTest
import kotlin.test.Test

/**
 * Test a player dodging as described on page 45 in the BB2020 Rulebook.
 *
 * Note, any skills that affect dodges are testing in their own test class.
 * This class only tests the basic functionality.
 */
class DodgeTests: JervisGameTest() {

    @BeforeTest
    override fun setUp() {
        super.setUp()

    }



    @Test
    fun noDodge_whenMovingAwayFromNonMarkingPlayer() {

    }

    @Test
    fun dodge_whenMovingAwayFromMarkingPlayer() {


    }

    @Test
    fun dodgeRoll_fail_knockedOverInTargetSquare() {

    }

    @Test
    fun dodgeRoll_fail_knockedOverInTarget() {

    }

}
