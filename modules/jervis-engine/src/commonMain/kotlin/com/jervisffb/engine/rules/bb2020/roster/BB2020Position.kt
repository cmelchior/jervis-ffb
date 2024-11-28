package com.jervisffb.engine.rules.bb2020.roster

import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.PlayerId
import com.jervisffb.engine.model.PlayerNo
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.bb2020.BB2020SkillCategory
import com.jervisffb.engine.rules.bb2020.skills.SkillFactory
import com.jervisffb.engine.rules.common.roster.Position
import com.jervisffb.engine.rules.common.roster.PositionId
import kotlinx.serialization.Serializable

@Serializable
data class BB2020Position(
    override val id: PositionId,
    override val quantity: Int,
    override val position: String,
    override val positionSingular: String,
    override val shortHand: String,
    override val cost: Int,
    override val move: Int,
    override val strenght: Int,
    override val agility: Int,
    val passing: Int?,
    override var armorValue: Int,
    override val skills: List<SkillFactory>,
    val primary: List<BB2020SkillCategory>,
    val secondary: List<BB2020SkillCategory>,
) : Position {
    override fun createPlayer(
        rules: Rules,
        team: Team,
        id: PlayerId,
        name: String,
        number: PlayerNo,
    ): Player {
        return Player(
            id,
            this,
            rules.moveRange,
            rules.strengthRange,
            rules.agilityRange,
            rules.passingRange,
            rules.armorValueRange
        ).apply {
            this.team = team
            this.name = name
            this.number = number
            baseMove = position.move
            baseStrenght = position.strenght
            baseAgility = position.agility
            basePassing = this@BB2020Position.passing
            baseArmorValue = position.armorValue
            positionSkills = position.skills.map { it.createSkill() }.toMutableList()
        }
    }

    override fun toString(): String {
        return "BB2020Position(position='$position')"
    }
}
