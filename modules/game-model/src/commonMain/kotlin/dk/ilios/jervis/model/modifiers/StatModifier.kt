package dk.ilios.jervis.model.modifiers

class StatModifier private constructor(val modifier: Int, val type: Type) {
    enum class Type {
        AV, MA, PA, AG, ST
    }
    companion object {
        fun AV(modifier: Int) = StatModifier(modifier, Type.AV)
        fun MA(modifier: Int) = StatModifier(modifier, Type.MA)
        fun PA(modifier: Int) = StatModifier(modifier, Type.PA)
        fun AG(modifier: Int) = StatModifier(modifier, Type.AG)
        fun ST(modifier: Int) = StatModifier(modifier, Type.ST)
    }
}
