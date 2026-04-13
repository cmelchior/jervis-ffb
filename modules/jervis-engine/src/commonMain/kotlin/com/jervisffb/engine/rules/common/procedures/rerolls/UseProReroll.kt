package com.jervisffb.engine.rules.common.procedures.rerolls

import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.SetSkillRerollUsed
import com.jervisffb.engine.commands.SetSkillUsed
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.context.AddContext
import com.jervisffb.engine.commands.context.RemoveContext
import com.jervisffb.engine.commands.context.UpdateContext
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.ParentNode
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.context.UseRerollContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.reports.ReportProResult
import com.jervisffb.engine.reports.ReportSkillUsed
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.engine.utils.assert

/**
 * Procedure controlling the use of Pro to reroll a die. If succesful, it will
 * set [UseRerollContext.rerollAllowed] to `true`, allowing the caller
 * to continue with the reroll.
 */
object UseProReroll : Procedure() {
    override val initialNode: Node = RollDie
    override fun onEnterProcedure(state: Game, rules: Rules): Command {
        return ReportSkillUsed(state.activePlayer!!, SkillType.PRO)
    }
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null
    override fun isValid(state: Game, rules: Rules) {
        val context = state.getRerollContext()
        assert(context.player != null) { "Missing player: $context"}
        assert(context.source != null) { "Missing reroll source: $context"}
    }

    object RollDie: ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command {
            val rerollContext = state.getRerollContext()
            val rerollSource = rerollContext.source!!
            val player = rerollContext.player!!
            val rollContext = ProRollContext(player)
            return compositeCommandOf(
                SetSkillUsed(player, player.getSkill(SkillType.PRO), used = true),
                SetSkillRerollUsed(rerollSource, used = true),
                AddContext(rollContext)
            )
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = ProRoll
        override fun onExitNode(state: Game, rules: Rules): Command {
            val rerollContext = state.getRerollContext()
            val rollContext = state.getContext<ProRollContext>()
            return compositeCommandOf(
                RemoveContext(rollContext),
                ReportProResult(rollContext, rerollContext.type),
                UpdateContext(rerollContext.copy(rerollAllowed = rollContext.isSuccess)),
                ExitProcedure()
            )
        }
    }
}
