package com.jervisffb.engine.rules.common.procedures.actions.foul

import com.jervisffb.engine.actions.Cancel
import com.jervisffb.engine.actions.CancelWhenReady
import com.jervisffb.engine.actions.Confirm
import com.jervisffb.engine.actions.ConfirmWhenReady
import com.jervisffb.engine.actions.Continue
import com.jervisffb.engine.actions.ContinueWhenReady
import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.commands.AddDiceModifier
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.SetBallLocation
import com.jervisffb.engine.commands.SetBallState
import com.jervisffb.engine.commands.SetBribeUsed
import com.jervisffb.engine.commands.SetCoachBanned
import com.jervisffb.engine.commands.SetCurrentBall
import com.jervisffb.engine.commands.SetPlayerLocation
import com.jervisffb.engine.commands.SetPlayerState
import com.jervisffb.engine.commands.SetTurnOver
import com.jervisffb.engine.commands.buildCompositeCommand
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.context.UpdateContext
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.ComputationNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.ParentNode
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.PlayerDogoutState
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.TurnOver
import com.jervisffb.engine.model.context.ProcedureContext
import com.jervisffb.engine.model.context.assertContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.locations.Dogout
import com.jervisffb.engine.model.modifiers.BrilliantCoachingModifiers
import com.jervisffb.engine.reports.ReportBribeResult
import com.jervisffb.engine.reports.ReportBribeUsed
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.builder.GameVersion
import com.jervisffb.engine.rules.common.procedures.Bounce
import com.jervisffb.engine.rules.common.procedures.D6DieRoll
import com.jervisffb.engine.rules.common.procedures.EndOfDriveSequence
import com.jervisffb.engine.rules.common.procedures.getResetChompedStateCommands
import com.jervisffb.engine.rules.common.tables.ArgueTheCallResult
import com.jervisffb.engine.utils.INVALID_ACTION
import com.jervisffb.engine.utils.INVALID_GAME_STATE


data class BeingSentOffContext(
    val player: Player,
    val argueTheCall: Boolean = false,
    val argueTheCallRoll: D6DieRoll? = null,
    val argueTheCallResult: ArgueTheCallResult? = null,
    val isBribeAvailable: Boolean = false,
    val usedBribe: Boolean = false,
    val bribeRoll: D6DieRoll? = null,
): ProcedureContext

/**
 * Procedure controlling "Being Sent-off".
 *
 * See page 69 in the BB2025 rulebook.
 */
object BeingSentOff: Procedure() {
    override val initialNode: Node = DecideToArgueTheCall
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null
    override fun isValid(state: Game, rules: Rules) = state.assertContext<BeingSentOffContext>()

    object DecideToArgueTheCall: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<BeingSentOffContext>().player.team
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val context = state.getContext<BeingSentOffContext>()
            return if (context.player.team.coachBanned) {
                // If the coach was already banned, they cannot argue the call again.
                listOf(ContinueWhenReady)
            } else {
                listOf(ConfirmWhenReady, CancelWhenReady)
            }
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            val context = state.getContext<BeingSentOffContext>()
            val canBribe = context.player.team.bribes.any { !it.used }
            return when (action) {
                Cancel,
                Continue -> {
                    compositeCommandOf(
                        UpdateContext(context.copy(argueTheCall = false)),
                        GotoNode(if (canBribe) ChooseToUseBribe else ResolveBeingSentOff)
                    )
                }
                Confirm -> {
                    compositeCommandOf(
                        UpdateContext(context.copy(argueTheCall = true)),
                        GotoNode(RollForArgueTheCall)
                    )
                }
                else -> INVALID_ACTION(action)
            }
        }
    }

    object RollForArgueTheCall: ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = ArgueTheCallRoll
        override fun onExitNode(state: Game, rules: Rules): Command = GotoNode(ChooseToUseBribe)
    }

    object ChooseToUseBribe: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<BeingSentOffContext>().player.team
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val context = state.getContext<BeingSentOffContext>()
            val hasBribes = context.player.team.bribes.any { !it.used }
            return when (hasBribes) {
                true -> listOf(ConfirmWhenReady, CancelWhenReady)
                false -> listOf(ContinueWhenReady)
            }
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            val context = state.getContext<BeingSentOffContext>()
            val useBribe = (action == Confirm)
            val player = context.player
            val team = player.team
            return when (useBribe) {
                true -> {
                    val bribeToUse = team.bribes.firstOrNull { !it.used } ?: INVALID_GAME_STATE("No bribes available")
                    compositeCommandOf(
                        ReportBribeUsed(context.player),
                        SetBribeUsed(bribeToUse, true),
                        UpdateContext(context.copy(usedBribe = true)),
                        GotoNode(RollForBribe)
                    )
                }
                false -> GotoNode(ResolveBeingSentOff)
            }
        }
    }

    object RollForBribe: ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure  = BribeRoll
        override fun onExitNode(state: Game, rules: Rules): Command = GotoNode(ResolveBeingSentOff)
    }

    object ResolveBeingSentOff: ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            val context = state.getContext<BeingSentOffContext>()
            val player = context.player
            val playerHadBall = player.hasBall()

            return buildCompositeCommand {
                // If a Bribe is used, we ignore the result with the exception being the coach
                // is still banned if "You're Outta Here" was rolled
                val bribeSuccess = (context.usedBribe && isBribeSuccess(context.bribeRoll!!.result))
                val banPlayer = (!bribeSuccess && context.argueTheCallResult != ArgueTheCallResult.WELL_IF_YOU_PUT_IT_LIKE_THAT)
                val banCoach = (context.argueTheCallResult == ArgueTheCallResult.YOURE_OUTTA_HERE)
                val endOfDrive = state.stack.containsProcedure(EndOfDriveSequence)

                if (context.isBribeAvailable && context.usedBribe) {
                    add(ReportBribeResult(context.player.team, bribeSuccess))
                }

                // Only way to prevent a turnover is when using a Bribe, otherwise all
                // results on Argue the Call table will cause a turnover.
                if (!bribeSuccess) {
                    add(SetTurnOver(TurnOver.STANDARD))
                }

                if (banCoach) {
                    addAllNonNull(
                        SetCoachBanned(player.team, true),
                        when (rules.baseVersion == GameVersion.BB2020) {
                            true -> AddDiceModifier(BrilliantCoachingModifiers.YOU_ARE_OUTTA_HERE, player.team.brilliantCoachingModifiers)
                            false -> null
                        }
                    )
                }

                if (banPlayer) {
                    add(buildCompositeCommand {
                        if (playerHadBall) {
                            // Prepare the ball to bounce
                            val ball = player.ball!!
                            addAll(
                                SetBallState.bouncing(ball),
                                SetBallLocation(ball, player.coordinates),
                                SetCurrentBall(ball),
                            )
                        }
                        addAll(
                            SetPlayerState(player, PlayerDogoutState.BANNED),
                            SetPlayerLocation(player, Dogout),
                        )
                        getResetChompedStateCommands(player)?.let { add(it) }
                    })
                }

                add(if (banPlayer && playerHadBall && !endOfDrive) GotoNode(BounceBallWhenBanned) else ExitProcedure())
            }
        }
    }

    object BounceBallWhenBanned: ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = Bounce
        override fun onExitNode(state: Game, rules: Rules): Command {
            return compositeCommandOf(
                SetCurrentBall(null),
                ExitProcedure()
            )
        }
    }

    // HELPER FUNCTIONS

    private fun isBribeSuccess(result: D6Result): Boolean {
        return result.value >= 2
    }
}
