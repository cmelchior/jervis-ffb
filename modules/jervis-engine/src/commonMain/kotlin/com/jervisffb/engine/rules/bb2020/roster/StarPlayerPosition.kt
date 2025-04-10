package com.jervisffb.engine.rules.bb2020.roster

import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.PlayerId
import com.jervisffb.engine.model.PlayerNo
import com.jervisffb.engine.model.PlayerType
import com.jervisffb.engine.model.PositionId
import com.jervisffb.engine.model.SkillId
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.bb2020.skills.SkillCategory
import com.jervisffb.engine.rules.common.roster.Position
import com.jervisffb.engine.serialize.PlayerUiData
import com.jervisffb.engine.serialize.SpriteSource
import kotlinx.serialization.Serializable

/**
 * We model star players as Positions, mostly to keep them similar to other players.
 * This also allows us to more easily support multiple star players of the same type
 * as well as being able to track changes to them, e.g. through Special Play cards
 * or other things that add temporary affects to players
 */
@Serializable
class StarPlayerPosition(
    override val id: PositionId,
    override val title: String,
    override val shortHand: String,
    override val cost: Int,
    override val move: Int,
    override val strength: Int,
    override val agility: Int,
    override val passing: Int?,
    override var armorValue: Int,
    override val skills: List<SkillId>,
    override val playsFor: List<RegionalSpecialRule>,
    override val icon: SpriteSource?,
    override val portrait: SpriteSource?,
): Position {
    override val quantity: Int = 1
    override val titleSingular: String = title
    override val primary: List<SkillCategory> = emptyList()
    override val secondary: List<SkillCategory> = emptyList()

    override fun createPlayer(
        rules: Rules,
        team: Team,
        id: PlayerId,
        name: String,
        number: PlayerNo,
        type: PlayerType,
        icon: PlayerUiData?
    ): Player {
        TODO("Not yet implemented")
    }
}
