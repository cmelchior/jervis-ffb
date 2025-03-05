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
import com.jervisffb.engine.rules.bb2020.skills.Duration
import com.jervisffb.engine.rules.bb2020.tables.Weather

/**
 * This procedure controls the End of a Team Turn.
 * Unfortunately, the order of events for this is not described in the rulebook,
 * so the current sequence is just a best-guess effort.
 *
 *  For now we choose the (somewhat arbitrary) order:
 *    - Prayers Of Nuffle (Throw a Rock)
 *    - Special Play Cards for active team (End of team turn)
 *    - Special Play Cards for opponent team with activation (End of Opponent Turn)
 *    - Temporary Skills/Characteristics are removed.
 *    - Roll over stunned players.
 */
object EndOfTurnSequence: Procedure() {
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
