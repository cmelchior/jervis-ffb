package dk.ilios.jervis.procedures

import compositeCommandOf
import dk.ilios.jervis.commands.Command
import dk.ilios.jervis.commands.fsm.ExitProcedure
import dk.ilios.jervis.commands.fsm.GotoNode
import dk.ilios.jervis.fsm.ComputationNode
import dk.ilios.jervis.fsm.ParentNode
import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.procedures.weather.SwelteringHeat
import dk.ilios.jervis.rules.Rules
import dk.ilios.jervis.rules.skills.Duration
import dk.ilios.jervis.rules.tables.Weather

/**
 * This procedure controls the End of Drive sequence as described
 * on page 66 in the rulebook.
 */
object EndOfDriveSequence: Procedure() {
    override val initialNode = DealWithSecretWeapons
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null

    object DealWithSecretWeapons: ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            return GotoNode(RecoverKnockedOutPlayers)
        }
    }

    object RecoverKnockedOutPlayers: ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            return compositeCommandOf(
                // TODO
                GotoNode(TheDriveEnds)
            )
        }
    }
    object TheDriveEnds: ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            // Remove special rules or effects that lasted for the duration of the drive
            // Unclear where in this process Sweltering Heat is applied.
            // For now, it doesn't really matter, so just run it afterwards
            val resetCommands = getResetTemporaryModifiersCommands(state, rules, Duration.END_OF_DRIVE)
            return compositeCommandOf(
                *resetCommands,
                if (state.weather == Weather.SWELTERING_HEAT) GotoNode(ResolveSwelteringHeat) else ExitProcedure()
            )
        }
    }

    object ResolveSwelteringHeat: ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = SwelteringHeat
        override fun onExitNode(state: Game, rules: Rules): Command {
            return ExitProcedure()
        }
    }
}
