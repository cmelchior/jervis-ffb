package dk.ilios.jervis.procedures

import compositeCommandOf
import dk.ilios.jervis.commands.Command
import dk.ilios.jervis.commands.ExitProcedure
import dk.ilios.jervis.commands.SetContext
import dk.ilios.jervis.commands.SetSkillRerollUsed
import dk.ilios.jervis.commands.SetTeamRerollUsed
import dk.ilios.jervis.fsm.ComputationNode
import dk.ilios.jervis.fsm.Node
import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.rules.Rules

/**
 * This class contains the rules for using various forms of rerolls.
 * Sometimes using a reroll does not actually allow you to reroll the
 * result.
 *
 * Define the rules for using a Pro reroll.
 */
object UseProReroll : Procedure() {
    override val initialNode: Node = UseReroll

    override fun onEnterProcedure(
        state: Game,
        rules: Rules,
    ): Command? = null

    override fun onExitProcedure(
        state: Game,
        rules: Rules,
    ): Command? = null

    object UseReroll : ComputationNode() {
        override fun apply(
            state: Game,
            rules: Rules,
        ): Command {
            TODO("Not yet implemented")
        }
    }
}

/**
 * Define the rules for using a Loner reroll.
 */
object UseLonerReroll : Procedure() {
    override val initialNode: Node = UseReroll

    override fun onEnterProcedure(
        state: Game,
        rules: Rules,
    ): Command? = null

    override fun onExitProcedure(
        state: Game,
        rules: Rules,
    ): Command? = null

    object UseReroll : ComputationNode() {
        override fun apply(
            state: Game,
            rules: Rules,
        ): Command {
            TODO("Not yet implemented")
        }
    }
}

object UseTeamReroll : Procedure() {
    override val initialNode: Node = UseReroll

    override fun onEnterProcedure(
        state: Game,
        rules: Rules,
    ): Command? = null

    override fun onExitProcedure(
        state: Game,
        rules: Rules,
    ): Command? = null

    object UseReroll : ComputationNode() {
        override fun apply(
            state: Game,
            rules: Rules,
        ): Command {
            val context = state.useRerollContext!!
            val result = RerollResultContext(context.roll, context.source, true)
            return compositeCommandOf(
                SetContext(Game::useRerollResult, result),
                SetTeamRerollUsed(context.source),
                ExitProcedure(),
            )
        }
    }
}

/**
 * Define the rules for using a normal skill reroll.
 */
object UseStandardSkillReroll : Procedure() {
    override val initialNode: Node = UseReroll

    override fun onEnterProcedure(
        state: Game,
        rules: Rules,
    ): Command? = null

    override fun onExitProcedure(
        state: Game,
        rules: Rules,
    ): Command? = null

    object UseReroll : ComputationNode() {
        override fun apply(
            state: Game,
            rules: Rules,
        ): Command {
            val context = state.useRerollContext!!
            val result = RerollResultContext(context.roll, context.source, rerollAllowed = true)
            return compositeCommandOf(
                SetContext(Game::useRerollResult, result),
                SetSkillRerollUsed(context.source),
                ExitProcedure(),
            )
        }
    }
}
