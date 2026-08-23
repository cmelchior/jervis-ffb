package com.jervisffb.engine.bb2025.procedures.inducements

import com.jervisffb.engine.bb2025.commands.AddTeamMascot
import com.jervisffb.engine.bb2025.inducements.BB2025InducementType
import com.jervisffb.engine.bb2025.procedures.rerolls.ExtraTeamTrainingReroll
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.buildCompositeCommand
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.context.AddContext
import com.jervisffb.engine.commands.context.RemoveContext
import com.jervisffb.engine.commands.context.UpdateContext
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.common.commands.AddBribe
import com.jervisffb.engine.common.commands.AddMortuaryAssistant
import com.jervisffb.engine.common.commands.AddPlagueDoctor
import com.jervisffb.engine.common.commands.AddTeamReroll
import com.jervisffb.engine.common.commands.AddWanderingApothecary
import com.jervisffb.engine.common.commands.AddWeatherMage
import com.jervisffb.engine.common.commands.SetBlitzersBestKegs
import com.jervisffb.engine.common.commands.SetHalflingMasterChefs
import com.jervisffb.engine.common.commands.SetPartTimeAssistantCoaches
import com.jervisffb.engine.common.commands.SetTempAgencyCheerleaders
import com.jervisffb.engine.common.context.PrayersToNuffleRollContext
import com.jervisffb.engine.common.inducements.CommonInducementSelection
import com.jervisffb.engine.common.inducements.CommonInducementType
import com.jervisffb.engine.common.procedures.PrayersToNuffleRoll
import com.jervisffb.engine.common.procedures.inducements.ApplyInducementsContext
import com.jervisffb.engine.fsm.ComputationNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.ParentNode
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.context.assertContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.inducements.Bribe
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.common.procedures.DummyProcedure
import com.jervisffb.engine.rules.common.skills.Duration
import com.jervisffb.engine.utils.INVALID_GAME_STATE

/**
 * This procedure is responsible for applying all selected inducements to the
 * team. It assumes that the parent procedure has validated that the inducements
 * are valid.
 */
object ApplyInducements : Procedure() {
    override val initialNode: Node = ApplyAutomaticInducements
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null
    override fun isValid(state: Game, rules: Rules) = state.assertContext<ApplyInducementsContext>()

    object ApplyAutomaticInducements: ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            val context = state.getContext<ApplyInducementsContext>()
            val team = context.team
            var updatedContext = context
            return buildCompositeCommand {
                context.inducements.inducements.forEach { inducement ->
                    when (inducement) {
                        is CommonInducementSelection.BiasedReferee -> { /* Not supported yet */ }
                        is CommonInducementSelection.InfamousCoach -> { /* Not supported yet */ }
                        is CommonInducementSelection.Mercenary -> { /* Not supported yet */ }
                        is CommonInducementSelection.Simple -> {
                            when (val type = inducement.type) {
                                is CommonInducementType -> {
                                    when (type) {
                                        CommonInducementType.BIASED_REFEREE -> INVALID_GAME_STATE("Use `InducementSelection.BiasedReferee` instead")
                                        CommonInducementType.BRIBE -> {
                                            repeat(inducement.count) {
                                                add(AddBribe(team, Bribe(duration = Duration.END_OF_GAME)))
                                            }
                                        }
                                        CommonInducementType.DESPERATE_MEASURES -> {
                                            updatedContext = updatedContext.copy(rollForDesperateMeasures = inducement.count)
                                        }
                                        CommonInducementType.EXTRA_TEAM_TRAINING -> {
                                            repeat(inducement.count) { i ->
                                                add(AddTeamReroll(team, ExtraTeamTrainingReroll(teamId = team.id, index = i)))
                                            }
                                        }
                                        CommonInducementType.HALFLING_MASTER_CHEF -> add(SetHalflingMasterChefs(team, inducement.count))
                                        CommonInducementType.INFAMOUS_COACHING_STAFF -> INVALID_GAME_STATE("Use `InducementSelection.InfamousCoach` instead")
                                        CommonInducementType.MORTUARY_ASSISTANT -> {
                                            repeat(inducement.count) {
                                                add(AddMortuaryAssistant(team))
                                            }
                                        }
                                        CommonInducementType.PART_TIME_ASSISTANT_COACH -> add(SetPartTimeAssistantCoaches(team, inducement.count))
                                        CommonInducementType.PLAGUE_DOCTOR -> {
                                            repeat(inducement.count) {
                                                add(AddPlagueDoctor(team))
                                            }
                                        }
                                        CommonInducementType.RIOTOUS_ROOKIE ->  { /* Not supported yet */ }
                                        CommonInducementType.STANDARD_MERCENARY_PLAYERS -> INVALID_GAME_STATE("Use `InducementSelection.Mercenary` instead")
                                        CommonInducementType.STAR_PLAYERS -> INVALID_GAME_STATE("Use `InducementSelection.StarPlayer` instead")
                                        CommonInducementType.TEMP_AGENCY_CHEERLEADER -> add(SetTempAgencyCheerleaders(team, inducement.count))
                                        CommonInducementType.WANDERING_APOTHECARY -> {
                                            repeat(inducement.count) {
                                                add(AddWanderingApothecary(team))
                                            }
                                        }
                                        CommonInducementType.WEATHER_MAGE -> {
                                            repeat(inducement.count) {
                                                add(AddWeatherMage(team))
                                            }
                                        }
                                        CommonInducementType.WIZARD -> INVALID_GAME_STATE("Use `InducementSelection.Wizard` instead")
                                    }
                                }
                                is BB2025InducementType -> {
                                    when (type) {
                                        BB2025InducementType.BLITZERS_BEST_KEGS -> add(SetBlitzersBestKegs(team, inducement.count))
                                        BB2025InducementType.PRAYERS_TO_NUFFLE -> {
                                            updatedContext = updatedContext.copy(rollForPrayers = inducement.count)
                                        }
                                        BB2025InducementType.TEAM_MASCOT -> {
                                            repeat(inducement.count) {
                                                add(AddTeamMascot(team))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        is CommonInducementSelection.StarPlayer ->  { /* Not supported yet */ }
                        is CommonInducementSelection.Wizard ->  { /* Not supported yet */ }
                    }
                }
                if (updatedContext != context) {
                    addAll(
                        UpdateContext(updatedContext),
                        GotoNode(RollForTableInducements)
                    )
                } else {
                    add(ExitProcedure())
                }
            }
        }
    }

    object RollForTableInducements: ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            val context = state.getContext<ApplyInducementsContext>()
            return when {
                context.rollForPrayers > 0 -> GotoNode(ApplyPrayersToNuffle)
                context.rollForDesperateMeasures > 0 -> GotoNode(ApplyDesperateMeasures)
                else -> ExitProcedure()
            }
        }
    }

    object ApplyPrayersToNuffle: ParentNode()  {
        override fun onEnterNode(state: Game, rules: Rules): Command? {
            val context = state.getContext<ApplyInducementsContext>()
            val prayersContext = PrayersToNuffleRollContext(
                team = context.team,
                rollsRemaining = context.rollForPrayers,
            )
            return AddContext(prayersContext)
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = PrayersToNuffleRoll
        override fun onExitNode(state: Game, rules: Rules): Command {
            val context = state.getContext<ApplyInducementsContext>()
            return compositeCommandOf(
                RemoveContext<PrayersToNuffleRollContext>(),
                when (context.rollForDesperateMeasures > 0) {
                    true -> GotoNode(ApplyDesperateMeasures)
                    false -> ExitProcedure()
                }
            )
        }
    }

    object ApplyDesperateMeasures: ParentNode()  {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = DummyProcedure
        override fun onExitNode(state: Game, rules: Rules): Command = ExitProcedure()
    }
}
