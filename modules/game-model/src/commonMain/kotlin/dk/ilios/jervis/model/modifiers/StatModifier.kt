package dk.ilios.jervis.model.modifiers

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

//@Serializable
//class StatModifier private constructor(
//    val modifier: Int,
//    val type: Type,
//    val expiresAt: ResetPolicy = ResetPolicy.NEVER
//) {
//    companion object {
//        fun AV(modifier: Int) = StatModifier(modifier, Type.AV)
//        fun MA(modifier: Int) = StatModifier(modifier, Type.MA)
//        fun PA(modifier: Int) = StatModifier(modifier, Type.PA)
//        fun AG(modifier: Int) = StatModifier(modifier, Type.AG)
//        fun ST(modifier: Int) = StatModifier(modifier, Type.ST)
//    }
//}
