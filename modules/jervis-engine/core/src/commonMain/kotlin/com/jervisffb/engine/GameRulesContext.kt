package com.jervisffb.engine

import com.jervisffb.engine.actions.MoveType
import com.jervisffb.engine.actions.SelectMoveType
import com.jervisffb.engine.actions.SelectPitchLocation
import com.jervisffb.engine.actions.TargetSquare
import com.jervisffb.engine.challenge.ChallengeContext
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.context.ProcedureContext
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.common.pathfinder.PathFinder
import com.jervisffb.engine.rules.common.planner.ActionPlanner
import com.jervisffb.engine.rules.common.planner.MoveCandidate
import com.jervisffb.engine.rules.common.planner.MovePolicy
import com.jervisffb.engine.rules.common.planner.MovePolicyContext
import com.jervisffb.engine.rules.policy.ActionFilterContext
import com.jervisffb.engine.rules.policy.ActionFilterPolicy
import com.jervisffb.engine.rules.policy.GameRulePolicy


/**
 * The effective rules for one running game. These consist of the [rules] plus
 * any additional, composable [GameRulePolicy] classes.
 *
 * This class is responsible for two things:
 * - Filter actions in and out of the [GameEngineController].
 * - Grant access to "Action Planners", i.e., if another component needs to
 *   reason about future actions. This must be done through this class instead
 *   of trying to guess.
 *
 * Filtering works the following way:
 *
 * - [ActionNode.getAvailableActions] returns all available actions without
 *   regard to policies.
 *
 * - [GameEngineController.getAvailableActions] will filter these actions using
 *   any configured [GameRulePolicy], so consumers only see valid options.
 *
 * - [GameEngineController.handleAction] will respect filters if validating
 *   actions, but if [GameEngineController.validateActions] is not enabled,
 *   it might be possible to send invalid actions into a procedure that will
 *   accept them.
 *
 * The context and all policies are required to be immutable. Runtime progress
 * belongs in [Game], [ProcedureContext] or [ChallengeContext].
 */
class GameRulesContext(
    val rules: Rules,
    policies: List<GameRulePolicy> = emptyList(),
) {

    /** List of all game policies that should be applied to actions. */
    val policies: List<GameRulePolicy> = policies.toList()

    /**
     * Can be used to plan future actions, taking into account the current
     * state of the game, rules, and policies.
     *
     * TODO: We should probably wrap it somehow so policies are applied.
     */
    val actionPlanner: ActionPlanner = rules.actionPlanner

    private val actionFilterPolicies: List<ActionFilterPolicy> = this.policies.filterIsInstance<ActionFilterPolicy>()
    private val movePolicies: List<MovePolicy> = this.policies.filterIsInstance<MovePolicy>()

    val hasMovePolicies: Boolean = movePolicies.isNotEmpty()

    /**
     * Filter all outgoing action requests from [GameEngineController] according
     * to the configured game policies.
     *
     * The updated [ActionRequest] is returned.
     */
    fun filterActionRequest(
        context: ActionFilterContext,
        request: ActionRequest,
    ): ActionRequest {
        val filteredRequest = actionFilterPolicies
            .asSequence()
            .filter { it.appliesDuring(context.phase) }
            .fold(request) { current, policy ->
                policy.filterRequest(context, current)
            }
        return filterMovementActions(context, filteredRequest)
    }

    /**
     * Check if a given [MoveType] is allowed at the current game state, taking
     * into account all configured [MovePolicy]s.
     */
    fun allowsMoveType(
        context: MovePolicyContext,
        type: MoveType,
    ): Boolean {
        return movePolicies
            .asSequence()
            .filter { it.appliesDuring(context.phase) }
            .all { it.allowsMoveType(context, type) }
    }

    /**
     * Check if a given [MoveCandidate] is allowed at the current game state,
     * taking into account all configured [MovePolicy]s.
     *
     * This is mostly used by [ActionPlanner] or [PathFinder] whn they search
     * for valid moves.
     */
    fun allowsMove(
        context: MovePolicyContext,
        candidate: MoveCandidate,
    ): Boolean {
        return movePolicies
            .asSequence()
            .filter { it.appliesDuring(context.phase) }
            .all { it.allowsMove(context, candidate) }
    }

    private fun filterMovementActions(
        context: ActionFilterContext,
        request: ActionRequest,
    ): ActionRequest {
        if (!hasMovePolicies) return request
        val player = context.state.activePlayer ?: return request
        val moveContext = MovePolicyContext(context.state, context.phase)
        val actions = request.actions.mapNotNull { descriptor ->
            when (descriptor) {
                is SelectMoveType -> {
                    descriptor.types
                        .filter { allowsMoveType(moveContext, it) }
                        .takeIf { it.isNotEmpty() }
                        ?.let(::SelectMoveType)
                }
                is SelectPitchLocation -> {
                    descriptor.squares
                        .filter { target ->
                            val moveType = target.moveTypeOrNull() ?: return@filter true
                            allowsMoveType(moveContext, moveType) &&
                                allowsMove(
                                    moveContext,
                                    MoveCandidate(player.id, moveType, player.coordinates, target),
                                )
                        }
                        .takeIf { it.isNotEmpty() }
                        ?.let(::SelectPitchLocation)
                }
                else -> descriptor
            }
        }
        return request.copy(actions = actions)
    }

    private fun TargetSquare.moveTypeOrNull(): MoveType? {
        return when (type) {
            TargetSquare.Type.JUMP -> MoveType.JUMP
            TargetSquare.Type.LEAP -> MoveType.LEAP
            TargetSquare.Type.POGO -> MoveType.POGO
            TargetSquare.Type.MOVE,
            TargetSquare.Type.RUSH -> MoveType.STANDARD
            else -> null
        }
    }
}
