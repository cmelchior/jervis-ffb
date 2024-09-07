package dk.ilios.jervis.tables

import dk.ilios.jervis.JervisGameTest
import dk.ilios.jervis.actions.Confirm
import dk.ilios.jervis.actions.DiceResults
import dk.ilios.jervis.actions.PlayerSelected
import dk.ilios.jervis.actions.RandomPlayersSelected
import dk.ilios.jervis.activatePlayer
import dk.ilios.jervis.controller.GameController
import dk.ilios.jervis.defaultFanFactor
import dk.ilios.jervis.defaultInducements
import dk.ilios.jervis.defaultJourneyMen
import dk.ilios.jervis.defaultKickOffHomeTeam
import dk.ilios.jervis.defaultPregame
import dk.ilios.jervis.defaultSetup
import dk.ilios.jervis.defaultWeather
import dk.ilios.jervis.skipTurns
import dk.ilios.jervis.ext.d16
import dk.ilios.jervis.ext.d3
import dk.ilios.jervis.ext.d6
import dk.ilios.jervis.ext.playerId
import dk.ilios.jervis.ext.playerNo
import dk.ilios.jervis.model.PlayerState
import dk.ilios.jervis.model.context.getContext
import dk.ilios.jervis.model.hasSkill
import dk.ilios.jervis.procedures.DetermineKickingTeam
import dk.ilios.jervis.procedures.FullGame
import dk.ilios.jervis.procedures.PrayersToNuffleRollContext
import dk.ilios.jervis.procedures.SetupTeam
import dk.ilios.jervis.rules.PlayerActionType
import dk.ilios.jervis.rules.skills.Duration
import dk.ilios.jervis.rules.skills.Loner
import dk.ilios.jervis.rules.skills.MightyBlow
import dk.ilios.jervis.rules.skills.Pro
import dk.ilios.jervis.rules.skills.Stab
import dk.ilios.jervis.rules.tables.PrayerStatModifier
import dk.ilios.jervis.rules.tables.PrayerToNuffle
import dk.ilios.jervis.utils.createDefaultGameState
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
            *activatePlayer("A1", PlayerActionType.FOUL),
            PlayerSelected("H1".playerId), // Select H1 as the target of the action
            PlayerSelected("H1".playerId), // Foul H1 since he is next to A1
            DiceResults(1.d6, 1.d6), // Armour roll = Caught by ref
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
            *defaultSetup(),
            *defaultKickOffHomeTeam()
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
            *defaultSetup(),
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
        assertTrue(awayTeam[1.playerNo]!!.hasSkill<Pro>())
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
            *defaultSetup(),
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
