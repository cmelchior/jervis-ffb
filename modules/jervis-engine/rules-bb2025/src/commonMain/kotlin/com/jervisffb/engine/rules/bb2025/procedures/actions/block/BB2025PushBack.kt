package com.jervisffb.engine.rules.bb2025.procedures.actions.block

import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.ParentNode
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.context.BlockContext
import com.jervisffb.engine.model.context.assertContext
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.bb2025.procedures.actions.block.push.PushedBack

/**
 * Resolve a pushback when select on a block die.
 *
 * Since the steps involved in this are complicated, the Pushback is split
 * into many different phases that can be combined in different ways.
 *
 * For Multiple Block, the pushback will stop after creating the push chain
 * as the rest of the phases must be run in lockstep. This is handled there.
 * Otherwise, this procedure is responsible for running the full pushback for
 * block actions against a single opponent.
 *
 * We introduce the concept "Push Chain" in this doc, this simply means the data
 * structure that tracks all the pushes and chain-pushes, starting from the
 * attacker and ending with the last player pushed.
 *
 * See page 62 in the BB2025 rulebook.
 * See [MultipleBlockAction] for how Push Back during Multiple Block is handled.
 */
object BB2025PushBack: Procedure() {
    override val initialNode: Node = PushPlayer
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null
    override fun isValid(state: Game, rules: Rules) {
        state.assertContext<BlockContext>()
    }

    object PushPlayer: ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure {
            return PushedBack
        }
        override fun onExitNode(state: Game, rules: Rules): Command {
            return ExitProcedure()
        }
    }
}
