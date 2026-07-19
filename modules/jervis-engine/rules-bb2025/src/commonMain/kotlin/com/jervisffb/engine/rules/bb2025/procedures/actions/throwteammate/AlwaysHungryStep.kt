package com.jervisffb.engine.rules.bb2025.procedures.actions.throwteammate

import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.SetBallLocation
import com.jervisffb.engine.commands.SetBallState
import com.jervisffb.engine.commands.SetCurrentBall
import com.jervisffb.engine.commands.SetPlayerLocation
import com.jervisffb.engine.commands.SetPlayerState
import com.jervisffb.engine.commands.SetTurnOver
import com.jervisffb.engine.commands.buildCompositeCommand
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.fsm.ComputationNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.ParentNode
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.PlayerDogoutState
import com.jervisffb.engine.model.TurnOver
import com.jervisffb.engine.model.context.ProcedureContext
import com.jervisffb.engine.model.context.assertContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.locations.Dogout
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.bb2025.skills.AlwaysHungry
import com.jervisffb.engine.rules.common.procedures.Bounce
import com.jervisffb.engine.rules.common.procedures.D6DieRoll

data class AlwaysHungryContext(
    val thrower: Player,
    val thrownPlayer: Player,
    val isHungryRoll: D6DieRoll? = null,
    val isHungry: Boolean = false,
    val squirmFreeRoll: D6DieRoll? = null,
    val squirmedFree: Boolean = false,
) : ProcedureContext

/**
 * Procedure controlling rolling for [AlwaysHungry] including side-effects.
 * If the end result is the player being fumbled, this is handled by
 * [ThrowPlayerStep]
 */
object AlwaysHungryStep: Procedure() {
    override val initialNode: Node = RollForAlwaysHungry
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null
    override fun isValid(state: Game, rules: Rules) = state.assertContext<AlwaysHungryContext>()

    object RollForAlwaysHungry: ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = AlwaysHungryRoll
        override fun onExitNode(state: Game, rules: Rules): Command {
            val context = state.getContext<AlwaysHungryContext>()
            return when (context.isHungry) {
                true -> GotoNode(RollToEatPlayer)
                false -> ExitProcedure()
            }
        }
    }

    object RollToEatPlayer: ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = AlwaysHungrySquirmFreeRoll
        override fun onExitNode(state: Game, rules: Rules): Command {
            val context = state.getContext<AlwaysHungryContext>()
            return when (context.squirmedFree) {
                true -> ExitProcedure()
                false -> GotoNode(EatPlayer)
            }
        }
    }

    object EatPlayer: ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            val context = state.getContext<AlwaysHungryContext>()
            val thrownPlayer = context.thrownPlayer
            return buildCompositeCommand {
                addAll(
                    SetPlayerState(thrownPlayer, PlayerDogoutState.DEAD, hasTackleZones = false),
                    SetPlayerLocation(thrownPlayer, Dogout)
                )
                if (thrownPlayer.hasBall()) {
                    val ball = thrownPlayer.ball!!
                    addAll(
                        SetBallState.bouncing(ball),
                        SetBallLocation(ball, thrownPlayer.coordinates),
                        SetCurrentBall(ball),
                        GotoNode(BounceBallFromEatenPlayer)
                    )
                } else {
                    add(ExitProcedure())
                }
            }
        }
    }

    object BounceBallFromEatenPlayer: ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = Bounce
        override fun onExitNode(state: Game, rules: Rules): Command {
            return compositeCommandOf(
                SetTurnOver(TurnOver.STANDARD),
                SetCurrentBall(null),
                ExitProcedure()
            )
        }
    }
}
