package dk.ilios.jervis.procedures.actions.block.standard

import compositeCommandOf
import dk.ilios.jervis.commands.Command
import dk.ilios.jervis.commands.SetContext
import dk.ilios.jervis.commands.fsm.ExitProcedure
import dk.ilios.jervis.commands.fsm.GotoNode
import dk.ilios.jervis.fsm.ComputationNode
import dk.ilios.jervis.fsm.Node
import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.context.assertContext
import dk.ilios.jervis.model.context.getContext
import dk.ilios.jervis.procedures.actions.block.BlockContext
import dk.ilios.jervis.rules.Rules

/**
 * Calculate all modifiers before rolling the block dice.
 *
 * @see [dk.ilios.jervis.procedures.actions.block.MultipleBlockStep]
 * @see [dk.ilios.jervis.procedures.actions.block.StandardBlockStep]
 */
object StandardBlockDetermineModifiers: Procedure() {
    override val initialNode: Node = DetermineAssists
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null
    override fun isValid(state: Game, rules: Rules) {
        state.assertContext<BlockContext>()
    }

    // Horns are applied before applying any other skills/traits and before counting assists
    // See page 78 in the rulebook.
    object ResolveHorns : ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            // TODO Implement Horns logic. Modify strength using the modifier system
            return GotoNode(ResolveDauntless)
        }
    }

    // Dauntless is applied before counting assists
    // See page 76 in the rulebook.
    object ResolveDauntless : ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            // TODO Implement Dauntless logic. Multiple block/Dauntless should just modify the players
            //  strength through the modifier system.
            return GotoNode(DetermineAssists)
        }
    }

    // Offensive/Defensive assists. Technically, you are allowed to choose whether to assist.
    // However, I cannot come up with a single (even bad) reason for why you would ever choose
    // to not assist, so we just automatically include all assists on both sides
    object DetermineAssists : ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
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
                ExitProcedure()
            )
        }
    }
}
