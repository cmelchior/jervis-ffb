package com.jervisffb.test

import com.jervisffb.engine.actions.Cancel
import com.jervisffb.engine.actions.Confirm
import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.actions.DirectionSelected
import com.jervisffb.engine.ext.d16
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.ext.dblock
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.model.Direction
import com.jervisffb.engine.model.PlayerState
import com.jervisffb.engine.model.locations.DogOut
import com.jervisffb.engine.rules.common.actions.PlayerStandardActionType
import com.jervisffb.test.ext.rollForward
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Class testing the behavior of the Apothecary.
 */
class ApothecaryTests: JervisGameTest() {

    @BeforeTest
    override fun setUp() {
        super.setUp()
        startDefaultGame()
    }

    @Test
    fun chooseOriginalBadlyHurtAfterReroll() {
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.BLOCK),
            *standardBlock("H1", 6.dblock),
            DirectionSelected(Direction.LEFT),
            Cancel, // Do not follow up
            DiceRollResults(6.d6, 6.d6), // Armor
            DiceRollResults(4.d6, 6.d6), // Casualty
            DiceRollResults(6.d16), // Badly Hurt
            Confirm,  // Use Apothecary
            DiceRollResults(1.d16), // Badly Hurt
            Cancel,  // Use first roll
        )
        assertEquals(PlayerState.RESERVE, state.getPlayerById("H1".playerId).state)
        assertEquals(DogOut, state.getPlayerById("H1".playerId).location)
    }
}
