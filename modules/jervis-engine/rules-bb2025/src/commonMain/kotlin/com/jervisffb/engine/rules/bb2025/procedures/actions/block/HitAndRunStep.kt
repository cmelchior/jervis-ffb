package com.jervisffb.engine.rules.bb2025.procedures.actions.block

import com.jervisffb.engine.actions.Cancel
import com.jervisffb.engine.actions.CancelWhenReady
import com.jervisffb.engine.actions.Confirm
import com.jervisffb.engine.actions.ConfirmWhenReady
import com.jervisffb.engine.actions.Continue
import com.jervisffb.engine.actions.ContinueWhenReady
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.actions.PitchSquareSelected
import com.jervisffb.engine.actions.SelectPitchLocation
import com.jervisffb.engine.actions.TargetSquare
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.SetPlayerLocation
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.isSkillAvailable
import com.jervisffb.engine.model.locations.PitchCoordinate
import com.jervisffb.engine.reports.ReportSkillUsed
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.engine.utils.INVALID_ACTION
import com.jervisffb.engine.utils.INVALID_GAME_STATE

/**
 * Procedure for controlling the Hit and Run extra move for Block and Stab.
 * action.
 *
 * If the active player doesn't have Hit and Run or it isn't applicable, this
 * procedure will exit without doing anything.
 */
object HitAndRunStep : Procedure() {
    override val initialNode: Node = ChooseToUseHitAndRun
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null
    override fun isValid(state: Game, rules: Rules) {
        if (state.activePlayer == null) INVALID_GAME_STATE("Missing active player")
    }

    object ChooseToUseHitAndRun : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.activePlayerOrThrow().team
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val player = state.activePlayerOrThrow()
            val isStanding = rules.isStanding(player)
            val hasHitAndRun = player.isSkillAvailable(SkillType.HIT_AND_RUN)
            val hasEligibleTargetSquares = getEligibleTargetSquares(player).isNotEmpty()
            return when (isStanding && hasHitAndRun && hasEligibleTargetSquares) {
                true -> listOf(ConfirmWhenReady, CancelWhenReady)
                false -> listOf(ContinueWhenReady)
            }
        }
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            val player = state.activePlayerOrThrow()
            return when (action) {
                Continue,
                Cancel -> ExitProcedure()
                Confirm -> compositeCommandOf(
                    ReportSkillUsed(player, SkillType.HIT_AND_RUN),
                    GotoNode(SelectTargetSquareOrCancel),
                )
                else -> INVALID_ACTION(action)
            }
        }
    }

    object SelectTargetSquareOrCancel: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.activePlayerOrThrow().team
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val eligibleTargetSquares = getEligibleTargetSquares(state.activePlayerOrThrow())
            return listOf(
                SelectPitchLocation(eligibleTargetSquares.map { TargetSquare.hitAndRun(it) }),
                CancelWhenReady, // Also allow canceling if no good move options are found
            )
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return when (action) {
                Cancel -> return ExitProcedure()
                is PitchSquareSelected -> {
                    val player = state.activePlayerOrThrow()
                    compositeCommandOf(
                        SetPlayerLocation(player, action.coordinate),
                        ExitProcedure()
                    )
                }
                else -> INVALID_ACTION(action)
            }
        }
    }

    // -- HELPER METHODS --
    fun getEligibleTargetSquares(player: Player): List<PitchCoordinate> {
        val state = player.team.game
        val rules = state.rules
        val player = state.activePlayerOrThrow()
        if (!rules.isStanding(player)) return emptyList()
        return player.coordinates.getSurroundingCoordinates(rules)
            .filter { !state.pitch[it].isOccupied() }
            .filter { target ->
                // Player with Hit and Run will be marked in the target square
                val isMarked = rules.isMarked(player, target)
                if (isMarked) return@filter false

                // Player with Hit and Run will be marking an opponent player in the target square
                // We know that player is standing (to trigger Hit and Run), so they will
                // automatically mark any opponent player adjacent to the target square.
                target.getSurroundingCoordinates(rules)
                    .none {
                        state.pitch[it].player?.let { p ->
                            player.team != p.team
                        } ?: false
                    }
            }
    }
}
