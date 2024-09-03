package dk.ilios.jervis.procedures.bb2020.prayersofnuffle

import compositeCommandOf
import dk.ilios.jervis.commands.Command
import dk.ilios.jervis.commands.ExitProcedure
import dk.ilios.jervis.commands.SetContext
import dk.ilios.jervis.fsm.ComputationNode
import dk.ilios.jervis.fsm.Node
import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.context.assertContext
import dk.ilios.jervis.model.context.getContext
import dk.ilios.jervis.procedures.PrayersToNuffleRollContext
import dk.ilios.jervis.reports.LogCategory
import dk.ilios.jervis.reports.SimpleLogEntry
import dk.ilios.jervis.rules.Rules

/**
 * Procedure for handling the Prayer of Nuffle "Treacherous Trapdoor" as described on page 39
 * of the rulebook.
 *
 * It is unclear what happens if you put a player on a trapdoor during setup. Does that count
 * as "enter for any reason". For now, we assume no.
 *
 * This means we need to check for trapdoors in the following cases:
 * - Move normal
 * - Dodge move
 * - Jump move
 * - Leap move
 * - Pushback or chain push as part of block/blitz
 * - Ball & Chain move
 * - Pogostick move
 * - Throw Team mate
 */
object TreacherousTrapdoor : Procedure() {
    override val initialNode: Node = ApplyEvent
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null
    override fun isValid(state: Game, rules: Rules) {
        state.assertContext<PrayersToNuffleRollContext>()
    }

    object ApplyEvent : ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            val context = state.getContext<PrayersToNuffleRollContext>()
            return compositeCommandOf(
                SetContext(context.copy(resultApplied = true)),
                SimpleLogEntry("${state.activeTeam} installed a Treacherous Trapdoor", category = LogCategory.GAME_PROGRESS),
                ExitProcedure(),
            )
        }
    }
}
