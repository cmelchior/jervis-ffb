package dk.ilios.jervis.procedures.actions.block

import compositeCommandOf
import dk.ilios.jervis.actions.BlockDice
import dk.ilios.jervis.actions.DBlockResult
import dk.ilios.jervis.commands.Command
import dk.ilios.jervis.commands.RemoveContext
import dk.ilios.jervis.commands.SetContext
import dk.ilios.jervis.commands.fsm.ExitProcedure
import dk.ilios.jervis.commands.fsm.GotoNode
import dk.ilios.jervis.fsm.ComputationNode
import dk.ilios.jervis.fsm.Node
import dk.ilios.jervis.fsm.ParentNode
import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.Player
import dk.ilios.jervis.model.context.ProcedureContext
import dk.ilios.jervis.model.context.assertContext
import dk.ilios.jervis.model.context.getContext
import dk.ilios.jervis.procedures.BlockDieRoll
import dk.ilios.jervis.rules.BlockType
import dk.ilios.jervis.rules.Rules

/**
 * Wrap temporary data needed to track a Block step
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
): ProcedureContext

data class BlockRollContext(
    val attacker: Player,
    val defender: Player,
    val isBlitzing: Boolean = false,
    val isUsingMultiBlock: Boolean = false,
    val roll: List<BlockDieRoll> = emptyList(),
    val result: DBlockResult,
): ProcedureContext

/**
 * Procedure for handling a block once attacker and defender have been identified. This includes
 * rolling dice and resolving the result.
 *
 * This procedure is called as part of a [BlockAction] or [BlitzAction].
 */
object BlockStep : Procedure() {
    override val initialNode: Node = DetermineAssists
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command {
        return compositeCommandOf(
            RemoveContext<BlockContext>(),
            RemoveContext<BlockRollContext>()
        )
    }
    override fun isValid(state: Game, rules: Rules) = state.assertContext<BlockContext>()

    // Horns are applied before applying any other skills/traits and before counting assists
    // See page 78 in the rulebook.
    object ResolveHorns : ComputationNode() {
        override fun apply(
            state: Game,
            rules: Rules,
        ): Command {
            // TODO Implement Horns logic. Modify strength using the modifier system
            return GotoNode(ResolveDauntless)
        }
    }

    // Dauntless is applied before counting assists
    // See page 76 in the rulebook.
    object ResolveDauntless : ComputationNode() {
        override fun apply(
            state: Game,
            rules: Rules,
        ): Command {
            // TODO Implement Dauntless logic. Multiple block/Dauntless should just modify the players
            //  strength through the modifier system.
            return GotoNode(DetermineAssists)
        }
    }

    // Offensive/Defensive assists. Technically, you are allowed to choose whether to assist.
    // However, I cannot come up with a single (even bad) reason for why you would ever choose
    // to not assist, so we just automatically include all assists on both sides
    object DetermineAssists : ComputationNode() {
        override fun apply(
            state: Game,
            rules: Rules,
        ): Command {
            val context = state.getContext<BlockContext>()
            val offensiveAssists =
                context.defender.coordinates.getSurroundingCoordinates(rules)
                    .mapNotNull { state.field[it].player }
                    .count { player -> rules.canOfferAssistAgainst(player, context.defender) }

            val defensiveAssists =
                context.attacker.coordinates.getSurroundingCoordinates(rules)
                    .mapNotNull { state.field[it].player }
                    .count { player -> rules.canOfferAssistAgainst(player, context.attacker) }

            return compositeCommandOf(
                SetContext(context.copy(offensiveAssists = offensiveAssists, defensiveAssists = defensiveAssists)),
                GotoNode(RollBlockDice),
            )
        }
    }

    object RollBlockDice : ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = BlockRoll
        override fun onExitNode(state: Game, rules: Rules): Command {
            return GotoNode(ResolveBlockDie)
        }
    }

    object ResolveBlockDie : ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure {
            // Select sub procedure based on the result of the die.
            return when(state.getContext<BlockRollContext>().result.blockResult) {
                BlockDice.PLAYER_DOWN -> PlayerDown
                BlockDice.BOTH_DOWN -> BothDown
                BlockDice.PUSH_BACK -> PushBack
                BlockDice.STUMBLE -> Stumble
                BlockDice.POW -> Pow
            }
        }

        override fun onExitNode(state: Game, rules: Rules): Command {
            // Once the block die is resolved, the block step is over
            // Block Actions will also quit immediately, while Blitz actions
            // might allow further movement
            return ExitProcedure()
        }

    }
}
