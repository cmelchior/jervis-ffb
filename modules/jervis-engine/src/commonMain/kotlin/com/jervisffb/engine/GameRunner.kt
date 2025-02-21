package com.jervisffb.engine

import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.rng.DiceRollGenerator
import com.jervisffb.engine.rules.Rules

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

    fun handleAction(action: GameAction)
    fun getAvailableActions(): ActionRequest

    // Chat
    // Combine System Logs + Game Logs
}
