package com.jervisffb.engine.rules.common.procedures.tables.prayers

import com.jervisffb.engine.actions.Continue
import com.jervisffb.engine.actions.ContinueWhenReady
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.actions.SelectPlayer
import com.jervisffb.engine.commands.AddPlayerSkill
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.fsm.castAction
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.PlayerState
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.context.assertContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.hasSkill
import com.jervisffb.engine.reports.ReportGameProgress
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.common.procedures.PrayersToNuffleRollContext
import com.jervisffb.engine.rules.common.skills.Duration
import com.jervisffb.engine.rules.common.skills.SkillType

/**
 * Procedure for handling the Prayer to Nuffle "Knuckle Dusters" as described on page 39
 * of the rulebook.
 */
object KnuckleDusters : Procedure() {
    override val initialNode: Node = ChoosePlayer
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null
    override fun isValid(state: Game, rules: Rules) {
        state.assertContext<PrayersToNuffleRollContext>()
    }

    object ChoosePlayer : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<PrayersToNuffleRollContext>().team
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val context = state.getContext<PrayersToNuffleRollContext>()
            val requestedAction = context.team
                .filter { it.state == PlayerState.RESERVE || it.location.isOnPitch(rules) }
                .filter { !it.hasSkill(SkillType.LONER) && !it.hasSkill(SkillType.MIGHTY_BLOW) }
                .let {
                    when (it.isNotEmpty()) {
                        true -> SelectPlayer.fromPlayers(it)
                        false -> ContinueWhenReady
                    }
                }
            return listOf(requestedAction)
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return when (action) {
                is Continue -> {
                    compositeCommandOf(
                        ReportGameProgress("No players are able to receive Iron Man"),
                        ExitProcedure(),
                    )
                }
                else -> {
                    castAction<PlayerSelected>(action) {
                        val player = it.getPlayer(state)
                        compositeCommandOf(
                            AddPlayerSkill(
                                player = player,
                                skill = rules.createSkill(
                                    player = player,
                                    skill = SkillType.MIGHTY_BLOW.id(1),
                                    expiresAt = Duration.END_OF_DRIVE
                                ),
                            ),
                            ReportGameProgress("${player.name} received Knuckle Dusters"),
                            ExitProcedure(),
                        )
                    }
                }
            }
        }
    }
}
