package com.jervisffb.engine.rules.bb2025.procedures.actions.block.push

import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.SetBallLocation
import com.jervisffb.engine.commands.SetBallState
import com.jervisffb.engine.commands.SetCurrentBall
import com.jervisffb.engine.commands.buildCompositeCommand
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.context.RemoveContext
import com.jervisffb.engine.commands.context.SetContext
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.fsm.ComputationNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.ParentNode
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.Ball
import com.jervisffb.engine.model.BallState
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.context.ProcedureContext
import com.jervisffb.engine.model.context.PushContext
import com.jervisffb.engine.model.context.ScoringATouchDownContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.locations.Location
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.bb2025.procedures.actions.block.BB2025PushBack
import com.jervisffb.engine.rules.bb2025.procedures.actions.block.MultipleBlockAction
import com.jervisffb.engine.rules.common.procedures.Bounce
import com.jervisffb.engine.rules.common.procedures.ThrowIn
import com.jervisffb.engine.rules.common.procedures.ThrowInContext
import com.jervisffb.engine.rules.common.procedures.actions.move.ScoringATouchdown
import com.jervisffb.engine.rules.common.procedures.tables.injury.RiskingInjuryContext
import com.jervisffb.engine.rules.common.procedures.tables.injury.RiskingInjuryMode
import com.jervisffb.engine.rules.common.procedures.tables.injury.RiskingInjuryRoll
import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.persistentSetOf

data class ResolvePushChainEventsContext(
    val currentPushChainIndex: Int = -1,
    val currentPushStep: PushContext.PushData? = null,
    val resolvingAttacker: Player? = null,
    val attackerResolved: Boolean = false,
    val visitedSquares: PersistentSet<Location> = persistentSetOf(),
): ProcedureContext {
    fun getCurrentPlayer(): Player {
        return currentPushStep?.pushee ?: resolvingAttacker ?: error("No player found: $this")
    }
}

/**
 * A Pushback is split into multiple phases to support both normal blocks and
 * Multiple Block as their order of resolution differs.
 *
 * This procesdure is responsible for resolving all events triggered by moving
 * player's part of the Push Chain created in [CreatePushChainStep], and moved
 * in [MovePlayersInPushChainStep]. This happens after the choice to follow up.
 *
 * See [BB2025PushBack] and [MultipleBlockAction] for more details on each case.
 *
 * Developer's Commentary:
 * The order in which events in a push chain is resolved in a Push Chain is not
 * specified in the rules, which means it is up to interpretation and "best
 * guess". Note, for the common case, where there is only one ball and no
 * trapdoors, this order doesn't really matter. But as soon as we enter a world
 * with multiple balls and floor hazards, the order becomes quite important.
 *
 * A longer discussion of this can be found in `rules-faq-bb2025.md`. So here
 * we mostly focus on the implementation.
 *
 * Push Chain events are resolved the following way:
 *
 * 1. We resolve all events, one square at a time, starting with the defender,
 *    then the rest of the chain, and finally the attacker.
 *
 * 2. If there is a bouncing ball in the square, it will now bounce until it
 *    comes to rest. This can trigger a touchdown.
 *
 * 3. If standing on a trapdoor. This must now be rolled.
 *    a. If failed, the player will roll for Injury and resolve it immediately.
 *    b. If failed and holding the ball, the ball will bounce until it comes to
 *       rest. This can trigger a Touchdown.
 *
 * 4. If pushed into the crowd. This must now be resolved.
 *    a. Roll for Injury and resolve this injury.
 *    b. Throw the ball back in until it is at rest. This can trigger a
 *       Touchdown.
 *
 * 5. The player is now standing fully in the square will all other events
 *    resolved.
 *    a. If that player is holding a ball, check if a Touchdown is scored.
 */
object ResolveEventsInPushChainStep: Procedure() {
    override val initialNode: Node = DetermineNextSquare
    override fun onEnterProcedure(state: Game, rules: Rules): Command {
        return SetContext(ResolvePushChainEventsContext())
    }
    override fun onExitProcedure(state: Game, rules: Rules): Command {
        return RemoveContext<ResolvePushChainEventsContext>()
    }

    object DetermineNextSquare: ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            val pushContext = state.getContext<PushContext>()
            val resolveContext = state.getContext<ResolvePushChainEventsContext>()
            val nextIndex = resolveContext.currentPushChainIndex + 1
            val nextPush = pushContext.pushChain.getOrNull(nextIndex)

            // We only want to check each location once, and due to circular pushes, the same square might be
            // in the Push Chain multiple times.
            return if (nextPush != null && nextPush.to!! !in resolveContext.visitedSquares) {
                compositeCommandOf(
                    SetContext(resolveContext.copy(
                        currentPushChainIndex = nextIndex,
                        currentPushStep = nextPush,
                        visitedSquares = resolveContext.visitedSquares.add(nextPush.to!!)
                    )),
                    GotoNode(ResolveBouncingBall)
                )

            } else if (!resolveContext.attackerResolved) {
                compositeCommandOf(
                    SetContext(resolveContext.copy(
                        currentPushChainIndex = nextIndex,
                        currentPushStep = null,
                        resolvingAttacker = pushContext.firstPusher,
                        attackerResolved = true
                    )),
                )
            } else {
                ExitProcedure()
            }
        }
    }

    object ResolveBouncingBall: ParentNode() {
        override fun skipNodeFor(state: Game, rules: Rules): Node? {
            val context = state.getContext<ResolvePushChainEventsContext>()
            val isOnField = stepIsOnField(context, rules)
            val containsBouncingBall = stepContainsBouncingBall(context, state, rules)
            return if (isOnField && containsBouncingBall) {
                null
            } else {
                ResolveTrapdoor
            }
        }
        override fun onEnterNode(state: Game, rules: Rules): Command {
            val context = state.getContext<ResolvePushChainEventsContext>()
            val ball = getBouncingBall(context, state)
            return SetCurrentBall(ball)
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = Bounce
        override fun onExitNode(state: Game, rules: Rules): Command {
            return compositeCommandOf(
                SetCurrentBall(null),
                GotoNode(ResolveTrapdoor)
            )
        }
    }

    object ResolveTrapdoor: ParentNode() {
        override fun skipNodeFor(state: Game, rules: Rules): Node? {
            val context = state.getContext<ResolvePushChainEventsContext>()
            return if (!stepContainsTrapdoor(context, state, rules)) {
                ResolveCrowdInjury
            } else {
                null
            }
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure {
            TODO("Not yet implemented")
        }
        override fun onExitNode(state: Game, rules: Rules): Command {
            TODO("Not yet implemented")
        }
    }

    object ResolveCrowdInjury: ParentNode() {
        override fun skipNodeFor(state: Game, rules: Rules): Node? {
            val context = state.getContext<ResolvePushChainEventsContext>()
            return if (!stepIsInTheCrowd(context, state, rules)) {
                CheckForTouchdown
            } else {
                null
            }
        }
        override fun onEnterNode(state: Game, rules: Rules): Command? {
            // Players should only be in the Crowd if part of the push chain, the attacker cannot end up
            // there without being pushed.
            val context = state.getContext<ResolvePushChainEventsContext>()
            val pushStep = context.currentPushStep ?: error("Missing push step: $context")
            val player = pushStep.pushee
            return buildCompositeCommand {
                // If player had the ball, prepare it to be thrown in again.
                // But it will not happen until next step in the sequence
                if (player.hasBall()) {
                    val ball = player.ball!!
                    val throwContext = ThrowInContext(
                        ball = ball,
                        outOfBoundsAt = pushStep.from,
                    )
                    addAll(
                        SetBallLocation(ball, pushStep.to!!),
                        SetBallState.outOfBounds(ball, pushStep.from),
                        SetContext(throwContext)
                    )
                }
                val injuryContext = RiskingInjuryContext(
                    player = player,
                    mode = RiskingInjuryMode.PUSHED_INTO_CROWD
                )
                add(SetContext(injuryContext))
            }
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = RiskingInjuryRoll
        override fun onExitNode(state: Game, rules: Rules): Command {
            val ball = state.balls.firstOrNull { it.state == BallState.OUT_OF_BOUNDS }
            return compositeCommandOf(
                RemoveContext<RiskingInjuryContext>(),
                if (ball != null) SetCurrentBall(ball) else null,
                GotoNode(if (ball != null) ThrowInBall else CheckForTouchdown)
            )
        }
    }

    object ThrowInBall: ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command {
            val ball = state.currentBall()
            return SetContext(
                ThrowInContext(
                    ball,
                    ball.outOfBoundsAt!!
                )
            )
        }
        override fun getChildProcedure(state: Game, rules: Rules) = ThrowIn
        override fun onExitNode(state: Game, rules: Rules): Command {
            return compositeCommandOf(
                RemoveContext<ThrowInContext>(),
                SetCurrentBall(null),
                GotoNode(CheckForTouchdown)
            )
        }
    }

    object CheckForTouchdown: ParentNode() {
        override fun skipNodeFor(state: Game, rules: Rules): Node? {
            val context = state.getContext<ResolvePushChainEventsContext>()
            val player = context.getCurrentPlayer()
            return if (!checkPlayerForTouchdown(player)) DetermineNextSquare else null
        }

        override fun onEnterNode(state: Game, rules: Rules): Command? {
            val context = state.getContext<ResolvePushChainEventsContext>()
            val player = context.getCurrentPlayer()
            return SetContext(ScoringATouchDownContext(player))
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = ScoringATouchdown
        override fun onExitNode(state: Game, rules: Rules): Command {
            return compositeCommandOf(
                RemoveContext<ScoringATouchDownContext>(),
                GotoNode(DetermineNextSquare)
            )
        }
    }

    // --- HELPER METHODS ---

    private fun stepIsOnField(context: ResolvePushChainEventsContext, rules: Rules): Boolean {
        with(context) {
            return currentPushStep?.to?.isOnField(rules) ?: resolvingAttacker?.location?.isOnField(rules) ?: false
        }
    }

    private fun stepContainsBouncingBall(context: ResolvePushChainEventsContext, state: Game, rules: Rules): Boolean {
        if (!stepIsOnField(context, rules)) return false
        with(context) {
            return if (currentPushStep != null) {
                val coord = currentPushStep.to
                if (coord != null && coord.isOnField(rules)) {
                    state.field[coord].balls.any { it.state == BallState.BOUNCING }
                } else {
                    false
                }
            } else if (resolvingAttacker != null && resolvingAttacker.location.isOnField(rules)) {
                val coord = resolvingAttacker.coordinates
                state.field[coord].balls.any { it.state == BallState.BOUNCING }
            } else {
                false
            }
        }
    }

    private fun getBouncingBall(context: ResolvePushChainEventsContext, state: Game): Ball {
        with(context) {
            return if (currentPushStep != null) {
                val coord = currentPushStep.to ?: error("No target coordinates found: $context")
                state.field[coord].balls.first { it.state == BallState.BOUNCING }
            } else if (resolvingAttacker != null) {
                val coord = resolvingAttacker.coordinates
                state.field[coord].balls.first { it.state == BallState.BOUNCING }
            } else {
                error("No bouncing ball: $context")
            }
        }
    }

    private fun stepContainsTrapdoor(context: ResolvePushChainEventsContext, state: Game, rules: Rules): Boolean {
        if (!stepIsOnField(context, rules)) return false
        // TODO Add support for Trapdoors
        return false
    }

    private fun stepIsInTheCrowd(context: ResolvePushChainEventsContext, state: Game, rules: Rules): Boolean {
        return !stepIsOnField(context, rules)
    }

    private fun checkPlayerForTouchdown(player: Player): Boolean {
        val game = player.team.game
        val rules = game.rules
        return !game.isTurnOver()
            && rules.isStanding(player)
            && player.hasBall()
    }
}
