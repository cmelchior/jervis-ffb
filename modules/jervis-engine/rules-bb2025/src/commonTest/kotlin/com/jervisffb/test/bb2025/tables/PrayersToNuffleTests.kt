package com.jervisffb.test.bb2025.tables

import com.jervisffb.engine.actions.Cancel
import com.jervisffb.engine.actions.Confirm
import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.actions.EndTurn
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.InducementSelection
import com.jervisffb.engine.actions.InducementsSelected
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.actions.RandomPlayersSelected
import com.jervisffb.engine.actions.SelectPlayer
import com.jervisffb.engine.actions.SelectRandomPlayers
import com.jervisffb.engine.actions.SkillSelected
import com.jervisffb.engine.bb2025.inducements.InducementType2025
import com.jervisffb.engine.bb2025.procedures.actions.move.RushRoll
import com.jervisffb.engine.bb2025.skills.Loner
import com.jervisffb.engine.bb2025.skills.MightyBlow
import com.jervisffb.engine.bb2025.skills.Stab
import com.jervisffb.engine.bb2025.tables.PrayerToNuffleTableResult2025
import com.jervisffb.engine.common.inducements.InducementSelectionCommon
import com.jervisffb.engine.common.modifiers.RushModifier
import com.jervisffb.engine.common.procedures.DetermineKickingTeamStep
import com.jervisffb.engine.ext.d16
import com.jervisffb.engine.ext.d3
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.ext.playerNo
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.PlayerDogoutState
import com.jervisffb.engine.model.PlayerPitchState
import com.jervisffb.engine.model.PlayerType
import com.jervisffb.engine.model.context.RushRollContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.modifiers.TeamFeature
import com.jervisffb.engine.model.modifiers.TeamFeatureType
import com.jervisffb.engine.rules.common.actions.PlayerStandardActionType
import com.jervisffb.engine.rules.common.skills.Duration
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.engine.rules.common.tables.GreasyCleatsStatModifier
import com.jervisffb.engine.rules.common.tables.IronManStatModifier
import com.jervisffb.engine.utils.containsInstance
import com.jervisffb.teams.THE_BLACK_GOBBO
import com.jervisffb.test.JervisGameBB2025Test
import com.jervisffb.test.activatePlayer
import com.jervisffb.test.defaultDetermineKickingTeam
import com.jervisffb.test.defaultFanFactor
import com.jervisffb.test.defaultKickOffHomeTeam
import com.jervisffb.test.defaultSetup
import com.jervisffb.test.defaultWeather
import com.jervisffb.test.ext.rollForward
import com.jervisffb.test.moveTo
import com.jervisffb.test.skipTurns
import com.jervisffb.test.utils.assertStanding
import com.jervisffb.test.utils.hasSkill
import com.jervisffb.test.utils.putProne
import kotlin.test.BeforeTest
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * This class is testing all the results on the Prayer to Nuffle Table.
 */
class PrayersToNuffleTests: JervisGameBB2025Test() {

    @BeforeTest
    override fun setUp() {
        super.setUp()
        homeTeam.currentTeamValue = 1_050_000
        awayTeam.currentTeamValue = 1_000_000
    }

    // This assumes that it is the away team buying inducements
    private fun buyInducements(vararg inducements: InducementSelection<*>): Array<GameAction> {
        return buildList {
            addAll(defaultFanFactor())
            add(defaultWeather())
            add(InducementsSelected(inducements.toList()))
        }.toTypedArray()
    }

    private fun startGameAfterInducements(): Array<GameAction> {
        return buildList {
            addAll(defaultDetermineKickingTeam())
            addAll(defaultSetup())
            addAll(defaultKickOffHomeTeam().filterNotNull())
        }.toTypedArray()
    }

    private fun createStarPlayer(): Player {
        return Player(
            rules,
            id = "away-starplayer".playerId,
            position = THE_BLACK_GOBBO,
            type = PlayerType.STAR_PLAYER
        )
    }

    @Test
    fun rerollPrayerIfAlreadyActive() {
        controller.rollForward(
            *buyInducements(
                InducementSelectionCommon.Simple(InducementType2025.PRAYERS_TO_NUFFLE, 2)
            ),
            1.d16, // First roll
            1.d16, // Second roll
            2.d16 // Rerolla
        )
        assertEquals(2, awayTeam.activePrayersToNuffle.size)
        assertTrue(awayTeam.hasPrayer(PrayerToNuffleTableResult2025.TREACHEROUS_TRAPDOOR))
        assertTrue(awayTeam.hasPrayer(PrayerToNuffleTableResult2025.FRIENDS_WITH_THE_REF))
        assertEquals(0, homeTeam.activePrayersToNuffle.size)
    }

    @Test
    @Ignore
    fun treacherousTrapdoor() {
        TODO("Trap doors not implemented yet")
    }

    @Test
    fun friendsWithTheRef() {
        controller.rollForward(
            *buyInducements(
                InducementSelectionCommon.Simple(InducementType2025.PRAYERS_TO_NUFFLE, 1)
            ),
            2.d16, // Roll Friends with the Ref
            *startGameAfterInducements(),
        )

        // Put player on home team on the ground so they can be fouled
        homeTeam[1.playerNo].state = PlayerPitchState.PRONE
        assertTrue(awayTeam.hasPrayer(PrayerToNuffleTableResult2025.FRIENDS_WITH_THE_REF))

        // Foul player and roll 5 to trigger the prayer
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.FOUL),
            PlayerSelected("H1".playerId), // Foul H1 since he is next to A1
            DiceRollResults(1.d6, 1.d6), // Armour roll = Caught by ref
            Confirm, // Argue the call
            5.d6, // Argue the call roll
            Confirm, // Accept using Friends with the Ref
        )
        assertTrue(state.getPlayerById("A1".playerId).location.isOnPitch(rules))
        state.getPlayerById("A1".playerId).assertStanding()

        // Check the prayer stays after the end of the drive (unlike BB2020)
        controller.rollForward(
            *skipTurns(15) // Will also end the half
        )
        assertTrue(awayTeam.hasPrayer(PrayerToNuffleTableResult2025.FRIENDS_WITH_THE_REF))
    }

    @Test
    fun stiletto() {
        awayTeam.forEachIndexed { i, it ->
            when (i) {
                1 -> it.addSkill(SkillType.LONER.idTarget(2)) // Players will Loner can get it, unlike BB2020
                13 -> it.state = PlayerDogoutState.KNOCKED_OUT // Selectable
            }
        }

        controller.rollForward(
            *buyInducements(
                InducementSelectionCommon.Simple(InducementType2025.PRAYERS_TO_NUFFLE, 1)
            ),
            3.d16, // Roll Stiletto
        )

        val availablePlayers = controller.getAvailableActions().get<SelectPlayer>().players
        assertEquals(awayTeam.size, availablePlayers.size)

        controller.rollForward(
            PlayerSelected("A1".playerId), // Give to A1
            *startGameAfterInducements(),
        )

        assertTrue(awayTeam.hasPrayer(PrayerToNuffleTableResult2025.STILETTO))
        val player = state.getPlayerById("A1".playerId)
        assertTrue(player.hasSkill<Stab>())
        val stabSkill = player.getSkill(SkillType.STAB)
        assertTrue(stabSkill.isTemporary)
        assertEquals(Duration.END_OF_GAME, stabSkill.expiresAt)

        // Does not go away after the drive (unlike in BB2020)
        controller.rollForward(
            *skipTurns(16) // Will also end the half
        )
        assertTrue(player.hasSkill<Stab>())
        assertTrue(awayTeam.hasPrayer(PrayerToNuffleTableResult2025.STILETTO))
    }

    // Not available to Star Players or players already with Stab
    @Test
    fun stiletto_notAvailableToSomePlayers() {
        awayTeam.forEach {
            it.addSkill(SkillType.STAB)
        }
        awayTeam.noToPlayer[14.playerNo] = createStarPlayer()
        awayTeam[1.playerNo].addSkill(SkillType.STAB)
        assertEquals(13, awayTeam.size)
        controller.rollForward(
            *buyInducements(
                InducementSelectionCommon.Simple(InducementType2025.PRAYERS_TO_NUFFLE, 1)
            ),
            3.d16, // Roll Stiletto
            *startGameAfterInducements()
        )

        // Team is marked as having the prayer, even if no one could actually get it
        assertTrue(awayTeam.hasPrayer(PrayerToNuffleTableResult2025.STILETTO))
    }

    @Test
    fun ironMan() {
        awayTeam.forEachIndexed { i, it ->
            when (i) {
                1 -> it.addSkill(SkillType.LONER.idTarget(2)) // Players will Loner can get it, unlike BB2020
                13 -> it.state = PlayerDogoutState.KNOCKED_OUT // Selectable
            }
        }

        controller.rollForward(
            *buyInducements(
                InducementSelectionCommon.Simple(InducementType2025.PRAYERS_TO_NUFFLE, 1)
            ),
            4.d16, // Roll Iron Man
        )

        val availablePlayers = controller.getAvailableActions().get<SelectPlayer>().players
        assertEquals(awayTeam.size, availablePlayers.size)

        controller.rollForward(
            PlayerSelected("A1".playerId), // Give to A1
            *startGameAfterInducements(),
        )

        assertTrue(awayTeam.hasPrayer(PrayerToNuffleTableResult2025.IRON_MAN))
        val player = state.getPlayerById("A1".playerId)
        assertTrue(player.armourModifiers.containsInstance<IronManStatModifier>())
        assertEquals(10, player.armorValue)
    }


    // Not available to Star Players
    @Test
    fun ironMan_notAvailableToSomePlayers() {
        awayTeam.noToPlayer.clear()
        awayTeam.noToPlayer[1.playerNo] = createStarPlayer()
        assertEquals(1, awayTeam.size)
        controller.rollForward(
            *buyInducements(
                InducementSelectionCommon.Simple(InducementType2025.PRAYERS_TO_NUFFLE, 1)
            ),
            4.d16, // Roll Iron Man, no one can have it
        )

        // Team is marked as having the prayer, even if no one could actually get it
        assertEquals(DetermineKickingTeamStep.SelectCoinSide, controller.currentNode())
        assertTrue(awayTeam.hasPrayer(PrayerToNuffleTableResult2025.IRON_MAN))
        assertEquals(0, awayTeam.count { it.statModifiers.containsInstance<IronManStatModifier>() })
    }

    @Test
    fun ironMan_onAV11() {
        val player = state.getPlayerById("A1".playerId).also {
            it.baseArmorValue = 11
            it.armorValue = 11
        }

        controller.rollForward(
            *buyInducements(
                InducementSelectionCommon.Simple(InducementType2025.PRAYERS_TO_NUFFLE, 1)
            ),
            4.d16, // Roll Iron Man
            PlayerSelected("A1".playerId), // Give it to A1
        )

        assertTrue(awayTeam.hasPrayer(PrayerToNuffleTableResult2025.IRON_MAN))
        assertTrue(player.armourModifiers.containsInstance<IronManStatModifier>())
        assertEquals(11, player.armorValue)
    }

    @Test
    fun knuckleDusters() {
        awayTeam.forEachIndexed { i, it ->
            when (i) {
                1 -> it.addSkill(SkillType.LONER.idTarget(2)) // Players will Loner can get it, unlike BB2020
                13 -> it.state = PlayerDogoutState.KNOCKED_OUT // Selectable
            }
        }

        controller.rollForward(
            *buyInducements(
                InducementSelectionCommon.Simple(InducementType2025.PRAYERS_TO_NUFFLE, 1)
            ),
            5.d16, // Roll Knuckle Dusters
        )

        val availablePlayers = controller.getAvailableActions().get<SelectPlayer>().players
        assertEquals(awayTeam.size, availablePlayers.size)

        controller.rollForward(
            PlayerSelected("A1".playerId), // Give to A1
            *startGameAfterInducements(),
        )

        assertTrue(awayTeam.hasPrayer(PrayerToNuffleTableResult2025.KNUCKLE_DUSTERS))
        val player = state.getPlayerById("A1".playerId)
        val mightyBlowSkill = player.getSkill(SkillType.MIGHTY_BLOW)
        assertTrue(mightyBlowSkill.isTemporary)
        assertEquals(Duration.END_OF_GAME, mightyBlowSkill.expiresAt)

        // Does not go away after the drive (unlike in BB2020)
        controller.rollForward(
            *skipTurns(16) // Will also end the half
        )
        assertTrue(player.hasSkill<MightyBlow>())
        assertTrue(awayTeam.hasPrayer(PrayerToNuffleTableResult2025.KNUCKLE_DUSTERS))
    }

    @Test
    fun knuckleDusters_notAvailableToSomePlayers() {
        val playerWithSkill = awayTeam.first().also {
            it.addSkill(SkillType.MIGHTY_BLOW)
        }
        awayTeam.noToPlayer.clear()
        awayTeam.noToPlayer[1.playerNo] = playerWithSkill
        awayTeam.noToPlayer[2.playerNo] = createStarPlayer()
        assertEquals(2, awayTeam.size)
        controller.rollForward(
            *buyInducements(
                InducementSelectionCommon.Simple(InducementType2025.PRAYERS_TO_NUFFLE, 1)
            ),
            5.d16, // Roll Iron Man, no one can have it
        )

        // Team is marked as having the prayer, even if no one could actually get it
        assertEquals(DetermineKickingTeamStep.SelectCoinSide, controller.currentNode())
        assertTrue(awayTeam.hasPrayer(PrayerToNuffleTableResult2025.KNUCKLE_DUSTERS))
    }

    @Test
    fun badHabits() {
        controller.rollForward(
            *buyInducements(
                InducementSelectionCommon.Simple(InducementType2025.PRAYERS_TO_NUFFLE, 1)
            ),
            6.d16, // Roll Bad Habits.
            2.d3, // Number of players affected
        )

        val action = controller.getAvailableActions().get<SelectRandomPlayers>()
        assertEquals(2, action.count)
        assertEquals(awayTeam.size, action.players.size)

        controller.rollForward(
            RandomPlayersSelected(listOf("H1".playerId, "H2".playerId)),
            *startGameAfterInducements()
        )

        assertTrue(awayTeam.hasPrayer(PrayerToNuffleTableResult2025.BAD_HABITS))
        assertEquals(2, homeTeam.count { it.hasSkill<Loner>() && it.getSkill(SkillType.LONER).value == 2 })

        // Does not go away after the drive (unlike in BB2020)
        controller.rollForward(
            *skipTurns(16) // Will also end the half
        )
        assertTrue(awayTeam.hasPrayer(PrayerToNuffleTableResult2025.BAD_HABITS))
        assertEquals(2, homeTeam.count { it.hasSkill<Loner>() && it.getSkill(SkillType.LONER).value == 2 })
    }

    @Test
    fun badHabits_notAvailableToSomePlayers() {
        // Give everyone except 1 loner, so when you roll 3 on the prayer
        // Only 1 can be selected
        homeTeam.forEachIndexed { i, it ->
            if (i > 0) it.addSkill(SkillType.LONER.idTarget(4))
        }
        homeTeam.noToPlayer[13.playerNo] = createStarPlayer()

        controller.rollForward(
            *buyInducements(
                InducementSelectionCommon.Simple(InducementType2025.PRAYERS_TO_NUFFLE, 1)
            ),
            6.d16, // Roll Bad Habits.
            2.d3, // Number of players affected
        )

        val action = controller.getAvailableActions().get<SelectRandomPlayers>()
        assertEquals(1, action.count)
        assertEquals(1, action.players.size)

        controller.rollForward(
            RandomPlayersSelected(listOf("H1".playerId)),
            *startGameAfterInducements()
        )

        assertTrue(awayTeam.hasPrayer(PrayerToNuffleTableResult2025.BAD_HABITS))
        assertEquals(1, homeTeam.count { it.hasSkill<Loner>() && it.getSkill(SkillType.LONER).value == 2 })
    }

    @Test
    fun badHabits_notAvailableToAnyPlayers() {
        homeTeam.forEach { it ->
            it.addSkill(SkillType.LONER.idTarget(4))
        }

        controller.rollForward(
            *buyInducements(
                InducementSelectionCommon.Simple(InducementType2025.PRAYERS_TO_NUFFLE, 1)
            ),
            6.d16, // Roll Bad Habits.
            2.d3, // Number of players affected
            *startGameAfterInducements()
        )

        assertTrue(awayTeam.hasPrayer(PrayerToNuffleTableResult2025.BAD_HABITS))
        assertTrue(homeTeam.none { it.hasSkill<Loner>() && it.getSkill(SkillType.LONER).value == 2 })
    }

    @Test
    fun greasyCleats() {
        controller.rollForward(
            *buyInducements(
                InducementSelectionCommon.Simple(InducementType2025.PRAYERS_TO_NUFFLE, 1)
            ),
            7.d16, // Roll Greasy Cleats
        )

        val availablePlayers = controller.getAvailableActions().get<SelectPlayer>().players
        assertEquals(awayTeam.size, availablePlayers.size)

        val player = homeTeam["H1".playerId]
        controller.rollForward(
            PlayerSelected(player), // Give to H1
            *startGameAfterInducements(),
        )

        assertTrue(awayTeam.hasPrayer(PrayerToNuffleTableResult2025.GREASY_CLEATS))
        assertTrue(player.moveModifiers.containsInstance<GreasyCleatsStatModifier>())
        assertEquals(5, player.move)

        // Does not go away after the drive (unlike in BB2020)
        controller.rollForward(
            *skipTurns(16) // Will also end the half
        )
        assertTrue(awayTeam.hasPrayer(PrayerToNuffleTableResult2025.GREASY_CLEATS))
        assertTrue(player.moveModifiers.containsInstance<GreasyCleatsStatModifier>())
    }

    @Test
    fun greasyCleats_noPlayersAvailable() {
        homeTeam.noToPlayer.clear()
        homeTeam.noToPlayer[1.playerNo] = createStarPlayer()
        assertEquals(1, homeTeam.size)
        controller.rollForward(
            *buyInducements(
                InducementSelectionCommon.Simple(InducementType2025.PRAYERS_TO_NUFFLE, 1)
            ),
            7.d16, // Roll Greasy Cleats, no one can have it
        )

        assertEquals(DetermineKickingTeamStep.SelectCoinSide, controller.currentNode())
        assertTrue(awayTeam.hasPrayer(PrayerToNuffleTableResult2025.GREASY_CLEATS))
        assertEquals(0, homeTeam.count { it.statModifiers.containsInstance<IronManStatModifier>() })
    }

    @Test
    fun blessingOfNuffle() {
        awayTeam.forEachIndexed { i, it ->
            when (i) {
                1 -> it.addSkill(SkillType.LONER.idTarget(2)) // Players will Loner can get it, unlike BB2020
                13 -> it.state = PlayerDogoutState.KNOCKED_OUT // Selectable
            }
        }

        controller.rollForward(
            *buyInducements(
                InducementSelectionCommon.Simple(InducementType2025.PRAYERS_TO_NUFFLE, 1)
            ),
            8.d16, // Roll Blessing of Nuffle
        )

        val availablePlayers = controller.getAvailableActions().get<SelectPlayer>().players
        assertEquals(awayTeam.size, availablePlayers.size)

        controller.rollForward(
            PlayerSelected("A1".playerId), // Give to A1
            *startGameAfterInducements(),
        )

        assertTrue(awayTeam.hasPrayer(PrayerToNuffleTableResult2025.BLESSING_OF_NUFFLE))
        val player = state.getPlayerById("A1".playerId)
        assertTrue(player.getSkill(SkillType.PRO).isTemporary)

        // Does not go away after the drive
        controller.rollForward(
            *skipTurns(16) // Will also end the half
        )

        assertTrue(player.getSkill(SkillType.PRO).isTemporary)
    }

    @Test
    fun blessedStatueOfNuffle_noValidPlayers() {
        // Give everyone Pro, so when you roll 8 on the prayer
        awayTeam.forEach { it.addSkill(SkillType.PRO) }
        awayTeam.noToPlayer[13.playerNo] = createStarPlayer()

        controller.rollForward(
            *buyInducements(
                InducementSelectionCommon.Simple(InducementType2025.PRAYERS_TO_NUFFLE, 1)
            ),
            8.d16, // Roll Blessing of Nuffle. Will be ignored.
            *startGameAfterInducements()
        )
        assertTrue(awayTeam.hasPrayer(PrayerToNuffleTableResult2025.BLESSING_OF_NUFFLE))
    }

    @Test
    fun molesUnderThePitch() {
        controller.rollForward(
            *buyInducements(
                InducementSelectionCommon.Simple(InducementType2025.PRAYERS_TO_NUFFLE, 1)
            ),
            9.d16, // Roll Moles under the Pitch.
            *startGameAfterInducements()
        )

        assertTrue(awayTeam.hasPrayer(PrayerToNuffleTableResult2025.MOLES_UNDER_THE_PITCH))
        assertFalse(awayTeam.hasFeature(TeamFeatureType.MOLES_UNDER_THE_PITCH))

        assertFalse(homeTeam.hasPrayer(PrayerToNuffleTableResult2025.MOLES_UNDER_THE_PITCH))
        assertTrue(homeTeam.hasFeature(TeamFeatureType.MOLES_UNDER_THE_PITCH))

        // Prayer and effects will not be removed after the half
        controller.rollForward(
            *skipTurns(16)
        )

        assertTrue(awayTeam.hasPrayer(PrayerToNuffleTableResult2025.MOLES_UNDER_THE_PITCH))
        assertTrue(homeTeam.hasFeature(TeamFeatureType.MOLES_UNDER_THE_PITCH))
    }

    @Test
    fun molesUnderThePitch_affectRushing() {
        controller.rollForward(
            *buyInducements(
                InducementSelectionCommon.Simple(InducementType2025.PRAYERS_TO_NUFFLE, 1)
            ),
            9.d16, // Roll Moles under the Pitch.
            *startGameAfterInducements()
        )

        // Reduce movement so we trigger rushing straight away
        val player = homeTeam["H6".playerId]
        controller.rollForward(
            EndTurn,
            *activatePlayer(player, PlayerStandardActionType.MOVE),
        )
        player.movesLeft = 0
        controller.rollForward(
            *moveTo(10, 0), // Requires Rush
            2.d6, // Should fail due to Moles Under The Pitch
        )
        assertEquals(RushRoll.ChooseReRollSource, controller.currentNode())
        val context = state.getContext<RushRollContext>()
        assertFalse(context.isSuccess)
        assertTrue(context.modifiers.contains(RushModifier.MOLES_UNDER_THE_PITCH_HOME))
    }

    // Unlike BB2020, Moles Under The Pitch only affects the opposite team, even if both teams roll it
    @Test
    fun molesUnderThePitch_onlyAffectOneTeam() {
        controller.rollForward(
            *buyInducements(
                InducementSelectionCommon.Simple(InducementType2025.PRAYERS_TO_NUFFLE, 1)
            ),
            9.d16, // Roll Moles under the Pitch.
            *startGameAfterInducements()
        )

        // Manually add for away team
        homeTeam.activePrayersToNuffle.add(PrayerToNuffleTableResult2025.MOLES_UNDER_THE_PITCH)
        awayTeam.addFeature(TeamFeature.molesUnderThePitch(Duration.END_OF_GAME))

        // Reduce movement so we trigger rushing straight away
        val player = awayTeam[6.playerNo].also {
            it.movesLeft = 0
        }
        controller.rollForward(
            *activatePlayer(player, PlayerStandardActionType.MOVE),
            *moveTo(13, 1), // Requires Rush
            3.d6, // Should succeed because Moles only affect one team.
        )
        assertEquals(RushRoll.ChooseReRollSource, controller.currentNode())
        val context = state.getContext<RushRollContext>()
        assertTrue(context.isSuccess)
        assertTrue(context.modifiers.contains(RushModifier.MOLES_UNDER_THE_PITCH_AWAY))
        assertFalse(context.modifiers.contains(RushModifier.MOLES_UNDER_THE_PITCH_HOME))
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
    fun underScrutiny() {
        controller.rollForward(
            *buyInducements(
                InducementSelectionCommon.Simple(InducementType2025.PRAYERS_TO_NUFFLE, 1)
            ),
            15.d16, // Roll Under Scrutiny
            *startGameAfterInducements()
        )

        assertTrue(awayTeam.hasPrayer(PrayerToNuffleTableResult2025.UNDER_SCRUTINY))
        assertFalse(awayTeam.hasFeature(TeamFeatureType.UNDER_SCRUTINY))
        assertTrue(homeTeam.hasFeature(TeamFeatureType.UNDER_SCRUTINY))

        // Prayer and effects will not be removed after the half
        controller.rollForward(
            *skipTurns(16)
        )

        assertTrue(awayTeam.hasPrayer(PrayerToNuffleTableResult2025.UNDER_SCRUTINY))
        assertFalse(awayTeam.hasFeature(TeamFeatureType.UNDER_SCRUTINY))
        assertTrue(homeTeam.hasFeature(TeamFeatureType.UNDER_SCRUTINY))
    }

    @Test
    fun underScrutiny_triggerOnArmourBroken() {
        controller.rollForward(
            *buyInducements(
                InducementSelectionCommon.Simple(InducementType2025.PRAYERS_TO_NUFFLE, 1)
            ),
            15.d16, // Roll Under Scrutiny
            *startGameAfterInducements(),
        )

        // Home is under scrutiny, so let home foul after receiving the kick-off.
        val victim = awayTeam[1.playerNo].also {
            it.putProne()
        }
        val fouler = homeTeam[1.playerNo]
        controller.rollForward(
            EndTurn,
            *activatePlayer(fouler, PlayerStandardActionType.FOUL),
            PlayerSelected(victim),
            DiceRollResults(5.d6, 6.d6), // Break armour without rolling doubles
            DiceRollResults(1.d6, 2.d6), // Stunned
            Cancel, // Do not argue the call
        )

        assertEquals(PlayerDogoutState.BANNED, homeTeam["H1".playerId].state)
        assertTrue(homeTeam.hasFeature(TeamFeatureType.UNDER_SCRUTINY))
    }

    @Test
    fun intensiveTraining() {
        controller.rollForward(
            *buyInducements(
                InducementSelectionCommon.Simple(InducementType2025.PRAYERS_TO_NUFFLE, 1)
            ),
            16.d16, // Roll Intensive Training.
        )

        val availablePlayers = controller.getAvailableActions().get<SelectPlayer>().players
        assertEquals(awayTeam.size, availablePlayers.size)

        val player = awayTeam[1.playerNo]
        controller.rollForward(
            PlayerSelected(player),
            SkillSelected(SkillType.BLOCK.id()),
        )

        assertTrue(awayTeam.hasPrayer(PrayerToNuffleTableResult2025.INTENSIVE_TRAINING))
        val skill = player.getSkill(SkillType.BLOCK)
        assertTrue(skill.isTemporary)
        assertEquals(Duration.END_OF_GAME, skill.expiresAt)
    }

    @Test
    fun intensiveTraining_noAvailablePlayers() {
        awayTeam.noToPlayer.clear()
        awayTeam.add(createStarPlayer().also { it.number = 13.playerNo })
        assertEquals(1, awayTeam.size)

        controller.rollForward(
            *buyInducements(
                InducementSelectionCommon.Simple(InducementType2025.PRAYERS_TO_NUFFLE, 1)
            ),
            16.d16, // Roll Intensive Training, but no player can receive it.
        )

        assertEquals(DetermineKickingTeamStep.SelectCoinSide, controller.currentNode())
        assertTrue(awayTeam.hasPrayer(PrayerToNuffleTableResult2025.INTENSIVE_TRAINING))
    }

}
