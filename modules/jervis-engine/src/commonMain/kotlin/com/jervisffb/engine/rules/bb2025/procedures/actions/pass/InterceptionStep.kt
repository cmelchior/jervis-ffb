package com.jervisffb.engine.rules.bb2025.procedures.actions.pass

import com.jervisffb.engine.actions.Cancel
import com.jervisffb.engine.actions.CancelWhenReady
import com.jervisffb.engine.actions.Confirm
import com.jervisffb.engine.actions.ConfirmWhenReady
import com.jervisffb.engine.actions.Continue
import com.jervisffb.engine.actions.ContinueWhenReady
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.actions.SelectPlayer
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.SetBallState
import com.jervisffb.engine.commands.SetTurnOver
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.context.AddContext
import com.jervisffb.engine.commands.context.RemoveContext
import com.jervisffb.engine.commands.context.UpdateContext
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.ComputationNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.ParentNode
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.fsm.castAction
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.TurnOver
import com.jervisffb.engine.model.context.ProcedureContext
import com.jervisffb.engine.model.context.assertContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.isSkillAvailable
import com.jervisffb.engine.model.locations.PitchCoordinate
import com.jervisffb.engine.model.modifiers.DiceModifier
import com.jervisffb.engine.model.modifiers.InterceptionModifier
import com.jervisffb.engine.reports.ReportInterception
import com.jervisffb.engine.reports.ReportSkillUsed
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.common.procedures.D6DieRoll
import com.jervisffb.engine.rules.common.procedures.actions.pass.PassContext
import com.jervisffb.engine.rules.common.procedures.actions.pass.PassingType
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.engine.utils.INVALID_GAME_STATE

data class InterceptionRollContext(
    val player: Player,
    val target: Int,
    val modifiers: List<DiceModifier> = emptyList(),
    val roll: D6DieRoll? = null,
    val isSuccess: Boolean = false
) : ProcedureContext {
    val rerolled: Boolean = roll?.rerollSource != null && roll.rerolledResult != null
}

data class InterceptionContext(
    val thrower: Player,
    // Target coordinates of the throw after resolving the throw type, but not including
    // scatter from a failed interception.
    val target: PitchCoordinate,
    val interceptingPlayer: Player? = null, // Player doing the interception, if any.
    val useCloudBurster: Boolean = false,
    val useExtraArms: Boolean = false, // If intercepting player is using Extra Arms or not
    val interceptionRoll: InterceptionRollContext? = null,
    val didIntercept: Boolean = false,
): ProcedureContext {
    // After passing interference, is the pass step allowed to continue or must it end
    val continueThrow = !didIntercept
}

/**
 * Procedure handling possible interceptions when throwing a ball or bomb.
 * If no players are eligible for interception, this procedure does nothing.
 *
 * Developer's Commentary:
 * For now, Cloud Burster is always used. The rationale is that if you want
 * the opponent to intercept it because it would be a disadvantage to them, they
 * could just opt out of intercepting.
 *
 * Very Long Legs is also always applied, leaving it up to the coach to decide
 * if they want to intercept or not. This means the sequence of events is:
 *
 * 1. Check if Cloud Burster is used
 * 2. If used, only players with Very Long Legs are eligible for interception.
 * 3. Confirm usage of Extra Arms if applicable.
 * 4. Roll interception dice.
 */
object InterceptionStep: Procedure() {
    override val initialNode: Node = CheckForCloudBurster
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null
    override fun isValid(state: Game, rules: Rules) {
        state.assertContext<InterceptionContext>()
        state.assertContext<PassContext>()
    }

    object CheckForCloudBurster: ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            val context = state.getContext<InterceptionContext>()
            val thrower = context.thrower
            val candidates = getInterceptionCandidates(rules, context)
            val anyVeryLongLegs = candidates.any { it.isSkillAvailable(SkillType.VERY_LONG_LEGS) }
            return if (candidates.isNotEmpty() && context.thrower.isSkillAvailable(SkillType.CLOUD_BURSTER)) {
                compositeCommandOf(
                    UpdateContext(context.copy(useCloudBurster = true)),
                    ReportSkillUsed(thrower, SkillType.CLOUD_BURSTER),
                    if (anyVeryLongLegs) GotoNode(SelectPlayerForInterception) else ExitProcedure()
                )
            } else {
                GotoNode(SelectPlayerForInterception)
            }
        }
    }

    object SelectPlayerForInterception : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.activePlayer!!.team.otherTeam()
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val context = state.getContext<InterceptionContext>()
            val candidates = getInterceptionCandidates(rules, context)
                .filter { player ->
                    // Check for CloudBurster and Very Long Legs combination
                    when (context.useCloudBurster) {
                        false -> true // All players are eligible
                        true -> player.isSkillAvailable(SkillType.VERY_LONG_LEGS) // Only Very Long Legged players are eligible if Cloud Burster is used
                    }
                }
                .filterNot { player ->
                    // Players with No Ball are not eligible for interception
                    player.isSkillAvailable(SkillType.NO_BALL)
                }

            return if (candidates.isNotEmpty()) {
                listOf(
                    SelectPlayer.fromPlayers(candidates),
                    CancelWhenReady
                )
            } else {
                // No eligible players found, continue to next step.
                listOf(ContinueWhenReady)
            }
        }
        override fun applyAction(
            action: GameAction,
            state: Game,
            rules: Rules
        ): Command {
            return when (action) {
                Continue, Cancel -> ExitProcedure()
                else -> {
                    castAction<PlayerSelected>(action) {
                        val context = state.getContext<InterceptionContext>()
                        compositeCommandOf(
                            UpdateContext(context.copy(interceptingPlayer = it.getPlayer(state))),
                            GotoNode(ChooseToUseExtraArms)
                        )
                    }
                }
            }
        }
    }

    object ChooseToUseExtraArms: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team {
            return state.getContext<InterceptionContext>().interceptingPlayer?.team ?: INVALID_GAME_STATE("Missing intercepting player")
        }

        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val player = state.getContext<InterceptionContext>().interceptingPlayer ?: INVALID_GAME_STATE("Missing intercepting player")
            return if (player.isSkillAvailable(SkillType.EXTRA_ARMS)) {
                listOf(ConfirmWhenReady, CancelWhenReady)
            } else {
                listOf(ContinueWhenReady)
            }
        }

        override fun applyAction(
            action: GameAction,
            state: Game,
            rules: Rules
        ): Command {
            val context = state.getContext<InterceptionContext>()
            val player = context.interceptingPlayer!!
            val useExtraArms = (action == Confirm)
            return compositeCommandOf(
                if (useExtraArms) {
                    ReportSkillUsed(player, SkillType.EXTRA_ARMS)
                } else {
                    null
                },
                UpdateContext(context.copy(useExtraArms = useExtraArms)),
                GotoNode(RollForInterception)
            )
        }
    }

    object RollForInterception : ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command {
            val passContext = state.getContext<PassContext>()
            val interceptionContext = state.getContext<InterceptionContext>()
            val player = interceptionContext.interceptingPlayer!!
            val modifiers = mutableListOf<DiceModifier>()
            rules.addMarkedModifiers(
                state,
                player.team,
                player.coordinates,
                modifiers,
                InterceptionModifier.MARKED
            )
            when (passContext.passingResult) {
                PassingType.ACCURATE -> InterceptionModifier.ACCURATE_PASS
                PassingType.INACCURATE -> InterceptionModifier.INACCURATE_PASS
                else -> INVALID_GAME_STATE("Unsupported pass result: ${passContext.passingResult}")
            }.let {
                modifiers.add(it)
            }
            if (interceptionContext.useExtraArms) {
                modifiers.add(InterceptionModifier.EXTRA_ARMS)
            }
            if (player.isSkillAvailable(SkillType.VERY_LONG_LEGS)) {
                modifiers.add(InterceptionModifier.VERY_LONG_LEGS)
            }
            if (player.isSkillAvailable(SkillType.STUNTY)) {
                modifiers.add(InterceptionModifier.STUNTY)
            }
            val context = InterceptionRollContext(
                player = player,
                target = player.agility,
                modifiers = modifiers
            )
            return AddContext(context)
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = InterceptionRoll
        override fun onExitNode(state: Game, rules: Rules): Command {
            val rollContext = state.getContext<InterceptionRollContext>()
            val interceptionContxt = state.getContext<InterceptionContext>()
            val interferencePlayer = interceptionContxt.interceptingPlayer ?: INVALID_GAME_STATE("Missing interception player")
            return if (rollContext.isSuccess) {
                compositeCommandOf(
                    UpdateContext(
                        interceptionContxt.copy(
                            interceptionRoll = rollContext,
                            didIntercept = true
                        )
                    ),
                    RemoveContext<InterceptionRollContext>(),
                    SetBallState.carried(state.currentBall(), rollContext.player),
                    ReportInterception(interceptionContxt.interceptingPlayer, true),
                    SetTurnOver(TurnOver.STANDARD),
                    ExitProcedure()
                )
            } else {
                // Player failed to intercept the ball
                compositeCommandOf(
                    UpdateContext(
                        interceptionContxt.copy(
                            interceptionRoll = rollContext,
                        )
                    ),
                    RemoveContext<InterceptionRollContext>(),
                    ReportInterception(interferencePlayer, false),
                    ExitProcedure()
                )
            }
        }
    }

    // HELPERS
    private fun getInterceptionCandidates(rules: Rules, context: InterceptionContext): List<Player> {
        return rules.rangeRuler.opponentPlayersUnderRuler(context.thrower, context.target)
    }
}
