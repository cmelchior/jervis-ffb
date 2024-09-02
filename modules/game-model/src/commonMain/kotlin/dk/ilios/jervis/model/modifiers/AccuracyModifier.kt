package dk.ilios.jervis.model.modifiers

enum class AccuracyModifier(override val modifier: Int, override val description: String) : DiceModifier {
    MARKED(-1, "Marked"),
    SHORT_PASS(-1, "Short Pass"),
    LONG_PASS(-2, "Long Pass"),
    LONG_BOMB(-3, "Long Bomb"),
    VERY_SUNNY(-1, "Very Sunny")
}

