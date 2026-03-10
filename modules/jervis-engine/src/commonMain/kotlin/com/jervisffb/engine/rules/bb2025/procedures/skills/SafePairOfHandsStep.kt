package com.jervisffb.engine.rules.bb2025.procedures.skills

import com.jervisffb.engine.actions.CancelWhenReady
import com.jervisffb.engine.actions.Confirm
import com.jervisffb.engine.actions.ConfirmWhenReady
import com.jervisffb.engine.actions.ContinueWhenReady
import com.jervisffb.engine.actions.DirectionSelected
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.actions.SelectDirection
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.SetBallLocation
import com.jervisffb.engine.commands.SetBallState
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.fsm.castAction
import com.jervisffb.engine.model.Direction
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.isSkillAvailable
import com.jervisffb.engine.reports.ReportSkillUsed
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.common.procedures.tables.injury.RiskingInjuryContext
import com.jervisffb.engine.rules.common.skills.SkillType

object SafePairOfHandsStep: Procedure() {
    override val initialNode: Node = ChooseToUseSafePairOfHands
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null

    object ChooseToUseSafePairOfHands: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team {
            val context = state.getContext<RiskingInjuryContext>()
            return context.player.team
        }
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val context = state.getContext<RiskingInjuryContext>()
            val hasSafePairOfHands = context.player.isSkillAvailable(SkillType.SAFE_PAIR_OF_HANDS)
            val hasBall = context.player.hasBall()
            val hasValidTargets = context.player.location.isOnField(rules) && context.player.coordinates.getSurroundingCoordinates(rules).any { !state.field[it].isOccupied() }
            return when (hasBall && hasSafePairOfHands && hasValidTargets) {
                true -> listOf(ConfirmWhenReady, CancelWhenReady)
                false -> listOf(ContinueWhenReady)
            }
        }
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            val context = state.getContext<RiskingInjuryContext>()
            val useSafePairOfHands = (action == Confirm)
            return if (useSafePairOfHands) {
                compositeCommandOf(
                    ReportSkillUsed(context.player, SkillType.SAFE_PAIR_OF_HANDS),
                    GotoNode(SelectSquareToPlaceBall)
                )
            } else {
                ExitProcedure()
            }
        }
    }

    object SelectSquareToPlaceBall: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team {
            return state.getContext<RiskingInjuryContext>().player.team
        }
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val context = state.getContext<RiskingInjuryContext>()
            val player = context.player
            val action = player.coordinates.getSurroundingCoordinates(rules)
                .filterNot { state.field[it].isOccupied() }
                .let { targets ->
                    val directions = targets.map { Direction.from(player.coordinates, it) }
                    SelectDirection(player.coordinates, directions)
                }
            return listOf(action)
        }
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return castAction<DirectionSelected>(action) { directionSelected ->
                val context = state.getContext<RiskingInjuryContext>()
                val target = context.player.coordinates.move(directionSelected.direction, 1)
                val ball = context.player.ball!!
                compositeCommandOf(
                    SetBallLocation(ball, target),
                    SetBallState.onGround(ball),
                    ExitProcedure()
                )
            }
        }
    }
}
