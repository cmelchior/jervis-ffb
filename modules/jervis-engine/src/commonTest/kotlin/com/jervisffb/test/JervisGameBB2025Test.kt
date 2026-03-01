package com.jervisffb.test

import com.jervisffb.engine.GameEngineController
import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.actions.FieldSquareSelected
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.ext.d8
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.rules.BB2025Rules
import com.jervisffb.engine.rules.StandardBB2025Rules
import com.jervisffb.engine.rules.builder.UndoActionBehavior
import com.jervisffb.engine.rules.common.procedures.FullGame
import com.jervisffb.test.bb2020.advancedHumanTeamAway
import com.jervisffb.test.bb2020.createAdvancedHomeTeam
import com.jervisffb.test.bb2020.createDefaultGameStateBB2020
import com.jervisffb.test.bb2025.createDefaultGameStateBB2025
import com.jervisffb.test.bb2025.createDefaultHomeTeamBB2025
import com.jervisffb.test.bb2025.humanTeamAwayBB2025
import com.jervisffb.test.ext.rollForward
import kotlin.test.BeforeTest

/**
 * Abstract class for tests that involving testing the flow of events during a
 * real game. This class is specific for the BB2025 ruleset.
 */
abstract class JervisGameBB2025Test {

    open val rules: BB2025Rules = StandardBB2025Rules().toBuilder().run {
        undoActionBehavior = UndoActionBehavior.ALLOWED
        build()
    }
    protected lateinit var state: Game
    protected lateinit var controller: GameEngineController
    protected lateinit var homeTeam: Team
    protected lateinit var awayTeam: Team

    @BeforeTest
    open fun setUp() {
        setupDefaultGame()
    }

    fun startDefaultGame() {
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            *defaultKickOffHomeTeam(),
        )
    }

    fun setupDefaultGame() {
        homeTeam = createDefaultHomeTeamBB2025(rules)
        awayTeam = humanTeamAwayBB2025(rules)
        state = createDefaultGameStateBB2025(rules, homeTeam, awayTeam)
        homeTeam = state.homeTeam
        awayTeam = state.awayTeam
        controller = GameEngineController(state, cacheActionDescriptor = false)
        controller.startTestMode(FullGame)
    }

    fun setupAndStartThrowTeamMateGame() {
        // Create new teams that have the option to throw a team-mate.
        // Remove Bone Head from the ogre to make testing easier.
        homeTeam = createAdvancedHomeTeam(rules)
        awayTeam = advancedHumanTeamAway(rules)
        homeTeam["H1".playerId].positionSkills.removeFirst()
        awayTeam["A1".playerId].positionSkills.removeFirst()
        state = createDefaultGameStateBB2020(rules, homeTeam, awayTeam)
        homeTeam = state.homeTeam
        awayTeam = state.awayTeam
        controller = GameEngineController(state)
        controller.startTestMode(FullGame)
        controller.rollForward(
            *defaultPregame(),
            *arrayOf(*throwTeamMateHomeSetup(), *throwTeamMateAwaySetup()),
            *defaultKickOffHomeTeam(
                placeKick = FieldSquareSelected(17, 5),
                deviate = DiceRollResults(4.d8, 1.d6),
                bounce = 4.d8
            ),
        )
    }

    private fun throwTeamMateHomeSetup(endSetup: Boolean = true): Array<GameAction> {
        val setup = buildList {
            // Place Hafling behind Ogre, ready to be thrown
            add("H1".playerId to FieldCoordinate(12, 5))
            add("H13".playerId to  FieldCoordinate(11, 5))
            add("H2".playerId to FieldCoordinate(12, 6))
            add("H3".playerId to FieldCoordinate(12, 7))
            add("H4".playerId to FieldCoordinate(12, 8))
            add("H5".playerId to FieldCoordinate(12, 9))
            add("H6".playerId to FieldCoordinate(11, 1))
            add("H7".playerId to FieldCoordinate(10, 1))
            add("H8".playerId to FieldCoordinate(10, 13))
            add("H9".playerId to FieldCoordinate(11, 13))
            add("H10".playerId to  FieldCoordinate(9, 7))
        }
        return teamSetup(setup, endSetup)
    }

    private fun throwTeamMateAwaySetup(endSetup: Boolean = true): Array<GameAction> {
        val setup= listOf(
            // Place Hafling behind Ogre, ready to be thrown
            "A1".playerId to FieldCoordinate(13, 5),
            "A13".playerId to FieldCoordinate(14, 5),
            "A2".playerId to FieldCoordinate(13, 6),
            "A3".playerId to FieldCoordinate(13, 7),
            "A4".playerId to FieldCoordinate(13, 8),
            "A5".playerId to FieldCoordinate(13, 9),
            "A6".playerId to FieldCoordinate(14, 1),
            "A7".playerId to FieldCoordinate(15, 1),
            "A8".playerId to FieldCoordinate(15, 13),
            "A9".playerId to FieldCoordinate(14, 13),
            "A10".playerId to FieldCoordinate(16, 7),
        )
        return teamSetup(setup, endSetup)
    }

}
