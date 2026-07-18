package com.jervisffb.resources.bb2025

import com.jervisffb.engine.model.PlayerKeyword
import com.jervisffb.engine.model.PlayerSize
import com.jervisffb.engine.model.PositionId
import com.jervisffb.engine.model.RosterId
import com.jervisffb.engine.rules.common.roster.RegionalSpecialRule
import com.jervisffb.engine.rules.common.roster.Roster
import com.jervisffb.engine.rules.common.roster.RosterPosition
import com.jervisffb.engine.rules.common.roster.TeamSpecialRule
import com.jervisffb.engine.rules.common.skills.SkillCategory.AGILITY
import com.jervisffb.engine.rules.common.skills.SkillCategory.DEVIOUS
import com.jervisffb.engine.rules.common.skills.SkillCategory.GENERAL
import com.jervisffb.engine.rules.common.skills.SkillCategory.MUTATIONS
import com.jervisffb.engine.rules.common.skills.SkillCategory.STRENGTH
import com.jervisffb.engine.rules.common.skills.SkillType.BLOCK
import com.jervisffb.engine.rules.common.skills.SkillType.BRAWLER
import com.jervisffb.engine.rules.common.skills.SkillType.BREATHE_FIRE
import com.jervisffb.engine.rules.common.skills.SkillType.DISTURBING_PRESENCE
import com.jervisffb.engine.rules.common.skills.SkillType.FRENZY
import com.jervisffb.engine.rules.common.skills.SkillType.HORNS
import com.jervisffb.engine.rules.common.skills.SkillType.IRON_HARD_SKIN
import com.jervisffb.engine.rules.common.skills.SkillType.LONER
import com.jervisffb.engine.rules.common.skills.SkillType.MIGHTY_BLOW
import com.jervisffb.engine.rules.common.skills.SkillType.SHADOWING
import com.jervisffb.engine.rules.common.skills.SkillType.SPRINT
import com.jervisffb.engine.rules.common.skills.SkillType.STAB
import com.jervisffb.engine.rules.common.skills.SkillType.SURE_FEET
import com.jervisffb.engine.rules.common.skills.SkillType.THICK_SKULL
import com.jervisffb.engine.rules.common.skills.SkillType.UNCHANNELLED_FURY
import com.jervisffb.engine.rules.common.skills.SkillType.UNSTEADY
import com.jervisffb.engine.serialize.RosterLogo
import com.jervisffb.engine.serialize.SingleSprite
import com.jervisffb.engine.serialize.SpriteSheet
import com.jervisffb.resources.iconRootPath
import com.jervisffb.resources.portraitRootPath
import kotlinx.serialization.Serializable

val HOBGOBLIN_LINEMEN =
    RosterPosition(
        PositionId("chaos-dwarf-hobgoblin-lineman"),
        16,
        "Hobgoblin Linemen",
        "Hobgoblin Lineman",
        "Hg",
        40_000,
        6, 3, 3, 4, 8,
        emptyList(),
        listOf(DEVIOUS),
        listOf(GENERAL, AGILITY, STRENGTH),
        emptyList(),
        listOf(PlayerKeyword.GOBLIN, PlayerKeyword.LINEMAN),
        PlayerSize.STANDARD,
        SpriteSheet.ini("${iconRootPath}/chaosdwarf_hobgoblin.png", 10),
        SingleSprite.ini("${portraitRootPath}/chaosdwarf_hobgoblin.png")
    )

val SNEAKY_STABBAS =
    RosterPosition(
        PositionId("chaos-dwarf-sneaky-stabba"),
        2,
        "Sneaky Stabbas",
        "Sneaky Stabba",
        "S",
        60_000,
        6, 3, 3, 5, 8,
        listOf(SHADOWING.id(), STAB.id()),
        listOf(GENERAL, DEVIOUS),
        listOf(AGILITY, STRENGTH),
        emptyList(),
        listOf(PlayerKeyword.GOBLIN, PlayerKeyword.SPECIAL),
        PlayerSize.STANDARD,
        SpriteSheet.ini("${iconRootPath}/chaosdwarf_hobgoblinsneakystabba.png", 2),
        SingleSprite.ini("${portraitRootPath}/chaosdwarf_hobgoblinsneakystabba.png")
    )

val CHAOS_DWARF_BLOCKERS =
    RosterPosition(
        PositionId("chaos-dwarf-chaos-dwarf-blocker"),
        4,
        "Chaos Dwarf Blockers",
        "Chaos Dwarf Blocker",
        "Cd",
        70_000,
        4, 3, 4, 6, 10,
        listOf(BLOCK.id(), IRON_HARD_SKIN.id(), THICK_SKULL.id()),
        listOf(GENERAL, STRENGTH),
        listOf(AGILITY, DEVIOUS, MUTATIONS),
        emptyList(),
        listOf(PlayerKeyword.DWARF, PlayerKeyword.BLOCKER),
        PlayerSize.STANDARD,
        SpriteSheet.ini("${iconRootPath}/chaosdwarf_chaosdwarfblocker.png", 6),
        SingleSprite.ini("${portraitRootPath}/chaosdwarf_chaosdwarfblocker.png")
    )

val FLAMESMITHS =
    RosterPosition(
        PositionId("chaos-dwarf-flamesmith"),
        2,
        "Flamesmiths",
        "Flamesmith",
        "F",
        80_000,
        5, 3, 4, 6, 10,
        listOf(
            DISTURBING_PRESENCE.id(),
            BRAWLER.id(),
            THICK_SKULL.id(),
            BREATHE_FIRE.id()
        ),
        listOf(GENERAL, STRENGTH),
        listOf(AGILITY, DEVIOUS, MUTATIONS),
        emptyList(),
        listOf(PlayerKeyword.DWARF, PlayerKeyword.SPECIAL),
        PlayerSize.STANDARD,
        SpriteSheet.ini("${iconRootPath}/chaosdwarf_chaosdwarfflamesmith.png", 2),
        SingleSprite.ini("${portraitRootPath}/chaosdwarf_chaosdwarfflamesmith.png")
    )

val BULL_CENTAUR_BLITZERS =
    RosterPosition(
        PositionId("chaos-dwarf-bull-centaur-blitzer"),
        2,
        "Bull Centaur Blitzers",
        "Bull Centaur Blitzer",
        "Bc",
        130_000,
        6, 4, 4, 6, 10,
        listOf(
            SPRINT.id(),
            SURE_FEET.id(),
            THICK_SKULL.id(),
            UNSTEADY.id()
        ),
        listOf(GENERAL, STRENGTH),
        listOf(AGILITY, DEVIOUS, MUTATIONS),
        emptyList(),
        listOf(PlayerKeyword.DWARF, PlayerKeyword.BLITZER),
        PlayerSize.STANDARD,
        SpriteSheet.ini("${iconRootPath}/chaosdwarf_bullcentaurblitzer.png", 2),
        SingleSprite.ini("${portraitRootPath}/chaosdwarf_bullcentaurblitzer.png")
    )

val ENSLAVED_MINOTAUR =
    RosterPosition(
        PositionId("chaos-dwarf-enslaved-minotaur"),
        1,
        "Enslaved Minotaur",
        "Enslaved Minotaur",
        "M",
        150_000,
        5, 5, 4, 6, 9,
        listOf(
            FRENZY.id(),
            HORNS.id(),
            MIGHTY_BLOW.id(),
            THICK_SKULL.id(),
            LONER.idTarget(4),
            UNCHANNELLED_FURY.id()
        ),
        listOf(STRENGTH, MUTATIONS),
        listOf(GENERAL, AGILITY),
        emptyList(),
        listOf(PlayerKeyword.MINOTAUR, PlayerKeyword.BIG_GUY),
        PlayerSize.BIG_GUY,
        SpriteSheet.ini("${iconRootPath}/chaosdwarf_enslavedminotaur.png", 1),
        SingleSprite.ini("${portraitRootPath}/chaosdwarf_enslavedminotaur.png")
    )

@Serializable
val CHAOS_DWARF_TEAM_BB2025 = Roster(
    id = RosterId("jervis-chaos-dwarf"),
    name = "Chaos Dwarf",
    tier = 1,
    numberOfRerolls = 8,
    rerollCost = 70_000,
    allowApothecary = true,
    positions = listOf(
        HOBGOBLIN_LINEMEN,
        SNEAKY_STABBAS,
        CHAOS_DWARF_BLOCKERS,
        FLAMESMITHS,
        BULL_CENTAUR_BLITZERS,
        ENSLAVED_MINOTAUR,
    ),
    leagues = listOf(RegionalSpecialRule.BADLANDS_BRAWL, RegionalSpecialRule.CHAOS_CLASH),
    specialRules = listOf(TeamSpecialRule.FAVOURED_OF_HASHUT),
    logo = RosterLogo.NONE
)
