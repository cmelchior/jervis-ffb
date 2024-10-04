package dk.ilios.jervis.procedures.actions.block

import dk.ilios.jervis.actions.DBlockResult
import dk.ilios.jervis.commands.Command
import dk.ilios.jervis.commands.fsm.ExitProcedure
import dk.ilios.jervis.commands.fsm.GotoNode
import dk.ilios.jervis.fsm.Node
import dk.ilios.jervis.fsm.ParentNode
import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.Player
import dk.ilios.jervis.model.context.ProcedureContext
import dk.ilios.jervis.model.context.assertContext
import dk.ilios.jervis.procedures.BlockDieRoll
import dk.ilios.jervis.procedures.actions.block.standard.StandardBlockApplyResult
import dk.ilios.jervis.procedures.actions.block.standard.StandardBlockChooseReroll
import dk.ilios.jervis.procedures.actions.block.standard.StandardBlockChooseResult
import dk.ilios.jervis.procedures.actions.block.standard.StandardBlockDetermineModifiers
import dk.ilios.jervis.procedures.actions.block.standard.StandardBlockRerollDice
import dk.ilios.jervis.procedures.actions.block.standard.StandardBlockRollDice
import dk.ilios.jervis.rules.BlockType
import dk.ilios.jervis.rules.Rules

/**
 * Wrap temporary data needed to track a "standard block". This can either
 * be part of a Blitz, a normal block action or multiple block .
 */
data class BlockContext(
    val attacker: Player,
    val defender: Player,
    val isBlitzing: Boolean = false,
    val isUsingJuggernaught: Boolean = false,
    val blockType: BlockType? = null,
    val isUsingMultiBlock: Boolean = false,
    val offensiveAssists: Int = 0,
    val defensiveAssists: Int = 0,
    val roll: List<BlockDieRoll> = emptyList(),
    val hasAcceptedResult: Boolean = false, // Do not want to reroll any further
    val resultIndex: Int = -1, // Index into `roll` that defines the selected roll
    val didFollowUp: Boolean = false,
    val aborted: Boolean = false,
): ProcedureContext {
    val result: DBlockResult
        get() = roll[resultIndex].result
}

/**
 * Procedure for handling a standard block once attacker and defender have been identified. This includes
 * rolling dice and resolving the result.
 *
 * This procedure is called as part of a [BlockAction] or [BlitzAction].
 */
object StandardBlockStep : Procedure() {
    override val initialNode: Node = DetermineAssists
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null
    override fun isValid(state: Game, rules: Rules) = state.assertContext<BlockContext>()

    object DetermineAssists: ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = StandardBlockDetermineModifiers
        override fun onExitNode(state: Game, rules: Rules): Command {
            return GotoNode(RollBlockDice)
        }
    }

    object RollBlockDice : ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = StandardBlockRollDice
        override fun onExitNode(state: Game, rules: Rules): Command {
            return GotoNode(SelectRerollType)
        }
    }

    object SelectRerollType : ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = StandardBlockChooseReroll
        override fun onExitNode(state: Game, rules: Rules): Command {
            return if (state.rerollContext != null) {
                GotoNode(RerollDice)
            } else {
                GotoNode(SelectBlockResult)
            }
        }
    }

    object RerollDice : ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = StandardBlockRerollDice
        override fun onExitNode(state: Game, rules: Rules): Command {
            return GotoNode(SelectRerollType)
        }
    }

    object SelectBlockResult : ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = StandardBlockChooseResult
        override fun onExitNode(state: Game, rules: Rules): Command {
            return GotoNode(ResolveBlockResult)
        }
    }

    object ResolveBlockResult : ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = StandardBlockApplyResult
        override fun onExitNode(state: Game, rules: Rules): Command {
            // Once the block die is resolved, the block step is over
            // and all injuries have been resolved
            return ExitProcedure()
        }
    }
}
