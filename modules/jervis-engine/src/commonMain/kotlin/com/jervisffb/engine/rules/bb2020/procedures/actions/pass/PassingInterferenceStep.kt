package com.jervisffb.engine.rules.bb2020.procedures.actions.pass

import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.fsm.ComputationNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.model.modifiers.DiceModifier
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.utils.INVALID_GAME_STATE

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
        // TODO Modifier Check for Pouring Rain
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
