package com.jervisffb.engine.bb2025.challenge.goal

import com.jervisffb.engine.GameDelta
import com.jervisffb.engine.bb2025.procedures.actions.block.singleblock.SingleStandardBlockRollDice
import com.jervisffb.engine.challenge.BaseGoalProgress
import com.jervisffb.engine.challenge.ChallengeContext
import com.jervisffb.engine.challenge.ChallengeContextHolder
import com.jervisffb.engine.challenge.ChallengeGoal
import com.jervisffb.engine.challenge.GoalBuilder
import com.jervisffb.engine.challenge.GoalModifier
import com.jervisffb.engine.challenge.GoalStatus
import com.jervisffb.engine.challenge.GoalTarget
import com.jervisffb.engine.common.context.BlockContext
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.context.getContext
import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toPersistentSet

data class BlockGoalContext(
    val potentialTargets: PersistentSet<Player>,
    val remainingBlocks: Int,
    val blockedTargets: PersistentSet<Player>,
): ChallengeContext {
    val hasRemainingBlocks = (remainingBlocks > 0)
}

/**
 * Goal that succeeds when [targetPlayers] have been blocked or blitzed.
 */
class BlockGoal(
    val targetTeam: Team,
    val targetPlayers: GoalTarget,
    override val modifiers: List<GoalModifier>
): ChallengeGoal() {

    override val description: String = buildString {
        when (targetPlayers) {
            is GoalTarget.AnyPlayers -> {
                append("Block ${targetPlayers.description}")
            }
            is GoalTarget.SpecificPlayer -> {
                append("Block ${targetPlayers.player.name}")
            }
        }
    }

    override fun initializeBase(state: Game): ChallengeContext {
        return when (targetPlayers) {
            is GoalTarget.AnyPlayers -> {
                BlockGoalContext(
                    potentialTargets = state.getTeam(targetTeam.id).filter { player -> !player.missNextGame }.toPersistentSet(),
                    remainingBlocks = targetPlayers.count,
                    blockedTargets = persistentSetOf(),
                )
            }
            is GoalTarget.SpecificPlayer -> {
                BlockGoalContext(
                    // Resolved against the game rather than used directly: the
                    // player on the goal belongs to the challenge's authoring
                    // team, and every game gets teams of its own.
                    potentialTargets = persistentSetOf(state.getPlayerById(targetPlayers.player.id)),
                    remainingBlocks = 1,
                    blockedTargets = persistentSetOf(),
                )
            }
        }
    }

    override fun evaluateBase(
        state: Game,
        delta: GameDelta,
        contexts: ChallengeContextHolder,
    ): BaseGoalProgress {

        val context = contexts.get<BlockGoalContext>()

        // We don't know for sure that a block has occurred until we start rolling dice.
        // E.g., Foul Appearance might abort the block after it started.
        val updatedContext = delta.steps
            .firstOrNull { step -> (step.node == SingleStandardBlockRollDice.RollDice) }
            ?.let {
                val defender = state.getContext<BlockContext>().defender
                when (context.potentialTargets.contains(defender)) {
                    true -> {
                        context.copy(
                            potentialTargets = context.potentialTargets.remove(defender),
                            blockedTargets = context.blockedTargets.add(defender),
                            remainingBlocks = context.remainingBlocks - 1,
                        )
                    }
                    false -> context
                }
            } ?: context

        val goalStatus = when (updatedContext.hasRemainingBlocks) {
            true -> GoalStatus.IN_PROGRESS
            false -> GoalStatus.COMPLETED
        }
        return BaseGoalProgress(goalStatus, updatedContext)
    }
}

class BlockGoalBuilder(
    var team: Team,
    var target: GoalTarget,
): GoalBuilder<BlockGoal, BlockGoalBuilder>() {

    override fun build(): BlockGoal {
        return BlockGoal(team,target, modifiers.toList())
    }
}
