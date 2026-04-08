package com.jervisffb.engine.rules.bb2020.procedures.actions.move

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
import com.jervisffb.engine.commands.SetPlayerIntermediateState
import com.jervisffb.engine.commands.SetPlayerLocation
import com.jervisffb.engine.commands.SetPlayerMoveLeft
import com.jervisffb.engine.commands.SetPlayerRushesLeft
import com.jervisffb.engine.commands.SetTurnOver
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
import com.jervisffb.engine.model.PlayerIntermediateState
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.TurnOver
import com.jervisffb.engine.model.context.JumpRollContext
import com.jervisffb.engine.model.context.MoveContext
import com.jervisffb.engine.model.context.RushRollContext
import com.jervisffb.engine.model.context.assertContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.isSkillAvailable
import com.jervisffb.engine.model.modifiers.DiceModifier
import com.jervisffb.engine.model.modifiers.JumpModifier
import com.jervisffb.engine.model.modifiers.MarkedModifier
import com.jervisffb.engine.rules.JUMP_DISTANCE
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.bb2020.procedures.tables.injury.BB2020FallingOver
import com.jervisffb.engine.rules.common.procedures.actions.move.JumpRoll
import com.jervisffb.engine.rules.common.procedures.actions.move.RushRoll
import com.jervisffb.engine.rules.common.procedures.calculateOptionsForMoveType
import com.jervisffb.engine.rules.common.procedures.tables.injury.RiskingInjuryContext
import com.jervisffb.engine.rules.common.procedures.tables.injury.RiskingInjuryMode
import com.jervisffb.engine.rules.common.skills.SkillType
import kotlinx.collections.immutable.toPersistentList
import kotlin.math.max

/**
 * Procedure controlling a Jump action.
 *
 * See page 45 in the BB2020 rulebook.
 * See page 56 in the BB2025 rulebook.
 *
 * The order of checks is:
 * 1. Tentacles
 * 2. Rush(es)
 * 3. Jump
 * 4. Shadowing
 *
 * Since the player does not need to Dodge to leave the square, any skills
 * that trigger on Dodge cannot trigger on Jumps.
 *
 * Developer's Commentary:
 * What happens in case a player needs to Rush twice to reach the target square,
 * seems a bit undefined by the rules. E.g., what happens if you fail the first Rush?
 *
 * 1) Do you end up in the Jump target?
 * 2) Do you end up in the starting square?
 * 3) Some other square (as the middle square is filled)?
 *
 * This was answered in the Designer's Commentary, and the ruling is that if you
 * fail the first Rush, you end up in the starting square. The FAQ entry
 * doesn't mention what happens when you fail the 2nd Rush.
 *
 * BB2025 Update: In BB2025 it is defined that failing any Rush leaves the player
 * in the starting field. So we apply the same rule to BB2020.
 *
 * The interaction between Jump and Tentacles is also a bit unclear,
 * https://www.reddit.com/r/bloodbowl/comments/xodttp/shadowing_and_tentacles_work_on_followups_jumps/
 *
 * This was clarified in the Designer's Commentary. Tentacles are rolled first.
 */
object JumpStep : Procedure() {
    override val initialNode: Node = SelectTargetSquareOrCancel
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null
    override fun isValid(state: Game, rules: Rules) = state.assertContext<MoveContext>()

    object SelectTargetSquareOrCancel : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team? = state.getContext<MoveContext>().player.team

        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val context = state.getContext<MoveContext>()
            val jumpingPlayer = context.player
            val eligibleJumpSquares = calculateOptionsForMoveType(state, rules, jumpingPlayer, MoveType.JUMP)
            return eligibleJumpSquares + CancelWhenReady
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return when (action) {
                Cancel -> ExitProcedure()
                else -> {
                    castAction<FieldSquareSelected>(action) { target ->
                        val context = state.getContext<MoveContext>()
                        compositeCommandOf(
                            UpdateContext(context.copy(target = target.coordinate)),
                            GotoNode(CheckIfRushingIsNeeded)
                        )
                    }
                }
            }
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
                    SetPlayerLocation(context.player, context.target!!),
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
     * Rush, they stay in the starting square. If they fail the second, they fall
     * over in the target square.
     */
    object RushTwice: ParentNode() {
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
                    SetPlayerLocation(moveContext.player, moveContext.target!!),
                    SetPlayerMoveLeft(player, player.movesLeft + 1),
                    SetPlayerRushesLeft(player, player.rushesLeft - 1),
                    RemoveContext(rushContext),
                    GotoNode(RushOnce)
                )
            } else {
                // Rush failed, player is Knocked Down in target square
                compositeCommandOf(
                    SetPlayerIntermediateState(player, PlayerIntermediateState.FALLEN_OVER),
                    SetTurnOver(TurnOver.STANDARD),
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
            val rushContext = state.getContext<RushRollContext>()
            val player = rushContext.player
            return if (rushContext.isSuccess) {
                compositeCommandOf(
                    RemoveContext(rushContext),
                    SetPlayerMoveLeft(player, player.movesLeft + 1),
                    SetPlayerRushesLeft(player, player.rushesLeft - 1),
                    GotoNode(ChooseToUseVeryLongLegs)
                )
            } else {
                // Rush failed, player is Knocked Down in target square
                compositeCommandOf(
                    RemoveContext(rushContext),
                    SetPlayerIntermediateState(player, PlayerIntermediateState.FALLEN_OVER),
                    SetTurnOver(TurnOver.STANDARD),
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
                GotoNode(CalculateJumpModifiers)
            )
        }
    }

    object CalculateJumpModifiers: ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            val moveContext = state.getContext<MoveContext>()
            val player = moveContext.player
            val jumpFromMarks = rules.calculateMarks(state, player.team, moveContext.startingSquare)
            val jumpToMarks = rules.calculateMarks(state, player.team, moveContext.target!!)
            val markModifier = MarkedModifier(max(jumpToMarks, jumpFromMarks), JumpModifier.MARKED)
            val modifiers = mutableListOf<DiceModifier>()
            modifiers.add(markModifier)
            if (moveContext.useVeryLongLegs) {
                modifiers.add(JumpModifier.VERY_LONG_LEGS)
            }
            return compositeCommandOf(
                AddContext(
                    JumpRollContext(
                        player = player,
                        startingSquare = moveContext.startingSquare,
                        modifiers = modifiers.toPersistentList(),
                    )
                ),
                GotoNode(RollForJump)
            )
        }
    }

    /**
     * Player could move to the target square (after rushes, tentacles) and can
     * now roll for Jump.
     */
    object RollForJump: ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = JumpRoll
        override fun onExitNode(state: Game, rules: Rules): Command {
            val moveContext = state.getContext<MoveContext>()
            val jumpContext = state.getContext<JumpRollContext>()
            val player = jumpContext.player
            return if (jumpContext.isSuccess) {
                compositeCommandOf(
                    RemoveContext(jumpContext),
                    GotoNode(ResolveMove)
                )
            } else if (!jumpContext.isSuccess && jumpContext.roll!!.result.value == 1) {
                // Rush failed catastrophically, player Falls Over in starting square
                compositeCommandOf(
                    SetPlayerLocation(player, moveContext.startingSquare),
                    SetPlayerIntermediateState(player, PlayerIntermediateState.FALLEN_OVER),
                    SetTurnOver(TurnOver.STANDARD),
                    RemoveContext(jumpContext),
                    GotoNode(ResolvePlayerFallingOver)
                )
            } else {
                // Rush failed, player is Knocked Down in target square
                compositeCommandOf(
                    SetPlayerIntermediateState(player, PlayerIntermediateState.FALLEN_OVER),
                    SetTurnOver(TurnOver.STANDARD),
                    RemoveContext(jumpContext),
                    GotoNode(ResolvePlayerFallingOver)
                )
            }
        }
    }

    object CheckIfShadowingIsAvailable: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<MoveContext>().player.team.otherTeam()
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            TODO("Not yet implemented")
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            TODO("Not yet implemented")
        }
    }

    object CheckTentacles: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<MoveContext>().player.team.otherTeam()
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            TODO("Not yet implemented")
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            TODO("Not yet implemented")
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
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = BB2020FallingOver
        override fun onExitNode(state: Game, rules: Rules): Command {
            // Regardless of the outcome, the player's action ends in a turnover
            return compositeCommandOf(
                ExitProcedure()
            )
        }
    }

    /**
     * Resolve the final result of the move after rolling for potential rushes, dodge and other skills.
     */
    object ResolveMove: ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            val context = state.getContext<MoveContext>()
            val movingPlayer = context.player
            return compositeCommandOf(
                // Player was already moved before rolling any dice, so here we just
                // adjust stats.
                SetPlayerMoveLeft(movingPlayer, movingPlayer.movesLeft - JUMP_DISTANCE),
                ExitProcedure()
            )
        }
    }
}
