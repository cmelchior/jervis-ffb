package com.jervisffb.engine.model.context

import com.jervisffb.engine.model.Ball
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.modifiers.DiceModifier
import com.jervisffb.engine.rules.common.procedures.D6DieRoll

data class CatchContext(
    val catchingPlayer: Player,
    val ball: Ball,
    val useExtraArms: Boolean = false,
    // This is for the Diving Catch +1 modifier, so is only applicable if ball
    // lands in players square
    val useDivingCatch: Boolean = false,
    val useNervesOfSteel: Boolean = false,
    val modifiers: List<DiceModifier> = emptyList(),
    val roll: D6DieRoll? = null,
    val isSuccess: Boolean = false,
) : ProcedureContext {
    val rerolled: Boolean = roll?.rerollSource != null && roll.rerolledResult != null

    // The catch is done as a Diving Catch, not a catch where the ball lands in the players square
    val isAttemptingDivingCatch: Boolean = (catchingPlayer.coordinates != ball.coordinates)

    // What is the target roll of the Catch roll?
    val rollTarget: Int = catchingPlayer.agility
}
