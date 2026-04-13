package com.jervisffb.engine.rules.common.procedures

import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.SetSkillRerollUsed
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.context.UpdateContext
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.fsm.ComputationNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.common.skills.Duration

/**
 * Define the rules for using a normal skill reroll.
 */
object UseStandardSkillReroll : Procedure() {
    override val initialNode: Node = UseReroll
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null

    object UseReroll : ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            val context = state.getRerollContext()
            val useSkill = (context.source != null && context.source.rerollResetAt != Duration.PERMANENT)
            return compositeCommandOf(
                when (useSkill) {
                    true -> SetSkillRerollUsed(context.source, used = true)
                    false -> null
                },
                UpdateContext(context.copy(rerollAllowed = true)),
                ExitProcedure()
            )
        }
    }
}
