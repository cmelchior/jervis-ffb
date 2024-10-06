package com.jervisffb.engine.rules.bb2020.procedures.actions.block

import compositeCommandOf
import com.jervisffb.engine.actions.ActionDescriptor
import com.jervisffb.engine.actions.Cancel
import com.jervisffb.engine.actions.CancelWhenReady
import com.jervisffb.engine.actions.Confirm
import com.jervisffb.engine.actions.ConfirmWhenReady
import com.jervisffb.engine.actions.Continue
import com.jervisffb.engine.actions.ContinueWhenReady
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.RemoveContext
import com.jervisffb.engine.commands.SetContext
import com.jervisffb.engine.commands.SetPlayerState
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.ParentNode
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.PlayerState
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.context.ProcedureContext
import com.jervisffb.engine.model.context.assertContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.hasSkill
import com.jervisffb.engine.rules.bb2020.procedures.tables.injury.KnockedDown
import com.jervisffb.engine.rules.bb2020.procedures.tables.injury.RiskingInjuryContext
import com.jervisffb.engine.reports.ReportStumbleResult
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.bb2020.skills.Dodge
import com.jervisffb.engine.rules.bb2020.skills.Tackle
import com.jervisffb.engine.utils.INVALID_ACTION


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
        val blockContext = state.getContext<BlockContext>()
        val stumbleContext = StumbleContext(
            blockContext.attacker,
            blockContext.defender,
        )
        return SetContext(stumbleContext)
    }
    override fun onExitProcedure(state: Game, rules: Rules): Command? {
        val context = state.getContext<PushContext>()
        val stumbleContext = state.getContext<StumbleContext>()
        return compositeCommandOf(
            RemoveContext<PushContext>(),
            ReportStumbleResult(context.firstPusher, context.firstPushee, stumbleContext.isDefenderDown())
        )
    }
    override fun isValid(state: Game, rules: Rules) = state.assertContext<BlockContext>()

    object ChooseToUseTackle: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team? = state.getContext<StumbleContext>().attacker.team
        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
            val stumbleContext = state.getContext<StumbleContext>()
            return if (stumbleContext.attacker.hasSkill<Tackle>()) {
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
            val updatedContext = state.getContext<StumbleContext>().copy(attackerUsesTackle = useTackle)
            return compositeCommandOf(
                SetContext(updatedContext),
                if (useTackle) GotoNode(ResolvePush) else GotoNode(ChooseToUseDodge)
            )
        }
    }

    object ChooseToUseDodge: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team? = state.getContext<StumbleContext>().defender.team
        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
            val stumbleContext = state.getContext<StumbleContext>()
            return if (stumbleContext.defender.hasSkill<Dodge>()) {
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
            val updatedContext = state.getContext<StumbleContext>().copy(defenderUsesDodge = useDodge)
            return compositeCommandOf(
                SetContext(updatedContext),
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
            return SetContext(pushContext)
        }

        override fun getChildProcedure(state: Game, rules: Rules): Procedure = PushStep

        override fun onExitNode(state: Game, rules: Rules): Command {
            val context = state.getContext<StumbleContext>()
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
        override fun onEnterNode(state: Game, rules: Rules): Command {
            val defender = state.getContext<StumbleContext>().defender
            val blockContext = state.getContext<BlockContext>()
            val injuryContext = RiskingInjuryContext(
                player = defender,
                isPartOfMultipleBlock = blockContext.isUsingMultiBlock
            )
            return compositeCommandOf(
                SetPlayerState(defender, PlayerState.KNOCKED_DOWN, hasTackleZones = false),
                SetContext(injuryContext)
            )
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = KnockedDown
        override fun onExitNode(state: Game, rules: Rules): Command {
            return compositeCommandOf(
                RemoveContext<RiskingInjuryContext>(),
                ExitProcedure()
            )
        }
    }
}
