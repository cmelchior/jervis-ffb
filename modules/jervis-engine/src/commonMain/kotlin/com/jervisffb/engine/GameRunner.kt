package com.jervisffb.engine

import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.rng.DiceRollGenerator
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.StandardBB2020Rules

/**
 * Top-level class for running all game types. This class is responsible for
 * sending actions in and out of the rules engine.
 *
 * Subclasses of this interface are considered the entry point for the model
 * layer in the MVVM architecture.
 *
 * TODO Right now the UI just uses this to interact with the controller. Is this abstraction needed?
 */
interface GameRunner {
    val controller: GameEngineController
    val state: Game
    val rules: Rules
    val diceGenerator: DiceRollGenerator
//    val setup: GameSetup

    fun handleAction(action: GameAction)
    fun getAvailableActions(): ActionRequest

    // Chat
    // Combine System Logs + Game Logs
}

/**
 * Interface describing all the properties needed to control running a full game
 */
data class GameSettings(
    val gameRules: Rules = StandardBB2020Rules(),
    val timerSettings: TimerSettings = TimerSettings.BB_CLOCK,
    val clientSelectedDiceRolls: Boolean = false // Are random events done remotely or on the host
//    val timers: TimerSettings
)
