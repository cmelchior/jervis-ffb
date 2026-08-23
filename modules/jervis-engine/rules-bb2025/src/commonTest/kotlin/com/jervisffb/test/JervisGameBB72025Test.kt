package com.jervisffb.test

import com.jervisffb.engine.GameEngineController
import com.jervisffb.engine.actions.D8Result
import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.PitchSquareSelected
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.bb2025.BB2025Rules
import com.jervisffb.engine.bb2025.BB72025Rules
import com.jervisffb.engine.common.procedures.FullGame
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.ext.d8
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.model.PlayerId
import com.jervisffb.engine.model.locations.PitchCoordinate
import com.jervisffb.engine.rules.builder.UndoActionBehavior
import com.jervisffb.engine.statistics.GameStatistics
import com.jervisffb.test.bb2025.createDefault2025AwayTeamForBB7
import com.jervisffb.test.bb2025.createDefault2025HomeTeamForBB7
import com.jervisffb.test.bb2025.createDefaultGameStateBB2025
import com.jervisffb.test.ext.rollForward
import kotlin.test.BeforeTest

/**
 * Abstract class for tests that involving testing the flow of events during a
 * real game. This class is specific for the BB7 BB2025 ruleset.
 */
abstract class JervisGameBB72025Test: JervisGameBB2025Test() {

    override val rules: BB2025Rules = BB72025Rules().update {
        undoActionBehavior = UndoActionBehavior.ALLOWED
    }

    @BeforeTest
    override fun setUp() {
        setupDefaultBB7Game()
    }

    override fun startDefaultGame() {
        controller.rollForward(
            *defaultBB2020Pregame(),
            *defaultSetup(),
            *defaultKickOffHomeTeam(),
        )
    }

    fun setupDefaultBB7Game(
        initialActions: List<GameAction> = emptyList(),
        protectInitialActions: Boolean = false,
        collectMetadata: Boolean = false
    ) {
        homeTeam = createDefault2025HomeTeamForBB7(rules)
        awayTeam = createDefault2025AwayTeamForBB7(rules)
        state = createDefaultGameStateBB2025(rules, homeTeam, awayTeam)
        homeTeam = state.homeTeam
        awayTeam = state.awayTeam
        controller = GameEngineController(
            state = state,
            initialActions = initialActions,
            protectInitialActions = protectInitialActions,
            cacheActionDescriptor = false,
            statistics = when (collectMetadata) {
                true -> GameStatistics()
                else -> null
            },
        )
        controller.startTestMode(FullGame)
    }

    fun defaultSetup(homeFirst: Boolean = true): Array<GameAction> {
        val homeTeam = defaultBB7HomeSetup()
        val awayTeam = defaultBB7AwaySetup()
        return if (homeFirst) {
            arrayOf(*homeTeam, *awayTeam)
        } else {
            arrayOf(*awayTeam, *homeTeam)
        }
    }

    fun defaultBB7HomeSetup(endSetup: Boolean = true): Array<GameAction> {
        val setup = buildList {
            add("H1".playerId to PitchCoordinate(6, 2))
            add("H2".playerId to PitchCoordinate(6, 3))
            add("H3".playerId to PitchCoordinate(6, 4))
            add("H4".playerId to PitchCoordinate(6, 5))
            add("H5".playerId to PitchCoordinate(6, 6))
            add("H6".playerId to PitchCoordinate(6, 7))
            add("H7".playerId to PitchCoordinate(6, 8))
        }
        return teamSetup(setup, endSetup)
    }

    fun defaultBB7AwaySetup(endSetup: Boolean = true): Array<GameAction> {
        val setup= listOf(
            "A1".playerId to PitchCoordinate(13, 2),
            "A2".playerId to PitchCoordinate(13, 3),
            "A3".playerId to PitchCoordinate(13, 4),
            "A4".playerId to PitchCoordinate(13, 5),
            "A5".playerId to PitchCoordinate(13, 6),
            "A6".playerId to PitchCoordinate(13, 7),
            "A7".playerId to PitchCoordinate(13, 8),
        )
        return teamSetup(setup, endSetup)
    }

    fun defaultKickOffHomeTeam(
        selectKicker: PlayerSelected? = PlayerSelected(PlayerId("H1")), // Select Kicker
        placeKick: PitchSquareSelected = PitchSquareSelected(14, 5), // center of Away Half,
        deviate: DiceRollResults = DiceRollResults(5.d8, 1.d6, 6.d6), // Land on [15,5]
        kickoffEvent: Array<GameAction?> = defaultKickOffEvent(),
        bounce: D8Result? = 4.d8 // Bounce to [14,5]
    ) = arrayOf(
        selectKicker,
        placeKick,
        deviate,
        *kickoffEvent,
        bounce
    )

    fun defaultKickOffAwayTeam(
        selectKicker: PlayerSelected = PlayerSelected("A1".playerId),
        placeKick: PitchSquareSelected = PitchSquareSelected(3, 5), // Center of Home Half,
        deviate: DiceRollResults = DiceRollResults(4.d8, 1.d6), // Land on [2,5]
        kickoffEvent: Array<GameAction?> = defaultKickOffEvent(),
        bounce: D8Result? = 5.d8 // Bounce to [3,5]
    ) = arrayOf(
        selectKicker,
        placeKick,
        deviate,
        *kickoffEvent,
        bounce
    )
}
