package com.jervisffb.tables

import com.jervisffb.JervisGameTest
import com.jervisffb.engine.actions.Confirm
import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.actions.FieldSquareSelected
import com.jervisffb.engine.actions.MoveType
import com.jervisffb.engine.actions.MoveTypeSelected
import com.jervisffb.engine.actions.PlayerActionSelected
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.actions.RandomPlayersSelected
import com.jervisffb.activatePlayer
import com.jervisffb.engine.controller.GameController
import com.jervisffb.defaultFanFactor
import com.jervisffb.defaultInducements
import com.jervisffb.defaultJourneyMen
import com.jervisffb.defaultKickOffHomeTeam
import com.jervisffb.defaultPregame
import com.jervisffb.defaultSetup
import com.jervisffb.defaultWeather
import com.jervisffb.engine.ext.d16
import com.jervisffb.engine.ext.d3
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.ext.playerNo
import com.jervisffb.engine.model.PlayerState
import com.jervisffb.engine.model.context.RushRollContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.hasSkill
import com.jervisffb.engine.model.modifiers.RushModifier
import com.jervisffb.engine.rules.bb2020.procedures.DetermineKickingTeam
import com.jervisffb.engine.rules.bb2020.procedures.FullGame
import com.jervisffb.engine.rules.bb2020.procedures.PrayersToNuffleRollContext
import com.jervisffb.engine.rules.bb2020.procedures.SetupTeam
import com.jervisffb.engine.rules.bb2020.procedures.actions.move.RushRoll
import com.jervisffb.engine.rules.PlayerStandardActionType
import com.jervisffb.engine.rules.bb2020.skills.Duration
import com.jervisffb.engine.rules.bb2020.skills.Loner
import com.jervisffb.engine.rules.bb2020.skills.MightyBlow
import com.jervisffb.engine.rules.bb2020.skills.Pro
import com.jervisffb.engine.rules.bb2020.skills.Stab
import com.jervisffb.engine.rules.bb2020.tables.PrayerStatModifier
import com.jervisffb.engine.rules.bb2020.tables.PrayerToNuffle
import com.jervisffb.skipTurns
import com.jervisffb.engine.utils.createDefaultGameState
import kotlin.test.BeforeTest
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.fail

/**
 * This class is testing all the results on the Prayer to Nuffle Table
 */
class PrayersToNuffleTests: JervisGameTest() {

    @BeforeTest
    override fun setUp() {
        super.setUp()
        // Trigger one roll on the Prayers to Nuffle table as a default
        // Some tests might overwrite this
        homeTeam.teamValue = 1_050_000
        awayTeam.teamValue = 1_000_000
    }


    @Test
    fun numberOfPrayers() {
        val tests: List<Triple<Int, Int, Int>> = listOf(
            Triple(1_000_000,  1_049_000, 0),
            Triple(1_000_000, 1_050_000, 1),
            Triple(1_000_000, 1_099_000, 1)
        )
        tests.forEach { (homeTv, awayTv, rolls) ->
            val state = createDefaultGameState(rules)
            state.homeTeam.teamValue = homeTv
            state.awayTeam.teamValue = awayTv
            val controller = GameController(rules, state)
            controller.startTestMode(FullGame)
            controller.rollForward(
                *defaultFanFactor(),
                defaultWeather(),
                *defaultJourneyMen(),
                *defaultInducements()
            )
            when(rolls) {
                0 -> assertEquals(DetermineKickingTeam.SelectCoinSide, controller.currentProcedure()!!.currentNode())
                1 -> {
                    val context = state.getContext<PrayersToNuffleRollContext>()
                    assertEquals(1, context.rollsRemaining)
                    assertEquals(state.homeTeam, context.team)
                }
                else -> fail("Unsupported value: rolls")
            }
        }
    }

    @Test
    fun rerollPrayerIfAlreadyActive() {
        // Trigger two rolls on Prayers to Nuffle
        homeTeam.teamValue = 1_100_000
        awayTeam.teamValue = 1_000_000
        controller.startTestMode(FullGame)
        controller.rollForward(
            *defaultPregame(
                prayersToNuffle = arrayOf(
                    1.d16, // First roll
                    1.d16, // Second roll
                    2.d16 // Reroll
                )
            ),
        )
        assertEquals(2, awayTeam.activePrayersToNuffle.size)
        assertTrue(awayTeam.hasPrayer(PrayerToNuffle.TREACHEROUS_TRAPDOOR))
        assertTrue(awayTeam.hasPrayer(PrayerToNuffle.FRIENDS_WITH_THE_REF))
        assertEquals(0, homeTeam.activePrayersToNuffle.size)
    }

    @Test
    @Ignore
    fun treacherousTrapdoor() {
        TODO("Trap doors not implemented yet")
    }

    @Test
    fun friendsWithTheRef() {
        controller.startTestMode(FullGame)
        controller.rollForward(
            *defaultPregame(
                prayersToNuffle = arrayOf(
                    2.d16, // Roll Friends with the Ref
                )
            ),
            *defaultSetup(),
            *defaultKickOffHomeTeam()
        )

        // Put player on home team on the ground so they can be fouled
        homeTeam[1.playerNo]!!.state = PlayerState.PRONE
        assertTrue(awayTeam.hasPrayer(PrayerToNuffle.FRIENDS_WITH_THE_REF))

        // Foul player and roll 5 to trigger the prayer
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.FOUL),
            PlayerSelected("H1".playerId), // Select H1 as the target of the action
            PlayerSelected("H1".playerId), // Foul H1 since he is next to A1
            DiceRollResults(1.d6, 1.d6), // Armour roll = Caught by ref
            Confirm, // Argue the call
            5.d6, // Argue the call roll
            Confirm, // Accept using Friends with the Ref
        )
        assertTrue(state.getPlayerById("A1".playerId)!!.location.isOnField(rules))
        assertEquals(PlayerState.STANDING, state.getPlayerById("A1".playerId)!!.state)

        // Check the prayer is gone by the end of drive
        controller.rollForward(
            *skipTurns(15) // Will also end the half
        )
        assertFalse(awayTeam.hasPrayer(PrayerToNuffle.FRIENDS_WITH_THE_REF))
    }

    @Test
    fun stiletto() {
        controller.startTestMode(FullGame)
        controller.rollForward(
            *defaultPregame(
                prayersToNuffle = arrayOf(
                    3.d16, // Roll Stiletto
                    PlayerSelected("A1".playerId), // Give to A1
                )
            ),
            *defaultSetup(),
            *defaultKickOffHomeTeam()
        )

        assertTrue(awayTeam.hasPrayer(PrayerToNuffle.STILETTO))
        val player = state.getPlayerById("A1".playerId)
        assertTrue(player.hasSkill<Stab>())
        val stabSkill = player.getSkill<Stab>()
        assertTrue(stabSkill.isTemporary)
        assertEquals(Duration.END_OF_DRIVE, stabSkill.expiresAt)

        // Goes away after the drive
        controller.rollForward(
            *skipTurns(16) // Will also end the half
        )

        assertFalse(player.hasSkill<Stab>())
        assertFalse(awayTeam.hasPrayer(PrayerToNuffle.STILETTO))
    }

    @Test
    fun stiletto_asKickOffEvent() {
        homeTeam.teamValue = 1_000_000
        controller.startTestMode(FullGame)
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            *defaultKickOffHomeTeam(
                kickoffEvent = arrayOf(
                    DiceRollResults(3.d6, 3.d6), // Cheering Fans
                    DiceRollResults(6.d6), // Cheering Fans Roll - Home
                    DiceRollResults(1.d6), // Cheering Fans Roll - Away
                    DiceRollResults(3.d16),
                ),
                bounce = null
            )
        )
        assertFalse(awayTeam.hasPrayer(PrayerToNuffle.STILETTO))
        assertFalse(homeTeam.hasPrayer(PrayerToNuffle.STILETTO))

        controller.rollForward(PlayerSelected("H1".playerId))
        assertTrue(homeTeam[1.playerNo].hasSkill<Stab>())
        assertTrue(homeTeam[1.playerNo].getSkill<Stab>().isTemporary)
        assertFalse(awayTeam.hasPrayer(PrayerToNuffle.STILETTO))
        assertTrue(homeTeam.hasPrayer(PrayerToNuffle.STILETTO))
    }

    @Test
    fun stiletto_notAvailableToSomePlayers() {
        awayTeam.forEachIndexed { i, it ->
            when (i) {
                0 -> it.state = PlayerState.KNOCKED_OUT
                1 -> it.addSkill(Stab())
                else -> it.addSkill(Loner(2))
            }
        }
        controller.startTestMode(FullGame)
        controller.rollForward(
            *defaultPregame(
                prayersToNuffle = arrayOf(
                    3.d16, // Roll Stiletto. Will be ignored
                )
            ),
        )

        // Team is marked as having the prayer, even if no one could actually get it
        assertTrue(awayTeam.hasPrayer(PrayerToNuffle.STILETTO))
        assertEquals(1, awayTeam.filter{ it.hasSkill<Stab>() }.size)
    }

    @Test
    fun ironMan() {
        controller.startTestMode(FullGame)
        controller.rollForward(
            *defaultPregame(
                prayersToNuffle = arrayOf(
                    4.d16, // Roll Iron Man
                    PlayerSelected("A1".playerId), // Give to A1
                )
            ),
            *defaultSetup(),
            *defaultKickOffHomeTeam()
        )

        assertTrue(awayTeam.hasPrayer(PrayerToNuffle.IRON_MAN))
        val player = state.getPlayerById("A1".playerId)
        assertTrue(player.armourModifiers.contains(PrayerStatModifier.IRON_MAN))
        assertEquals(10, player.armorValue)
    }

    @Test
    fun ironMan_asKickOffEvent() {
        homeTeam.teamValue = 1_000_000
        controller.startTestMode(FullGame)
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            *defaultKickOffHomeTeam(
                kickoffEvent = arrayOf(
                    DiceRollResults(3.d6, 3.d6), // Cheering Fans
                    DiceRollResults(6.d6), // Cheering Fans Roll - Home
                    DiceRollResults(1.d6), // Cheering Fans Roll - Away
                    DiceRollResults(4.d16),
                ),
                bounce = null
            )
        )
        assertFalse(awayTeam.hasPrayer(PrayerToNuffle.IRON_MAN))
        assertFalse(homeTeam.hasPrayer(PrayerToNuffle.IRON_MAN))

        controller.rollForward(PlayerSelected("H1".playerId))
        assertTrue(homeTeam[1.playerNo].armourModifiers.contains(PrayerStatModifier.IRON_MAN))
        assertFalse(awayTeam.hasPrayer(PrayerToNuffle.IRON_MAN))
        assertTrue(homeTeam.hasPrayer(PrayerToNuffle.IRON_MAN))
    }

    @Test
    fun ironMan_notAvailableToSomePlayers() {
        awayTeam.forEachIndexed { i, it ->
            when (i) {
                // Should it not be available to players that already have AV11?
                0 -> it.state = PlayerState.KNOCKED_OUT
                else -> it.addSkill(Loner(2))
            }
        }
        controller.startTestMode(FullGame)
        controller.rollForward(
            *defaultPregame(
                prayersToNuffle = arrayOf(
                    4.d16, // Roll Iron Man. Will be ignored
                )
            ),
        )

        // Team is marked as having the prayer, even if no one could actually get it
        assertEquals(SetupTeam, controller.currentProcedure()!!.procedure)
        assertTrue(awayTeam.hasPrayer(PrayerToNuffle.IRON_MAN))
        assertEquals(0, awayTeam.filter { it.getStatModifiers().contains(PrayerStatModifier.IRON_MAN)}.size)
    }

    @Test
    fun ironMan_onAV11() {
        val player = state.getPlayerById("A1".playerId)
        player.baseArmorValue = 11
        controller.startTestMode(FullGame)
        controller.rollForward(
            *defaultPregame(
                prayersToNuffle = arrayOf(
                    4.d16, // Roll Iron Man.
                    PlayerSelected("A1".playerId), // Give it to A1
                )
            ),
        )
        assertTrue(awayTeam.hasPrayer(PrayerToNuffle.IRON_MAN))
        assertTrue(player.armourModifiers.contains(PrayerStatModifier.IRON_MAN))
        assertEquals(11, player.armorValue)
    }

    @Test
    fun knuckleDusters() {
        controller.startTestMode(FullGame)
        controller.rollForward(
            *defaultPregame(
                prayersToNuffle = arrayOf(
                    5.d16, // Roll Knuckle Dusters
                    PlayerSelected("A1".playerId), // Give to A1
                )
            ),
            *defaultSetup(),
            *defaultKickOffHomeTeam()
        )

        assertTrue(awayTeam.hasPrayer(PrayerToNuffle.KNUCKLE_DUSTERS))
        val player = state.getPlayerById("A1".playerId)
        assertTrue(player.hasSkill<MightyBlow>())
        val mightyBlowSkill = player.getSkill<MightyBlow>()
        assertTrue(mightyBlowSkill.isTemporary)
        assertEquals(Duration.END_OF_DRIVE, mightyBlowSkill.expiresAt)
        assertEquals(1, mightyBlowSkill.value)

        // Will be removed after the drive
        controller.rollForward(
            *skipTurns(16) // Will also end the half
        )
        assertFalse(player.hasSkill<MightyBlow>())
        assertFalse(awayTeam.hasPrayer(PrayerToNuffle.KNUCKLE_DUSTERS))
    }

    @Test
    fun knuckleDusters_asKickOffEvent() {
        homeTeam.teamValue = 1_000_000
        controller.startTestMode(FullGame)
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            *defaultKickOffHomeTeam(
                kickoffEvent = arrayOf(
                    DiceRollResults(3.d6, 3.d6), // Cheering Fans
                    DiceRollResults(6.d6), // Cheering Fans Roll - Home
                    DiceRollResults(1.d6), // Cheering Fans Roll - Away
                    DiceRollResults(5.d16),
                ),
                bounce = null
            )
        )
        assertFalse(awayTeam.hasPrayer(PrayerToNuffle.GREASY_CLEATS))
        assertFalse(homeTeam.hasPrayer(PrayerToNuffle.GREASY_CLEATS))

        controller.rollForward(PlayerSelected("H1".playerId))
        assertTrue(homeTeam[1.playerNo].hasSkill<MightyBlow>())
        assertTrue(homeTeam[1.playerNo].getSkill<MightyBlow>().isTemporary)
        assertFalse(awayTeam.hasPrayer(PrayerToNuffle.KNUCKLE_DUSTERS))
        assertTrue(homeTeam.hasPrayer(PrayerToNuffle.KNUCKLE_DUSTERS))
    }

    @Test
    fun knuckleDusters_notAvailableToSomePlayers() {
        awayTeam.forEachIndexed { i, it ->
            when (i) {
                // Should it not be available to players that already have AV11?
                0 -> it.state = PlayerState.KNOCKED_OUT
                1 -> it.addSkill(MightyBlow(2))
                else -> it.addSkill(Loner(2))
            }
        }
        controller.startTestMode(FullGame)
        controller.rollForward(
            *defaultPregame(
                prayersToNuffle = arrayOf(
                    5.d16, // Roll Knuckle Dusters. Will be ignored
                )
            ),
        )

        // Team is marked as having the prayer, even if no one could actually get it
        assertEquals(SetupTeam, controller.currentProcedure()!!.procedure)
        assertEquals(1, awayTeam.filter{ it.hasSkill<MightyBlow>() }.size)
        assertTrue(awayTeam.hasPrayer(PrayerToNuffle.KNUCKLE_DUSTERS))
    }

    @Test
    fun badHabits() {
        controller.startTestMode(FullGame)
        controller.rollForward(
            *defaultPregame(
                prayersToNuffle = arrayOf(
                    6.d16, // Roll Bad Habits.
                    2.d3, // Number of players affected
                    RandomPlayersSelected(listOf("H1".playerId, "H2".playerId)),
                )
            ),
            *defaultSetup(),
            *defaultKickOffHomeTeam()
        )

        assertTrue(awayTeam.hasPrayer(PrayerToNuffle.BAD_HABITS))
        assertEquals(2, homeTeam.count { it.hasSkill<Loner>() && it.getSkill<Loner>().value == 2 })

        // Prayer and effects will be removed after the drive
        controller.rollForward(
            *skipTurns(16) // Will also end the half
        )
        assertFalse(awayTeam.hasPrayer(PrayerToNuffle.BAD_HABITS))
        assertEquals(0, homeTeam.count { it.hasSkill<Loner>() && it.getSkill<Loner>().value == 2 })
    }

    @Test
    fun badHabits_asKickOffEvent() {
        homeTeam.teamValue = 1_000_000
        controller.startTestMode(FullGame)
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            *defaultKickOffHomeTeam(
                kickoffEvent = arrayOf(
                    DiceRollResults(3.d6, 3.d6), // Cheering Fans
                    DiceRollResults(6.d6), // Cheering Fans Roll - Home
                    DiceRollResults(1.d6), // Cheering Fans Roll - Away
                    DiceRollResults(6.d16),
                ),
                bounce = null
            )
        )
        assertFalse(awayTeam.hasPrayer(PrayerToNuffle.BAD_HABITS))
        assertFalse(homeTeam.hasPrayer(PrayerToNuffle.BAD_HABITS))

        controller.rollForward(
            2.d3, // Number of players affected
            RandomPlayersSelected(listOf("A1".playerId, "A2".playerId)),
        )
        assertTrue(awayTeam[1.playerNo].hasSkill<Loner>())
        assertTrue(awayTeam[1.playerNo].getSkill<Loner>().isTemporary)
        assertTrue(awayTeam[2.playerNo].hasSkill<Loner>())
        assertTrue(awayTeam[2.playerNo].getSkill<Loner>().isTemporary)
        assertFalse(awayTeam.hasPrayer(PrayerToNuffle.BAD_HABITS))
        assertTrue(homeTeam.hasPrayer(PrayerToNuffle.BAD_HABITS))
    }

    @Test
    fun badHabits_notAvailableToSomePlayers() {
        // Give everyone except 1 loner, so when you roll 3 on the prayer
        // Only 1 can be selected
        homeTeam.forEachIndexed { i, it ->
            if (i > 0) it.addSkill(Loner(4))
        }
        controller.startTestMode(FullGame)
        controller.rollForward(
            *defaultPregame(
                prayersToNuffle = arrayOf(
                    6.d16, // Roll Bad Habits.
                    3.d3, // Number of players affected
                    RandomPlayersSelected(listOf("H1".playerId)),
                )
            ),
            *defaultSetup(),
            *defaultKickOffHomeTeam()
        )

        assertTrue(awayTeam.hasPrayer(PrayerToNuffle.BAD_HABITS))
        assertEquals(1, homeTeam.count { it.hasSkill<Loner>() && it.getSkill<Loner>().value == 2 })
    }

    @Test
    fun badHabits_notAvailableToAnyPlayers() {
        homeTeam.forEachIndexed { i, it ->
            when (i) {
                0 -> it.state = PlayerState.KNOCKED_OUT
                else -> it.addSkill(Loner(4))
            }
        }
        controller.startTestMode(FullGame)
        controller.rollForward(
            *defaultPregame(
                prayersToNuffle = arrayOf(
                    6.d16, // Roll Bad Habits.
                    3.d3, // Number of players affected
                )
            ),
        )

        assertTrue(awayTeam.hasPrayer(PrayerToNuffle.BAD_HABITS))
        assertEquals(0, homeTeam.count { it.hasSkill<Loner>() && it.getSkill<Loner>().value == 2 })
    }

    @Test
    fun greasyCleats() {
        controller.startTestMode(FullGame)
        controller.rollForward(
            *defaultPregame(
                prayersToNuffle = arrayOf(
                    7.d16, // Roll Greasy Cleats.
                    PlayerSelected("H1".playerId), // Select H1 as target
                )
            ),
            *defaultSetup(),
            *defaultKickOffHomeTeam()
        )

        assertTrue(awayTeam.hasPrayer(PrayerToNuffle.GREASY_CLEATS))
        assertTrue(homeTeam[1.playerNo]!!.moveModifiers.contains(PrayerStatModifier.GREASY_CLEATS))

        // Prayer and effects will be removed after the drive
        controller.rollForward(
            *skipTurns(16) // Will also end the half
        )
        assertFalse(awayTeam.hasPrayer(PrayerToNuffle.GREASY_CLEATS))
        assertFalse(homeTeam[1.playerNo]!!.moveModifiers.contains(PrayerStatModifier.GREASY_CLEATS))
    }

    @Test
    fun greasyCleats_asKickOffEvent() {
        homeTeam.teamValue = 1_000_000
        controller.startTestMode(FullGame)
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            *defaultKickOffHomeTeam(
                kickoffEvent = arrayOf(
                    DiceRollResults(3.d6, 3.d6), // Cheering Fans
                    DiceRollResults(6.d6), // Cheering Fans Roll - Home
                    DiceRollResults(1.d6), // Cheering Fans Roll - Away
                    DiceRollResults(7.d16),
                ),
                bounce = null
            )
        )
        assertFalse(awayTeam.hasPrayer(PrayerToNuffle.GREASY_CLEATS))
        assertFalse(homeTeam.hasPrayer(PrayerToNuffle.GREASY_CLEATS))

        controller.rollForward(PlayerSelected("A1".playerId))
        assertTrue(awayTeam[1.playerNo].moveModifiers.contains(PrayerStatModifier.GREASY_CLEATS))
        assertFalse(awayTeam.hasPrayer(PrayerToNuffle.GREASY_CLEATS))
        assertTrue(homeTeam.hasPrayer(PrayerToNuffle.GREASY_CLEATS))
    }

    @Test
    fun greasyCleats_noPlayersAvailable() {
        homeTeam.forEachIndexed { i, it ->
            it.state = PlayerState.KNOCKED_OUT
        }
        controller.startTestMode(FullGame)
        controller.rollForward(
            *defaultPregame(
                prayersToNuffle = arrayOf(
                    7.d16, // Roll Greasy Cleats. Will be ignored
                )
            ),
        )
        assertTrue(awayTeam.hasPrayer(PrayerToNuffle.GREASY_CLEATS))
        assertEquals(0, homeTeam.count { it.getStatModifiers().contains(PrayerStatModifier.GREASY_CLEATS) })
    }

    @Test
    fun blessedStatueOfNuffle() {
        controller.startTestMode(FullGame)
        controller.rollForward(
            *defaultPregame(
                prayersToNuffle = arrayOf(
                    8.d16, // Roll Blessed Statue.
                    PlayerSelected("A1".playerId), // Select A1 as target
                )
            ),
            *defaultSetup(),
            *defaultKickOffHomeTeam()
        )
        assertTrue(awayTeam.hasPrayer(PrayerToNuffle.BLESSED_STATUE_OF_NUFFLE))
        assertTrue(awayTeam[1.playerNo].hasSkill<Pro>())
    }

    @Test
    fun blessedStatueOfNuffle_asKickOffEvent() {
        homeTeam.teamValue = 1_000_000
        controller.startTestMode(FullGame)
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            *defaultKickOffHomeTeam(
                kickoffEvent = arrayOf(
                    DiceRollResults(3.d6, 3.d6), // Cheering Fans
                    DiceRollResults(6.d6), // Cheering Fans Roll - Home
                    DiceRollResults(1.d6), // Cheering Fans Roll - Away
                    DiceRollResults(8.d16),
                ),
                bounce = null
            )
        )
        assertFalse(awayTeam.hasPrayer(PrayerToNuffle.BLESSED_STATUE_OF_NUFFLE))
        assertFalse(homeTeam.hasPrayer(PrayerToNuffle.BLESSED_STATUE_OF_NUFFLE))

        controller.rollForward(PlayerSelected("H1".playerId))
        assertTrue(homeTeam[1.playerNo].hasSkill<Pro>())
        assertTrue(homeTeam[1.playerNo].getSkill<Pro>().isTemporary)
        assertFalse(awayTeam.hasPrayer(PrayerToNuffle.BLESSED_STATUE_OF_NUFFLE))
        assertTrue(homeTeam.hasPrayer(PrayerToNuffle.BLESSED_STATUE_OF_NUFFLE))
    }

    @Test
    fun blessedStatueOfNuffle_noValidPlayers() {
        awayTeam.forEachIndexed { i, player ->
            when (i) {
                0 -> player.state = PlayerState.KNOCKED_OUT
                1 -> player.addSkill(Pro(isTemporary = false))
                else -> player.addSkill(Loner(2))
            }
        }
        controller.startTestMode(FullGame)
        controller.rollForward(
            *defaultPregame(
                prayersToNuffle = arrayOf(
                    8.d16, // Roll BlessedStatue. Will be ignored
                )
            ),
        )
        assertTrue(awayTeam.hasPrayer(PrayerToNuffle.BLESSED_STATUE_OF_NUFFLE))
        assertEquals(0, awayTeam.count { it.hasSkill<Pro>() && it.getSkill<Pro>().isTemporary })
    }

    @Test
    fun molesUnderThePitch() {
        controller.startTestMode(FullGame)
        controller.rollForward(
            *defaultPregame(
                prayersToNuffle = arrayOf(
                    9.d16, // Roll Moles Under the Pitch.
                )
            ),
            *defaultSetup(),
            *defaultKickOffHomeTeam()
        )
        assertTrue(awayTeam.hasPrayer(PrayerToNuffle.MOLES_UNDER_THE_PITCH))
        assertFalse(homeTeam.hasPrayer(PrayerToNuffle.MOLES_UNDER_THE_PITCH))

        // Prayer and effects will be removed after the half
        controller.rollForward(
            *skipTurns(16)
        )
        assertFalse(awayTeam.hasPrayer(PrayerToNuffle.MOLES_UNDER_THE_PITCH))
        assertFalse(homeTeam.hasPrayer(PrayerToNuffle.MOLES_UNDER_THE_PITCH))
    }

    @Test
    fun molesUnderThePitch_affectRushing() {
        controller.startTestMode(FullGame)
        controller.rollForward(
            *defaultPregame(
                prayersToNuffle = arrayOf(
                    9.d16, // Roll Moles Under the Pitch.
                )
            ),
            *defaultSetup(),
            *defaultKickOffHomeTeam()
        )
        assertTrue(awayTeam.hasPrayer(PrayerToNuffle.MOLES_UNDER_THE_PITCH))
        assertFalse(homeTeam.hasPrayer(PrayerToNuffle.MOLES_UNDER_THE_PITCH))

        // Reduce movement so we trigger rushing straight away
        awayTeam[6.playerNo].movesLeft = 0
        controller.rollForward(
            PlayerSelected("A6".playerId),
            PlayerActionSelected(PlayerStandardActionType.MOVE),
            MoveTypeSelected(MoveType.STANDARD),
            FieldSquareSelected(13, 1), // Requires Rush
            2.d6, // Should fail due to Moles Under The Pitch
        )
        assertEquals(RushRoll.ChooseReRollSource, controller.currentNode())
        val context = state.getContext<RushRollContext>()
        assertFalse(context.isSuccess)
        assertTrue(context.modifiers.contains(RushModifier.MOLES_UNDER_THE_PITCH_AWAY))
    }

    @Test
    fun molesUnderThePitch_canAffectRushingTwice() {
        controller.startTestMode(FullGame)
        controller.rollForward(
            *defaultPregame(
                prayersToNuffle = arrayOf(
                    9.d16, // Roll Moles Under the Pitch.
                )
            ),
            *defaultSetup(),
            *defaultKickOffHomeTeam(
                kickoffEvent = arrayOf(
                    DiceRollResults(3.d6, 3.d6), // Cheering fans
                    6.d6, // Brilliant coaching, Home
                    1.d6, // Brilliant coaching, Away
                    9.d16, // Home Team rolls Moles Under The Pitch
                )
            )
        )
        assertTrue(awayTeam.hasPrayer(PrayerToNuffle.MOLES_UNDER_THE_PITCH))
        assertTrue(homeTeam.hasPrayer(PrayerToNuffle.MOLES_UNDER_THE_PITCH))

        // Reduce movement so we trigger rushing straight away
        awayTeam[6.playerNo].movesLeft = 0
        controller.rollForward(
            PlayerSelected("A6".playerId),
            PlayerActionSelected(PlayerStandardActionType.MOVE),
            MoveTypeSelected(MoveType.STANDARD),
            FieldSquareSelected(13, 1), // Requires Rush
            3.d6, // Should fail due to 2xMoles Under The Pitch
        )
        assertEquals(RushRoll.ChooseReRollSource, controller.currentNode())
        val context = state.getContext<RushRollContext>()
        assertFalse(context.isSuccess)
        assertTrue(context.modifiers.contains(RushModifier.MOLES_UNDER_THE_PITCH_AWAY))
        assertTrue(context.modifiers.contains(RushModifier.MOLES_UNDER_THE_PITCH_HOME))
    }

    @Test
    @Ignore
    fun perfectPassing() {
        TODO()
    }

    @Test
    @Ignore
    fun fanInteraction() {
        TODO()
    }

    @Test
    @Ignore
    fun necessaryViolence() {
        TODO()
    }

    @Test
    @Ignore
    fun foulingFrenzy() {
        TODO()
    }

    @Test
    @Ignore
    fun throwRock_hit() {
        TODO("Stalling not implemented yet")
    }

    @Test
    @Ignore
    fun throwRock_misses() {
        TODO("Stalling not implemented yet")
    }

    @Test
    @Ignore
    fun throwRock_noStallingPlayers() {
        TODO("Stalling not implemented yet")
    }

    @Test
    @Ignore
    fun underScrutiny() {
        TODO()
    }

    @Test
    @Ignore
    fun intensiveTraining() {
        TODO()
    }
}
