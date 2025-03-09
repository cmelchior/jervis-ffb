package com.jervisffb.engine.rules.builder

import com.jervisffb.engine.model.BallType

/**
 * Interface representing how the ball being used for the game is selected.
 *
 * [StandardBall] is used for a normal game of Blood Bowl.
 * [RollOnUnusualBallTable] is used if you want to use the rules for "A Load of Balls"
 * as described in Death Zone page 68.
 * [SpecificUnusualBall] is an alternative option for just choosing whatever ball is fun
 * to use.
 */
sealed interface BallSelectorRule

data object StandardBall: BallSelectorRule
data object RollOnUnusualBallTable: BallSelectorRule
data class SpecificUnusualBall(val type: BallType): BallSelectorRule
