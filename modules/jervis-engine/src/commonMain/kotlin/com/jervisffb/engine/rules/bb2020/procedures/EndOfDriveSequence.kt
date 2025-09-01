package com.jervisffb.engine.rules.bb2020.procedures

import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.fsm.ComputationNode
import com.jervisffb.engine.fsm.ParentNode
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.bb2020.procedures.tables.weather.SwelteringHeat
import com.jervisffb.engine.rules.bb2020.tables.Weather
import com.jervisffb.engine.rules.common.skills.Duration

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

            // TODO Check for multiple balls here and remove duplicates
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
