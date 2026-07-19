package com.jervisffb.engine.rules.bb2025.procedures.skills

import com.jervisffb.engine.actions.Cancel
import com.jervisffb.engine.actions.CancelWhenReady
import com.jervisffb.engine.actions.Continue
import com.jervisffb.engine.actions.ContinueWhenReady
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.actions.SelectPlayer
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.context.UpdateContext
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.ParentNode
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.context.TentaclesRollContext
import com.jervisffb.engine.model.context.assertContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.isSkillAvailable
import com.jervisffb.engine.reports.ReportSkillUsed
import com.jervisffb.engine.reports.ReportTentaclesResult
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.engine.utils.INVALID_ACTION

/**
 * Class wrapping using the Tentacles skill in BB2025.
 */
object TentaclesStep: Procedure() {
    override val initialNode: Node = ChooseToUseTentacles
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null
    override fun isValid(state: Game, rules: Rules) = state.assertContext<TentaclesRollContext>()

    object ChooseToUseTentacles: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<TentaclesRollContext>().movingPlayer.team.otherTeam()
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val context = state.getContext<TentaclesRollContext>()
            val eligiblePlayers = context.movingPlayer.coordinates.getSurroundingCoordinates(rules)
                .asSequence()
                .mapNotNull { state.pitch[it].player }
                .filter { it.team != context.movingPlayer.team }
                .filter { it.isSkillAvailable(SkillType.TENTACLES) }
                .toList()

            return if (eligiblePlayers.isEmpty()) {
                listOf(ContinueWhenReady)
            } else {
                listOf(SelectPlayer.fromPlayers(eligiblePlayers), CancelWhenReady)
            }
        }
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return when (action) {
                is PlayerSelected -> {
                    val context = state.getContext<TentaclesRollContext>()
                    val player = action.getPlayer(state)
                    compositeCommandOf(
                        ReportSkillUsed(player, SkillType.TENTACLES),
                        UpdateContext(context.copy(tentaclePlayer = player)),
                        GotoNode(RollTentaclesDie),
                    )
                }
                is Cancel,
                is Continue -> {
                    ExitProcedure()
                }
                else -> INVALID_ACTION(action)
            }
        }
    }

    object RollTentaclesDie: ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = TentaclesRoll
        override fun onExitNode(state: Game, rules: Rules): Command {
            val context = state.getContext<TentaclesRollContext>()
            return compositeCommandOf(
                ReportTentaclesResult(context),
                ExitProcedure()
            )
        }
    }
}
