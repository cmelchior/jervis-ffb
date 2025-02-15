package com.jervisffb.engine.rules.common.roster

import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.PlayerId
import com.jervisffb.engine.model.PlayerNo
import com.jervisffb.engine.model.PositionId
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.bb2020.skills.SkillFactory
import com.jervisffb.engine.serialize.PlayerUiData
import com.jervisffb.engine.serialize.SpriteSource

interface Position {
    val id: PositionId
    val quantity: Int
    val title: String
    val titleSingular: String
    val shortHand: String
    val cost: Int
    val move: Int
    val strenght: Int
    val agility: Int
    val armorValue: Int

    val skills: List<SkillFactory>
    val icon: SpriteSource?
    val portrait: SpriteSource?

    fun createPlayer(
        rules: Rules,
        team: Team,
        id: PlayerId,
        name: String,
        number: PlayerNo,
        icon: PlayerUiData?
    ): Player
}
