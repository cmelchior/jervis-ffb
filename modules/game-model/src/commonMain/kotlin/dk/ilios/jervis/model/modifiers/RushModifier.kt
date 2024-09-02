package dk.ilios.jervis.model.modifiers

/**
 * Modifiers that can affect a Rush roll.
 *
 * @see [dk.ilios.jervis.procedures.actions.move.RushRoll]
 */
enum class RushModifier(override val modifier: Int, override val description: String) : DiceModifier {
    BLIZZARD(-1, "Blizzard"),
}
