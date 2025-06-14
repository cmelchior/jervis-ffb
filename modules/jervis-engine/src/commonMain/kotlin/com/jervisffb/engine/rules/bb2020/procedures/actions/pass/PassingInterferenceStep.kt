package com.jervisffb.engine.rules.bb2020.procedures.actions.pass

import com.jervisffb.engine.actions.Continue
import com.jervisffb.engine.actions.ContinueWhenReady
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.actions.SelectPlayer
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.SetBallLocation
import com.jervisffb.engine.commands.SetBallState
import com.jervisffb.engine.commands.SetTurnOver
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.context.RemoveContext
import com.jervisffb.engine.commands.context.SetContext
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.ParentNode
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.fsm.checkTypeAndValue
import com.jervisffb.engine.model.BallState
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.TurnOver
import com.jervisffb.engine.model.context.PassingInterferenceContext
import com.jervisffb.engine.model.context.PassingInterferenceRollContext
import com.jervisffb.engine.model.context.assertContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.modifiers.DiceModifier
import com.jervisffb.engine.model.modifiers.PassingInterferenceModifier
import com.jervisffb.engine.reports.ReportDeflection
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.bb2020.procedures.Catch
import com.jervisffb.engine.utils.INVALID_GAME_STATE

/**
 * Procedure for checking for passing interference as part of a [PassAction].
 *
 * See page 50 in the rulebook.
 */
object PassingInterferenceStep: Procedure() {
    override val initialNode: Node = SelectPlayerForInterference
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null
    override fun isValid(state: Game, rules: Rules) {
        state.assertContext<PassingInterferenceContext>()
        state.assertContext<PassContext>()
    }

    object SelectPlayerForInterference : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.activePlayer!!.team.otherTeam()
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val context = state.getContext<PassingInterferenceContext>()
            val selectPlayerAction = rules.rangeRuler.opponentPlayersUnderRuler(context.thrower, context.target)
                .let { eligiblePlayers ->
                    if (eligiblePlayers.isNotEmpty()) {
                        SelectPlayer(eligiblePlayers.map { player -> player.id })
                    } else {
                        // No eligible players found, continue to next step.
                        ContinueWhenReady
                    }
                }

            return listOf(selectPlayerAction)
        }
        override fun applyAction(
            action: GameAction,
            state: Game,
            rules: Rules
        ): Command {
            return when (action) {
                Continue -> ExitProcedure()
                else -> {
                    checkTypeAndValue<PlayerSelected>(state, action) {
                        val context = state.getContext<PassingInterferenceContext>()
                        compositeCommandOf(
                            SetContext(context.copy(interferencePlayer = it.getPlayer(state))),
                            GotoNode(RollForInterference)
                        )
                    }
                }
            }
        }
    }

    object RollForInterference : ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command {
            val passContext = state.getContext<PassContext>()
            val interferenceContext = state.getContext<PassingInterferenceContext>()
            val player = interferenceContext.interferencePlayer!!
            val modifiers = mutableListOf<DiceModifier>()
            rules.addMarkedModifiers(
                state,
                player.team,
                player.coordinates,
                modifiers,
                PassingInterferenceModifier.MARKED
            )
            when (passContext.passingResult) {
                PassingType.ACCURATE -> PassingInterferenceModifier.ACCURATE_PASS
                PassingType.INACCURATE -> PassingInterferenceModifier.INACCURATE_PASS
                PassingType.WILDLY_INACCURATE -> PassingInterferenceModifier.WILDLY_INACCURATE_PASS
                PassingType.FUMBLED -> null
                null -> INVALID_GAME_STATE("Missing passing result value")
            }?.let {
                modifiers.add(it)
            }
            val context = PassingInterferenceRollContext(
                player = player,
                target = player.agility,
                modifiers = modifiers
            )
            return SetContext(context)
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = PassingInterferenceRoll
        override fun onExitNode(state: Game, rules: Rules): Command {
            val rollContext = state.getContext<PassingInterferenceRollContext>()
            val interferenceContext = state.getContext<PassingInterferenceContext>()
            val interferencePlayer = interferenceContext.interferencePlayer ?: INVALID_GAME_STATE("Missing interference player")
            return if (rollContext.isSuccess) {
                compositeCommandOf(
                    SetContext(
                        interferenceContext.copy(
                            didDeflect = true,
                            interferenceRoll = rollContext,
                        )
                    ),
                    RemoveContext<PassingInterferenceRollContext>(),
                    SetBallState.deflected(state.currentBall()),
                    SetBallLocation(state.currentBall(), interferencePlayer.coordinates),
                    ReportDeflection(interferenceContext.interferencePlayer, true),
                    GotoNode(ConvertDeflectionToInterception)
                )
            } else {
                // Player failed to deflect the ball, abort immediately.
                return compositeCommandOf(
                    SetContext(
                        interferenceContext.copy(
                            interferenceRoll = rollContext,
                        )
                    ),
                    RemoveContext<PassingInterferenceRollContext>(),
                    ReportDeflection(interferencePlayer, false),
                    ExitProcedure()
                )
            }
        }
    }

    object ConvertDeflectionToInterception: ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = Catch
        override fun onExitNode(state: Game, rules: Rules): Command {
            val context = state.getContext<PassingInterferenceContext>()
            val interferencePlayer = context.interferencePlayer ?: INVALID_GAME_STATE("Missing interference player")

            // We have 3 cases after passing interference
            // 1. It didn't succeed, and the ball is still in the air on the
            //    way to the target.
            // 2. The ball was deflected or intercepted and ended up in the
            //    hands of the opponent or on the floor. This is a turnover.
            // 3. The ball was deflected or intercepted and ended up in the
            //    hands of the thrower team. This ends the pass but is not a
            //    turnover.
            val ball = state.currentBall()
            val isThrowContinues = !context.didDeflect
            val isTurnover = (ball.state == BallState.ON_GROUND) || rules.teamHasBall(interferencePlayer.team, ball)
            val endPass = rules.teamHasBall(context.thrower.team, ball)

            return when {
                isThrowContinues || endPass -> ExitProcedure()
                isTurnover -> {
                    compositeCommandOf(
                        SetTurnOver(TurnOver.STANDARD),
                        ExitProcedure()
                    )
                }
                else -> INVALID_GAME_STATE("Wrong game state")
            }
        }
    }
}
