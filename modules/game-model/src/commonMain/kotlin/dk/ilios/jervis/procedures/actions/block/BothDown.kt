package dk.ilios.jervis.procedures.actions.block

import compositeCommandOf
import dk.ilios.jervis.actions.ActionDescriptor
import dk.ilios.jervis.actions.Cancel
import dk.ilios.jervis.actions.CancelWhenReady
import dk.ilios.jervis.actions.Confirm
import dk.ilios.jervis.actions.ConfirmWhenReady
import dk.ilios.jervis.actions.Continue
import dk.ilios.jervis.actions.ContinueWhenReady
import dk.ilios.jervis.actions.GameAction
import dk.ilios.jervis.commands.Command
import dk.ilios.jervis.commands.RemoveContext
import dk.ilios.jervis.commands.SetContext
import dk.ilios.jervis.commands.SetHasTackleZones
import dk.ilios.jervis.commands.SetPlayerState
import dk.ilios.jervis.commands.fsm.ExitProcedure
import dk.ilios.jervis.commands.fsm.GotoNode
import dk.ilios.jervis.fsm.ActionNode
import dk.ilios.jervis.fsm.ComputationNode
import dk.ilios.jervis.fsm.Node
import dk.ilios.jervis.fsm.ParentNode
import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.Player
import dk.ilios.jervis.model.PlayerState
import dk.ilios.jervis.model.Team
import dk.ilios.jervis.model.context.ProcedureContext
import dk.ilios.jervis.model.context.getContext
import dk.ilios.jervis.procedures.tables.injury.KnockedDown
import dk.ilios.jervis.procedures.tables.injury.RiskingInjuryContext
import dk.ilios.jervis.reports.ReportBothDownResult
import dk.ilios.jervis.rules.Rules
import dk.ilios.jervis.rules.skills.Block
import dk.ilios.jervis.utils.INVALID_ACTION

data class BothDownContext(
    val attacker: Player,
    val defender: Player,
    val attackUsesBlock: Boolean = false,
    val defenderUsesBlock: Boolean = false,
    val attackerUsesWrestle: Boolean = false,
    val defenderUsesWrestle: Boolean = false,
) : ProcedureContext

/**
 * Resolve a "Both Down" selected as a block result.
 * See page 57 in the rulebook.
 */
object BothDown: Procedure() {
    override val initialNode: Node = AttackerChooseToUseWrestle
    override fun onEnterProcedure(state: Game, rules: Rules): Command? {
        val blockContext = state.getContext<BlockResultContext>()
        return SetContext(BothDownContext(
            blockContext.attacker,
            blockContext.defender,
        ))
    }
    override fun onExitProcedure(state: Game, rules: Rules): Command? {
        return compositeCommandOf(
            ReportBothDownResult(state.getContext<BothDownContext>()),
            RemoveContext<BothDownContext>()
        )
    }

    object AttackerChooseToUseWrestle: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<BothDownContext>().attacker.team
        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
            val context = state.getContext<BothDownContext>()
            // TODO Figure out how to check for Wrestle
            val hasWrestle = false
            return when (hasWrestle) {
                true -> listOf(ConfirmWhenReady, CancelWhenReady)
                false -> listOf(ContinueWhenReady)
            }
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            val context = state.getContext<BothDownContext>()
            val useWrestle = when (action) {
                Confirm -> true
                Cancel,
                Continue -> false
                else -> INVALID_ACTION(action)
            }
            return compositeCommandOf(
                SetContext(context.copy(attackerUsesWrestle = useWrestle)),
                GotoNode(DefenderChooseToUseWrestle)
            )
        }
    }

    object DefenderChooseToUseWrestle: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<BothDownContext>().defender.team
        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
            val context = state.getContext<BothDownContext>()
            // TODO Figure out how to check for Wrestle
            val hasWrestle = false
            return when (hasWrestle) {
                true -> listOf(ConfirmWhenReady, CancelWhenReady)
                false -> listOf(ContinueWhenReady)
            }
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            val context = state.getContext<BothDownContext>()
            val useWrestle = when (action) {
                Confirm -> true
                Cancel,
                Continue -> false
                else -> INVALID_ACTION(action)
            }
            return compositeCommandOf(
                SetContext(context.copy(defenderUsesWrestle = useWrestle)),
                GotoNode(AttackerChooseToUseBlock)
            )
        }
    }

    object AttackerChooseToUseBlock: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<BothDownContext>().attacker.team
        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
            val context = state.getContext<BothDownContext>()
            val hasBlock = (context.attacker.getSkillOrNull<Block>() != null)
            return when (hasBlock) {
                true -> listOf(ConfirmWhenReady, CancelWhenReady)
                false -> listOf(ContinueWhenReady)
            }
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            val context = state.getContext<BothDownContext>()
            val useBlock = when (action) {
                Confirm -> true
                Cancel,
                Continue -> false
                else -> INVALID_ACTION(action)
            }
            return compositeCommandOf(
                SetContext(context.copy(attackUsesBlock = useBlock)),
                GotoNode(DefenderChooseToUseBlock)
            )
        }
    }

    object DefenderChooseToUseBlock: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<BothDownContext>().defender.team
        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
            val context = state.getContext<BothDownContext>()
            val hasBlock = context.defender.getSkillOrNull<Block>() != null
            return when (hasBlock) {
                true -> listOf(ConfirmWhenReady, CancelWhenReady)
                false -> listOf(ContinueWhenReady)
            }
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            val context = state.getContext<BothDownContext>()
            val useBlock = when (action) {
                Confirm -> true
                Cancel,
                Continue -> false
                else -> INVALID_ACTION(action)
            }
            return compositeCommandOf(
                SetContext(context.copy(defenderUsesBlock = useBlock)),
                GotoNode(ResolveBothDown)
            )
        }
    }

    object ResolveBothDown: ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            val context = state.getContext<BothDownContext>()

            // If Wrestle was used, both players are just placed prone and nothing more happens.
            // Otherwise check if one or both players need to roll injury
            if (context.attackerUsesWrestle || context.defenderUsesWrestle) {
                return compositeCommandOf(
                    SetPlayerState(context.attacker, PlayerState.PRONE, hasTackleZones = false),
                    SetPlayerState(context.defender, PlayerState.PRONE, hasTackleZones = false),
                    ExitProcedure()
                )
            } else {
                return GotoNode(ResolveDefenderInjury)
            }
        }
    }

    object ResolveDefenderInjury: ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            val context = state.getContext<BothDownContext>()
            return if (!context.defenderUsesBlock) {
                GotoNode(RollDefenderInjury)
            } else {
                GotoNode(ResolveAttackerInjury)
            }
        }
    }

    object RollDefenderInjury: ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command {
            val context = state.getContext<BothDownContext>()
            return SetContext(RiskingInjuryContext(context.defender))
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = KnockedDown
        override fun onExitNode(state: Game, rules: Rules): Command {
            return GotoNode(ResolveAttackerInjury)
        }
    }

    object ResolveAttackerInjury: ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            val context = state.getContext<BothDownContext>()
            return if (!context.attackUsesBlock) {
                GotoNode(RollAttackerInjury)
            } else {
                ExitProcedure()
            }
        }
    }

    object RollAttackerInjury: ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command {
            val context = state.getContext<BothDownContext>()
            return SetContext(RiskingInjuryContext(context.attacker))
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = KnockedDown
        override fun onExitNode(state: Game, rules: Rules): Command {
            // Attacker went down, so its turn ends immediately, commonly because it is a turnover,
            // but if it happened during a kick-off blitz, it just ends the Blitz.
            return compositeCommandOf(
                RemoveContext<RiskingInjuryContext>(),
                ExitProcedure()
            )
        }
    }
}
