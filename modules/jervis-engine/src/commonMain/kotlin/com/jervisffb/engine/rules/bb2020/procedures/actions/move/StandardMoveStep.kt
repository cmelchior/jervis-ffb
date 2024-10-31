package com.jervisffb.engine.rules.bb2020.procedures.actions.move

import com.jervisffb.engine.actions.ActionDescriptor
import com.jervisffb.engine.actions.EndAction
import com.jervisffb.engine.actions.EndActionWhenReady
import com.jervisffb.engine.actions.FieldSquareSelected
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.MoveType
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.RemoveContext
import com.jervisffb.engine.commands.SetContext
import com.jervisffb.engine.commands.SetPlayerMoveLeft
import com.jervisffb.engine.commands.SetPlayerRushesLeft
import com.jervisffb.engine.commands.SetPlayerState
import com.jervisffb.engine.commands.SetTurnOver
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.ComputationNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.ParentNode
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.fsm.checkTypeAndValue
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.PlayerState
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.TurnOver
import com.jervisffb.engine.model.context.DodgeRollContext
import com.jervisffb.engine.model.context.MoveContext
import com.jervisffb.engine.model.context.RushRollContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.bb2020.procedures.tables.injury.FallingOver
import com.jervisffb.engine.rules.bb2020.procedures.tables.injury.RiskingInjuryContext
import com.jervisffb.engine.rules.bb2020.procedures.tables.injury.RiskingInjuryMode

/**
 * Handle a player moving a single step using a normal move.
 *
 * This sub procedure is purely used by [ResolveMoveTypeStep], which is also
 * responsible for controlling the lifecycle of [MoveContext].
 *
 * The order of checks is:
 * 1. Tentacles
 * 1. Rush
 *  a. Sprint
 *  b. Sure Feet
 * 2. Dodge
 *   a. Two Heads / Stunty* / Titchy*
 *   b. Break Tackle
 *   c. Prehensile Tail
 *   d. Diving Tackle
 * 3. Shadowing
 */
object StandardMoveStep: Procedure() {
    override val initialNode: Node = SelectTargetSquareOrEndAction
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null

    object SelectTargetSquareOrEndAction: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<MoveContext>().player.team
        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
            val context = state.getContext<MoveContext>()
            val player = context.player
            val eligibleSquares = calculateOptionsForMoveType(state, rules, player, MoveType.STANDARD)
            return eligibleSquares + listOf(EndActionWhenReady)
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            val context = state.getContext<MoveContext>()
            return when(action) {
                // If the player has not moved, this will not use their action
                EndAction -> ExitProcedure()
                else -> checkTypeAndValue<FieldSquareSelected>(state, rules, action) {
                    compositeCommandOf(
                        SetContext(context.copy(target = it.coordinate, hasMoved = true)),
                        GotoNode(MovePlayer),
                    )
                }
            }
        }
    }

    // When moving a player, they are placed into the target square
    // before rolling any dice.
    object MovePlayer: ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command {
            val moveContext = state.getContext<MoveContext>()
            return SetContext(
                MovePlayerIntoSquareContext(
                    player = moveContext.player,
                    target = moveContext.target!!,
                )
            )
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = MovePlayerIntoSquare
        override fun onExitNode(state: Game, rules: Rules): Command {
            return if (state.turnOver != null) {
                ExitProcedure() // Something went wrong when moving the player
            } else {
                GotoNode(CheckIfRushingIsNeeded)
            }
        }
    }

    object CheckIfRushingIsNeeded : ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            val context = state.getContext<MoveContext>()
            return if (context.player.movesLeft == 0) {
                GotoNode(ResolveRush)
            } else {
                GotoNode(CheckIfDodgeIsNeeded)
            }
        }
    }

    /**
     * Player has no ordinary move allowance left, so need to make a Rush roll.
     * If successful, they gain +1 movement allowance.
     */
    object ResolveRush: ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command {
            val moveContext = state.getContext<MoveContext>()
            return SetContext(RushRollContext(moveContext.player, moveContext.target!!))
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = RushRoll
        override fun onExitNode(state: Game, rules: Rules): Command {
            val rushContext = state.getContext<RushRollContext>()
            val player = rushContext.player
            return if (rushContext.isSuccess) {
                compositeCommandOf(
                    SetPlayerRushesLeft(player, player.rushesLeft - 1),
                    SetPlayerMoveLeft(player, player.movesLeft + 1),
                    RemoveContext<RushRollContext>(),
                    GotoNode(CheckIfDodgeIsNeeded)
                )
            } else {
                // Rush failed, player is Knocked Down in target square
                return compositeCommandOf(
                    SetPlayerState(player, PlayerState.FALLEN_OVER),
                    SetTurnOver(TurnOver.STANDARD),
                    RemoveContext<RushRollContext>(),
                    GotoNode(ResolvePlayerFallingOver)
                )
            }
        }
    }

    object CheckIfDodgeIsNeeded : ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            val context = state.getContext<MoveContext>()
            val isMarked = rules.isMarked(context.player, context.startingSquare)
            return if (isMarked) {
                GotoNode(ResolveDodge)
            } else {
                GotoNode(ResolveMove) // Shadowing here
            }
        }
    }

    object ResolveDodge: ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command {
            val moveContext = state.getContext<MoveContext>()
            return SetContext(context = DodgeRollContext(
                moveContext.player,
                moveContext.startingSquare,
                moveContext.target!!
            )
            )
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = DodgeRoll
        override fun onExitNode(state: Game, rules: Rules): Command {
            val dodgeContext = state.getContext<DodgeRollContext>()
            val player = dodgeContext.player
            return if (dodgeContext.isSuccess) {
                compositeCommandOf(
                    RemoveContext<DodgeRollContext>(),
                    GotoNode(ResolveMove/*CheckIfShadowingIsAvailable*/)
                )
            } else {
                compositeCommandOf(
                    SetPlayerState(player, PlayerState.FALLEN_OVER, hasTackleZones = false),
                    SetTurnOver(TurnOver.STANDARD),
                    RemoveContext<DodgeRollContext>(),
                    GotoNode(ResolvePlayerFallingOver)
                )
            }
        }
    }

    object CheckIfShadowingIsAvailable: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<MoveContext>().player.team.otherTeam()
        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
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
            return SetContext(RiskingInjuryContext(context.player, mode = RiskingInjuryMode.FALLING_OVER))
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = FallingOver
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
                SetPlayerMoveLeft(movingPlayer, movingPlayer.movesLeft - 1),
                GotoNode(CheckForScoring)
            )
        }
    }

    object CheckForScoring : ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command {
            val context = state.getContext<MoveContext>()
            return SetContext(ScoringATouchDownContext(context.player))
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = ScoringATouchdown
        override fun onExitNode(state: Game, rules: Rules): Command {
            return ExitProcedure()
        }
    }
}
