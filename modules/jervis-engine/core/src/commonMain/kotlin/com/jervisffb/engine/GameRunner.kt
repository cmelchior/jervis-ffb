package com.jervisffb.engine

import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.rng.DiceRollGenerator
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.builder.DiceRollOwner

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
 * Class describing all the properties needed to control running a full game.
 * NOTE: This is currently only being used on the Server. Do we need it elsewhere?
 */
data class GameSettings(
    val gameRules: Rules,
    val initialActions: List<GameAction> = listOf(),
    val isHotseatGame: Boolean = false,
) {
    // Are random events done on the client or inside the server
    val clientSelectedDiceRolls: Boolean = (gameRules.diceRollsOwner == DiceRollOwner.ROLL_ON_CLIENT)
    val timerSettings = gameRules.timers
}
