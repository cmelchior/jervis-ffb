package com.jervisffb.resources.bb2025

import com.jervisffb.engine.model.PlayerKeyword
import com.jervisffb.engine.model.PlayerSize
import com.jervisffb.engine.model.PositionId
import com.jervisffb.engine.model.RosterId
import com.jervisffb.engine.rules.common.roster.RegionalSpecialRule
import com.jervisffb.engine.rules.common.roster.RegionalSpecialRule.SYLVANIAN_SPOTLIGHT
import com.jervisffb.engine.rules.common.roster.Roster
import com.jervisffb.engine.rules.common.roster.RosterPosition
import com.jervisffb.engine.rules.common.roster.TeamSpecialRule
import com.jervisffb.engine.rules.common.roster.TeamSpecialRule.MASTERS_OF_UNDEATH
import com.jervisffb.engine.rules.common.skills.SkillCategory.AGILITY
import com.jervisffb.engine.rules.common.skills.SkillCategory.GENERAL
import com.jervisffb.engine.rules.common.skills.SkillCategory.PASSING
import com.jervisffb.engine.rules.common.skills.SkillCategory.STRENGTH
import com.jervisffb.engine.rules.common.skills.SkillType.BLOOD_LUST
import com.jervisffb.engine.rules.common.skills.SkillType.CLAWS
import com.jervisffb.engine.rules.common.skills.SkillType.FRENZY
import com.jervisffb.engine.rules.common.skills.SkillType.HYPNOTIC_GAZE
import com.jervisffb.engine.rules.common.skills.SkillType.JUGGERNAUT
import com.jervisffb.engine.rules.common.skills.SkillType.LONER
import com.jervisffb.engine.rules.common.skills.SkillType.PASS
import com.jervisffb.engine.rules.common.skills.SkillType.REGENERATION
import com.jervisffb.engine.sprites.RosterLogo
import com.jervisffb.engine.sprites.SingleSprite
import com.jervisffb.engine.sprites.SpriteSheet
import com.jervisffb.resources.iconRootPath
import com.jervisffb.resources.portraitRootPath
import kotlinx.serialization.Serializable

val VAMPIRE_THRALL_LINEMAN = RosterPosition(
    PositionId("vampire-thrall-lineman"),
    16,
    "Thrall Linemans",
    "Thrall Lineman",
    "L",
    40000,
    6, 3, 3, 4, 8,
    listOf(),
    listOf(GENERAL),
    listOf(AGILITY, STRENGTH),
    emptyList(),
    listOf(PlayerKeyword.THRALL, PlayerKeyword.LINEMAN),
    PlayerSize.STANDARD,
    SpriteSheet.ini("${iconRootPath}/vampire_thrall.png"),
    SingleSprite.ini("${portraitRootPath}/vampire_thrall.png")
)

val VAMPIRE_VAMPIRE_RUNNER = RosterPosition(
    PositionId("vampire-vampire-runner"),
    2,
    "Vampire Runners",
    "Vampire Runner",
    "vR",
    100000,
    8, 3, 2, 3, 8,
    listOf(HYPNOTIC_GAZE.id(), REGENERATION.id(), BLOOD_LUST.idTarget(2)),
    listOf(GENERAL, AGILITY),
    listOf(PASSING, STRENGTH),
    emptyList(),
    listOf(PlayerKeyword.VAMPIRE, PlayerKeyword.RUNNER),
    PlayerSize.STANDARD,
    SpriteSheet.ini("${iconRootPath}/vampire_vampirerunner.png"),
    SingleSprite.ini("${portraitRootPath}/vampire_vampirerunner.png")
)

val VAMPIRE_VAMPIRE_THROWER = RosterPosition(
    PositionId("vampire-vampire-thrower"),
    2,
    "Vampire Throwers",
    "Vampire Thrower",
    "vT",
    110000,
    6, 4, 2, 2, 9,
    listOf(PASS.id(), HYPNOTIC_GAZE.id(), REGENERATION.id(), BLOOD_LUST.idTarget(2)),
    listOf(GENERAL, AGILITY, PASSING),
    listOf(STRENGTH),
    emptyList(),
    listOf(PlayerKeyword.VAMPIRE, PlayerKeyword.THROWER),
    PlayerSize.STANDARD,
    SpriteSheet.ini("${iconRootPath}/vampire_vampirethrower.png"),
    SingleSprite.ini("${portraitRootPath}/vampire_vampirethrower.png")
)

val VAMPIRE_VAMPIRE_BLITZER = RosterPosition(
    PositionId("vampire-vampire-blitzer"),
    2,
    "Vampire Blitzers",
    "Vampire Blitzer",
    "vZ",
    110000,
    6, 4, 2, 4, 9,
    listOf(JUGGERNAUT.id(), HYPNOTIC_GAZE.id(), REGENERATION.id(), BLOOD_LUST.idTarget(3)),
    listOf(GENERAL, AGILITY, STRENGTH),
    listOf(),
    emptyList(),
    listOf(PlayerKeyword.VAMPIRE, PlayerKeyword.BLITZER),
    PlayerSize.STANDARD,
    SpriteSheet.ini("${iconRootPath}/vampire_vampireblitzer.png"),
    SingleSprite.ini("${portraitRootPath}/vampire_vampireblitzer.png")
)

val VAMPIRE_VARGHEIST = RosterPosition(
    PositionId("vampire-vargheist"),
    1,
    "Vargheists",
    "Vargheist",
    "VG",
    150000,
    5, 5, 4, 6, 10,
    listOf(FRENZY.id(), CLAWS.id(), LONER.idTarget(4), REGENERATION.id(), BLOOD_LUST.idTarget(3)),
    listOf(STRENGTH),
    listOf(AGILITY, GENERAL),
    emptyList(),
    listOf(PlayerKeyword.VAMPIRE, PlayerKeyword.BIG_GUY),
    PlayerSize.BIG_GUY,
    SpriteSheet.ini("${iconRootPath}/vampire_vargheist.png"),
    SingleSprite.ini("${portraitRootPath}/vampire_vargheist.png")
)

@Serializable
val VAMPIRE_TEAM_BB2025 = Roster(
    id = RosterId("jervis-vampire"),
    name = "Vampire",
    tier = 2,
    numberOfRerolls = 8,
    rerollCost = 60000,
    allowApothecary = true,
    positions = listOf(VAMPIRE_THRALL_LINEMAN, VAMPIRE_VAMPIRE_RUNNER, VAMPIRE_VAMPIRE_THROWER, VAMPIRE_VAMPIRE_BLITZER, VAMPIRE_VARGHEIST),
    leagues = listOf(RegionalSpecialRule.SYLVANIAN_SPOTLIGHT),
    specialRules = listOf(TeamSpecialRule.MASTERS_OF_UNDEATH),
    logo = RosterLogo(
        large = SingleSprite.fumbbl("486355"),
        small = SingleSprite.fumbbl("486355")
    )
)
