package com.jervisffb.engine.bb2025.procedures.inducements

import com.jervisffb.engine.actions.InducementSelection
import com.jervisffb.engine.bb2025.commands.AddTeamMascot
import com.jervisffb.engine.bb2025.procedures.rerolls.ExtraTeamTrainingReroll
import com.jervisffb.engine.common.commands.AddBribe
import com.jervisffb.engine.common.commands.AddMortuaryAssistant
import com.jervisffb.engine.common.commands.AddPlagueDoctor
import com.jervisffb.engine.common.commands.AddTeamReroll
import com.jervisffb.engine.common.commands.AddWanderingApothecary
import com.jervisffb.engine.common.commands.AddWeatherMage
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.common.commands.SetBlitzersBestKegs
import com.jervisffb.engine.common.commands.SetHalflingMasterChefs
import com.jervisffb.engine.common.commands.SetPartTimeAssistantCoaches
import com.jervisffb.engine.common.commands.SetTempAgencyCheerleaders
import com.jervisffb.engine.commands.buildCompositeCommand
import com.jervisffb.engine.commands.context.UpdateContext
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.fsm.ComputationNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.context.assertContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.inducements.Bribe
import com.jervisffb.engine.model.inducements.settings.InducementType
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.common.procedures.inducements.ApplyInducementsContext
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
            var rollForPrayers = false
            return buildCompositeCommand {
                context.inducements.inducements.forEach { inducement ->
                    when (inducement) {
                        is InducementSelection.BiasedReferee -> { /* Not supported yet */ }
                        is InducementSelection.InfamousCoach -> { /* Not supported yet */ }
                        is InducementSelection.Mercenary -> { /* Not supported yet */ }
                        is InducementSelection.Simple -> {
                            when (inducement.type) {
                                InducementType.PRAYERS_TO_NUFFLE -> {
                                    add(UpdateContext(context.copy(rollForPrayers = inducement.count)))
                                    rollForPrayers = true
                                }
                                InducementType.PART_TIME_ASSISTANT_COACH -> add(SetPartTimeAssistantCoaches(team, inducement.count))
                                InducementType.TEMP_AGENCY_CHEERLEADER -> add(SetTempAgencyCheerleaders(team, inducement.count))
                                InducementType.TEAM_MASCOT -> {
                                    repeat(inducement.count) {
                                        add(AddTeamMascot(team))
                                    }
                                }
                                InducementType.WEATHER_MAGE -> {
                                    repeat(inducement.count) {
                                        add(AddWeatherMage(team))
                                    }
                                }
                                InducementType.BLITZERS_BEST_KEGS -> add(SetBlitzersBestKegs(team, inducement.count))
                                InducementType.BRIBE -> {
                                    repeat(inducement.count) {
                                        add(AddBribe(team, Bribe(duration = Duration.END_OF_GAME)))
                                    }
                                }
                                InducementType.EXTRA_TEAM_TRAINING -> {
                                    repeat(inducement.count) { i ->
                                        add(AddTeamReroll(team, ExtraTeamTrainingReroll(teamId = team.id, index = i)))
                                    }
                                }
                                InducementType.MORTUARY_ASSISTANT -> {
                                    repeat(inducement.count) {
                                        add(AddMortuaryAssistant(team))
                                    }
                                }
                                InducementType.PLAGUE_DOCTOR -> {
                                    repeat(inducement.count) {
                                        add(AddPlagueDoctor(team))
                                    }
                                }
                                InducementType.RIOTOUS_ROOKIE ->  { /* Not supported yet */ }
                                InducementType.WANDERING_APOTHECARY -> {
                                    repeat(inducement.count) {
                                        add(AddWanderingApothecary(team))
                                    }
                                }
                                InducementType.HALFLING_MASTER_CHEF -> add(SetHalflingMasterChefs(team, inducement.count))
                                InducementType.BIASED_REFEREE -> INVALID_GAME_STATE("Use `InducementSelection.BiasedReferee` instead")
                                InducementType.INFAMOUS_COACHING_STAFF -> INVALID_GAME_STATE("Use `InducementSelection.InfamousCoach` instead")
                                InducementType.STANDARD_MERCENARY_PLAYERS -> INVALID_GAME_STATE("Use `InducementSelection.Mercenary` instead")
                                InducementType.STAR_PLAYERS -> INVALID_GAME_STATE("Use `InducementSelection.StarPlayer` instead")
                                InducementType.WIZARD -> INVALID_GAME_STATE("Use `InducementSelection.Wizard` instead")
                                InducementType.BLOODWEISER_KEG -> add(SetBlitzersBestKegs(team, inducement.count)) // The name for this changed from BB2020 to BB2025
                                InducementType.SPECIAL_PLAY ->  { /* Not supported yet */ }
                                InducementType.WAAAGH_DRUMMER ->  { /* Not supported yet */ }
                                InducementType.CAVORTING_NURGLINGS ->  { /* Not supported yet */ }
                                InducementType.DWARFEN_RUNESMITH ->  { /* Not supported yet */ }
                                InducementType.HALFLING_HOTPOT ->  { /* Not supported yet */ }
                                InducementType.MASTER_OF_BALLISTICS ->  { /* Not supported yet */ }
                                InducementType.EXPANDED_MERCENARY_PLAYERS ->  { /* Not supported yet */ }
                                InducementType.GIANT ->  { /* Not supported yet */ }
                                InducementType.DESPERATE_MEASURES ->  { /* Not supported yet */ }
                                InducementType.BRETONNIAN_PASTRIES ->  { /* Not supported yet */ }
                                InducementType.BRETONNIAN_DAMSEL ->  { /* Not supported yet */ }
                                InducementType.CANOPIC_JAR ->  { /* Not supported yet */ }
                            }
                        }
                        is InducementSelection.StarPlayer ->  { /* Not supported yet */ }
                        is InducementSelection.Wizard ->  { /* Not supported yet */ }
                    }
                }
                if (rollForPrayers) {
                    add(GotoNode(ApplyPrayersToNuffle))
                } else {
                    add(ExitProcedure())
                }
            }
        }
    }

    object ApplyPrayersToNuffle: ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            return ExitProcedure()
        }
    }
}
