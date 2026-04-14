package com.jervisffb.engine.rules.bb2025.procedures.skills

import com.jervisffb.engine.actions.ContinueWhenReady
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.context.BlockContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.rules.Rules

/**
 * Class wrapping using the Trickster skill in BB2025, but does not include
 * checking for touchdowns after the Block.
 */
object UseTricksterStep: Procedure() {
    override val initialNode: Node = ChooseTouseTrickster
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null

    object ChooseTouseTrickster: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<BlockContext>().attacker.team.otherTeam()
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            return listOf(ContinueWhenReady)

        }
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            // TODO Need to determine exact semantics of this Skill
            return ExitProcedure()
        }
    }
}
