package com.jervisffb.engine.common.procedures.inducements.spells

import com.jervisffb.engine.model.PlayerKeyword
import com.jervisffb.engine.model.PlayerSize
import com.jervisffb.engine.model.PositionId
import com.jervisffb.engine.rules.common.roster.RosterPosition
import com.jervisffb.engine.rules.common.skills.SkillType.DODGE
import com.jervisffb.engine.rules.common.skills.SkillType.LEAP
import com.jervisffb.engine.rules.common.skills.SkillType.NO_BALL
import com.jervisffb.engine.rules.common.skills.SkillType.STUNTY
import com.jervisffb.engine.rules.common.skills.SkillType.TITCHY
import com.jervisffb.engine.rules.common.skills.SkillType.VERY_LONG_LEGS
import com.jervisffb.engine.sprites.SpriteSheet
import com.jervisffb.engine.sprites.SpriteSource

/**
 * The position a player takes while they are turned into a Frog by the "Zap!"
 * spell. The player keeps their own [portrait], so it is still possible to see
 * who the frog used to be.
 *
 * Turning a player into a frog is just a matter of putting them into this
 * position for the rest of the drive, see
 * [com.jervisffb.engine.commands.TransformPlayer].
 */
val FROG_POSITION: (portrait: SpriteSource?) -> RosterPosition = { portrait ->
    RosterPosition(
        id = PositionId("spell-frog"),
        quantity = 1,
        title = "Frog",
        titleSingular = "Frog",
        shortHand = "F",
        cost = 0,
        move = 5,
        strength = 1,
        agility = 2,
        passing = null,
        armorValue = 5,
        skills = listOf(DODGE.id(), LEAP.id(), NO_BALL.id(), STUNTY.id(), TITCHY.id(), VERY_LONG_LEGS.id()),
        primary = emptyList(),
        secondary = emptyList(),
        specialRules = emptyList(),
        keywords = listOf(PlayerKeyword.FROG, PlayerKeyword.SPECIAL),
        size = PlayerSize.STANDARD,
        icon = SpriteSheet.embedded("fumbbl/players/zapped_player.png", 1),
        portrait = portrait
    )
}
