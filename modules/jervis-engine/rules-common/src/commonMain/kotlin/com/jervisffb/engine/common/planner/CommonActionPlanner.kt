package com.jervisffb.engine.common.planner

import com.jervisffb.engine.actions.Cancel
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.MoveType
import com.jervisffb.engine.actions.TargetSquare
import com.jervisffb.engine.common.pathfinder.CommonPathFinder
import com.jervisffb.engine.common.procedures.calculateMoveTypesAvailable
import com.jervisffb.engine.common.procedures.calculateOptionsForMoveType
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.PlayerPitchState
import com.jervisffb.engine.model.isSkillAvailable
import com.jervisffb.engine.rules.SPRINT_EXTRA_RUSHES
import com.jervisffb.engine.rules.common.pathfinder.PathFinder
import com.jervisffb.engine.rules.common.planner.ActionPlanner
import com.jervisffb.engine.rules.common.planner.MoveCandidate
import com.jervisffb.engine.rules.common.planner.MovePlan
import com.jervisffb.engine.rules.common.planner.MovePolicyContext
import com.jervisffb.engine.rules.common.planner.PlannedMove
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.engine.rules.policy.GameRulePhase
import kotlinx.serialization.Serializable

/**
 * Standard movement planner shared by all supported Blood Bowl rulesets.
 */
@Serializable
object CommonActionPlanner : ActionPlanner {

    override val pathFinder: PathFinder = CommonPathFinder()

    override fun createMovePlan(
        state: Game,
        player: Player,
        maxSteps: Int?,
    ): MovePlan {
        require(maxSteps == null || maxSteps >= 0) { "maxSteps must be non-negative: $maxSteps" }
        val policyContext = MovePolicyContext(state, GameRulePhase.LIVE)
        if (!state.rulesContext.allowsMoveType(policyContext, MoveType.STANDARD)) {
            return MovePlan.empty(movesUsedBeforePath(player))
        }
        val isProne = (player.state == PlayerPitchState.PRONE)
        return when (isProne) {
            true -> createAfterStandingUpPlan(state, player, policyContext, maxSteps)
            false -> createStandardPlan(state, player, policyContext, maxSteps)
        }
    }

    private fun createStandardPlan(
        state: Game,
        player: Player,
        policyContext: MovePolicyContext,
        maxSteps: Int?,
    ): MovePlan {
        val rules = state.rules
        val start = player.coordinates
        val requiresDodge = rules.calculateMarks(state, player.team, start) > 0
        val targets = standardMoveTargets(state, player)
            .filter { target ->
                state.rulesContext.allowsMove(
                    policyContext,
                    MoveCandidate(player.id, MoveType.STANDARD, start, target),
                )
            }
        val maximumPathLength = when (requiresDodge) {
            true -> 1
            false -> player.movesLeft.coerceAtLeast(0)
        }.limitTo(maxSteps)
        val immediateMoves = if (maxSteps == null || maximumPathLength > 0) {
            targets.associate { target ->
                target.coordinate to plannedMove(target)
            }
        } else {
            emptyMap()
        }
        val paths = maximumPathLength
            .takeIf { it > 0 }
            ?.let { pathFinder.calculateAllPaths(state, player, it) }

        return MovePlan(
            immediateMoves = immediateMoves,
            paths = paths,
            maximumPathLength = maximumPathLength,
            movesUsedBeforePath = movesUsedBeforePath(player),
            normalMovesAvailable = player.movesLeft.coerceAtLeast(0),
            startingRequiresDodge = requiresDodge,
            pathActionTail = pathActionTail(player),
        )
    }

    private fun createAfterStandingUpPlan(
        state: Game,
        player: Player,
        policyContext: MovePolicyContext,
        maxSteps: Int?,
    ): MovePlan {
        val rules = state.rules
        val start = player.coordinates
        val hasJumpUp = player.isSkillAvailable(SkillType.JUMP_UP)
        val standingUpCost = if (hasJumpUp) 0 else rules.moveRequiredForStandingUp
        val movesAfterStandingUp = (player.movesLeft - standingUpCost).coerceAtLeast(0)
        val maximumPathLength = movesAfterStandingUp.limitTo(maxSteps)
        val requiresDodge = rules.calculateMarks(state, player.team, start) > 0
        val requiresRush = (movesAfterStandingUp == 0)
        val movesUsedBeforePath = movesUsedBeforePath(player) + standingUpCost
        val futureRushes = player.rushesLeft +
            if (player.isSkillAvailable(SkillType.SPRINT)) SPRINT_EXTRA_RUSHES else 0

        if ((maxSteps != null && maxSteps <= 0) || movesAfterStandingUp + futureRushes <= 0) {
            return MovePlan.empty(movesUsedBeforePath)
        }

        if (requiresDodge || requiresRush) {
            val immediateMoves = start
                .getSurroundingCoordinates(rules, 1, includeOutOfBounds = false)
                .asSequence()
                .filter { state.pitch[it].isUnoccupied() }
                .map { TargetSquare.move(it, requiresRush, requiresDodge) }
                .filter { target ->
                    state.rulesContext.allowsMove(
                        policyContext,
                        MoveCandidate(player.id, MoveType.STANDARD, start, target),
                    )
                }
                .associate { target -> target.coordinate to plannedMove(target) }
            return MovePlan(
                immediateMoves = immediateMoves,
                paths = null,
                maximumPathLength = 0,
                movesUsedBeforePath = movesUsedBeforePath,
                normalMovesAvailable = movesAfterStandingUp,
                startingRequiresDodge = requiresDodge,
            )
        }

        val paths = maximumPathLength
            .takeIf { it > 0 }
            ?.let { pathFinder.calculateAllPaths(state, player, it) }
        return MovePlan(
            immediateMoves = emptyMap(),
            paths = paths,
            maximumPathLength = maximumPathLength,
            movesUsedBeforePath = movesUsedBeforePath,
            normalMovesAvailable = movesAfterStandingUp,
            startingRequiresDodge = false,
            pathActionTail = pathActionTail(player),
        )
    }

    private fun plannedMove(target: TargetSquare) =
        PlannedMove(
            target,
            MovePlan.createStandardMoveAction(target.coordinate),
        )

    private fun standardMoveTargets(
        state: Game,
        player: Player,
    ): List<TargetSquare> {
        val baseTargets = calculateOptionsForMoveType(state, state.rules, player, MoveType.STANDARD)
            .flatMap { it.squares }
        if (baseTargets.isNotEmpty()) return baseTargets

        val standardMoveIsAvailable = calculateMoveTypesAvailable(state, player)
            ?.types
            ?.contains(MoveType.STANDARD)
            ?: false
        if (!standardMoveIsAvailable) return emptyList()

        val requiresDodge = state.rules.calculateMarks(state, player.team, player.coordinates) > 0
        return player.coordinates
            .getSurroundingCoordinates(state.rules, 1, includeOutOfBounds = false)
            .filter { state.pitch[it].isUnoccupied() }
            .map { TargetSquare.move(it, needRush = true, needDodge = requiresDodge) }
    }

    private fun movesUsedBeforePath(player: Player): Int = player.move - player.movesLeft

    private fun pathActionTail(player: Player): List<GameAction> {
        val shouldDeclineFumblerooski = player.isSkillAvailable(SkillType.FUMBLEROOSKI) && player.hasBall()
        return if (shouldDeclineFumblerooski) listOf(Cancel) else emptyList()
    }

    private fun Int.limitTo(maxSteps: Int?): Int =
        if (maxSteps == null) this else minOf(this, maxSteps)
}
