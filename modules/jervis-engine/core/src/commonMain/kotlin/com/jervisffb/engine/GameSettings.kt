package com.jervisffb.engine

import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.builder.DiceRollOwner

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
