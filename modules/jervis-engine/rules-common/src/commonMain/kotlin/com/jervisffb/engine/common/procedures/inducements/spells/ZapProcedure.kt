package com.jervisffb.engine.common.procedures.inducements.spells

import com.jervisffb.engine.actions.Cancel
import com.jervisffb.engine.actions.CancelWhenReady
import com.jervisffb.engine.actions.Continue
import com.jervisffb.engine.actions.ContinueWhenReady
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.SetBallLocation
import com.jervisffb.engine.commands.SetBallState
import com.jervisffb.engine.commands.TransformPlayer
import com.jervisffb.engine.commands.buildCompositeCommand
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.context.AddContext
import com.jervisffb.engine.commands.context.RemoveContext
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.common.comm.SetInducementEffectUsed
import com.jervisffb.engine.common.commands.SetCurrentBall
import com.jervisffb.engine.common.context.ResolveInducementEffectsContext
import com.jervisffb.engine.common.procedures.Bounce
import com.jervisffb.engine.common.reports.ReportZapResult
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.ParentNode
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.context.ProcedureContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.context.getContextOrNull
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.common.procedures.D6DieRoll
import com.jervisffb.engine.rules.common.skills.Duration
import com.jervisffb.engine.utils.INVALID_ACTION
import com.jervisffb.engine.utils.INVALID_GAME_STATE
import com.jervisffb.engine.utils.requireGameState

data class ZapContext(
    val spell: ZapSpell,
    val target: Player,
    val roll: D6DieRoll? = null,
    val isSuccess: Boolean = false,
): ProcedureContext

/**
 * Procedure handling the effect of using the "Zap!" Spell available to
 * Sports-Wizards.
 *
 * See page 149 in the BB2025 Rulebook.
 */
object ZapProcedure: Procedure() {
    override val initialNode: Node = SelectPlayer
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? {
        val inducementContext = state.getContext<ResolveInducementEffectsContext>()
        val zapContext = state.getContextOrNull<ZapContext>()
        return when (zapContext != null) {
            true -> {
                compositeCommandOf(
                    SetInducementEffectUsed(inducementContext.inducement, used = (zapContext.roll != null)),
                    RemoveContext(zapContext)
                )
            }
            false -> null
        }
    }
    override fun isValid(state: Game, rules: Rules) {
        state.getContext<ResolveInducementEffectsContext>().let {
            requireGameState(!it.inducement.used) { "Inducement already used: ${it.inducement}" }
        }
    }

    object SelectPlayer : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<ResolveInducementEffectsContext>().team
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val players = (state.awayTeam.noToPlayer.values + state.homeTeam.noToPlayer.values)
                .filter { it.location.isOnPitch(rules) }

            return when (players.isNotEmpty()) {
                true -> listOf(com.jervisffb.engine.actions.SelectPlayer.fromPlayers(players), CancelWhenReady)
                false -> listOf(ContinueWhenReady)
            }
        }
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return when (action) {
                Continue,
                Cancel -> ExitProcedure()
                is PlayerSelected -> {
                    val inducementContext = state.getContext<ResolveInducementEffectsContext>()
                    val spell = inducementContext.inducement as ZapSpell
                    val context = ZapContext(spell, action.getPlayer(state))
                    compositeCommandOf(
                        AddContext(context),
                        GotoNode(RollToTransform)
                    )
                }
                else -> INVALID_ACTION(action)
            }
        }
    }

    object RollToTransform: ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = ZapRoll
        override fun onExitNode(state: Game, rules: Rules): Command {
            val context = state.getContext<ZapContext>()

            return buildCompositeCommand {
                when (context.isSuccess) {
                    true -> {
                        add(ReportZapResult(context))
                        // The player keeps their identity while transformed, so every reference
                        // to them stays valid. See `PlayerTransformation`.
                        context.spell.getFrogPositionData(context.target).let { frogPosition ->
                            add(TransformPlayer(context.target, frogPosition, Duration.END_OF_DRIVE))
                        }
                        when (context.target.hasBall()) {
                            true -> add(GotoNode(BounceBall))
                            false -> add(ExitProcedure())
                        }
                    }
                    false -> {
                        addAll(
                            ReportZapResult(context),
                            ExitProcedure()
                        )
                    }
                }
            }
        }
    }

    object BounceBall: ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command {
            val frog = state.getContext<ZapContext>().target
            val ball = frog.ball ?: INVALID_GAME_STATE("Ball not found: ${frog.id}")
            return compositeCommandOf(
                SetCurrentBall(ball),
                SetBallLocation(ball, frog.coordinates),
                SetBallState.bouncing(ball),
            )
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = Bounce
        override fun onExitNode(state: Game, rules: Rules): Command {
            return compositeCommandOf(
                SetCurrentBall(null),
                ExitProcedure(),
            )
        }
    }
}
