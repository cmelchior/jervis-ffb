package com.jervisffb.engine.model

import com.jervisffb.engine.model.locations.Dogout
import com.jervisffb.engine.model.locations.OnPitchLocation

/**
 * This interface describes the high-level state of players. It is split into
 * two enums, one for on-pitch states, and one for dogout state. These enums
 * should only contain "stable" states. [PlayerIntermediateState] contains
 * states that are temporary and are used when transitioning between [
 * PlayerState]s.
 *
 * **Developer's Commentary**
 * There is some overlap between state and status effect, and it isn't clear
 * what the difference is, so for now, the separation is a best-guess for what
 * makes sense to model the rules the best. It might change in the future.
 */
sealed interface PlayerState {
    val label: String
}

/**
 * States for players on the pitch. These should only be used when
 * [Player.location] is a [OnPitchLocation].
 */
enum class PlayerPitchState(override val label: String) : PlayerState {
    STANDING("Standing"),
    PRONE("Prone"),
    STUNNED("Stunned"),
    // This state should only be visible to a player during their own team turn.
    // After that, it should turn into a normal STUNNED state.
    STUNNED_OWN_TURN("Stunned in own turn"),
}

/**
 * States for players in the Dogout. These should only be used when
 * [Player.location] is [Dogout].
 */
enum class PlayerDogoutState(override val label: String) : PlayerState {
    RESERVE("Reserve"),
    KNOCKED_OUT("Knocked Out"),
    BADLY_HURT("Badly Hurt"),
    LASTING_INJURY("Lasting Injury"),
    SERIOUSLY_HURT("Seriously Hurt"),
    SERIOUS_INJURY("Serious Injury"),
    DEAD("DEAD"),
    FAINTED("Fainted"), // From Sweltering Heat
    BANNED("Banned"), // From being sent off by the Ref
    DODGY_SNACK("Dodgy Snack"), // Miss the drive from Dodgy Snack
}


/**
 * Intermediate Player states. These states indicate that [Player.state]
 * is currently changing to a new value, but we do not yet know what that
 * value is, e.g., a player being Knocked Down will transition from STANDING to
 * PRONE. While this is happening, the intermediate state is set to KNOCKED_DOWN.
 */
enum class PlayerIntermediateState {
    KNOCKED_DOWN,
    FALLEN_OVER,
}
