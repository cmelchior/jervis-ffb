package com.jervisffb.engine.rules.bb2020.roster

import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.PlayerId
import com.jervisffb.engine.model.PlayerNo
import com.jervisffb.engine.model.PlayerSize
import com.jervisffb.engine.model.PlayerType
import com.jervisffb.engine.model.PositionId
import com.jervisffb.engine.model.SkillId
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.common.roster.Position
import com.jervisffb.engine.rules.common.skills.SkillCategory
import com.jervisffb.engine.serialize.PlayerUiData
import com.jervisffb.engine.serialize.SpriteSource
import kotlinx.serialization.Serializable

@Serializable
data class RosterPosition(
    override val id: PositionId,
    override val quantity: Int,
    override val title: String,
    override val titleSingular: String,
    override val shortHand: String,
    // In total value, e.g. 100.000 (not 100 K)
    override val cost: Int,
    override val move: Int,
    override val strength: Int,
    override val agility: Int,
    override val passing: Int?,
    override var armorValue: Int,
    override val skills: List<SkillId>,
    override val primary: List<SkillCategory>,
    override val secondary: List<SkillCategory>,
    override val size: PlayerSize,
    override val icon: SpriteSource?,
    override val portrait: SpriteSource?,
) : Position {
    override val playsFor: List<RegionalSpecialRule> = emptyList()

    override fun createPlayer(
        rules: Rules,
        team: Team,
        id: PlayerId,
        name: String,
        number: PlayerNo,
        type: PlayerType,
        icon: PlayerUiData?
    ): Player {
        return Player(
            rules,
            id,
            this,
            icon,
            type
        ).apply {
            this.team = team
            this.name = name
            this.number = number
            baseMove = position.move
            move = position.move
            baseStrength = position.strength
            strength = position.strength
            baseAgility = position.agility
            agility = position.agility
            basePassing = this@RosterPosition.passing
            passing = this@RosterPosition.passing
            baseArmorValue = position.armorValue
            armorValue = position.armorValue
            positionSkills = position.skills.mapNotNull { skill ->
                // TODO For now we just ignore skills not supported
                if (rules.skillSettings.isSkillSupported(skill.type)) {
                    rules.createSkill(this, skill)
                } else {
                    null
                }
            }.toMutableList()
        }
    }

    override fun toString(): String {
        return "BB2020Position(position='$title')"
    }
}
