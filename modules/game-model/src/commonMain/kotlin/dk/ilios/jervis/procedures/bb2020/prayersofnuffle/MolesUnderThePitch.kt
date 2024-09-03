package dk.ilios.jervis.procedures.bb2020.prayersofnuffle

import compositeCommandOf
import dk.ilios.jervis.commands.AddPrayersToNuffle
import dk.ilios.jervis.commands.Command
import dk.ilios.jervis.commands.ExitProcedure
import dk.ilios.jervis.fsm.ComputationNode
import dk.ilios.jervis.fsm.Node
import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.context.assertContext
import dk.ilios.jervis.procedures.PrayersToNuffleRollContext
import dk.ilios.jervis.reports.LogCategory
import dk.ilios.jervis.reports.SimpleLogEntry
import dk.ilios.jervis.rules.Rules
import dk.ilios.jervis.rules.tables.PrayerToNuffle

/**
 * Procedure for handling the Prayer of Nuffle "Moles under the Pitch" as described on page 39
 * of the rulebook.
 */
object MolesUnderThePitch : Procedure() {
    override val initialNode: Node = ApplyEvent
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null
    override fun isValid(state: Game, rules: Rules) {
        state.assertContext<PrayersToNuffleRollContext>()
    }

    object ApplyEvent : ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            return compositeCommandOf(
                SimpleLogEntry("Moles under the Pitch!", category = LogCategory.GAME_PROGRESS),
                ExitProcedure(),
            )
        }
    }
}
