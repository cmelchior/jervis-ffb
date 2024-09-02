package dk.ilios.jervis.model.modifiers

/**
 * Static Modifiers that can affect a Pickup roll.
 *
 * @see [dk.ilios.jervis.procedures.Pickup]
 * @see [dk.ilios.jervis.procedures.PickupRoll]
 * @see [MarkedModifier]
 */
enum class PickupModifier(override val modifier: Int, override val description: String) : DiceModifier {
    POURING_RAIN(-1, "Pouring Rain"),
}


