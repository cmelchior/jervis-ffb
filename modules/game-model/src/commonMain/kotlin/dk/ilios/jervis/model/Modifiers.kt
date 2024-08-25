package dk.ilios.jervis.model

interface DiceModifier {
    val modifier: Int
    val description: String
}

class NigglingInjuryModifier(val player: Player) : DiceModifier {
    override val modifier: Int = player.nigglingInjuries * -1
    override val description: String = "Niggling Injury"
}

class StatModifier private constructor(val modifier: Int, val type: Type ) {
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


