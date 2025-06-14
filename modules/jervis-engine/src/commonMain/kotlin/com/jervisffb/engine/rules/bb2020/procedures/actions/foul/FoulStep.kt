package com.jervisffb.engine.rules.bb2020.procedures.actions.foul

import com.jervisffb.engine.actions.Cancel
import com.jervisffb.engine.actions.CancelWhenReady
import com.jervisffb.engine.actions.Confirm
import com.jervisffb.engine.actions.ConfirmWhenReady
import com.jervisffb.engine.actions.Continue
import com.jervisffb.engine.actions.ContinueWhenReady
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.commands.AddDiceModifier
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.context.RemoveContext
import com.jervisffb.engine.commands.SetBallLocation
import com.jervisffb.engine.commands.SetBallState
import com.jervisffb.engine.commands.SetCoachBanned
import com.jervisffb.engine.commands.context.SetContext
import com.jervisffb.engine.commands.SetCurrentBall
import com.jervisffb.engine.commands.SetPlayerLocation
import com.jervisffb.engine.commands.SetPlayerState
import com.jervisffb.engine.commands.SetTurnOver
import com.jervisffb.engine.commands.buildCompositeCommand
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.ComputationNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.ParentNode
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.PlayerState
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.TurnOver
import com.jervisffb.engine.model.context.assertContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.locations.DogOut
import com.jervisffb.engine.model.modifiers.BrilliantCoachingModifiers
import com.jervisffb.engine.model.modifiers.DefensiveAssistsModifier
import com.jervisffb.engine.model.modifiers.OffensiveAssistModifier
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.bb2020.procedures.Bounce
import com.jervisffb.engine.rules.bb2020.procedures.tables.injury.RiskingInjuryContext
import com.jervisffb.engine.rules.bb2020.procedures.tables.injury.RiskingInjuryMode
import com.jervisffb.engine.rules.bb2020.procedures.tables.injury.RiskingInjuryRoll
import com.jervisffb.engine.rules.bb2020.tables.ArgueTheCallResult
import com.jervisffb.engine.utils.INVALID_ACTION
import com.jervisffb.engine.utils.INVALID_GAME_STATE

/**
 * Procedure for handling the Foul part of a [FoulAction].
 */
object FoulStep: Procedure() {
    override val initialNode: Node = CalculateAssists
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null
    override fun isValid(state: Game, rules: Rules) = state.assertContext<FoulContext>()

    object CalculateAssists: ComputationNode() {
        // For now, assume that both sides want all assists to count at all time
        // Could there be a case where the defender wants the foul to succeed?
        override fun apply(state: Game, rules: Rules): Command {
            val context = state.getContext<FoulContext>()
            val fouler = context.fouler
            val victim = context.victim!!
            val offensiveAssists = rules.calculateOffensiveAssists(fouler, victim)
            val defensiveAssists = rules.calculateDefensiveAssists(victim, fouler)
            return compositeCommandOf(
                SetContext(context.copy(foulAssists = offensiveAssists, defensiveAssists = defensiveAssists)),
                GotoNode(RollForFoul)
            )
        }
    }

    object RollForFoul: ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command {
            val foulContext = state.getContext<FoulContext>()
            val injuryContext = RiskingInjuryContext(
                player = foulContext.victim!!,
                mode = RiskingInjuryMode.FOUL,
                armourModifiers = listOf(
                    OffensiveAssistModifier(foulContext.foulAssists),
                    DefensiveAssistsModifier(foulContext.defensiveAssists * -1)
                )
            )
            return SetContext(injuryContext)
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = RiskingInjuryRoll
        override fun onExitNode(state: Game, rules: Rules): Command {
            val foulContext =state.getContext<FoulContext>()
            val injuryContext = state.getContext<RiskingInjuryContext>()
            val spottedByRefArmour: Boolean = (injuryContext.armourRoll[0] == injuryContext.armourRoll[1])
            val spottedByRefInjury: Boolean = (injuryContext.injuryRoll.isNotEmpty() && (injuryContext.injuryRoll[0] == injuryContext.injuryRoll[1]))
            val spottedByRef = spottedByRefArmour || spottedByRefInjury
            return buildCompositeCommand {
                add(RemoveContext<RiskingInjuryContext>())
                add(
                    SetContext(foulContext.copy(
                        injuryRoll = injuryContext,
                        spottedByTheRef = spottedByRef,
                        hasFouled = true
                    ))
                )
                if (spottedByRef) {
                    // Regardless of the result of rolling on the Argue the Ref table
                    // a turn-over always happens.
                    add(SetTurnOver(TurnOver.STANDARD))
                    add(GotoNode(DecideToArgueTheCall))
                } else {
                    add(ExitProcedure())
                }
            }
        }
    }

    object DecideToArgueTheCall: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<FoulContext>().fouler.team
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            return if (state.activeTeamOrThrow().coachBanned) {
                // If the coach was already banned, they cannot argue the call again.
                listOf(ContinueWhenReady)
            } else {
                listOf(ConfirmWhenReady, CancelWhenReady)
            }
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            val context = state.getContext<FoulContext>()
            val foulerHadBall = context.fouler.hasBall()
            return when (action) {
                Cancel, Continue -> {
                    compositeCommandOf(
                        banPlayer(context.fouler),
                        SetContext(context.copy(argueTheCall = false)),
                        if (foulerHadBall) GotoNode(BounceBallWhenBanned) else ExitProcedure()
                    )
                }
                Confirm -> {
                    compositeCommandOf(
                        SetContext(context.copy(argueTheCall = true)),
                        GotoNode(RollForArgueThCall)
                    )
                }
                else -> INVALID_ACTION(action)
            }
        }
    }

    object RollForArgueThCall: ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = ArgueTheCallRoll
        override fun onExitNode(state: Game, rules: Rules): Command {
            val context = state.getContext<FoulContext>()
            val foulerHadBall = context.fouler.hasBall()
            return when (context.argueTheCallResult) {
                ArgueTheCallResult.YOURE_OUTTA_HERE -> {
                    compositeCommandOf(
                        SetCoachBanned(context.fouler.team, true),
                        AddDiceModifier(BrilliantCoachingModifiers.YOU_ARE_OUTTA_HERE, context.fouler.team.brilliantCoachingModifiers),
                        banPlayer(context.fouler),
                        if (foulerHadBall) GotoNode(BounceBallWhenBanned) else ExitProcedure()
                    )
                }
                ArgueTheCallResult.I_DONT_CARE -> {
                    compositeCommandOf(
                        banPlayer(context.fouler),
                        if (foulerHadBall) GotoNode(BounceBallWhenBanned) else ExitProcedure()
                    )
                }
                ArgueTheCallResult.WELL_IF_YOU_PUT_IT_LIKE_THAT -> {
                    // Nothing happens to the player (but a turn-over still happens)
                    ExitProcedure()
                }
                null -> INVALID_GAME_STATE("Missing argue the call result")
            }
        }
    }

    object BounceBallWhenBanned: ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = Bounce
        override fun onExitNode(state: Game, rules: Rules): Command {
            return ExitProcedure()
        }
    }

    // HELPER FUNCTIONS

    fun banPlayer(player: Player): Command {
        return buildCompositeCommand {
            if (player.hasBall()) {
                val ball = player.ball!!
                // Prepare the ball to bounce
                addAll(
                    SetBallState.bouncing(ball),
                    SetBallLocation(ball, player.coordinates),
                    SetCurrentBall(ball),
                )
            }
            addAll(
                SetPlayerState(player, PlayerState.BANNED),
                SetPlayerLocation(player, DogOut),
            )
        }
    }
}
