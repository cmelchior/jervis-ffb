package com.jervisffb.engine.challenge

/**
 * Enum describing whether a [ChallengeGoal] or [GoalModifier] has been reached.
 *
 * [IN_PROGRESS] exists because a goal that has not been reached yet is
 * different from one the coach has failed to reach. Without it, the UI would
 * show an unsolvable challenge as merely unsolved.
 *
 * Note; in many cases it is hard to determine that a goal has failed, so in
 * most cases [IN_PROGRESS] will be returned.
 *
 * Example:
 * A modifier requiring 2D Block against a player, but the player is first
 * pushed using a 1D Push. In this case, we cannot fail the modifier after the
 * first block.
 */
enum class GoalStatus {
    COMPLETED,
    FAILED,
    IN_PROGRESS,
}
