package dk.ilios.jervis.procedures.actions.block

import dk.ilios.jervis.commands.Command
import dk.ilios.jervis.commands.ExitProcedure
import dk.ilios.jervis.commands.GotoNode
import dk.ilios.jervis.fsm.Node
import dk.ilios.jervis.fsm.ParentNode
import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.procedures.actions.block.PushStep.SelectPushDirection
import dk.ilios.jervis.procedures.injury.RiskingInjuryRoll
import dk.ilios.jervis.rules.Rules

object Pow: Procedure() {
    override val initialNode: Node = SelectPushDirection

    override fun onEnterProcedure(state: Game, rules: Rules): Command? {
        TODO("Not yet implemented")
    }

    override fun onExitProcedure(state: Game, rules: Rules): Command? {
        TODO("Report result of ")
    }

    // Push the player, including chain pushes
    object ResolvePush: ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure {
            // Set PushContext
            return PushStep
        }

        override fun onExitNode(state: Game, rules: Rules): Command {
            val context = state.blockRollResultContext!!
            return if (context.defender.location.isOnField(rules)) {
                GotoNode(ResolvePlayerDown)
            } else {
                ExitProcedure()
            }
        }
    }

    // If the player is still on the field, resolve them going down.
    // Otherwise, it was resolved as part of the Chain Push
    object ResolvePlayerDown: ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure {
            return RiskingInjuryRoll
        }

        override fun onExitNode(state: Game, rules: Rules): Command {
            return ExitProcedure()
        }
    }

}
