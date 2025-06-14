package com.jervisffb.engine.rules.bb2020.procedures.actions.block

import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.SetCurrentBall
import com.jervisffb.engine.commands.buildCompositeCommand
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.context.RemoveContext
import com.jervisffb.engine.commands.context.SetContext
import com.jervisffb.engine.commands.context.SetContextProperty
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.fsm.ComputationNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.ParentNode
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.Ball
import com.jervisffb.engine.model.BallState
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.bb2020.procedures.Bounce
import com.jervisffb.engine.rules.bb2020.procedures.ThrowIn
import com.jervisffb.engine.rules.bb2020.procedures.ThrowInContext
import com.jervisffb.engine.rules.bb2020.procedures.actions.move.ScoringATouchDownContext
import com.jervisffb.engine.rules.bb2020.procedures.actions.move.ScoringATouchdown
import com.jervisffb.engine.utils.INVALID_GAME_STATE

/**
 * This procedure is controlling the last part of a Push Chain (after the follow-up).
 * The is contained in its own procedure because it differs slightly from how
 * it works in Multiple Block, whereas the first part is shared between single and
 * multiple block.
 *
 * @see [PushStepInitialMoveSequence] For the full description of the Push Step sequence for single blocks.
 * @see [MultipleBlockAction] For the full description of sequence in Multiple Block.
 */
object PushStepResolveSingleBlockPushChain: Procedure() {
    override val initialNode: Node = CheckForTrapDoors
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command {
        return compositeCommandOf(
            RemoveContext<ThrowInContext>()
        )
    }
    override fun isValid(state: Game, rules: Rules) {
        val context = state.getContext<PushContext>()
        if (context.isMultipleBlock) {
            INVALID_GAME_STATE("Use XXX instead")
        }
    }

    // TODO
    object CheckForTrapDoors: ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            return GotoNode(CheckDefenderPushChainForScoringAfterTrapdoors)
        }
    }

    /**
     * After moving all players (and resolving trapdoors), check if any players
     * starting from the defender to the end of the push chain is holding the
     * ball in a scoring position. If a touchdown is scored, we skip checking
     * remaining players.
     */
    object CheckDefenderPushChainForScoringAfterTrapdoors: ParentNode() {
        override fun skipNodeFor(state: Game, rules: Rules): Node? {
            val context = state.getContext<PushContext>()
            val pushStep = context.pushChain.firstOrNull { checkPlayerForTouchdown(it) }
            return if (pushStep == null) CheckAttackerForScoringAfterTrapdoors else null
        }
        override fun onEnterNode(state: Game, rules: Rules): Command {
            val context = state.getContext<PushContext>()
            val push = context.pushChain.first { checkPlayerForTouchdown(it) }
            val player = push.pushee
            return SetContext(ScoringATouchDownContext(player))
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = ScoringATouchdown
        override fun onExitNode(state: Game, rules: Rules): Command {
            val scoreContext =  state.getContext<ScoringATouchDownContext>()
            val context = state.getContext<PushContext>()
            // Mark the step we just checked as complete.
            val pushData: PushContext.PushData = context.pushChain.firstNotNullOf { el ->
                if (el.pushee == scoreContext.player) el else null
            }
            return compositeCommandOf(
                RemoveContext<ScoringATouchDownContext>(),
                SetContextProperty(PushContext.PushData::checkedForScoringAfterTrapdoors, pushData, true),
                GotoNode(CheckAttackerForScoringAfterTrapdoors)
            )
        }
        private fun checkPlayerForTouchdown(step: PushContext.PushData): Boolean {
            val game = step.pushee.team.game
            val rules = game.rules
            return !game.isTurnOver()
                && rules.isStanding(step.pushee)
                && step.pushee.hasBall()
                && !step.checkedForScoringAfterTrapdoors
        }
    }

    object CheckAttackerForScoringAfterTrapdoors: ParentNode() {
        override fun skipNodeFor(state: Game, rules: Rules): Node? {
            val context = state.getContext<PushContext>()
            val player = context.firstPusher
            return if (!state.isTurnOver() && rules.isStanding(player) && player.hasBall()) null else ResolveBallEvents
        }
        override fun onEnterNode(state: Game, rules: Rules): Command {
            val context = state.getContext<PushContext>()
            val player = context.firstPusher
            return SetContext(ScoringATouchDownContext(player))
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = ScoringATouchdown
        override fun onExitNode(state: Game, rules: Rules): Command {
            return compositeCommandOf(
                RemoveContext<ScoringATouchDownContext>(),
                GotoNode(ResolveBallEvents)
            )
        }
    }

    /**
     * As a last step of the push chain, we fully resolve all ball events
     * (bouncing/thrown-in), starting from the defender's location, going down
     * the push chain and ending with the attacker. All balls must be fully
     * resolved, but only the first to trigger a touchdown will count.
     */
    object ResolveBallEvents: ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            // Find first square with unhandled ball in it.
            val context = state.getContext<PushContext>()

            // First run through squares we know are on the field
            var selectedBall: Ball? = null
            for (step in 0..< context.pushChain.size) {
                val square = context.pushChain[step].to!!
                if (square.isOnField(rules)) {
                    val ball = state.field[square].balls.singleOrNull()
                    if (ball != null && ball.state == BallState.BOUNCING) {
                        selectedBall = ball
                        break
                    }
                }
            }

            // Then Check throw-in
            if (selectedBall == null && context.pushChain.last().to == FieldCoordinate.OUT_OF_BOUNDS) {
                val ball = state.balls.singleOrNull { it.state == BallState.OUT_OF_BOUNDS }
                selectedBall = ball
            }

            // Finally check the attackers location (if the choose to follow up, otherwise they coul not have lost the ball)
            if (selectedBall == null && context.followsUp) {
                val ball = state.field[context.pushChain.first().from].balls.singleOrNull()
                if (ball != null) {
                    if (ball.state != BallState.BOUNCING) INVALID_GAME_STATE("Unexpected ball state: ${ball.state}")
                    selectedBall = ball
                }
            }

            return if (selectedBall != null) {
                buildCompositeCommand {
                    add(SetCurrentBall(selectedBall))
                    val nextNode =
                        when (selectedBall.state) {
                            BallState.BOUNCING -> BounceBall
                            BallState.OUT_OF_BOUNDS -> ThrowInBall
                            else -> INVALID_GAME_STATE("Unexpected ball state: ${selectedBall.state}")
                        }
                    add(GotoNode(nextNode))
                }
            } else {
                compositeCommandOf(
                    SetCurrentBall(null),
                    ExitProcedure()
                )
            }
        }
    }

    object BounceBall : ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = Bounce
        override fun onExitNode(state: Game, rules: Rules): Command {
            return GotoNode(ResolveBallEvents)
        }
    }

    object ThrowInBall: ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command {
            val ball = state.currentBall()
            return SetContext(ThrowInContext(
                ball,
                ball.outOfBoundsAt!!
            ))
        }
        override fun getChildProcedure(state: Game, rules: Rules) = ThrowIn
        override fun onExitNode(state: Game, rules: Rules): Command {
            return compositeCommandOf(
                RemoveContext<ThrowInContext>(),
                GotoNode(ResolveBallEvents)
            )
        }
    }
}
