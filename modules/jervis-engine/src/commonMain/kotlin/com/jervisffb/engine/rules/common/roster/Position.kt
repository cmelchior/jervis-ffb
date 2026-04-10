package com.jervisffb.engine.rules.common.roster

import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.PlayerId
import com.jervisffb.engine.model.PlayerKeyword
import com.jervisffb.engine.model.PlayerNo
import com.jervisffb.engine.model.PlayerSize
import com.jervisffb.engine.model.PlayerType
import com.jervisffb.engine.model.PositionId
import com.jervisffb.engine.model.SkillId
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.common.skills.SkillCategory
import com.jervisffb.engine.serialize.PlayerUiData
import com.jervisffb.engine.serialize.SpriteSource

/**
 * Interface describing a position on a team. This includes all types
 * of players.
 *
 * Developer's Commentary:
 * For now, this interface is a superset of the BB2020 and BB2025 rulesets,
 * which means that some properties might not make sense for a given ruleset.
 * E.g., keywords are used in BB2025, but not BB2020.
 *
 * It is unclear if this strategy is the best, but for now it seems to work.
 * [Roster] is using a similar strategy.
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
    val specialRules: List<PlayerSpecialRule>
    val keywords: List<PlayerKeyword>
    // If set, this position can only play for teams with the given regional special rule
    val playsFor: List<RegionalSpecialRule>
    val size: PlayerSize
    val icon: SpriteSource?
    val portrait: SpriteSource?

    /**
     * Create a new player for this position.
     */
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
