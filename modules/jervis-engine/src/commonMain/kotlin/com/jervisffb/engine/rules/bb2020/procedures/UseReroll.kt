package com.jervisffb.engine.rules.bb2020.procedures

import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.SetOldContext
import com.jervisffb.engine.commands.SetSkillRerollUsed
import com.jervisffb.engine.commands.SetTeamRerollUsed
import com.jervisffb.engine.fsm.ComputationNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.context.UseRerollContext
import com.jervisffb.engine.rules.Rules

/**
 * This class contains the rules for using various forms of rerolls.
 * Sometimes using a reroll does not actually allow you to reroll the
 * result.
 *
 * Define the rules for using a Pro reroll.
 */
object UseProReroll : Procedure() {
    override val initialNode: Node = UseReroll
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null

    object UseReroll : ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
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
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null

    object UseReroll : ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            val context = state.rerollContext!!
            val result = UseRerollContext(context.roll, context.source, true)
            return compositeCommandOf(
                SetOldContext(Game::rerollContext, result),
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
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null

    object UseReroll : ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            val context = state.rerollContext!!
            val result = UseRerollContext(context.roll, context.source, rerollAllowed = true)
            return compositeCommandOf(
                SetOldContext(Game::rerollContext, result),
                SetSkillRerollUsed(context.source),
                ExitProcedure(),
            )
        }
    }
}
