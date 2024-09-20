package dk.ilios.jervis.model

/**
 * Which way a placer is facing.
 * This is only relevant for Giant Mercenaries. See page 54 in Death Zone.
 * All other players should just use [PlayerFacing.UNKNOWN].
 */
enum class PlayerFacing {
    LEFT,
    RIGHT,
    UP,
    DOWN,
    UNKNOWN
}
