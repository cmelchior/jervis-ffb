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
import com.jervisffb.engine.commands.SetPlayerLocation
import com.jervisffb.engine.commands.UseShadowingSkill
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.context.RemoveContext
import com.jervisffb.engine.commands.context.SetContext
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.ParentNode
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.context.MoveContext
import com.jervisffb.engine.model.context.ShadowingRollContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.isSkillAvailable
import com.jervisffb.engine.reports.ReportSkillUsed
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.engine.utils.INVALID_ACTION

/**
 * Class wrapping using the Shadowing skill in BB2025.
 */
object UseShadowingStep: Procedure() {
    override val initialNode: Node = CheckIfShadowingIsAvailable
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null

    object CheckIfShadowingIsAvailable: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<MoveContext>().player.team.otherTeam()

        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val context = state.getContext<MoveContext>()
            val eligiblePlayers = context.startingSquare.getSurroundingCoordinates(rules)
                .filter { coord ->
                    state.field[coord].player
                        ?.let { player -> player.team != context.player.team}
                        ?: false
                }
                .mapNotNull { state.field[it].player }
                .filter { it.isSkillAvailable(SkillType.SHADOWING) }
                .let { playerList ->
                    if (playerList.isNotEmpty()) {
                        SelectPlayer.fromPlayers(playerList)
                    } else {
                        null
                    }
                }

            return if (eligiblePlayers == null) {
                listOf(ContinueWhenReady)
            } else {
                listOf(eligiblePlayers, CancelWhenReady)
            }
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            val context = state.getContext<MoveContext>()
            return when (action) {
                is PlayerSelected -> {
                    val player = action.getPlayer(state)
                    val shadowingContext = ShadowingRollContext(player = player)
                    compositeCommandOf(
                        ReportSkillUsed(player, player.getSkill(SkillType.SHADOWING)),
                        UseShadowingSkill(player),
                        SetContext(shadowingContext),
                        GotoNode(RollShadowingDie),
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

    object RollShadowingDie: ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = ShadowingRoll
        override fun onExitNode(state: Game, rules: Rules): Command {
            val moveContext = state.getContext<MoveContext>()
            val context = state.getContext<ShadowingRollContext>()
            return if (context.isSuccess) {
                compositeCommandOf(
                    SetPlayerLocation(context.player, moveContext.startingSquare),
                    RemoveContext<ShadowingRollContext>(),
                    ExitProcedure()
                )
            } else {
                compositeCommandOf(
                    RemoveContext<ShadowingRollContext>(),
                    ExitProcedure()
                )
            }
        }
    }
}
