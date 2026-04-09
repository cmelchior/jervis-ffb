package com.jervisffb.engine.rules.common.procedures

import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.SetSkillRerollUsed
import com.jervisffb.engine.commands.SetTeamRerollUsed
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.fsm.ComputationNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.common.skills.Duration

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
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null
    object UseReroll : ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
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
            return compositeCommandOf(
                SetTeamRerollUsed(state.activeTeamOrThrow(), context.source),
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
            return compositeCommandOf(
                if (context.source.rerollResetAt != Duration.PERMANENT) {
                    SetSkillRerollUsed(context.source, used = true)
                } else {
                    null
                },
                ExitProcedure(),
            )
        }
    }
}
