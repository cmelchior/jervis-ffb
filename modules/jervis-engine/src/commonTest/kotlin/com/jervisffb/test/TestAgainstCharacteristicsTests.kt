package com.jervisffb.test

import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.ext.playerNo
import com.jervisffb.engine.model.modifiers.DiceModifier
import com.jervisffb.engine.rules.bb2020.testAgainstAgility
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Class responsible for testing characteristic tests as described on
 * page 29 in the rulebook.
 */
class TestAgainstCharacteristicsTests: JervisGameTest() {

    class DummyDiceModifier(
        override val modifier: Int
    ) : DiceModifier {
        override val description: String = "Dummy"
    }

    @Test
    fun agility1AlwaysFails() {
        val player = homeTeam[1.playerNo]
        assertEquals(3, player.agility)
        assertFalse(testAgainstAgility(player, 1.d6, emptyList()))
        assertFalse(testAgainstAgility(player, 1.d6, listOf(DummyDiceModifier(6))))
    }

    @Test
    fun agility6AlwaysSucceeds() {
        val player = homeTeam[1.playerNo]
        assertEquals(3, player.agility)
        assertTrue(testAgainstAgility(player, 6.d6, emptyList()))
        assertTrue(testAgainstAgility(player, 6.d6, listOf(DummyDiceModifier(-6))))
    }

    @Test
    fun agilityModifiers() {
        val player = homeTeam[1.playerNo]
        assertEquals(3, player.agility)
        assertTrue(testAgainstAgility(player, 2.d6, listOf(DummyDiceModifier(1))))
        assertFalse(testAgainstAgility(player, 2.d6, listOf(DummyDiceModifier(-1))))
        assertTrue(testAgainstAgility(player, 3.d6, listOf(DummyDiceModifier(1))))
        assertFalse(testAgainstAgility(player, 3.d6, listOf(DummyDiceModifier(-1))))
        assertTrue(testAgainstAgility(player, 4.d6, listOf(DummyDiceModifier(1))))
        assertTrue(testAgainstAgility(player, 4.d6, listOf(DummyDiceModifier(-1))))
        assertFalse(testAgainstAgility(player, 4.d6, listOf(DummyDiceModifier(-2))))
        assertTrue(testAgainstAgility(player, 5.d6, listOf(DummyDiceModifier(1))))
        assertTrue(testAgainstAgility(player, 5.d6, listOf(DummyDiceModifier(-2))))
        assertFalse(testAgainstAgility(player, 5.d6, listOf(DummyDiceModifier(-3))))
    }
}
