package com.jervisffb.engine.rules.common.procedures.tables.prayers

import com.jervisffb.engine.actions.Continue
import com.jervisffb.engine.actions.ContinueWhenReady
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.actions.SelectPlayer
import com.jervisffb.engine.actions.SelectSkill
import com.jervisffb.engine.actions.SkillSelected
import com.jervisffb.engine.commands.AddPlayerSkill
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.context.SetContext
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.fsm.castAction
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
import com.jervisffb.engine.rules.common.procedures.PrayersToNuffleRollContext
import com.jervisffb.engine.rules.common.skills.Duration
import com.jervisffb.engine.rules.common.skills.SkillType

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
        override fun actionOwner(state: Game, rules: Rules): Team? = state.getContext<PrayersToNuffleRollContext>().team
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val availablePlayers = state.getContext<PrayersToNuffleRollContext>().team
                .filter {it.state == PlayerState.RESERVE }
                .filter { !it.hasSkill(SkillType.LONER) }
                .map { SelectPlayer(it) }
            return availablePlayers.ifEmpty {
                listOf(ContinueWhenReady)
            }
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return when (action) {
                is Continue -> {
                    compositeCommandOf(
                        ReportGameProgress("No players are able to receive Intensive Training"),
                        ExitProcedure(),
                    )
                }
                else -> {
                    castAction<PlayerSelected>(action) {
                        compositeCommandOf(
                            SetContext(IntensiveTrainingContext(it.getPlayer(state))),
                            GotoNode(SelectSkill)
                        )
                    }
                }
            }
        }
    }

    object SelectSkill : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<IntensiveTrainingContext>().player.team
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val context = state.getContext<IntensiveTrainingContext>()
            return listOf(SelectSkill(skills = context.player.position.primary.flatMap {
                rules.skillSettings.getAvailableSkillsIds(it)
            }))
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return castAction<SkillSelected>(action) {
                val context = state.getContext<IntensiveTrainingContext>()
                val skill = rules.createSkill(context.player, it.skill, expiresAt = Duration.END_OF_GAME)
                return compositeCommandOf(
                    AddPlayerSkill(context.player, skill),
                    ReportGameProgress("${context.player.name} receives ${skill.name} due to Intensive Training"),
                    ExitProcedure()
                )
            }
        }
    }
}
