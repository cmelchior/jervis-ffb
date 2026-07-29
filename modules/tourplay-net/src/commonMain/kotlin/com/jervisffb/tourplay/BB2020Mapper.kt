package com.jervisffb.tourplay

import com.jervisffb.engine.ext.playerNo
import com.jervisffb.engine.model.PlayerId
import com.jervisffb.engine.model.PlayerLevel
import com.jervisffb.engine.model.PlayerSize
import com.jervisffb.engine.model.PlayerType
import com.jervisffb.engine.model.PositionId
import com.jervisffb.engine.model.RosterId
import com.jervisffb.engine.model.TeamId
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.builder.GameVersion
import com.jervisffb.engine.rules.common.roster.Roster
import com.jervisffb.engine.rules.common.roster.RosterPosition
import com.jervisffb.engine.serialization.PlayerUiData
import com.jervisffb.engine.serialization.SerializedPlayer
import com.jervisffb.engine.serialization.SerializedTeam
import com.jervisffb.engine.sprites.SingleSprite
import com.jervisffb.engine.sprites.SpriteSheet
import com.jervisffb.tourplay.api.TourPlayRoster
import com.jervisffb.tourplay.api.positionShortHand

/**
 * Class responsible for mapping BB2020 TourPlay teams to Jervis.
 */
class BB2020Mapper: JervisMapper() {

    override fun convertToJervisRoster(rules: Rules, roster: TourPlayRoster): Roster {
        val positions = roster.rosterMaster.lineUpMasters.map { position ->
            // TODO How to map from TourPlay to FUMBBL Icons / Portraits?
            // As a temporary solution. We need some place holders
            val iconRef = SpriteSheet.generated(position.positionShortHand)
            val portraitRef = SingleSprite.embedded("jervis/portraits/default_portrait.png")
            RosterPosition(
                id = PositionId(position.id.toString()),
                quantity = position.quantity,
                title = position.position, // API doesn't return the "group" title, only the singular title
                titleSingular = position.position,
                shortHand = position.position.first().toString(), // API doesn't have a short hand, just just the first letter (for now)
                cost = position.cost,
                move = position.ma,
                strength = position.st,
                agility = position.ag,
                passing = position.pa,
                armorValue = position.av,
                skills = position.skills.mapNotNull {
                    // TODO It looks like attribute increases are tracked here as well
                    //  Take a closer look at skillAttributeMaster
                    convertTourPlaySkillToSkillId(rules, it.skillMaster.name)
                },
                primary = mapToSkillCategory(position.skillNormal),
                secondary = mapToSkillCategory(position.skillDouble),
                emptyList(),
                keywords = emptyList(),
                size = when {
                    position.isBigGuy == true -> PlayerSize.BIG_GUY
                    position.position == "Giant" -> PlayerSize.GIANT // TODO Unclear if this is correct
                    else -> PlayerSize.STANDARD
                },
                icon = iconRef,
                portrait = portraitRef,
            )
        }
        val specialRules = convertRosterSpecialRules(roster.rosterMaster.teamSpecialRules)
        val logo = extractRosterLogo(roster)
        return Roster(
            id = RosterId(roster.rosterMaster.id.toString()),
            name = roster.rosterMaster.name,
            tier = roster.rosterMaster.tier,
            numberOfRerolls = 8, // Is there a limit?
            rerollCost = roster.rosterMaster.prizeReRoll,
            allowApothecary = roster.rosterMaster.apothecary,
            leagues = emptyList(),
            specialRules = specialRules,
            positions = positions,
            logo = logo,
        )
    }

    override fun convertToJervisTeam(rules: Rules, jervisRoster: Roster, team: TourPlayRoster): SerializedTeam {
        // This only supports basic conversion.
        // Some things are unclear for FUMBBL Teams:
        // - How are player types like MERCENARY and JOURNEYMEN defined?
        // - How are stat increases defined?
        // - How are injuries defined? Especially niggling and miss next game?
        return SerializedTeam(
            id = TeamId(team.id.toString()),
            name = team.teamName,
            // TourPlay doesn't support BB2025 yet, so just hardcode the version to BB2020
            version = GameVersion.BB2020,
            // Right now we just assume that the FUMBBL team matches the given game type
            // We probably need to refine this later.
            type = rules.gameType,
            players = team.lineUps.map { player ->
                val position = jervisRoster.positions.first { it.id.value == player.lineUpMaster.id.toString() }
                SerializedPlayer(
                    id = PlayerId(player.id.toString()),
                    name = player.name,
                    number = player.number.playerNo,
                    position = position.id,
                    type = PlayerType.STANDARD, // Unclear if this is always true?
                    statModifiers = emptyList(), // How are these defined?
                    extraSkills = player.skills.mapNotNull {
                        convertTourPlaySkillToSkillId(rules, it.skillMaster.name)
                    }.map { it.serialize() },
                    nigglingInjuries = 0, // Unclear how these are defined
                    missNextGame = false, // Unclear how these are defined
                    starPlayerPoints = player.starPlayerPoints,
                    level = PlayerLevel.ROOKIE, // Unclear how this is defined
                    cost = position.cost + player.cost,
                    icon = PlayerUiData(
                        sprite = position.icon,
                        portrait = position.portrait,
                    )
                )
            },
            roster = jervisRoster,
            rerolls = team.reRolls,
            apothecaries = if (team.apothecary) 1 else 0,
            cheerleaders = team.cheerLeaders,
            assistantCoaches = team.assistantCoaches,
            treasury = team.treasury,
            fanFactor = team.fanFactor,
            teamValue = team.teamValue * 1000, // TourPlay tracks Team Value as its "short hand" value
            currentTeamValue = team.teamValue * 1000, // Unclear if this is current or something else?
            specialRules = jervisRoster.specialRules,
            teamLogo = jervisRoster.logo,
        )
    }
}
