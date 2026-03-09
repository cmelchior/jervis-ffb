package com.jervisffb.engine.rules.bb2025.procedures.actions.block.push

import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.SetBallState
import com.jervisffb.engine.commands.SetPlayerLocation
import com.jervisffb.engine.commands.SetTurnOver
import com.jervisffb.engine.commands.buildCompositeCommand
import com.jervisffb.engine.commands.context.SetContext
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.fsm.ComputationNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.TurnOver
import com.jervisffb.engine.model.context.PushContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.hasSkill
import com.jervisffb.engine.model.locations.DogOut
import com.jervisffb.engine.reports.ReportPushedIntoCrowd
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.bb2020.skills.Leader
import com.jervisffb.engine.rules.bb2025.procedures.actions.block.BB2025PushBack
import com.jervisffb.engine.rules.bb2025.procedures.actions.block.MultipleBlockAction
import com.jervisffb.engine.rules.common.skills.SkillType

/**
 * Procedure for moving all players' part of a Push Chain created by.
 * [CreatePushChainStep]
 *
 * A Pushback is split into multiple phases to support both normal blocks and
 * Multiple Block as their order of resolution differs.
 *
 * See [BB2025PushBack] and [MultipleBlockAction] for more details on each.
 */
object MovePlayersInPushChainStep: Procedure() {
    override val initialNode: Node = MovePushedPlayers
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null

    /**
     * Resolve the push-chain by moving all players part of it. For now, we only
     * update their field location. Crowd injuries and balls bouncing happens
     * later.
     */
    object MovePushedPlayers: ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            val context = state.getContext<PushContext>()

            // If Stand Firm was used, or player is Rooted, no players are moved, so just exit early.
            if (context.isDefenderImmovable) {
                return ExitProcedure()
            }

            // Otherwise, execute push commands from the last to the first, this way we avoid having to deal
            // with squares needing to have to players temporarily. This should be a safe implementation
            // detail, since all commands are executed before creating the game delta.
            return buildCompositeCommand {
                var pushedIntoCrowd = false
                context.pushChain.reversed().forEach { push ->
                    val to = push.to!!
                    // If OUT_OF_BOUNDS, further processing happens in `ResolvePushedIntoTheCrowd`
                    val outOfBounds = to.isOutOfBounds(rules)
                    if (outOfBounds) {
                        // See page 58 in the rulebook. If a player with the ball is pushed into the crowd,
                        // it is a turnover. The Throw-in is handled in `ResolvePushedIntoTheCrowd`
                        if (push.pushee.hasBall() && push.pushee.team == state.activeTeam) {
                            add(SetTurnOver(TurnOver.STANDARD))
                        }
                        addAll(
                            SetPlayerLocation(push.pushee, DogOut),
                            ReportPushedIntoCrowd(push.pushee, push.from)
                        )

                        // Check if Leader rerolls are still available after a player with Leader
                        // left the field.
                        if (push.pushee.hasSkill(SkillType.LEADER)) {
                            Leader.removeLeaderRerollIfNotAvailable(push.pushee.team)?.let { removeRerollCommand ->
                                add(removeRerollCommand)
                            }
                        }

                        pushedIntoCrowd = true
                    } else {
                        // At this stage, there should only be one ball on the square,
                        // Even if the player is holding another ball, it isn't knocked loose yet.
                        add(SetPlayerLocation(push.pushee, to))
                        state.field[to].balls.singleOrNull()?.let {
                            add(SetBallState.bouncing(it))
                        }
                    }
                }
                add(SetContext(context.copy(pushedIntoTheCrowd = pushedIntoCrowd)))
                add(ExitProcedure())
            }
        }
    }
}
