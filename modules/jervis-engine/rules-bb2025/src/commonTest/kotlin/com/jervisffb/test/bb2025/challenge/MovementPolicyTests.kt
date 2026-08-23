package com.jervisffb.test.bb2025.challenge

import com.jervisffb.engine.GameEngineController
import com.jervisffb.engine.GameRulesContext
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.MoveType
import com.jervisffb.engine.actions.MoveTypeSelected
import com.jervisffb.engine.actions.Revert
import com.jervisffb.engine.actions.SelectMoveType
import com.jervisffb.engine.actions.SelectPitchLocation
import com.jervisffb.engine.actions.SetPlayerState
import com.jervisffb.engine.actions.TargetSquare
import com.jervisffb.engine.bb2025.StandardBB2025Rules
import com.jervisffb.engine.bb2025.challenge.goal.DebugGoalBuilder
import com.jervisffb.engine.bb2025.challenge.rule.MoveTypesAvailable
import com.jervisffb.engine.challenge.ChallengeBuilder
import com.jervisffb.engine.challenge.ChallengeCategory
import com.jervisffb.engine.common.procedures.FullGame
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.model.ChallengeId
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.PlayerPitchState
import com.jervisffb.engine.model.locations.PitchCoordinate
import com.jervisffb.engine.rules.common.actions.PlayerStandardActionType
import com.jervisffb.engine.rules.common.planner.MoveCandidate
import com.jervisffb.engine.rules.common.planner.MovePolicy
import com.jervisffb.engine.rules.common.planner.MovePolicyContext
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.engine.rules.policy.GameRulePolicy
import com.jervisffb.engine.utils.InvalidActionException
import com.jervisffb.test.activatePlayer
import com.jervisffb.test.bb2025.createDefaultHomeTeamBB2025
import com.jervisffb.test.bb2025.humanTeamAwayBB2025
import com.jervisffb.test.defaultBB2020Pregame
import com.jervisffb.test.defaultKickOffHomeTeam
import com.jervisffb.test.defaultSetup
import com.jervisffb.test.ext.rollForward
import com.jervisffb.test.jump
import com.jervisffb.test.jumpTo
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MovementPolicyTests {

    private data class Fixture(
        val state: Game,
        val controller: GameEngineController,
    )

    private fun createFixture(
        policies: List<GameRulePolicy> = emptyList(),
        initialActions: List<GameAction> = emptyList(),
        startDefaultGame: Boolean = true,
        cacheActionDescriptor: Boolean = false,
    ): Fixture {
        val rules = StandardBB2025Rules()
        val state = Game(
            GameRulesContext(rules, policies),
            createDefaultHomeTeamBB2025(rules),
            humanTeamAwayBB2025(rules),
        )
        val controller = GameEngineController(
            state = state,
            initialActions = initialActions,
            validateInitialActions = true,
            cacheActionDescriptor = cacheActionDescriptor,
            allowAdminActionsInInitialActions = true,
        )
        controller.startTestMode(FullGame)
        if (startDefaultGame) {
            controller.rollForward(
                *defaultBB2020Pregame(),
                *defaultSetup(),
                *defaultKickOffHomeTeam(),
            )
        }
        return Fixture(state, controller)
    }

    @Test
    fun emptyContextLeavesBaseMovementAvailable() {
        val (state, controller) = createFixture()
        state.getPlayerById("H1".playerId).state = PlayerPitchState.PRONE

        controller.rollForward(*activatePlayer("A1", PlayerStandardActionType.MOVE))

        val request = controller.getAvailableActions()
        assertTrue(request.contains(MoveType.STANDARD))
        assertTrue(request.contains(MoveType.JUMP))
        assertNotNull(request.getOrNull<SelectMoveType>())
    }

    @Test
    fun challengeRulesContributeTheirPoliciesToTheGameContext() {
        val rules = StandardBB2025Rules()
        val restriction = MoveTypesAvailable(jump = false)
        val challenge = ChallengeBuilder(ChallengeId("movement-policy-context")).apply {
            name = "Movement policy context"
            description = "Verify challenge policy installation."
            category = ChallengeCategory.BLOCKING
            gameRules = rules
            homeTeam = createDefaultHomeTeamBB2025(rules)
            awayTeam = humanTeamAwayBB2025(rules)
            goal = DebugGoalBuilder().build()
            addRule(restriction)
        }.build()

        assertEquals(restriction.policies, challenge.createGame().state.rulesContext.policies)
    }

    @Test
    fun jumpMoveTypeIsRemovedAndRejected() {
        val restriction = MoveTypesAvailable(jump = false)
        val (state, controller) = createFixture(restriction.policies)
        state.getPlayerById("H1".playerId).state = PlayerPitchState.PRONE
        controller.rollForward(*activatePlayer("A1", PlayerStandardActionType.MOVE))

        assertFalse(controller.getAvailableActions().contains(MoveType.JUMP))
        assertFailsWith<InvalidActionException> {
            controller.handleAction(MoveTypeSelected(MoveType.JUMP))
        }
    }

    @Test
    fun dodgeTargetsAreRemovedFromActionsAndPlans() {
        val restriction = MoveTypesAvailable(dodge = false)
        val (state, controller) = createFixture(restriction.policies)
        controller.rollForward(*activatePlayer("A1", PlayerStandardActionType.MOVE))

        val plan = state.rulesContext.actionPlanner.createMovePlan(state, state.activePlayer!!)
        assertTrue(plan.neighborMoves.isEmpty())
        assertFalse(plan.hasPaths)

        controller.handleAction(MoveTypeSelected(MoveType.STANDARD))
        assertNull(controller.getAvailableActions().getOrNull<SelectPitchLocation>())
    }

    @Test
    fun rushTargetsAreRemovedFromActionsAndPlans() {
        val restriction = MoveTypesAvailable(rush = false)
        val (state, controller) = createFixture(restriction.policies)
        val player = state.getPlayerById("A8".playerId)
        controller.rollForward(*activatePlayer(player, PlayerStandardActionType.MOVE))
        player.movesLeft = 0

        val plan = state.rulesContext.actionPlanner.createMovePlan(state, state.activePlayer!!)
        assertTrue(plan.neighborMoves.isEmpty())
        assertFalse(plan.hasPaths)

        controller.handleAction(MoveTypeSelected(MoveType.STANDARD))
        assertNull(controller.getAvailableActions().getOrNull<SelectPitchLocation>())
    }

    @Test
    fun plannerCanIncludeRushTargetsWhenRequested() {
        val (state, controller) = createFixture()
        val player = state.getPlayerById("A8".playerId)
        controller.rollForward(*activatePlayer(player, PlayerStandardActionType.MOVE))
        player.movesLeft = 0

        val defaultPlan = state.rulesContext.actionPlanner.createMovePlan(state, player)
        assertTrue(defaultPlan.neighborMoves.isEmpty())

        val planWithRushes = state.rulesContext.actionPlanner.createMovePlan(
            state,
            player,
            includeRushes = true,
        )
        assertTrue(planWithRushes.neighborMoves.isNotEmpty())
        assertTrue(planWithRushes.neighborMoves.values.all { it.target.requiresRush })
    }

    @Test
    fun multipleMovementPoliciesCompose() {
        val policies = MoveTypesAvailable(dodge = false).policies +
            MoveTypesAvailable(rush = false).policies
        val (state, controller) = createFixture(policies)
        controller.rollForward(*activatePlayer("A8", PlayerStandardActionType.MOVE))
        val player = state.activePlayerOrThrow()
        val context = MovePolicyContext(state)
        val target = PitchCoordinate(player.coordinates.x + 1, player.coordinates.y)

        fun candidate(requiresDodge: Boolean, requiresRush: Boolean) = MoveCandidate(
            player = player.id,
            type = MoveType.STANDARD,
            from = player.coordinates,
            target = TargetSquare.move(target, requiresRush, requiresDodge),
        )

        assertFalse(state.rulesContext.allowsMove(context, candidate(requiresDodge = true, requiresRush = false)))
        assertFalse(state.rulesContext.allowsMove(context, candidate(requiresDodge = false, requiresRush = true)))
        assertTrue(state.rulesContext.allowsMove(context, candidate(requiresDodge = false, requiresRush = false)))
    }

    @Test
    fun plannerOnlyReturnsExecutablePolicyApprovedMoves() {
        val blockedSquare = PitchCoordinate(16, 13)
        val policy = object : MovePolicy {
            override fun allowsMove(context: MovePolicyContext, candidate: MoveCandidate): Boolean {
                return candidate.target.coordinate != blockedSquare
            }
        }
        val (state, controller) = createFixture(listOf(policy))
        val player = state.getPlayerById("A8".playerId)
        controller.rollForward(*activatePlayer(player, PlayerStandardActionType.MOVE))

        val plan = state.rulesContext.actionPlanner.createMovePlan(state, state.activePlayer!!)
        assertFalse(plan.neighborMoves.containsKey(blockedSquare))
        plan.neighborMoves.values.forEach { plannedMove ->
            val nodeBefore = controller.currentNode()
            controller.handleAction(plannedMove.action)
            controller.handleAction(Revert)
            assertEquals(nodeBefore, controller.currentNode())
        }

        val goal = PitchCoordinate(18, 13)
        val path = plan.getClosestPathTo(goal)
        assertTrue(path.isNotEmpty())
        assertFalse(path.any { it.target.coordinate == blockedSquare })
        path.forEach { controller.handleAction(it.action) }
        assertEquals(goal, player.coordinates)
    }

    @Test
    fun plannerCreatesExecutablePathsForAfterStandingUp() {
        val (state, controller) = createFixture()
        val player = state.getPlayerById("A8".playerId)
        player.state = PlayerPitchState.PRONE
        controller.rollForward(*activatePlayer(player, PlayerStandardActionType.MOVE))

        val plan = state.rulesContext.actionPlanner.createMovePlan(state, state.activePlayer!!)
        val goal = PitchCoordinate(18, 13)
        val path = plan.getClosestPathTo(goal)
        assertEquals(state.rules.moveRequiredForStandingUp, plan.movesUsedBeforePath)
        assertTrue(path.isNotEmpty())

        controller.handleAction(MoveTypeSelected(MoveType.STAND_UP))
        path.forEach { controller.handleAction(it.action) }
        assertEquals(goal, player.coordinates)
    }

    @Test
    fun plannerAnticipatesSprintBecomingAvailable() {
        val (state, controller) = createFixture()
        val player = state.getPlayerById("A8".playerId)
        player.addSkill(SkillType.SPRINT)
        controller.rollForward(*activatePlayer(player, PlayerStandardActionType.MOVE))
        player.movesLeft = 0
        player.rushesLeft = 0

        assertTrue(controller.getAvailableActions().contains(MoveType.STANDARD))
        val plan = state.rulesContext.actionPlanner.createMovePlan(
            state,
            state.activePlayer!!,
            includeRushes = true,
        )
        assertTrue(plan.neighborMoves.isNotEmpty())
        assertTrue(plan.neighborMoves.values.all { it.target.requiresRush })

        controller.handleAction(plan.neighborMoves.values.first().action)
    }

    @Test
    fun initialActionsBypassChallengePoliciesButLiveActionsDoNot() {
        val restriction = MoveTypesAvailable(jump = false)
        val initialActions = buildList {
            addAll(defaultBB2020Pregame())
            addAll(defaultSetup())
            add(SetPlayerState("H1".playerId, PlayerPitchState.PRONE, 12, 5))
            addAll(defaultKickOffHomeTeam().filterNotNull())
            addAll(activatePlayer("A1", PlayerStandardActionType.MOVE))
            addAll(jumpTo(11, 5))
            addAll(jump(6.d6).filterNotNull())
        }
        val (state, controller) = createFixture(
            policies = restriction.policies,
            initialActions = initialActions,
            startDefaultGame = false,
            cacheActionDescriptor = true,
        )

        assertEquals(PitchCoordinate(11, 5), state.getPlayerById("A1".playerId).coordinates)
        assertFalse(controller.getAvailableActions().contains(MoveType.JUMP))
        assertFailsWith<InvalidActionException> {
            controller.handleAction(MoveTypeSelected(MoveType.JUMP))
        }
    }
}
