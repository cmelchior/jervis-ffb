package com.jervisffb.engine.rules.common.procedures

import com.jervisffb.engine.actions.CancelWhenReady
import com.jervisffb.engine.actions.Confirm
import com.jervisffb.engine.actions.ConfirmWhenReady
import com.jervisffb.engine.actions.ContinueWhenReady
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.SetBallLocation
import com.jervisffb.engine.commands.SetBallState
import com.jervisffb.engine.commands.buildCompositeCommand
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
import com.jervisffb.engine.model.BallState
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.context.CatchContext
import com.jervisffb.engine.model.context.PassingInterferenceContext
import com.jervisffb.engine.model.context.ScoringATouchDownContext
import com.jervisffb.engine.model.context.assertContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.context.getContextOrNull
import com.jervisffb.engine.model.context.hasContext
import com.jervisffb.engine.model.isSkillAvailable
import com.jervisffb.engine.model.modifiers.CatchModifier
import com.jervisffb.engine.model.modifiers.DiceModifier
import com.jervisffb.engine.reports.ReportCatch
import com.jervisffb.engine.reports.ReportInterception
import com.jervisffb.engine.reports.ReportNoBallAffectingAction
import com.jervisffb.engine.reports.ReportSkillUsed
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.common.procedures.actions.move.ScoringATouchdown
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.engine.rules.common.tables.Weather
import com.jervisffb.engine.utils.INVALID_GAME_STATE

/**
 * Resolve a player attempting to catch the ball.
 *
 * See page 51 in the BB2020 rulebook.
 * See page 72 in the BB2025 rulebook.
 *
 * This procedure assumes that the parent already checked if the cath is valid
 * in the first place.
 */
object Catch : Procedure() {
    override val initialNode: Node = CheckForNoBallSkill
    override fun onEnterProcedure(state: Game, rules: Rules): Command ? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null
    override fun isValid(state: Game, rules: Rules) {
        state.assertContext<CatchContext>()
    }

    object CheckForNoBallSkill: ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            val context = state.getContext<CatchContext>()
            val player = context.catchingPlayer
            val hasNoBall = player.isSkillAvailable(SkillType.NO_BALL)
            return if (hasNoBall) {
                compositeCommandOf(
                    ReportNoBallAffectingAction(player, ReportNoBallAffectingAction.ActionType.CATCH),
                    SetBallState.bouncing(context.ball),
                    GotoNode(CatchFailed)
                )
            } else {
                GotoNode(ChooseToUseExtraArms)
            }
        }
    }

    object ChooseToUseExtraArms: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team {
            return state.getContext<CatchContext>().catchingPlayer.team
        }
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val context = state.getContext<CatchContext>()
            val player = context.catchingPlayer
            val hasExtraArms = player.isSkillAvailable(SkillType.EXTRA_ARMS)
            return when (hasExtraArms) {
                true -> listOf(ConfirmWhenReady, CancelWhenReady)
                false -> listOf(ContinueWhenReady)
            }
        }
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            val context = state.getContext<CatchContext>()
            val player = context.catchingPlayer
            val useExtraArms = (action == Confirm)
            return compositeCommandOf(
                if (useExtraArms) {
                    ReportSkillUsed(player, SkillType.EXTRA_ARMS)
                } else {
                    null
                },
                UpdateContext(context.copy(useExtraArms = useExtraArms)),
                GotoNode(ChooseToUseNervesOfSteel)
            )
        }
    }

    object ChooseToUseNervesOfSteel: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team {
            return state.getContext<CatchContext>().catchingPlayer.team
        }
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val context = state.getContext<CatchContext>()
            val player = context.catchingPlayer
            val hasExtraArms = player.isSkillAvailable(SkillType.NERVES_OF_STEEL)
            return when (hasExtraArms) {
                true -> listOf(ConfirmWhenReady, CancelWhenReady)
                false -> listOf(ContinueWhenReady)
            }
        }
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            val context = state.getContext<CatchContext>()
            val player = context.catchingPlayer
            val useNervesOfSteel = (action == Confirm)
            return compositeCommandOf(
                if (useNervesOfSteel) {
                    ReportSkillUsed(player, SkillType.NERVES_OF_STEEL)
                } else {
                    null
                },
                UpdateContext(context.copy(useNervesOfSteel = useNervesOfSteel)),
                GotoNode(ChooseToUseDivingCatch)
            )
        }
    }

    // This only applies if the ball is in the target square. This includes bounces if the first catch failed.
    object ChooseToUseDivingCatch: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team {
            return state.getContext<CatchContext>().catchingPlayer.team
        }
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val context = state.getContext<CatchContext>()
            val player = context.catchingPlayer
            val hasDivingCatch = player.isSkillAvailable(SkillType.DIVING_CATCH)
            val isOnTarget = (context.catchingPlayer.location == context.ball.location)
            return when (hasDivingCatch && isOnTarget) {
                true -> listOf(ConfirmWhenReady, CancelWhenReady)
                false -> listOf(ContinueWhenReady)
            }
        }
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            val context = state.getContext<CatchContext>()
            val player = context.catchingPlayer
            val useDivingCatch = (action == Confirm)
            return compositeCommandOf(
                if (useDivingCatch) {
                    ReportSkillUsed(player, SkillType.DIVING_CATCH)
                } else {
                    null
                },
                UpdateContext(context.copy(useDivingCatch = useDivingCatch)),
                GotoNode(CalculateModifiers)
            )
        }
    }

    object CalculateModifiers: ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            val context = state.getContext<CatchContext>()
            val modifiers = mutableListOf<DiceModifier>()
            val ballStateModifier = when (context.ball.state) {
                BallState.BOUNCING -> CatchModifier.BOUNCING
                BallState.DEVIATING -> CatchModifier.DEVIATED
                BallState.SCATTERED -> CatchModifier.SCATTERED
                BallState.THROW_IN -> CatchModifier.THROW_IN
                BallState.DEFLECTED -> CatchModifier.CONVERT_DEFLECTION
                else -> null
            }
            if (ballStateModifier != null) modifiers.add(ballStateModifier)

            // Add marked modifiers from other players, unless the catch has Nerves of Steel
            if (!context.useNervesOfSteel) {
                rules.addMarkedModifiers(
                    state,
                    context.catchingPlayer.team,
                    context.ball.location,
                    modifiers,
                    CatchModifier.MARKED
                )
            }

            // Check the weather
            if (state.weather == Weather.POURING_RAIN) {
                modifiers.add(CatchModifier.POURING_RAIN)
            }

            // Players with extra arms can use it
            if (context.useExtraArms) {
                modifiers.add(CatchModifier.EXTRA_ARMS)
            }

            if (context.useDivingCatch) {
                modifiers.add(CatchModifier.DIVING_CATCH)
            }

            // TODO Check for disturbing presence.
            return compositeCommandOf(
                UpdateContext(context.copy(modifiers = modifiers)),
                GotoNode(RollToCatch)
            )
        }
    }

    object RollToCatch : ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = CatchRoll
        override fun onExitNode(state: Game, rules: Rules): Command {
            val context = state.getContext<CatchContext>()
            val passingInterferenceContext = state.getContextOrNull<PassingInterferenceContext>()
            val roll = context.roll!!
            val ball = state.currentBall()
            return if (context.isSuccess) {
                buildCompositeCommand {
                    addAll(
                        SetBallState.carried(ball, context.catchingPlayer),
                        ReportCatch(context.catchingPlayer, context.catchingPlayer.agility, context.modifiers, roll.result, true),
                    )
                    if (ball.state == BallState.DEFLECTED) {
                        addAll(
                            UpdateContext(passingInterferenceContext!!.copy(didIntercept = true)),
                            ReportInterception(context.catchingPlayer, true)
                        )
                    }
                    add(GotoNode(CheckForTouchDown))
                }
            } else {
                buildCompositeCommand {
                    val newBallState = when (ball.state) {
                        BallState.DEFLECTED -> SetBallState.scattered(ball)
                        else -> SetBallState.bouncing(ball)
                    }
                    addAll(
                        newBallState,
                        ReportCatch(context.catchingPlayer, context.catchingPlayer.agility, context.modifiers, roll.result, false)
                    )
                    if (ball.state == BallState.DEFLECTED) {
                        add(ReportInterception(context.catchingPlayer, false))
                    }
                    add(GotoNode(CatchFailed))
                }
            }
        }
    }

    object CatchFailed : ParentNode() {
        // If a catch failed while resolving Diving Catch Players, we should not handle the failure here,
        // but transfer control back, so other players with Diving Catch get a chance to catch it.
        override fun skipNodeFor(state: Game, rules: Rules): Node? {
            val isResolvingDivingCatchPlayers = state.hasContext<DivingCatchContext>()
            return when (isResolvingDivingCatchPlayers) {
                true -> ExitProcedureNode
                false -> null
            }
        }
        override fun onEnterNode(state: Game, rules: Rules): Command? {
            val ball = state.currentBall()
            return when (ball.state) {
                BallState.SCATTERED -> {
                    val scatterContext = ScatterRollContext(from = ball.location)
                    AddContext(scatterContext)
                }
                else -> null
            }
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure {
            return if (state.currentBall().state == BallState.SCATTERED) {
                ScatterRoll
            } else {
                Bounce
            }
        }
        override fun onExitNode(state: Game, rules: Rules): Command {
            return compositeCommandOf(
                if (state.currentBall().state == BallState.SCATTERED) {
                    GotoNode(ResolveScatteredBallLanding)
                } else {
                    ExitProcedure()
                }
            )
        }
    }

    // The ball scattered after a failed catch attempt, now it needs to land.
    // This can either be on the ground, on a player or result in a throw-in because
    // it scattered out of bounds. This
    object ResolveScatteredBallLanding: ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            val scatterContext = state.getContext<ScatterRollContext>()
            val landOutOfBounds = (scatterContext.outOfBoundsAt != null)
            val landsOnCatchingPlayer = scatterContext.landsAt?.let {
                if (it.isOutOfBounds(rules)) {
                    false
                } else {
                    state.field[it].player?.let { player -> rules.canCatch(player) }
                }
            } ?: false
            return when {
                landOutOfBounds -> GotoNode(ScatteredBallLandingOutOfBounds)
                landsOnCatchingPlayer -> GotoNode(ScatteredBallLandingOnPlayer)
                !landsOnCatchingPlayer -> GotoNode(ScatteredBallLandingOnEmptySquare)
                else -> INVALID_GAME_STATE("Unexpected game state")
            }
        }
    }

    object ScatteredBallLandingOutOfBounds: ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command {
            val ball = state.currentBall()
            val scatterContext = state.getContext<ScatterRollContext>()
            val outOfBoundsAt = scatterContext.outOfBoundsAt ?: INVALID_GAME_STATE("Missing outOfBoundsAt: $scatterContext")
            return compositeCommandOf(
                RemoveContext(scatterContext),
                SetBallLocation(ball, scatterContext.landsAt!!),
                SetBallState.outOfBounds(ball, outOfBoundsAt),
                AddContext(ThrowInContext(
                    ball = ball,
                    outOfBoundsAt = outOfBoundsAt,
                )),
            )
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = ThrowIn
        override fun onExitNode(state: Game, rules: Rules): Command {
            return compositeCommandOf(
                RemoveContext<ThrowInContext>(),
                ExitProcedure()
            )
        }
    }

    object ScatteredBallLandingOnPlayer: ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command? {
            val ball = state.currentBall()
            val scatterContext = state.getContext<ScatterRollContext>()
            val landsAt = scatterContext.landsAt ?: INVALID_GAME_STATE("Missing landsAt: $scatterContext")
            return compositeCommandOf(
                RemoveContext(scatterContext),
                SetBallState.scattered(ball),
                SetBallLocation(ball, landsAt),
                AddContext(CatchContext(
                    state.field[landsAt].player!!,
                    ball
                ))
            )
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = Catch
        override fun onExitNode(state: Game, rules: Rules): Command {
            return compositeCommandOf(
                RemoveContext<CatchContext>(),
                ExitProcedure()
            )
        }
    }

    object ScatteredBallLandingOnEmptySquare: ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command {
            val ball = state.currentBall()
            val scatterContext = state.getContext<ScatterRollContext>()
            return compositeCommandOf(
                RemoveContext(scatterContext),
                SetBallState.bouncing(ball),
                SetBallLocation(ball, scatterContext.landsAt!!)
            )
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = Bounce
        override fun onExitNode(state: Game, rules: Rules): Command = ExitProcedure()
    }

    // If the catch succeeded, then we need to check if the player has a touchdown.
    object CheckForTouchDown : ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command {
            val context = state.getContext<CatchContext>()
            return AddContext(ScoringATouchDownContext(context.catchingPlayer))
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = ScoringATouchdown
        override fun onExitNode(state: Game, rules: Rules): Command {
            return compositeCommandOf(
                RemoveContext<ScoringATouchDownContext>(),
                ExitProcedure()
            )
        }
    }
}
