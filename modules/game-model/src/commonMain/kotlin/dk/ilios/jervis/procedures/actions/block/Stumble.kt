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
import dk.ilios.jervis.commands.ExitProcedure
import dk.ilios.jervis.commands.GotoNode
import dk.ilios.jervis.commands.SetContext
import dk.ilios.jervis.commands.SetPlayerState
import dk.ilios.jervis.fsm.ActionNode
import dk.ilios.jervis.fsm.Node
import dk.ilios.jervis.fsm.ParentNode
import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.Player
import dk.ilios.jervis.model.PlayerState
import dk.ilios.jervis.model.ProcedureContext
import dk.ilios.jervis.procedures.injury.RiskingInjuryRoll
import dk.ilios.jervis.procedures.injury.RiskingInjuryRollContext
import dk.ilios.jervis.reports.ReportPowResult
import dk.ilios.jervis.rules.Rules
import dk.ilios.jervis.rules.skills.Dodge
import dk.ilios.jervis.rules.skills.Tackle
import dk.ilios.jervis.utils.INVALID_ACTION


data class StumbleContext(
    val attacker: Player,
    val defender: Player,
    val attackerUsesTackle: Boolean = false,
    val defenderUsesDodge: Boolean = false,
) : ProcedureContext {
    fun isDefenderDown(): Boolean {
        return !defenderUsesDodge || attackerUsesTackle
    }
}

/**
 * Resolve a Stumble when selected on a block die.
 * See page 57 in the rulebook.
 */
object Stumble: Procedure() {
    override val initialNode: Node = ChooseToUseTackle

    override fun onEnterProcedure(state: Game, rules: Rules): Command {
        val blockContext = state.blockRollResultContext!!
        val stumbleContext = StumbleContext(
            blockContext.attacker,
            blockContext.defender,
        )
        return SetContext(Game::stumbleContext, stumbleContext)
    }

    override fun onExitProcedure(state: Game, rules: Rules): Command? {
        val context = state.pushContext!!
        return compositeCommandOf(
            SetContext(Game::pushContext, null),
            ReportPowResult(context.pusher, context.pushee)
        )
    }

    object ChooseToUseTackle: ActionNode() {
        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
            val stumbleContext = state.stumbleContext!!
            return if (stumbleContext.attacker.getSkillOrNull<Tackle>()?.isAvailable() == true) {
                listOf(ConfirmWhenReady, CancelWhenReady)
            } else {
                listOf(ContinueWhenReady)
            }
        }
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            val useTackle = when(action) {
                Confirm -> true
                Cancel,
                Continue -> false
                else -> INVALID_ACTION(action)
            }
            val updatedContext = state.stumbleContext!!.copy(attackerUsesTackle = useTackle)
            return compositeCommandOf(
                SetContext(Game::stumbleContext, updatedContext),
                if (useTackle) GotoNode(ResolvePush) else GotoNode(ChooseToUseDodge)
            )
        }
    }

    object ChooseToUseDodge: ActionNode() {
        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
            val stumbleContext = state.stumbleContext!!
            return if (stumbleContext.defender.getSkillOrNull<Dodge>()?.isAvailable() == true) {
                listOf(ConfirmWhenReady, CancelWhenReady)
            } else {
                listOf(ContinueWhenReady)
            }
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            val useDodge = when(action) {
                Confirm -> true
                Cancel,
                Continue -> false
                else -> INVALID_ACTION(action)
            }
            val updatedContext = state.stumbleContext!!.copy(defenderUsesDodge = useDodge)
            return compositeCommandOf(
                SetContext(Game::stumbleContext, updatedContext),
                GotoNode(ResolvePush)
            )
        }
    }

    // Push the player, including chain pushes. At the end of the push, the player
    // is Knocked Down if either the attacker was using Tackle or the defender
    // didn't have Dodge.
    object ResolvePush: ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command? {
            val pushContext = createPushContext(state)
            return SetContext(Game::pushContext, pushContext)
        }

        override fun getChildProcedure(state: Game, rules: Rules): Procedure = PushStep

        override fun onExitNode(state: Game, rules: Rules): Command {
            val context = state.stumbleContext!!
            return if (context.defender.location.isOnField(rules) && context.isDefenderDown()) {
                GotoNode(ResolvePlayerDown)
            } else {
                ExitProcedure()
            }
        }
    }

    // If the player is still on the field, resolve them going down.
    // Otherwise, it was resolved as part of the Chain Push
    object ResolvePlayerDown: ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command? {
            val defender = state.stumbleContext!!.defender
            val injuryContext = RiskingInjuryRollContext(defender)
            return compositeCommandOf(
                SetPlayerState(defender, PlayerState.KNOCKED_DOWN),
                SetContext(Game::riskingInjuryRollsContext, injuryContext)
            )
        }

        override fun getChildProcedure(state: Game, rules: Rules): Procedure {
            return RiskingInjuryRoll
        }

        override fun onExitNode(state: Game, rules: Rules): Command {
            return compositeCommandOf(
                SetContext(Game::riskingInjuryRollsContext, null),
                ExitProcedure()
            )
        }
    }
}
