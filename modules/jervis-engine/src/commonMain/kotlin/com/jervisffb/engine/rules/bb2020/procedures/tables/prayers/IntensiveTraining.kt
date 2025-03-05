package com.jervisffb.engine.rules.bb2020.procedures.tables.prayers

import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.actions.SelectPlayer
import com.jervisffb.engine.actions.SelectSkill
import com.jervisffb.engine.actions.SkillSelected
import com.jervisffb.engine.commands.AddPlayerSkill
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.SetContext
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.fsm.checkType
import com.jervisffb.engine.fsm.checkTypeAndValue
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.PlayerState
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.context.ProcedureContext
import com.jervisffb.engine.model.context.assertContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.hasSkill
import com.jervisffb.engine.reports.ReportGameProgress
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.bb2020.procedures.PrayersToNuffleRollContext
import com.jervisffb.engine.rules.bb2020.roster.BB2020Position
import com.jervisffb.engine.rules.bb2020.skills.Duration
import com.jervisffb.engine.rules.bb2020.skills.Loner

data class IntensiveTrainingContext(
    val player: Player,
): ProcedureContext

/**
 * Procedure for handling the Prayer to Nuffle "Intensive Training" as described on page 39
 * of the rulebook.
 */
object IntensiveTraining : Procedure() {
    override val initialNode: Node = SelectPlayer
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null
    override fun isValid(state: Game, rules: Rules) = state.assertContext<PrayersToNuffleRollContext>()

    object SelectPlayer : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team? = null
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            return state.activeTeam
                .filter { it.state == PlayerState.STANDING }
                .filter { !it.hasSkill<Loner>() }
                .map { SelectPlayer(it) }
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return checkType<PlayerSelected>(action) {
                return compositeCommandOf(
                    SetContext(IntensiveTrainingContext(it.getPlayer(state))),
                    GotoNode(SelectSkill)
                )
            }
        }
    }

    object SelectSkill : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<IntensiveTrainingContext>().player.team
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val context = state.getContext<IntensiveTrainingContext>()
            return listOf(SelectSkill(skills = (context.player.position as BB2020Position).primary.flatMap { it.skills }))
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return checkTypeAndValue<SkillSelected>(state, rules, action) {
                val context = state.getContext<IntensiveTrainingContext>()
                val skill = it.skill.createSkill(isTemporary = true, expiresAt = Duration.END_OF_GAME)
                return compositeCommandOf(
                    AddPlayerSkill(context.player, skill),
                    ReportGameProgress("${context.player.name} receives ${skill.name} due to Intensive Training"),
                    ExitProcedure()
                )
            }
        }
    }
}
