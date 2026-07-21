package com.jervisffb.engine.bb2020.procedures.rerolls

import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.common.commands.SetTeamRerollUsed
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.context.AddContext
import com.jervisffb.engine.commands.context.RemoveContext
import com.jervisffb.engine.commands.context.UpdateContext
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.ParentNode
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.isSkillAvailable
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.common.procedures.rerolls.LonerRoll
import com.jervisffb.engine.common.procedures.rerolls.LonerRollContext
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.engine.utils.INVALID_GAME_STATE
import com.jervisffb.engine.utils.assert

/**
 * Procedure controlling how to use a Team Reroll.
 */
object BB2020UseTeamReroll : Procedure() {
    override val initialNode: Node = CheckForLoner
    override fun onEnterProcedure(state: Game, rules: Rules): Command {
        // We mark the re-roll used before running any checks. This is an easy
        // way to ensure that re-rolls are not used twice in case we run into
        // recursive reroll usage (like using team rerolls to roll failed loner)
        // If this turns out to be false, we reset the flag later.
        val context = state.getRerollContext()
        val reroll = context.source ?: INVALID_GAME_STATE("Cannot use team reroll as no reroll source: $context")
        return compositeCommandOf(
            SetTeamRerollUsed(state.activeTeamOrThrow(), reroll),
            UpdateContext(context.copy(
                rerollDice = context.originalRoll,
                rerollAllowed = true,
            ))
        )
    }
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null
    override fun isValid(state: Game, rules: Rules) {
        val context = state.getRerollContext()
        assert(context.source != null) {
            "Cannot use re-roll as no re-roll source is selected: $context"
        }
    }

    object CheckForLoner: ParentNode() {
        override fun skipNodeFor(state: Game, rules: Rules): Node? {
            val context = state.getRerollContext()
            return when (context.player?.isSkillAvailable(SkillType.LONER)) {
                false,
                null -> ExitProcedureNode
                true -> null
            }
        }
        override fun onEnterNode(state: Game, rules: Rules): Command {
            val rerollContext = state.getRerollContext()
            val player = rerollContext.player ?: INVALID_GAME_STATE("Mising player: $rerollContext")
            return AddContext(LonerRollContext(player))
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = LonerRoll
        override fun onExitNode(state: Game, rules: Rules): Command {
            val rerollContext = state.getRerollContext()
            val lonerContext = state.getContext<LonerRollContext>()
            val canReroll = lonerContext.isSuccess
            return compositeCommandOf(
                RemoveContext(lonerContext),
                UpdateContext(rerollContext.copy(
                    rerollAllowed = canReroll
                )),
                ExitProcedure()
            )
        }
    }
}
