package com.jervisffb.engine.serialize

import com.jervisffb.engine.model.Coach
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.TeamId
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.bb2020.roster.BB2020Roster
import com.jervisffb.engine.rules.bb2020.roster.SpecialRules
import com.jervisffb.engine.rules.bb2020.skills.Duration
import com.jervisffb.engine.rules.builder.GameType
import com.jervisffb.engine.teamBuilder
import com.jervisffb.utils.jervisLogger
import kotlinx.serialization.Serializable

/**
 * Variant of [com.jervisffb.engine.model.Team] that is optimized for being
 * saved to disk. We use a separate class to make the conversion more explicit.
 * It also allows more flexibility with regard to how we load teams from
 * a saved state.
 *
 * This class represents the team state before starting a game.
 */
@Serializable
class SerializedTeam(
    val id: TeamId,
    val name: String,
    val type: GameType,
    val players: List<SerializedPlayer>,
    val roster: BB2020Roster,
    val rerolls: Int,
    val apothecaries: Int,
    val cheerleaders: Int,
    val assistantCoaches: Int,
    val treasury: Int,
    val fanFactor: Int,
    val teamValue: Int,
    val currentTeamValue: Int,
    val specialRules: List<SpecialRules>,
    val teamLogo: RosterLogo?
) {
    companion object {
        val LOG = jervisLogger()

        fun serialize(team: Team): SerializedTeam {
            return SerializedTeam(
                team.id,
                team.name,
                team.type,
                team.map { SerializedPlayer.serialize(it) },
                team.roster,
                team.rerolls.count { it.duration == Duration.PERMANENT },
                team.teamApothecaries.size,
                team.teamCheerleaders,
                team.teamAssistantCoaches,
                team.treasury,
                team.fanFactor,
                team.teamValue,
                team.currentTeamValue,
                team.specialRules,
                team.teamLogo
            )
        }

        fun deserialize(rules: Rules, teamData: SerializedTeam, coach: Coach): Team {
            // Skills might change subtly between rules.
            // For that reason, we need to pass in the the rules, so we can use them to look up
            return teamBuilder(rules, teamData.roster) {
                id = teamData.id
                name = teamData.name
                type = teamData.type
                this.coach = coach

                teamData.players.forEach { playerData ->
                    addPlayer(
                        playerData.id,
                        playerData.name,
                        playerData.number,
                        teamData.roster[playerData.position],
                        playerData.extraSkills.mapNotNull { skillDescription ->
                            // TODO For now, we just ignore skills we do not support
                            rules.skillSettings.getSkillId(skillDescription).also { skillId ->
                                if (skillId == null) {
                                    LOG.d { "Could not find skill for: '$skillDescription'"}
                                }
                            }
                        },
                        playerData.statModifiers,
                    )
                }
                rerolls = teamData.rerolls
                apothecaries = teamData.apothecaries
                cheerleaders = teamData.cheerleaders
                assistentCoaches = teamData.assistantCoaches
                treasury = teamData.treasury
                fanFactor = teamData.fanFactor
                teamValue = teamData.teamValue
                currentTeamValue = teamData.currentTeamValue
                specialRules.addAll(teamData.specialRules)
                teamLogo = teamData.teamLogo
            }
        }
    }
}
