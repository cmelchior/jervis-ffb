package dk.ilios.jervis.procedures.actions.pass

import dk.ilios.jervis.actions.D6Result
import dk.ilios.jervis.commands.Command
import dk.ilios.jervis.commands.ExitProcedure
import dk.ilios.jervis.fsm.ComputationNode
import dk.ilios.jervis.fsm.Node
import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.model.modifiers.DiceModifier
import dk.ilios.jervis.model.FieldCoordinate
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.Player
import dk.ilios.jervis.rules.Rules
import dk.ilios.jervis.utils.INVALID_GAME_STATE

data class PassingInteferenceContext(
    val thrower: Player,
    val target: FieldCoordinate, // Final target coordinates after modifications (ie. scatter/deviate)
    val interferPlayer: Player? = null, // Player doing the interference, if any.
    val interferRoll: D6Result? = null,
    val interferRollModifiers: List<DiceModifier> = emptyList(),
    val didDeflect: Boolean = false,
    val didIntercept: Boolean = false,
)


/**
 * Procedure for checking for passing interference as part of a  [PassAction].
 *
 * See page 50 in the rulebook.
 */
object PassingInterferenceStep: Procedure() {
    override val initialNode: Node = Dummy
    override fun onEnterProcedure(state: Game, rules: Rules): Command? {
        if (state.passingInteferenceContext == null) {
            INVALID_GAME_STATE("Passing interference step has not been initialized")
        }
        return null
    }

    override fun onExitProcedure(state: Game, rules: Rules): Command? = null

    object Dummy : ComputationNode() {
        override fun apply(
            state: Game,
            rules: Rules,
        ): Command = ExitProcedure()
    }
}
