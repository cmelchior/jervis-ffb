package com.jervisffb.engine.bb2025.procedures.actions.ballandchain

import com.jervisffb.engine.bb2025.context.BallAndChainContext
import com.jervisffb.engine.bb2025.procedures.actions.block.push.PushedBack
import com.jervisffb.engine.bb2025.procedures.getResetChompedStateCommands
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.SetBallLocation
import com.jervisffb.engine.commands.SetBallState
import com.jervisffb.engine.commands.SetPlayerLocation
import com.jervisffb.engine.commands.buildCompositeCommand
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.context.AddContext
import com.jervisffb.engine.commands.context.RemoveContext
import com.jervisffb.engine.commands.context.UpdateContext
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.common.commands.SetCurrentBall
import com.jervisffb.engine.common.commands.SetPlayerMoveLeft
import com.jervisffb.engine.common.commands.SetPlayerRushesLeft
import com.jervisffb.engine.common.commands.SetTurnOver
import com.jervisffb.engine.common.context.BlockContext
import com.jervisffb.engine.common.context.RiskingInjuryContext
import com.jervisffb.engine.common.context.StandardBlockPolicy
import com.jervisffb.engine.common.context.ThrowInContext
import com.jervisffb.engine.common.procedures.ThrowIn
import com.jervisffb.engine.common.procedures.actions.move.ScoringATouchdown
import com.jervisffb.engine.common.procedures.getResetPlayerTemporaryModifiersCommands
import com.jervisffb.engine.common.procedures.tables.injury.RiskingInjuryMode
import com.jervisffb.engine.common.reports.ReportPushedIntoCrowd
import com.jervisffb.engine.fsm.ComputationNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.ParentNode
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.BallState
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.TurnOver
import com.jervisffb.engine.model.context.MovePlayerIntoSquareContext
import com.jervisffb.engine.model.context.RushRollContext
import com.jervisffb.engine.model.context.ScoringATouchDownContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.locations.Dogout
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.common.skills.Duration

/**
 * This procedure controls resolving one "step" during a Ball & Chain action
 * after a direction has been selected.
 *
 * See [BallAndChainAction]
 */
object BallAndChainStep : Procedure() {
    override val initialNode: Node = RollDirection
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null

    object RollDirection : ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = BallAndChainDirectionRoll
        override fun onExitNode(state: Game, rules: Rules): Command {
            val player = state.getContext<BallAndChainContext>().player
            return when (player.movesLeft > 0) {
                true -> compositeCommandOf(
                    SetPlayerMoveLeft(player, player.movesLeft - 1),
                    GotoNode(ResolveDestination)
                )
                false -> GotoNode(RollRush)
            }
        }
    }

    object RollRush : ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command {
            val context = state.getContext<BallAndChainContext>()
            return AddContext(RushRollContext(context.player, context.destination!!))
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = rules.rushRoll
        override fun onExitNode(state: Game, rules: Rules): Command {
            val context = state.getContext<BallAndChainContext>()
            val rush = state.getContext<RushRollContext>()
            return compositeCommandOf(
                SetPlayerRushesLeft(context.player, context.player.rushesLeft - 1),
                UpdateContext(context.copy(rushFailed = !rush.isSuccess)),
                RemoveContext(rush),
                GotoNode(ResolveDestination),
            )
        }
    }

    object ResolveDestination : ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            val context = state.getContext<BallAndChainContext>()
            val destination = context.destination!!
            if (destination.isOutOfBounds(rules)) return GotoNode(ResolveCrowd)
            val occupant = state.pitch[destination].player ?: return GotoNode(MoveIntoSquare)
            val isStanding = rules.isStanding(occupant)
            return compositeCommandOf(
                UpdateContext(context.copy(occupant = occupant)),
                AddContext(BlockContext(
                    attacker = context.player,
                    defender = occupant,
                    policy = StandardBlockPolicy(
                        ignoreFoulAppearance = true,
                        forceFollowUp = true,
                        pushWithoutBlockDice = !isStanding,
                        deferAttackerTouchdown = context.rushFailed,
                    ),
                )),
                GotoNode(when (isStanding) {
                    true -> BlockOccupant
                    false -> PushProneOccupant
                }),
            )
        }
    }

    object BlockOccupant : ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = rules.standardBlockStep
        override fun onExitNode(state: Game, rules: Rules): Command {
            val player = state.getContext<BallAndChainContext>().player
            return compositeCommandOf(
                RemoveContext<BlockContext>(),
                *getResetPlayerTemporaryModifiersCommands(state, rules, player, Duration.END_OF_ACTION),
                GotoNode(ResolvePendingFall),
            )
        }
    }

    object PushProneOccupant : ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = PushedBack
        override fun onExitNode(state: Game, rules: Rules): Command {
            return compositeCommandOf(
                RemoveContext<BlockContext>(),
                GotoNode(InjureProneOccupant),
            )
        }
    }

    object InjureProneOccupant : ParentNode() {
        override fun skipNodeFor(state: Game, rules: Rules): Node? {
            return when (state.getContext<BallAndChainContext>().occupant!!.location.isOnPitch(rules)) {
                true -> null
                false -> ResolvePendingFall
            }
        }
        override fun onEnterNode(state: Game, rules: Rules): Command {
            return AddContext(RiskingInjuryContext(
                player = state.getContext<BallAndChainContext>().occupant!!,
                mode = RiskingInjuryMode.HIT_WHILE_PRONE,
            ))
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = rules.riskingInjuryRoll
        override fun onExitNode(state: Game, rules: Rules): Command {
            return compositeCommandOf(
                RemoveContext<RiskingInjuryContext>(),
                GotoNode(ResolvePendingFall),
            )
        }
    }

    object MoveIntoSquare : ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command {
            val context = state.getContext<BallAndChainContext>()
            return AddContext(MovePlayerIntoSquareContext(context.player, context.destination!!, bounceLooseBallImmediately = true))
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = rules.movePlayerIntoSquare
        override fun onExitNode(state: Game, rules: Rules): Command {
            return compositeCommandOf(
                RemoveContext<MovePlayerIntoSquareContext>(),
                GotoNode(ResolvePendingFall),
            )
        }
    }

    object ResolveCrowd : ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command {
            val context = state.getContext<BallAndChainContext>()
            val player = context.player
            return buildCompositeCommand {
                player.ball?.let { ball ->
                    addAll(
                        SetBallLocation(ball, context.destination!!),
                        SetBallState.outOfBounds(ball, context.origin),
                        SetCurrentBall(ball),
                    )
                }
                getResetChompedStateCommands(player, Dogout)?.let { add(it) }
                addAll(
                    SetPlayerLocation(player, Dogout),
                    ReportPushedIntoCrowd(player, context.origin),
                    SetTurnOver(TurnOver.STANDARD),
                    AddContext(RiskingInjuryContext(player, mode = RiskingInjuryMode.PUSHED_INTO_CROWD)),
                )
            }
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = rules.riskingInjuryRoll
        override fun onExitNode(state: Game, rules: Rules): Command {
            return compositeCommandOf(
                RemoveContext<RiskingInjuryContext>(),
                when (state.currentBallOrNull()?.state == BallState.OUT_OF_BOUNDS) {
                    true -> GotoNode(ThrowBallBack)
                    false -> ExitProcedure()
                },
            )
        }
    }

    object ThrowBallBack : ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command {
            return AddContext(ThrowInContext(
                state.currentBall(),
                state.currentBall().outOfBoundsAt!!)
            )
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = ThrowIn
        override fun onExitNode(state: Game, rules: Rules): Command {
            return compositeCommandOf(
                RemoveContext<ThrowInContext>(),
                SetCurrentBall(null),
                ExitProcedure(),
            )
        }
    }

    object ResolvePendingFall : ParentNode() {
        override fun skipNodeFor(state: Game, rules: Rules): Node? {
            val context = state.getContext<BallAndChainContext>()
            return when (context.rushFailed && context.player.location.isOnPitch(rules) && rules.isStanding(context.player)) {
                true -> null
                false -> CheckForTouchdown
            }
        }
        override fun onEnterNode(state: Game, rules: Rules): Command {
            return AddContext(RiskingInjuryContext(
                player = state.getContext<BallAndChainContext>().player,
                mode = RiskingInjuryMode.FALLING_OVER,
            ))
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = rules.fallingOverStep
        override fun onExitNode(state: Game, rules: Rules): Command {
            return compositeCommandOf(
                RemoveContext<RiskingInjuryContext>(),
                ExitProcedure()
            )
        }
    }

    object CheckForTouchdown : ParentNode() {
        override fun skipNodeFor(state: Game, rules: Rules): Node? {
            val player = state.getContext<BallAndChainContext>().player
            return when (player.location.isOnPitch(rules) && rules.isStanding(player) && player.hasBall() && !state.isTurnOver()) {
                true -> null
                false -> ExitProcedureNode
            }
        }
        override fun onEnterNode(state: Game, rules: Rules): Command {
            return AddContext(ScoringATouchDownContext(state.getContext<BallAndChainContext>().player))
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
