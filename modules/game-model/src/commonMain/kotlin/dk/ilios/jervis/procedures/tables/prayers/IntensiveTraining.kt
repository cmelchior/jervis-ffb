package dk.ilios.jervis.procedures.tables.prayers

import compositeCommandOf
import dk.ilios.jervis.actions.ActionDescriptor
import dk.ilios.jervis.actions.GameAction
import dk.ilios.jervis.actions.PlayerSelected
import dk.ilios.jervis.actions.SelectPlayer
import dk.ilios.jervis.actions.SelectSkill
import dk.ilios.jervis.actions.SkillSelected
import dk.ilios.jervis.commands.AddPlayerSkill
import dk.ilios.jervis.commands.Command
import dk.ilios.jervis.commands.SetContext
import dk.ilios.jervis.commands.fsm.ExitProcedure
import dk.ilios.jervis.commands.fsm.GotoNode
import dk.ilios.jervis.fsm.ActionNode
import dk.ilios.jervis.fsm.Node
import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.Player
import dk.ilios.jervis.model.PlayerState
import dk.ilios.jervis.model.Team
import dk.ilios.jervis.model.context.ProcedureContext
import dk.ilios.jervis.model.context.assertContext
import dk.ilios.jervis.model.context.getContext
import dk.ilios.jervis.model.hasSkill
import dk.ilios.jervis.procedures.PrayersToNuffleRollContext
import dk.ilios.jervis.reports.ReportGameProgress
import dk.ilios.jervis.rules.Rules
import dk.ilios.jervis.rules.roster.bb2020.BB2020Position
import dk.ilios.jervis.rules.skills.Duration
import dk.ilios.jervis.rules.skills.Loner

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
        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
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
        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
            val context = state.getContext<IntensiveTrainingContext>()
            return (context.player.position as BB2020Position).primary.flatMap { it.skills }.map {
                SelectSkill(it)
            }
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return checkTypeAndValue<SkillSelected>(state, rules, action, this) {
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
