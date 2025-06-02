package com.jervisffb.engine.actions

import com.jervisffb.engine.GameEngineController
import kotlinx.serialization.Serializable

/**
 * These ID's uniquely identify a [GameAction] that has been handled by the
 * [GameEngineController]. The IDs should always be increasing. This means that
 * looking at the action history should have a list of action ids ranging from 1 until
 * count(actions).
 *
 * This also makes it possible to reason about multiple events arriving
 * at the GameController. If it sees a GameAction with an ID that has already
 * been processed, the next action with the same ID should be ignored (or throw
 * an error).
 *
 * Generally, if we see ids that are further ahead in the future than
 * [GameEngineController.nextActionIndex], that points to a logical error in the code,
 * but due to how we use [Revert] (it deletes history), then there is a chance we
 * can see an event that is further ahead (e.g., if an event is en-route from the UI and
 * a Revert happens before it can be handled). In this particular case, we should just drop
 * the action similar to how we drop actions that are outdated.
 *
 * To correctly detect these scenarios, we need to track the number of "reverts" seen.
 * You can think of this as generations. As soon as a [GameEngineController] handles
 * a revert, it should reject all actions that have an [revertsSeen] less than the ones
 * executed by the Engine. Note, this is not handled by the [GameEngineController], but
 * should be handled by upper layers.
 *
 * @param value always incrementing value.
 * @param revertsSeen Describes how many reverts the controller has seen so far for this id.
 */
@Serializable
data class GameActionId(val value: Int, val revertsSeen: Int = 0) {
    operator fun plus(increment: Int): GameActionId {
        return GameActionId(value + increment, revertsSeen)
    }
    operator fun minus(increment: Int): GameActionId {
        return GameActionId(value - increment, revertsSeen = revertsSeen)
    }
    // An ID with a lower `revertsSeen` count is always smaller, regardless of what
    // `value` is.
    operator fun compareTo(other: GameActionId): Int {
        return when (val compareValue = revertsSeen.compareTo(other.revertsSeen)) {
            0 -> return value.compareTo(other.value)
            else -> compareValue
        }
    }

    fun toSimpleIdString(): String {
        return value.toString()
    }

    override fun toString(): String {
        return "[$value,$revertsSeen]"
    }
}
