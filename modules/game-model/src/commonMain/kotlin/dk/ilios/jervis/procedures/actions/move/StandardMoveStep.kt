package dk.ilios.jervis.procedures.actions.move

import compositeCommandOf
import dk.ilios.jervis.actions.ActionDescriptor
import dk.ilios.jervis.actions.EndActionWhenReady
import dk.ilios.jervis.actions.FieldSquareSelected
import dk.ilios.jervis.actions.GameAction
import dk.ilios.jervis.actions.MoveType
import dk.ilios.jervis.commands.Command
import dk.ilios.jervis.commands.ExitProcedure
import dk.ilios.jervis.commands.GotoNode
import dk.ilios.jervis.commands.RemoveContext
import dk.ilios.jervis.commands.SetContext
import dk.ilios.jervis.commands.SetPlayerMoveLeft
import dk.ilios.jervis.commands.SetPlayerRushesLeft
import dk.ilios.jervis.commands.SetPlayerState
import dk.ilios.jervis.fsm.ActionNode
import dk.ilios.jervis.fsm.ComputationNode
import dk.ilios.jervis.fsm.Node
import dk.ilios.jervis.fsm.ParentNode
import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.PlayerState
import dk.ilios.jervis.model.context.DodgeRollContext
import dk.ilios.jervis.model.context.MoveContext
import dk.ilios.jervis.model.context.RushRollContext
import dk.ilios.jervis.model.context.getContext
import dk.ilios.jervis.procedures.injury.FallingOver
import dk.ilios.jervis.procedures.injury.RiskingInjuryContext
import dk.ilios.jervis.procedures.injury.RiskingInjuryMode
import dk.ilios.jervis.rules.Rules

/**
 * Handle a player moving a single step using a normal move.
 *
 * This sub procedure is purely used by [MoveTypeSelectorStep], which is also
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
        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
            val player = state.getContext<MoveContext>().player
            val eligibleSquares = calculateOptionsForMoveType(state, rules, player, MoveType.STANDARD)
            return eligibleSquares + listOf(EndActionWhenReady)
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return checkType<FieldSquareSelected>(action) {
                val context = state.getContext<MoveContext>()
                compositeCommandOf(
                    SetContext(context.copy(target = it.coordinate)),
                    GotoNode(MovePlayer),
                )
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
            return if (state.isTurnOver) {
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
                    SetPlayerState(player, PlayerState.FALLED_OVER),
                    RemoveContext<RushRollContext>(),
                    GotoNode(ResolvePlayerFallingOver)
                )
            }
        }
    }

    object CheckIfDodgeIsNeeded : ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            val context = state.getContext<MoveContext>()
            val isMarked = context.startingSquare.coordinate.getSurroundingCoordinates(rules, 1)
                .filter { state.field[it].player != null }
                .filter { state.field[it].player!!.team != context.player.team }
                .firstOrNull { rules.canMark(state.field[it].player!!) } != null

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
            ))
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
                    SetPlayerState(player, PlayerState.KNOCKED_DOWN),
                    RemoveContext<DodgeRollContext>(),
                    GotoNode(ResolvePlayerFallingOver)
                )
            }
        }
    }

    object CheckIfShadowingIsAvailable: ActionNode() {
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
            return SetContext(RiskingInjuryContext(context.player, RiskingInjuryMode.FALLING_OVER))
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
                ExitProcedure()
            )
        }
    }
}
