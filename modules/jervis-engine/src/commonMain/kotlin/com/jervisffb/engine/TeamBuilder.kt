package com.jervisffb.engine

import com.jervisffb.engine.model.Coach
import com.jervisffb.engine.model.CoachId
import com.jervisffb.engine.model.PlayerId
import com.jervisffb.engine.model.PlayerNo
import com.jervisffb.engine.model.PlayerType
import com.jervisffb.engine.model.PositionId
import com.jervisffb.engine.model.SkillId
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.TeamId
import com.jervisffb.engine.model.inducements.Apothecary
import com.jervisffb.engine.model.inducements.ApothecaryType
import com.jervisffb.engine.model.modifiers.StatModifier
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.builder.GameType
import com.jervisffb.engine.rules.builder.GameVersion
import com.jervisffb.engine.rules.common.rerolls.RegularTeamReroll
import com.jervisffb.engine.rules.common.roster.PlayerSpecialRule
import com.jervisffb.engine.rules.common.roster.Position
import com.jervisffb.engine.rules.common.roster.Roster
import com.jervisffb.engine.rules.common.roster.SpecialRules
import com.jervisffb.engine.rules.common.skills.Duration
import com.jervisffb.engine.serialize.PlayerUiData
import com.jervisffb.engine.serialize.RosterLogo
import com.jervisffb.engine.serialize.SingleSprite
import com.jervisffb.engine.serialize.SpriteSheet
import kotlin.random.Random

private data class PlayerData(
    val id: PlayerId,
    val name: String,
    val number: PlayerNo,
    val type: Position,
    val extraSkills: List<SkillId> = emptyList(),
    val statModifiers: List<StatModifier>,
    val extraSpecialRules: List<PlayerSpecialRule> = emptyList(),
    val icon: PlayerUiData? = null,
)

class TeamBuilder(val rules: Rules, val roster: Roster) {
    private val players: MutableMap<PlayerNo, PlayerData> = mutableMapOf()
    var id: TeamId = TeamId("team-${Random.nextLong()}")
    var coach: Coach = Coach(CoachId("jervis-coach"), "Jervis")
    var name: String = ""
    var version: GameVersion = rules.baseVersion
    var type: GameType = GameType.STANDARD
    var rerolls: Int = 0
        set(value) {
            if (roster.numberOfRerolls < value || value < 0) {
                throw IllegalArgumentException("This team only allows ${roster.numberOfRerolls}, not $value")
            }
            field = value
        }
    var cheerleaders: Int = 0
    var assistentCoaches: Int = 0
    var fanFactor: Int = 0
    var teamValue: Int = 0
    var currentTeamValue: Int = 0
    var treasury: Int = 0
    var dedicatedFans: Int = 0
    // For now, just use sensible defaults for the league
    var league = roster.leagues.singleOrNull() ?: roster.leagues.firstOrNull()
    val specialRules = mutableListOf<SpecialRules>()
    var teamLogo: RosterLogo? = null
    var apothecaries: Int = 0
        set(value) {
            if (!roster.allowApothecary && value > 0) {
                throw IllegalArgumentException("This team does not allow an apothecary")
            }
            field = value
        }

    // Track spriteIds so we can avoid re-using existing ones as much as possible
    private val usedSprites = mutableMapOf<PositionId, Int>()

    fun addPlayer(
        id: PlayerId,
        name: String,
        number: PlayerNo,
        type: Position,
        skills: List<SkillId> = emptyList(),
        statModifiers: List<StatModifier> = emptyList(),
        specialRules: List<PlayerSpecialRule> = emptyList()
    ) {
        if (players.containsKey(number)) {
            throw IllegalArgumentException("Player with number $number already exits: ${players[number]}")
        }
        val allowedOnTeam = type.quantity
        if (players.values.count { it.type == type } == allowedOnTeam) {
            throw IllegalArgumentException("Max number of $type are already on the team.")
        }
        val playerIcon = createDefaultUiData(type)
        players[number] = PlayerData(
            id = id,
            name = name,
            number = number,
            type = type,
            extraSkills = skills,
            statModifiers = statModifiers,
            extraSpecialRules = specialRules,
            icon = playerIcon
        )
    }

    private fun createDefaultUiData(type: Position): PlayerUiData {
        val playerSprite = when (val sprite = type.icon) {
            is SingleSprite -> sprite
            is SpriteSheet -> {
                // If a sprite sheet is known to have multiple variants, rotate between them
                // otherwise, just reuse the same one.
                if (sprite.variants != null) {
                    val index = usedSprites.getOrPut(type.id) { 0 }
                    usedSprites[type.id] = (index + 1) % sprite.variants
                    sprite.copy(
                        selectedIndex = index
                    )
                } else {
                    // If no variants, there is only one, so just return directly
                    sprite.copy(selectedIndex = 0)
                }
            }
            null -> null
        }
        return PlayerUiData(playerSprite,type.portrait)
    }

    fun build(): Team {
        return Team(id, name, version, type, roster, coach).apply {
            this@TeamBuilder.players.forEach {
                val data: PlayerData = it.value
                add(data.type.createPlayer(
                    rules,
                    this@apply,
                    data.id,
                    data.name,
                    data.number,
                    PlayerType.STANDARD,
                    data.icon,
                ).also { player ->
                    player.extraSkills.addAll(data.extraSkills.map { rules.createSkill(player, it, Duration.PERMANENT) })
                    player.extraSpecialRules.addAll(data.extraSpecialRules)
                    data.statModifiers.forEach {
                        when (it.type) {
                            StatModifier.Type.AV -> player.armourModifiers.add(it)
                            StatModifier.Type.MA -> player.moveModifiers.add(it)
                            StatModifier.Type.PA -> player.passingModifiers.add(it)
                            StatModifier.Type.AG -> player.agilityModifiers.add(it)
                            StatModifier.Type.ST -> player.strengthModifiers.add(it)
                        }
                    }
                })
            }
            this.league = this@TeamBuilder.league
            this.rerolls.addAll((0 ..<this@TeamBuilder.rerolls).map {
                RegularTeamReroll(id, it)
            })
            this.teamApothecaries.addAll((0 until this@TeamBuilder.apothecaries).map { Apothecary(false, ApothecaryType.STANDARD) })
            this.teamCheerleaders = this@TeamBuilder.cheerleaders
            this.teamAssistantCoaches = this@TeamBuilder.assistentCoaches
            this.dedicatedFans = this@TeamBuilder.dedicatedFans
            this.specialRules.addAll(this@TeamBuilder.specialRules)
            this.teamValue = this@TeamBuilder.teamValue
            this.currentTeamValue = this@TeamBuilder.currentTeamValue
            this.teamLogo = this@TeamBuilder.teamLogo
        }
    }
}

fun teamBuilder(
    rules: Rules,
    roster: Roster,
    action: TeamBuilder.() -> Unit,
): Team {
    val builder = TeamBuilder(rules, roster)
    action(builder)
    return builder.build()
}
