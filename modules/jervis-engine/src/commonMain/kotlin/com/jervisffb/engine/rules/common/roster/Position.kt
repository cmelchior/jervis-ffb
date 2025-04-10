package com.jervisffb.engine.rules.common.roster

import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.PlayerId
import com.jervisffb.engine.model.PlayerNo
import com.jervisffb.engine.model.PlayerType
import com.jervisffb.engine.model.PositionId
import com.jervisffb.engine.model.SkillId
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.bb2020.roster.RegionalSpecialRule
import com.jervisffb.engine.rules.bb2020.skills.SkillCategory
import com.jervisffb.engine.serialize.PlayerUiData
import com.jervisffb.engine.serialize.SpriteSource

/**
 * Interface describing a position on a team. This includes all types
 * of players.
 *
 * For now, this interface is tailored towards BB2020 rulesets, but e.g.
 * BB2016 didn't have parsing. So if we want the rules engine to support
 * multiple rulesets we probably have to rethink this strategy.
 */
interface Position {
    val id: PositionId
    val quantity: Int
    val title: String
    val titleSingular: String
    val shortHand: String
    val cost: Int
    val move: Int
    val strength: Int
    val agility: Int
    val passing: Int?
    val armorValue: Int
    val primary: List<SkillCategory>
    val secondary: List<SkillCategory>
    val skills: List<SkillId>
    // If set, this position can only play for teams with the given regional special rule
    val playsFor: List<RegionalSpecialRule>
    val icon: SpriteSource?
    val portrait: SpriteSource?

    fun createPlayer(
        rules: Rules,
        team: Team,
        id: PlayerId,
        name: String,
        number: PlayerNo,
        type: PlayerType,
        icon: PlayerUiData?
    ): Player
}
