package com.jervisffb.engine.rules.bb2025.procedures.actions.move

import com.jervisffb.engine.actions.Cancel
import com.jervisffb.engine.actions.CancelWhenReady
import com.jervisffb.engine.actions.Confirm
import com.jervisffb.engine.actions.ConfirmWhenReady
import com.jervisffb.engine.actions.ContinueWhenReady
import com.jervisffb.engine.actions.FieldSquareSelected
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.actions.MoveType
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.SetBallLocation
import com.jervisffb.engine.commands.SetBallState
import com.jervisffb.engine.commands.SetPlayerLocation
import com.jervisffb.engine.commands.SetPlayerMoveLeft
import com.jervisffb.engine.commands.SetPlayerRushesLeft
import com.jervisffb.engine.commands.SetSkillUsed
import com.jervisffb.engine.commands.buildCompositeCommand
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.context.AddContext
import com.jervisffb.engine.commands.context.RemoveContext
import com.jervisffb.engine.commands.context.UpdateContext
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.ComputationNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.ParentNode
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.fsm.castAction
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.context.LeapRollContext
import com.jervisffb.engine.model.context.MoveContext
import com.jervisffb.engine.model.context.RushRollContext
import com.jervisffb.engine.model.context.assertContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.isSkillAvailable
import com.jervisffb.engine.model.modifiers.DiceModifier
import com.jervisffb.engine.model.modifiers.LeapModifier
import com.jervisffb.engine.model.modifiers.MarkedModifier
import com.jervisffb.engine.reports.ReportLeapResult
import com.jervisffb.engine.reports.ReportSkillUsed
import com.jervisffb.engine.rules.JUMP_DISTANCE
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.SPRINT_EXTRA_RUSHES
import com.jervisffb.engine.rules.bb2025.procedures.tables.injury.BB2025FallingOver
import com.jervisffb.engine.rules.common.procedures.actions.move.RushRoll
import com.jervisffb.engine.rules.common.procedures.calculateOptionsForMoveType
import com.jervisffb.engine.rules.common.procedures.estimatedMovesLeft
import com.jervisffb.engine.rules.common.procedures.tables.injury.RiskingInjuryContext
import com.jervisffb.engine.rules.common.procedures.tables.injury.RiskingInjuryMode
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.engine.utils.INVALID_GAME_STATE
import com.jervisffb.engine.utils.sum
import kotlinx.collections.immutable.toPersistentList
import kotlin.math.max

/**
 * Procedure controlling a Leap Action.
 * Leap is the same as Jump, expect modifiers are calculated slightly
 * differently.
 *
 * See page 56 in the BB2025 rulebook for rules on Jump.
 * See page 130 in the BB2025 rulebook for rules on Leap.
 *
 * The order of checks is:
 * 1. Tentacles
 * 2. Rush(es)
 * 3. Leap Roll
 * 3. Diving Tackle
 *
 * Since the player does not need to Dodge to leave the square, any skills
 * that trigger on Dodge cannot trigger on Jumps.
 *
 * In BB2025, the rules define the consequence of failing either Rush roll as
 * the player will Fall Over in the starting square.
 *
 * Note, Jump, Leap and Pogo behave almost the same way, but they are still
 * separated into 3 different procedures. This means that it is easier to
 * introduce specific functionality, but shared logic must also be implemented
 * across the 3 procedures.
 *
 * See [com.jervisffb.engine.rules.common.procedures.actions.move.JumpStep]
 * See [PogoStep]
 */
object LeapStep : Procedure() {
    override val initialNode: Node = CheckForSprint
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null
    override fun isValid(state: Game, rules: Rules) = state.assertContext<MoveContext>()

    // If the moving player has no more moves or rushes left, they will
    // automatically apply Sprint to gain one extra square of movement.
    // If this isn't possible, the move is not possible after all and will be
    // automatically rejected
    object CheckForSprint: ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            val context = state.getContext<MoveContext>()
            val movingPlayer = context.player
            val hasMovesLeft = movingPlayer.estimatedMovesLeft(includeSprint = false) >= JUMP_DISTANCE
            val hasSprint = movingPlayer.isSkillAvailable(SkillType.SPRINT)
            return when {
                hasMovesLeft -> GotoNode(SelectTargetSquareOrCancel)
                hasSprint -> {
                    compositeCommandOf(
                        ReportSkillUsed(movingPlayer, SkillType.SPRINT),
                        SetSkillUsed(movingPlayer, movingPlayer.getSkill(SkillType.SPRINT), true),
                        SetPlayerRushesLeft(movingPlayer, movingPlayer.rushesLeft + SPRINT_EXTRA_RUSHES),
                        GotoNode(SelectTargetSquareOrCancel)
                    )
                }
                !hasSprint -> ExitProcedure()
                else -> INVALID_GAME_STATE("hasMovesLeft=$hasMovesLeft, hasSprint=$hasSprint")
            }
        }
    }

    object SelectTargetSquareOrCancel : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<MoveContext>().player.team
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val context = state.getContext<MoveContext>()
            val leapingPlayer = context.player
            val eligibleLeapSquares = calculateOptionsForMoveType(state, rules, leapingPlayer, MoveType.LEAP)
            return eligibleLeapSquares + CancelWhenReady
        }
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return when (action) {
                Cancel -> ExitProcedure()
                else -> {
                    castAction<FieldSquareSelected>(action) { target ->
                        val context = state.getContext<MoveContext>()
                        compositeCommandOf(
                            UpdateContext(context.copy(target = target.coordinate)),
                            GotoNode(ChooseToUseTentacles)
                        )
                    }
                }
            }
        }
    }

    // TODO Implement Tentacles
    object ChooseToUseTentacles: ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            return GotoNode(CheckIfRushingIsNeeded)
        }
    }

    // Check if rushing is needed and move player, so they will fall over in the correct
    // place if any potential rushes fail.
    object CheckIfRushingIsNeeded : ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            val context = state.getContext<MoveContext>()
            return when (context.player.movesLeft) {
                0 -> compositeCommandOf(
                    GotoNode(RushTwice)
                )
                1 -> compositeCommandOf(
                    GotoNode(RushOnce)
                )
                else -> compositeCommandOf(
                    SetPlayerLocation(context.player, context.target!!),
                    GotoNode(ChooseToUseVeryLongLegs)
                )
            }
        }
    }

    /**
     * Player needs two rushes to reach the target square. If they fail the first
     * Rush, they stay in the starting square. If they fail the second, they also
     * fall over in the starting square.
     */
    object RushTwice: ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command {
            val moveContext = state.getContext<MoveContext>()
            return AddContext(RushRollContext(moveContext.player, moveContext.target!!))
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = RushRoll
        override fun onExitNode(state: Game, rules: Rules): Command {
            val rushContext = state.getContext<RushRollContext>()
            val player = rushContext.player
            return if (rushContext.isSuccess) {
                compositeCommandOf(
                    SetPlayerMoveLeft(player, player.movesLeft + 1),
                    SetPlayerRushesLeft(player, player.rushesLeft - 1),
                    RemoveContext(rushContext),
                    GotoNode(RushOnce)
                )
            } else {
                // Rush failed, player is Knocked Down in target square
                compositeCommandOf(
                    RemoveContext(rushContext),
                    GotoNode(ResolvePlayerFallingOver)
                )
            }
        }
    }

    /**
     * Player only need one rush to reach target square
     */
    object RushOnce: ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command {
            val moveContext = state.getContext<MoveContext>()
            return AddContext(RushRollContext(moveContext.player, moveContext.target!!))
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = RushRoll
        override fun onExitNode(state: Game, rules: Rules): Command {
            val moveContext = state.getContext<MoveContext>()
            val rushContext = state.getContext<RushRollContext>()
            val player = rushContext.player
            return if (rushContext.isSuccess) {
                compositeCommandOf(
                    SetPlayerMoveLeft(player, player.movesLeft + 1),
                    SetPlayerRushesLeft(player, player.rushesLeft - 1),
                    SetPlayerLocation(moveContext.player, moveContext.target!!),
                    RemoveContext(rushContext),
                    GotoNode(ChooseToUseVeryLongLegs)
                )
            } else {
                // Rush failed, player is Knocked Down in target square
                compositeCommandOf(
                    RemoveContext(rushContext),
                    GotoNode(ResolvePlayerFallingOver)
                )
            }
        }
    }

    object ChooseToUseVeryLongLegs: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team {
            return state.getContext<MoveContext>().player.team
        }
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val context = state.getContext<MoveContext>()
            val player = context.player
            return if (player.isSkillAvailable(SkillType.VERY_LONG_LEGS)) {
                listOf(ConfirmWhenReady, CancelWhenReady)
            } else {
                listOf(ContinueWhenReady)
            }
        }
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            val context = state.getContext<MoveContext>()
            val usingVeryLongLegs = (action is Confirm)
            return compositeCommandOf(
                UpdateContext(context.copy(useVeryLongLegs = usingVeryLongLegs)),
                GotoNode(ChooseToUseLeapModifier)
            )
        }
    }

    object ChooseToUseLeapModifier: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<MoveContext>().player.team
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            // Even though we are using the Leap skill, adding the modifier is still optional, but it is only
            // available if modifiers are less than -1.
            val currentModifiers = calculateLeapModifiers(state)
            return if (currentModifiers.sum() <= -2) {
                listOf(ConfirmWhenReady, CancelWhenReady)
            } else {
                listOf(ContinueWhenReady)
            }
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            val context = state.getContext<MoveContext>()
            val useLeapModifier = (action is Confirm)
            return compositeCommandOf(
                UpdateContext(context.copy(useLeapModifier = useLeapModifier)),
                GotoNode(CalculateLeapRollModifiers)
            )
        }
    }

    object CalculateLeapRollModifiers: ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            val context = state.getContext<MoveContext>()
            val modifiers = calculateLeapModifiers(state).toPersistentList()
            return compositeCommandOf(
                AddContext(
                    LeapRollContext(
                        player = context.player,
                        startingSquare = context.startingSquare,
                        modifiers = modifiers,
                    )
                ),
                GotoNode(RollForLeap)
            )
        }
    }

    /**
     * Player could move to the target square (after rushes, tentacles) and can
     * now roll to Leap.
     */
    object RollForLeap: ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = LeapRoll
        override fun onExitNode(state: Game, rules: Rules): Command {
            val moveContext = state.getContext<MoveContext>()
            val leapContext = state.getContext<LeapRollContext>()
            val player = leapContext.player
            return if (leapContext.isSuccess) {
                compositeCommandOf(
                    SetPlayerMoveLeft(player, player.movesLeft - JUMP_DISTANCE),
                    ReportLeapResult(leapContext, moveContext.target!!),
                    GotoNode(ChooseToUseFumblerooskiAfterLeapingToTargetSquare)
                )
            } else if (!leapContext.isSuccess && leapContext.roll!!.result.value == 1) {
                // Leap failed catastrophically, player Falls Over in starting square
                compositeCommandOf(
                    SetPlayerLocation(player, moveContext.startingSquare),
                    RemoveContext(leapContext),
                    ReportLeapResult(leapContext, moveContext.startingSquare),
                    GotoNode(ResolvePlayerFallingOver)
                )
            } else {
                // Leap failed, player Falls Over in target square
                compositeCommandOf(
                    ReportLeapResult(leapContext, moveContext.target!!),
                    GotoNode(ChooseToUseFumblerooskiAfterLeapingToTargetSquare)
                )
            }
        }
    }

    object ChooseToUseFumblerooskiAfterLeapingToTargetSquare: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team {
            return state.getContext<MoveContext>().player.team
        }
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val context = state.getContext<MoveContext>()
            val hasFumblerooski = context.player.isSkillAvailable(SkillType.FUMBLEROOSKI)
            val hasBall = context.player.hasBall()
            return when (hasFumblerooski && hasBall) {
                true -> listOf(ConfirmWhenReady, CancelWhenReady)
                false -> listOf(ContinueWhenReady)
            }
        }
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            val context = state.getContext<MoveContext>()
            val leapContext = state.getContext<LeapRollContext>()
            val skillUsed = (action == Confirm)
            return buildCompositeCommand {
                add(RemoveContext(leapContext))
                if (skillUsed) {
                    val player = context.player
                    val ball = player.ball ?: INVALID_GAME_STATE("Player must have a ball to use Fumblerooski: $player")
                    addAll(
                        ReportSkillUsed(player, SkillType.FUMBLEROOSKI),
                        SetBallState.onGround(ball),
                        SetBallLocation(ball, context.startingSquare),
                    )
                }
                val targetNode = when (leapContext.isSuccess) {
                    true -> ResolveMove
                    false -> ResolvePlayerFallingOver
                }
                add(GotoNode(targetNode))
            }
        }
    }

    /**
     * The player failed its move and fell over. This creates a turnover
     * and requires an injury roll. Regardless of why the player fell down.
     */
    object ResolvePlayerFallingOver: ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command {
            val context = state.getContext<MoveContext>()
            return AddContext(RiskingInjuryContext(context.player, mode = RiskingInjuryMode.FALLING_OVER))
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = BB2025FallingOver
        override fun onExitNode(state: Game, rules: Rules): Command {
            // Regardless of the outcome, the player's action ends in a turnover
            return compositeCommandOf(
                RemoveContext<RiskingInjuryContext>(),
                ExitProcedure()
            )
        }
    }

    /**
     * Leaping player moved successfully to the target square. This happened in a previous node,
     * so we only update metadata here.
     */
    object ResolveMove: ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            val context = state.getContext<MoveContext>()
            val movingPlayer = context.player
            return compositeCommandOf(
                // Player was already moved just after rolling dice, so we just exit here.
                ExitProcedure()
            )
        }
    }

    // HELPER METHODS

    // Calculates all relevant modifiers at the current stage
    private fun calculateLeapModifiers(state: Game): List<DiceModifier> {
        val rules = state.rules
        val moveContext = state.getContext<MoveContext>()
        val player = moveContext.player
        val leapFromMarks = rules.calculateMarks(state, player.team, moveContext.startingSquare)
        val leapToMarks = rules.calculateMarks(state, player.team, moveContext.target!!)
        val markModifier = MarkedModifier(max(leapToMarks, leapFromMarks), LeapModifier.MARKED)
        val modifiers = mutableListOf<DiceModifier>()
        modifiers.add(markModifier)
        if (markModifier.modifier <= -2 && moveContext.useLeapModifier) {
            modifiers.add(LeapModifier.LEAP)
        }
        if (moveContext.useVeryLongLegs) {
            modifiers.add(LeapModifier.VERY_LONG_LEGS)
        }
        return modifiers
    }
}
