package dk.ilios.jervis.model.modifiers

import dk.ilios.jervis.model.modifiers.StatModifier.Type
import dk.ilios.jervis.rules.skills.Duration

/**
 * Interface describing a change to a player's base line stat.
 *
 * This, among other things, includes injuries, characteristic improvements, prayer
 * of nuffle effects and so on.
 */
interface StatModifier {
    enum class Type {
        AV, MA, PA, AG, ST
    }
    val description: String
    val modifier: Int
    val type: Type
    val expiresAt: Duration
}

enum class SkillStatModifier(
    override val description: String,
    override val modifier: Int,
    override val type: Type,
    override val expiresAt: Duration = Duration.PERMANENT
): StatModifier {
    HORNS("Horns (+1 ST)", 1, Type.ST, expiresAt = Duration.END_OF_ACTION),
}
