package com.jervisffb.engine.model

// TODO Should we split this into DogoutState and FieldState?
/**
 * This enum describes the high-level state of players. This enum should only
 * contain "stable" states. [PlayerIntermediateState] contains states that
 * are temporary and are used when transitioning between [PlayerState]s.
 *
 * **Developer's Commentary**
 * There is some overlap between state and status effect, and it isn't clear
 * what the difference is, so for now, the separation is a best-guess for what
 * makes sense to model the rules the best. It might change in the future.
 */
enum class PlayerState {
    // Dogout states
    RESERVE,
    KNOCKED_OUT,
    BADLY_HURT,
    LASTING_INJURY,
    SERIOUSLY_HURT,
    SERIOUS_INJURY,
    DEAD,
    FAINTED, // From Sweltering Heat
    BANNED, // From being sent off by the Ref
    DODGY_SNACK, // Miss the drive from Dodgy Snack

    // Field states
    STANDING,
    PRONE,
    STUNNED,
    // This state should only be visible to a player during their own team turn.
    // After that, it should turn into a normal STUNNED state.
    STUNNED_OWN_TURN,

//    MOVING,
//    UNKNOWN,
//    MISSING,
//    FALLING,
//    BLOCKED,
//    EXHAUSTED,
//    BEING_DRAGGED,
//    PICKED_UP,
//    HIT_ON_GROUND,
//    HIT_BY_FIREBALL,
//    HIT_BY_LIGHTNING,
//    HIT_BY_BOMB,
//    SETUP_PREVENTED
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
