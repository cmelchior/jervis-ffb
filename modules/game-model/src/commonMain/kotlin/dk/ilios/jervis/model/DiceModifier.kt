package dk.ilios.jervis.model

interface DiceModifier {
    val modifier: Int
    val description: String
}

class NigglingInjuryModifier(val player: Player) : DiceModifier {
    override val modifier: Int = player.nigglingInjuries * -1
    override val description: String = "Niggling Injury"
}
