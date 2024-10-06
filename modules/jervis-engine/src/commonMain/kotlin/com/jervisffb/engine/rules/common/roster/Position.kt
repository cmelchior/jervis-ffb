package com.jervisffb.engine.rules.common.roster

import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.PlayerId
import com.jervisffb.engine.model.PlayerNo
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.bb2020.skills.SkillFactory


interface Position {
    val roster: Roster
    val quantity: Int
    val position: String
    val positionSingular: String
    val cost: Int

    val move: Int
    val strenght: Int
    val agility: Int
    val armorValue: Int

    val skills: List<SkillFactory>

    fun createPlayer(
        rules: Rules,
        team: Team,
        id: PlayerId,
        name: String,
        number: PlayerNo,
    ): Player
}
