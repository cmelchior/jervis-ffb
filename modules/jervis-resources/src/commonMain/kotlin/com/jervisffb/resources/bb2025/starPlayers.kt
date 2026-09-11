package com.jervisffb.resources.bb2025

import com.jervisffb.engine.model.PlayerKeyword
import com.jervisffb.engine.model.PlayerSize
import com.jervisffb.engine.model.PositionId
import com.jervisffb.engine.rules.common.roster.RegionalSpecialRule
import com.jervisffb.engine.rules.common.roster.StarPlayerPosition
import com.jervisffb.engine.rules.common.roster.TeamSpecialRule
import com.jervisffb.engine.rules.common.skills.SkillType.ACCURATE
import com.jervisffb.engine.rules.common.skills.SkillType.ALL_YOU_CAN_EAT
import com.jervisffb.engine.rules.common.skills.SkillType.ANIMAL_SAVAGERY
import com.jervisffb.engine.rules.common.skills.SkillType.A_SNEAKY_PAIR
import com.jervisffb.engine.rules.common.skills.SkillType.BALEFUL_HEX
import com.jervisffb.engine.rules.common.skills.SkillType.BALL_AND_CHAIN
import com.jervisffb.engine.rules.common.skills.SkillType.BEER_BARREL_BASH
import com.jervisffb.engine.rules.common.skills.SkillType.BLACK_INK
import com.jervisffb.engine.rules.common.skills.SkillType.BLASTIN_SOLVES_EVERYTHING
import com.jervisffb.engine.rules.common.skills.SkillType.BLAST_IT
import com.jervisffb.engine.rules.common.skills.SkillType.BLIND_RAGE
import com.jervisffb.engine.rules.common.skills.SkillType.BLOCK
import com.jervisffb.engine.rules.common.skills.SkillType.BLOOD_LUST
import com.jervisffb.engine.rules.common.skills.SkillType.BOMBARDIER
import com.jervisffb.engine.rules.common.skills.SkillType.BONE_HEAD
import com.jervisffb.engine.rules.common.skills.SkillType.BOUNDING_LEAP
import com.jervisffb.engine.rules.common.skills.SkillType.BRAWLER
import com.jervisffb.engine.rules.common.skills.SkillType.BREAK_TACKLE
import com.jervisffb.engine.rules.common.skills.SkillType.BULLSEYE
import com.jervisffb.engine.rules.common.skills.SkillType.CANNONEER
import com.jervisffb.engine.rules.common.skills.SkillType.CATCH
import com.jervisffb.engine.rules.common.skills.SkillType.CATCH_OF_THE_DAY
import com.jervisffb.engine.rules.common.skills.SkillType.CHAINSAW
import com.jervisffb.engine.rules.common.skills.SkillType.CLAWS
import com.jervisffb.engine.rules.common.skills.SkillType.CONSUMMATE_PROFESSIONAL
import com.jervisffb.engine.rules.common.skills.SkillType.CRUSHING_BLOW
import com.jervisffb.engine.rules.common.skills.SkillType.DAUNTLESS
import com.jervisffb.engine.rules.common.skills.SkillType.DEFENSIVE
import com.jervisffb.engine.rules.common.skills.SkillType.DIRTY_PLAYER
import com.jervisffb.engine.rules.common.skills.SkillType.DISTURBING_PRESENCE
import com.jervisffb.engine.rules.common.skills.SkillType.DIVING_CATCH
import com.jervisffb.engine.rules.common.skills.SkillType.DODGE
import com.jervisffb.engine.rules.common.skills.SkillType.DRUNKARD
import com.jervisffb.engine.rules.common.skills.SkillType.DUMP_OFF
import com.jervisffb.engine.rules.common.skills.SkillType.DWARFEN_GRIT
import com.jervisffb.engine.rules.common.skills.SkillType.DWARVEN_SCOURGE
import com.jervisffb.engine.rules.common.skills.SkillType.EXCUSE_ME_ARE_YOU_A_ZOAT
import com.jervisffb.engine.rules.common.skills.SkillType.EXTRA_ARMS
import com.jervisffb.engine.rules.common.skills.SkillType.FEND
import com.jervisffb.engine.rules.common.skills.SkillType.FOUL_APPEARANCE
import com.jervisffb.engine.rules.common.skills.SkillType.FRENZIED_RUSH
import com.jervisffb.engine.rules.common.skills.SkillType.FRENZY
import com.jervisffb.engine.rules.common.skills.SkillType.FURIOUS_OUTBURST
import com.jervisffb.engine.rules.common.skills.SkillType.FURY_OF_THE_BLOOD_GOD
import com.jervisffb.engine.rules.common.skills.SkillType.GORED_BY_THE_BULL
import com.jervisffb.engine.rules.common.skills.SkillType.GRAB
import com.jervisffb.engine.rules.common.skills.SkillType.GUARD
import com.jervisffb.engine.rules.common.skills.SkillType.HAIL_MARY_PASS
import com.jervisffb.engine.rules.common.skills.SkillType.HALFLING_LUCK
import com.jervisffb.engine.rules.common.skills.SkillType.HATRED
import com.jervisffb.engine.rules.common.skills.SkillType.HORNS
import com.jervisffb.engine.rules.common.skills.SkillType.HYPNOTIC_GAZE
import com.jervisffb.engine.rules.common.skills.SkillType.ILL_BE_BACK
import com.jervisffb.engine.rules.common.skills.SkillType.ILL_CARRY_YOU
import com.jervisffb.engine.rules.common.skills.SkillType.INCORPOREAL
import com.jervisffb.engine.rules.common.skills.SkillType.INDOMITABLE
import com.jervisffb.engine.rules.common.skills.SkillType.JUGGERNAUT
import com.jervisffb.engine.rules.common.skills.SkillType.JUMP_UP
import com.jervisffb.engine.rules.common.skills.SkillType.KABOOM
import com.jervisffb.engine.rules.common.skills.SkillType.KICK_EM_WHILE_THEYRE_DOWN
import com.jervisffb.engine.rules.common.skills.SkillType.KICK_TEAMMATE
import com.jervisffb.engine.rules.common.skills.SkillType.KRUMP_AND_SMASH
import com.jervisffb.engine.rules.common.skills.SkillType.LEADER
import com.jervisffb.engine.rules.common.skills.SkillType.LEAP
import com.jervisffb.engine.rules.common.skills.SkillType.LETHAL_FLIGHT
import com.jervisffb.engine.rules.common.skills.SkillType.LONER
import com.jervisffb.engine.rules.common.skills.SkillType.LONE_FOULER
import com.jervisffb.engine.rules.common.skills.SkillType.LOOK_INTO_MY_EYES
import com.jervisffb.engine.rules.common.skills.SkillType.LORD_OF_CHAOS
import com.jervisffb.engine.rules.common.skills.SkillType.MASTER_ASSASSIN
import com.jervisffb.engine.rules.common.skills.SkillType.MAXIMUM_CARNAGE
import com.jervisffb.engine.rules.common.skills.SkillType.MESMERISING_DANCE
import com.jervisffb.engine.rules.common.skills.SkillType.MIGHTY_BLOW
import com.jervisffb.engine.rules.common.skills.SkillType.MONSTROUS_MOUTH
import com.jervisffb.engine.rules.common.skills.SkillType.MULTIPLE_BLOCK
import com.jervisffb.engine.rules.common.skills.SkillType.NERVES_OF_STEEL
import com.jervisffb.engine.rules.common.skills.SkillType.NO_BALL
import com.jervisffb.engine.rules.common.skills.SkillType.OLD_PRO
import com.jervisffb.engine.rules.common.skills.SkillType.ON_THE_BALL
import com.jervisffb.engine.rules.common.skills.SkillType.PASS
import com.jervisffb.engine.rules.common.skills.SkillType.PLAGUE_RIDDEN
import com.jervisffb.engine.rules.common.skills.SkillType.POGO_STICK
import com.jervisffb.engine.rules.common.skills.SkillType.PREHENSILE_TAIL
import com.jervisffb.engine.rules.common.skills.SkillType.PRIMAL_SAVAGERY
import com.jervisffb.engine.rules.common.skills.SkillType.PRO
import com.jervisffb.engine.rules.common.skills.SkillType.PUMP_UP_THE_CROWD
import com.jervisffb.engine.rules.common.skills.SkillType.PUTRID_REGURGITATION
import com.jervisffb.engine.rules.common.skills.SkillType.PUT_THE_BOOT_IN
import com.jervisffb.engine.rules.common.skills.SkillType.QUICK_BITE
import com.jervisffb.engine.rules.common.skills.SkillType.QUICK_FOUL
import com.jervisffb.engine.rules.common.skills.SkillType.RAIDING_PARTY
import com.jervisffb.engine.rules.common.skills.SkillType.RAM
import com.jervisffb.engine.rules.common.skills.SkillType.REGENERATION
import com.jervisffb.engine.rules.common.skills.SkillType.RELIABLE
import com.jervisffb.engine.rules.common.skills.SkillType.RIGHT_STUFF
import com.jervisffb.engine.rules.common.skills.SkillType.SABOTEUR
import com.jervisffb.engine.rules.common.skills.SkillType.SAFE_PAIR_OF_HANDS
import com.jervisffb.engine.rules.common.skills.SkillType.SAFE_PASS
import com.jervisffb.engine.rules.common.skills.SkillType.SAVAGE_BLOW
import com.jervisffb.engine.rules.common.skills.SkillType.SAVAGE_MAULING
import com.jervisffb.engine.rules.common.skills.SkillType.SECRET_WEAPON
import com.jervisffb.engine.rules.common.skills.SkillType.SHADOWING
import com.jervisffb.engine.rules.common.skills.SkillType.SHOT_TO_NOTHING
import com.jervisffb.engine.rules.common.skills.SkillType.SIDESTEP
import com.jervisffb.engine.rules.common.skills.SkillType.SLASHING_NAILS
import com.jervisffb.engine.rules.common.skills.SkillType.SLAYER
import com.jervisffb.engine.rules.common.skills.SkillType.SNEAKIEST_OF_THE_LOT
import com.jervisffb.engine.rules.common.skills.SkillType.SNEAKY_GIT
import com.jervisffb.engine.rules.common.skills.SkillType.SPRINT
import com.jervisffb.engine.rules.common.skills.SkillType.STAB
import com.jervisffb.engine.rules.common.skills.SkillType.STAND_FIRM
import com.jervisffb.engine.rules.common.skills.SkillType.STAR_OF_THE_SHOW
import com.jervisffb.engine.rules.common.skills.SkillType.STEADY_FOOTING
import com.jervisffb.engine.rules.common.skills.SkillType.STRIP_BALL
import com.jervisffb.engine.rules.common.skills.SkillType.STRONG_ARM
import com.jervisffb.engine.rules.common.skills.SkillType.STRONG_PASSING_GAME
import com.jervisffb.engine.rules.common.skills.SkillType.STUNTY
import com.jervisffb.engine.rules.common.skills.SkillType.SURE_FEET
import com.jervisffb.engine.rules.common.skills.SkillType.SURE_HANDS
import com.jervisffb.engine.rules.common.skills.SkillType.SWIFT_AS_THE_BREEZE
import com.jervisffb.engine.rules.common.skills.SkillType.TACKLE
import com.jervisffb.engine.rules.common.skills.SkillType.TASTY_MORSEL
import com.jervisffb.engine.rules.common.skills.SkillType.TAUNT
import com.jervisffb.engine.rules.common.skills.SkillType.TENTACLES
import com.jervisffb.engine.rules.common.skills.SkillType.THE_BALLISTA
import com.jervisffb.engine.rules.common.skills.SkillType.THE_FLASHING_BLADE
import com.jervisffb.engine.rules.common.skills.SkillType.THICK_SKULL
import com.jervisffb.engine.rules.common.skills.SkillType.THINKING_MANS_TROLL
import com.jervisffb.engine.rules.common.skills.SkillType.THROW_TEAMMATE
import com.jervisffb.engine.rules.common.skills.SkillType.TIMMMBER
import com.jervisffb.engine.rules.common.skills.SkillType.TITCHY
import com.jervisffb.engine.rules.common.skills.SkillType.TOXIN_CONNOISSEUR
import com.jervisffb.engine.rules.common.skills.SkillType.TREACHEROUS
import com.jervisffb.engine.rules.common.skills.SkillType.TWO_HEADS
import com.jervisffb.engine.rules.common.skills.SkillType.UNCHANNELLED_FURY
import com.jervisffb.engine.rules.common.skills.SkillType.UNSTEADY
import com.jervisffb.engine.rules.common.skills.SkillType.UNSTOPPABLE_MOMENTUM
import com.jervisffb.engine.rules.common.skills.SkillType.VICIOUS_VINES
import com.jervisffb.engine.rules.common.skills.SkillType.WATCH_OUT
import com.jervisffb.engine.rules.common.skills.SkillType.WHIRLING_DERVISH
import com.jervisffb.engine.rules.common.skills.SkillType.WISDOM_OF_THE_WHITE_DWARF
import com.jervisffb.engine.rules.common.skills.SkillType.WOODLAND_FURY
import com.jervisffb.engine.rules.common.skills.SkillType.WORKING_IN_TANDEM
import com.jervisffb.engine.rules.common.skills.SkillType.WRESTLE
import com.jervisffb.engine.rules.common.skills.SkillType.YOINK
import com.jervisffb.engine.sprites.SingleSprite
import com.jervisffb.engine.sprites.SpriteSheet
import com.jervisffb.resources.iconRootPath
import com.jervisffb.resources.portraitRootPath
import kotlinx.collections.immutable.toPersistentList

/**
 * This file contains all Star Players available in the BB2025 ruleset.
 *
 * Developer's Commentary:
 * TODO: Star Players that must be hired as a pair, like Dribl & Drull, are
 *  modelled as two independent positions since the engine has no concept of
 *  hiring two players as one inducement yet.
 *
 * Star player icons and portraits was lifted from:
 * - https://fumbbl.com/p/stars (HTML page)
 * - `curl https://fumbbl.com/api/roster/get/5160` (JSON list of star players)
 */
val AKHORNE_THE_SQUIRREL_BB2025 = StarPlayerPosition(
    id = PositionId("akhorne-the-squirrel"),
    title = "Akhorne The Squirrel",
    shortHand = "As",
    cost = 80_000,
    move = 7, strength = 1, agility = 2, passing = null, armorValue = 6,
    skills = listOf(
        CLAWS.id(),
        DAUNTLESS.id(),
        DODGE.id(),
        FRENZY.id(),
        JUMP_UP.id(),
        LONER.idTarget(4),
        NO_BALL.id(),
        SIDESTEP.id(),
        STUNTY.id(),
        TITCHY.id(),
        BLIND_RAGE.id(),
    ),
    specialRules = emptyList(),
    keywords = listOf(PlayerKeyword.BLITZER, PlayerKeyword.SQUIRREL),
    playsFor = emptyList(), // Any team
    size = PlayerSize.STANDARD,
    icon = SpriteSheet.ini("${iconRootPath}/AkhorneTheSquirrel.png", 1),
    portrait = SingleSprite.ini("${portraitRootPath}/AkhorneTheSquirrel.png"),
)

val ANQI_PANQI_BB2025 = StarPlayerPosition(
    id = PositionId("anqi-panqi"),
    title = "Anqi Panqi",
    shortHand = "Ap",
    cost = 190_000,
    move = 7, strength = 4, agility = 5, passing = 6, armorValue = 10,
    skills = listOf(
        BLOCK.id(),
        GRAB.id(),
        LONER.idTarget(4),
        STAND_FIRM.id(),
        UNSTEADY.id(),
        SAVAGE_BLOW.id(),
    ),
    specialRules = emptyList(),
    keywords = listOf(PlayerKeyword.BLOCKER, PlayerKeyword.LIZARDMAN),
    playsFor = listOf(RegionalSpecialRule.LUSTRIAN_SUPERLEAGUE),
    size = PlayerSize.STANDARD,
    icon = SpriteSheet.ini("${iconRootPath}/AnqiPanqi.png", 1),
    portrait = SingleSprite.ini("${portraitRootPath}/AnqiPanqi.png"),
)

val BARIK_FARBLAST_BB2025 = StarPlayerPosition(
    id = PositionId("barik-farblast"),
    title = "Barik Farblast",
    shortHand = "Bf",
    cost = 80_000,
    move = 6, strength = 3, agility = 4, passing = 3, armorValue = 9,
    skills = listOf(
        CANNONEER.id(),
        HAIL_MARY_PASS.id(),
        LONER.idTarget(4),
        PASS.id(),
        SECRET_WEAPON.id(),
        SURE_HANDS.id(),
        THICK_SKULL.id(),
        BLAST_IT.id(),
    ),
    specialRules = emptyList(),
    keywords = listOf(PlayerKeyword.DWARF, PlayerKeyword.THROWER),
    playsFor = listOf(RegionalSpecialRule.OLD_WORLD_CLASSIC, RegionalSpecialRule.WORLDS_EDGE_SUPERLEAGUE),
    size = PlayerSize.STANDARD,
    icon = SpriteSheet.ini("${iconRootPath}/BarikFarblast.png", 1),
    portrait = SingleSprite.ini("${portraitRootPath}/BarikFarblast.png"),
)

val BILEROT_VOMITFLESH_BB2025 = StarPlayerPosition(
    id = PositionId("bilerot-vomitflesh"),
    title = "Bilerot Vomitflesh",
    shortHand = "Bv",
    cost = 180_000,
    move = 4, strength = 5, agility = 4, passing = 6, armorValue = 10,
    skills = listOf(
        DIRTY_PLAYER.id(),
        DISTURBING_PRESENCE.id(),
        FOUL_APPEARANCE.id(),
        LONE_FOULER.id(),
        LONER.idTarget(4),
        REGENERATION.id(),
        UNSTEADY.id(),
        PUTRID_REGURGITATION.id(),
    ),
    specialRules = emptyList(),
    keywords = listOf(PlayerKeyword.BLOCKER, PlayerKeyword.HUMAN),
    playsFor = listOf(TeamSpecialRule.FAVOURED_OF_NURGLE),
    size = PlayerSize.STANDARD,
    icon = SpriteSheet.ini("${iconRootPath}/BilerotVomitflesh.png", 1),
    portrait = SingleSprite.ini("${portraitRootPath}/BilerotVomitflesh.png"),
)

val BOA_KONSSSTRIKTR_BB2025 = StarPlayerPosition(
    id = PositionId("boa-konssstriktr"),
    title = "Boa Kon'ssstriktr",
    shortHand = "Bs",
    cost = 180_000,
    move = 6, strength = 3, agility = 3, passing = 4, armorValue = 9,
    skills = listOf(
        DODGE.id(),
        FEND.id(),
        HYPNOTIC_GAZE.id(),
        LONER.idTarget(4),
        PREHENSILE_TAIL.id(),
        SAFE_PAIR_OF_HANDS.id(),
        SIDESTEP.id(),
        LOOK_INTO_MY_EYES.id(),
    ),
    specialRules = emptyList(),
    keywords = listOf(PlayerKeyword.RUNNER, PlayerKeyword.SNAKEMAN),
    playsFor = listOf(RegionalSpecialRule.LUSTRIAN_SUPERLEAGUE),
    size = PlayerSize.STANDARD,
    icon = SpriteSheet.ini("${iconRootPath}/BoaKonssstriktr.png", 1),
    portrait = SingleSprite.ini("${portraitRootPath}/BoaKonssstriktr.png"),
)

val BOMBER_DRIBBLESNOT_BB2025 = StarPlayerPosition(
    id = PositionId("bomber-dribblesnot"),
    title = "Bomber Dribblesnot",
    shortHand = "Bd",
    cost = 80_000,
    move = 6, strength = 2, agility = 3, passing = 3, armorValue = 8,
    skills = listOf(
        ACCURATE.id(),
        BOMBARDIER.id(),
        DODGE.id(),
        LONER.idTarget(4),
        RIGHT_STUFF.id(),
        SECRET_WEAPON.id(),
        STUNTY.id(),
        KABOOM.id(),
    ),
    specialRules = emptyList(),
    keywords = listOf(PlayerKeyword.GOBLIN, PlayerKeyword.SPECIAL),
    playsFor = listOf(RegionalSpecialRule.BADLANDS_BRAWL, RegionalSpecialRule.UNDERWORLD_CHALLENGE),
    size = PlayerSize.STANDARD,
    icon = SpriteSheet.ini("${iconRootPath}/BomberDribblesnot.png", 1),
    portrait = SingleSprite.ini("${portraitRootPath}/BomberDribblesnot.png"),
)

val CAPTAIN_KARINA_VON_RIESZ_BB2025 = StarPlayerPosition(
    id = PositionId("captain-karina-von-riesz"),
    title = "Captain Karina von Riesz",
    shortHand = "Cr",
    cost = 230_000,
    move = 7, strength = 4, agility = 2, passing = 3, armorValue = 9,
    skills = listOf(
        BLOOD_LUST.idTarget(2),
        DODGE.id(),
        HYPNOTIC_GAZE.id(),
        JUMP_UP.id(),
        LONER.idTarget(4),
        REGENERATION.id(),
        TASTY_MORSEL.id(),
    ),
    specialRules = emptyList(),
    keywords = listOf(PlayerKeyword.RUNNER, PlayerKeyword.VAMPIRE),
    playsFor = listOf(RegionalSpecialRule.SYLVANIAN_SPOTLIGHT),
    size = PlayerSize.STANDARD,
    icon = SpriteSheet.ini("${iconRootPath}/CaptainKarinaVonRiesz.png", 1),
    portrait = SingleSprite.ini("${portraitRootPath}/CaptainKarinaVonRiesz.png"),
)

val CINDY_PIEWHISTLE_BB2025 = StarPlayerPosition(
    id = PositionId("cindy-piewhistle"),
    title = "Cindy Piewhistle",
    shortHand = "Cp",
    cost = 100_000,
    move = 5, strength = 2, agility = 3, passing = 3, armorValue = 7,
    skills = listOf(
        ACCURATE.id(),
        BOMBARDIER.id(),
        DODGE.id(),
        LONER.idTarget(4),
        SECRET_WEAPON.id(),
        STUNTY.id(),
        ALL_YOU_CAN_EAT.id(),
    ),
    specialRules = emptyList(),
    keywords = listOf(PlayerKeyword.HALFLING, PlayerKeyword.SPECIAL),
    playsFor = listOf(RegionalSpecialRule.HAFLING_THIMBLE_CUP, RegionalSpecialRule.OLD_WORLD_CLASSIC),
    size = PlayerSize.STANDARD,
    icon = SpriteSheet.ini("${iconRootPath}/CindyPiewhistle.png", 1),
    portrait = SingleSprite.ini("${portraitRootPath}/CindyPiewhistle.png"),
)

val COUNT_LUTHOR_VON_DRAKENBORG_BB2025 = StarPlayerPosition(
    id = PositionId("count-luthor-von-drakenborg"),
    title = "Count Luthor von Drakenborg",
    shortHand = "Cd",
    cost = 300_000,
    move = 6, strength = 5, agility = 2, passing = 3, armorValue = 10,
    skills = listOf(
        BLOCK.id(),
        HYPNOTIC_GAZE.id(),
        LONER.idTarget(4),
        REGENERATION.id(),
        SIDESTEP.id(),
        STAR_OF_THE_SHOW.id(),
    ),
    specialRules = emptyList(),
    keywords = listOf(PlayerKeyword.BLOCKER, PlayerKeyword.VAMPIRE),
    playsFor = listOf(RegionalSpecialRule.SYLVANIAN_SPOTLIGHT),
    size = PlayerSize.STANDARD,
    icon = SpriteSheet.ini("${iconRootPath}/CountLuthorVonDrakenborg.png", 1),
    portrait = SingleSprite.ini("${portraitRootPath}/CountLuthorVonDrakenborg.png"),
)

val CRUMBLEBERRY_BB2025 = StarPlayerPosition(
    id = PositionId("crumbleberry"),
    title = "Crumbleberry",
    shortHand = "Cu",
    // Crumbleberry must be hired together with Grak for a combined cost of 250,000.
    // Hiring pairs isn't supported yet, so for now both halves list the full price.
    cost = 250_000,
    move = 5, strength = 2, agility = 3, passing = 5, armorValue = 7,
    skills = listOf(
        DODGE.id(),
        LETHAL_FLIGHT.id(),
        LONER.idTarget(4),
        RIGHT_STUFF.id(),
        STUNTY.id(),
        SURE_HANDS.id(),
        ILL_CARRY_YOU.id(),
    ),
    specialRules = emptyList(),
    keywords = listOf(PlayerKeyword.HALFLING, PlayerKeyword.LINEMAN),
    playsFor = emptyList(), // Any Team
    size = PlayerSize.STANDARD,
    icon = SpriteSheet.ini("${iconRootPath}/Crumbleberry.png", 1),
    portrait = SingleSprite.ini("${portraitRootPath}/Crumbleberry.png"),
)

val DEEPROOT_STRONGBRANCH_BB2025 = StarPlayerPosition(
    id = PositionId("deeproot-strongbranch"),
    title = "Deeproot Strongbranch",
    shortHand = "Ds",
    cost = 280_000,
    move = 2, strength = 7, agility = 5, passing = 4, armorValue = 11,
    skills = listOf(
        BLOCK.id(),
        BULLSEYE.id(),
        LONER.idTarget(4),
        MIGHTY_BLOW.id(),
        STAND_FIRM.id(),
        STRONG_ARM.id(),
        THICK_SKULL.id(),
        THROW_TEAMMATE.id(),
        TIMMMBER.id(),
        RELIABLE.id(),
    ),
    specialRules = emptyList(),
    keywords = listOf(PlayerKeyword.BIG_GUY, PlayerKeyword.TREEMAN),
    playsFor = listOf(RegionalSpecialRule.WOODLAND_LEAGUE),
    size = PlayerSize.BIG_GUY,
    icon = SpriteSheet.ini("${iconRootPath}/DeeprootStrongbranch.png", 1),
    portrait = SingleSprite.ini("${portraitRootPath}/DeeprootStrongbranch.png"),
)

val DRIBL_BB2025 = StarPlayerPosition(
    id = PositionId("dribl"),
    title = "Dribl",
    shortHand = "Dr",
    // Dribl must be hired together with Drull for a combined cost of 230,000.
    // Hiring pairs isn't supported yet, so for now both halves list the full price.
    cost = 230_000,
    move = 8, strength = 2, agility = 3, passing = 4, armorValue = 8,
    skills = listOf(
        DIRTY_PLAYER.id(),
        DODGE.id(),
        LONER.idTarget(4),
        QUICK_FOUL.id(),
        SIDESTEP.id(),
        SNEAKY_GIT.id(),
        STUNTY.id(),
        A_SNEAKY_PAIR.id(),
    ),
    specialRules = emptyList(),
    keywords = listOf(PlayerKeyword.SKINK, PlayerKeyword.SPECIAL),
    playsFor = listOf(RegionalSpecialRule.LUSTRIAN_SUPERLEAGUE),
    size = PlayerSize.STANDARD,
    icon = SpriteSheet.ini("${iconRootPath}/Dribl.png", 1),
    portrait = SingleSprite.ini("${portraitRootPath}/Dribl.png"),
)

val DRULL_BB2025 = StarPlayerPosition(
    id = PositionId("drull"),
    title = "Drull",
    shortHand = "Du",
    // Drull must be hired together with Dribl for a combined cost of 230,000.
    // Hiring pairs isn't supported yet, so for now both halves list the full price.
    cost = 230_000,
    move = 8, strength = 2, agility = 3, passing = 4, armorValue = 8,
    skills = listOf(
        DODGE.id(),
        LONER.idTarget(4),
        SIDESTEP.id(),
        STAB.id(),
        STUNTY.id(),
        A_SNEAKY_PAIR.id(),
    ),
    specialRules = emptyList(),
    keywords = listOf(PlayerKeyword.SKINK, PlayerKeyword.SPECIAL),
    playsFor = listOf(RegionalSpecialRule.LUSTRIAN_SUPERLEAGUE),
    size = PlayerSize.STANDARD,
    icon = SpriteSheet.ini("${iconRootPath}/Drull.png", 1),
    portrait = SingleSprite.ini("${portraitRootPath}/Drull.png"),
)

val ELDRIL_SIDEWINDER_BB2025 = StarPlayerPosition(
    id = PositionId("eldril-sidewinder"),
    title = "Eldril Sidewinder",
    shortHand = "Es",
    cost = 220_000,
    move = 8, strength = 3, agility = 2, passing = 3, armorValue = 8,
    skills = listOf(
        CATCH.id(),
        DODGE.id(),
        HYPNOTIC_GAZE.id(),
        LONER.idTarget(4),
        NERVES_OF_STEEL.id(),
        ON_THE_BALL.id(),
        MESMERISING_DANCE.id(),
    ),
    specialRules = emptyList(),
    keywords = listOf(PlayerKeyword.CATCHER, PlayerKeyword.ELF),
    playsFor = listOf(RegionalSpecialRule.ELVEN_KINGDOMS_LEAGUE),
    size = PlayerSize.STANDARD,
    icon = SpriteSheet.ini("${iconRootPath}/EldrilSidewinder.png", 1),
    portrait = SingleSprite.ini("${portraitRootPath}/EldrilSidewinder.png"),
)

val ESTELLE_LA_VENEAUX_BB2025 = StarPlayerPosition(
    id = PositionId("estelle-la-veneaux"),
    title = "Estelle la Veneaux",
    shortHand = "Ev",
    cost = 190_000,
    move = 6, strength = 3, agility = 3, passing = 4, armorValue = 8,
    skills = listOf(
        DISTURBING_PRESENCE.id(),
        DODGE.id(),
        GUARD.id(),
        LONER.idTarget(4),
        SIDESTEP.id(),
        BALEFUL_HEX.id(),
    ),
    specialRules = emptyList(),
    keywords = listOf(PlayerKeyword.HUMAN, PlayerKeyword.LINEMAN),
    playsFor = listOf(RegionalSpecialRule.LUSTRIAN_SUPERLEAGUE),
    size = PlayerSize.STANDARD,
    icon = SpriteSheet.ini("${iconRootPath}/EstelleLaVeneaux.png", 1),
    portrait = SingleSprite.ini("${portraitRootPath}/EstelleLaVeneaux.png"),
)

val FUNGUS_THE_LOON_BB2025 = StarPlayerPosition(
    id = PositionId("fungus-the-loon"),
    title = "Fungus the Loon",
    shortHand = "Fl",
    cost = 80_000,
    move = 4, strength = 7, agility = 3, passing = null, armorValue = 8,
    skills = listOf(
        BALL_AND_CHAIN.id(),
        LONER.idTarget(4),
        MIGHTY_BLOW.id(),
        NO_BALL.id(),
        SECRET_WEAPON.id(),
        STUNTY.id(),
        WHIRLING_DERVISH.id(),
    ),
    specialRules = emptyList(),
    keywords = listOf(PlayerKeyword.GOBLIN, PlayerKeyword.SPECIAL),
    playsFor = listOf(RegionalSpecialRule.BADLANDS_BRAWL, RegionalSpecialRule.UNDERWORLD_CHALLENGE),
    size = PlayerSize.STANDARD,
    icon = SpriteSheet.ini("${iconRootPath}/FungusTheLoon.png", 1),
    portrait = SingleSprite.ini("${portraitRootPath}/FungusTheLoon.png"),
)

val GLART_SMASHRIP_BB2025 = StarPlayerPosition(
    id = PositionId("glart-smashrip"),
    title = "Glart Smashrip",
    shortHand = "Gs",
    cost = 175_000,
    move = 5, strength = 4, agility = 4, passing = 6, armorValue = 9,
    skills = listOf(
        BLOCK.id(),
        CLAWS.id(),
        GRAB.id(),
        JUGGERNAUT.id(),
        LONER.idTarget(4),
        STAND_FIRM.id(),
        FRENZIED_RUSH.id(),
    ),
    specialRules = emptyList(),
    keywords = listOf(PlayerKeyword.BLOCKER, PlayerKeyword.SKAVEN),
    playsFor = listOf(RegionalSpecialRule.UNDERWORLD_CHALLENGE),
    size = PlayerSize.STANDARD,
    icon = SpriteSheet.ini("${iconRootPath}/GlartSmashrip.png", 1),
    portrait = SingleSprite.ini("${portraitRootPath}/GlartSmashrip.png"),
)

val GLORIEL_SUMMERBLOOM_BB2025 = StarPlayerPosition(
    id = PositionId("gloriel-summerbloom"),
    title = "Gloriel Summerbloom",
    shortHand = "Gu",
    cost = 150_000,
    move = 7, strength = 2, agility = 2, passing = 2, armorValue = 8,
    skills = listOf(
        ACCURATE.id(),
        DODGE.id(),
        LONER.idTarget(3),
        PASS.id(),
        SIDESTEP.id(),
        SURE_HANDS.id(),
        SHOT_TO_NOTHING.id(),
    ),
    specialRules = emptyList(),
    keywords = listOf(PlayerKeyword.ELF, PlayerKeyword.THROWER),
    playsFor = listOf(RegionalSpecialRule.ELVEN_KINGDOMS_LEAGUE),
    size = PlayerSize.STANDARD,
    icon = SpriteSheet.ini("${iconRootPath}/GlorielSummerbloom.png", 1),
    portrait = SingleSprite.ini("${portraitRootPath}/GlorielSummerbloom.png"),
)

val GLOTL_STOP_BB2025 = StarPlayerPosition(
    id = PositionId("glotl-stop"),
    title = "Glotl Stop",
    shortHand = "Gt",
    cost = 260_000,
    move = 6, strength = 6, agility = 5, passing = 6, armorValue = 10,
    skills = listOf(
        ANIMAL_SAVAGERY.id(),
        FRENZY.id(),
        LONER.idTarget(4),
        MIGHTY_BLOW.id(),
        PREHENSILE_TAIL.id(),
        STAND_FIRM.id(),
        THICK_SKULL.id(),
        PRIMAL_SAVAGERY.id(),
    ),
    specialRules = emptyList(),
    keywords = listOf(PlayerKeyword.BIG_GUY, PlayerKeyword.LIZARDMAN),
    playsFor = listOf(RegionalSpecialRule.LUSTRIAN_SUPERLEAGUE),
    size = PlayerSize.BIG_GUY,
    icon = SpriteSheet.ini("${iconRootPath}/GlotlStop.png", 1),
    portrait = SingleSprite.ini("${portraitRootPath}/GlotlStop.png"),
)

val GRAK_BB2025 = StarPlayerPosition(
    id = PositionId("grak"),
    title = "Grak",
    shortHand = "Gr",
    // Grak must be hired together with Crumbleberry for a combined cost of 250,000.
    // Hiring pairs isn't supported yet, so for now both halves list the full price.
    cost = 250_000,
    move = 5, strength = 5, agility = 4, passing = 4, armorValue = 10,
    skills = listOf(
        BONE_HEAD.id(),
        KICK_TEAMMATE.id(),
        LONER.idTarget(4),
        MIGHTY_BLOW.id(),
        THICK_SKULL.id(),
        ILL_CARRY_YOU.id(),
    ),
    specialRules = emptyList(),
    keywords = listOf(PlayerKeyword.BIG_GUY, PlayerKeyword.OGRE),
    playsFor = emptyList(), // Any Team
    size = PlayerSize.BIG_GUY,
    icon = SpriteSheet.ini("${iconRootPath}/Grak.png", 1),
    portrait = SingleSprite.ini("${portraitRootPath}/Grak.png"),
)

val GRASHNAK_BLACKHOOF_BB2025 = StarPlayerPosition(
    id = PositionId("grashnak-blackhoof"),
    title = "Grashnak Blackhoof",
    shortHand = "Gb",
    cost = 240_000,
    move = 6, strength = 6, agility = 4, passing = 6, armorValue = 9,
    skills = listOf(
        FRENZY.id(),
        HORNS.id(),
        LONER.idTarget(4),
        MIGHTY_BLOW.id(),
        THICK_SKULL.id(),
        UNCHANNELLED_FURY.id(),
        GORED_BY_THE_BULL.id(),
    ),
    specialRules = emptyList(),
    keywords = listOf(PlayerKeyword.BIG_GUY, PlayerKeyword.MINOTAUR),
    playsFor = listOf(RegionalSpecialRule.CHAOS_CLASH),
    size = PlayerSize.BIG_GUY,
    icon = SpriteSheet.ini("${iconRootPath}/GrashnakBlackhoof.png", 1),
    portrait = SingleSprite.ini("${portraitRootPath}/GrashnakBlackhoof.png"),
)

val GRETCHEN_WACHTER_BB2025 = StarPlayerPosition(
    id = PositionId("gretchen-wachter"),
    title = "Gretchen Wächter",
    shortHand = "Gw",
    cost = 180_000,
    move = 7, strength = 3, agility = 2, passing = null, armorValue = 9,
    skills = listOf(
        DISTURBING_PRESENCE.id(),
        DODGE.id(),
        FOUL_APPEARANCE.id(),
        JUMP_UP.id(),
        LONER.idTarget(4),
        NO_BALL.id(),
        REGENERATION.id(),
        SHADOWING.id(),
        SIDESTEP.id(),
        INCORPOREAL.id(),
    ),
    specialRules = emptyList(),
    keywords = listOf(PlayerKeyword.SPECIAL, PlayerKeyword.UNDEAD, PlayerKeyword.WRAITH),
    playsFor = listOf(RegionalSpecialRule.SYLVANIAN_SPOTLIGHT),
    size = PlayerSize.STANDARD,
    icon = SpriteSheet.ini("${iconRootPath}/GretchenWächterTheBloodBowlWidow.png", 1),
    portrait = SingleSprite.ini("${portraitRootPath}/GretchenWächterTheBloodBowlWidow.png"),
)

val GRIFF_OBERWALD_BB2025 = StarPlayerPosition(
    id = PositionId("griff-oberwald"),
    title = "Griff Oberwald",
    shortHand = "Go",
    cost = 300_000,
    move = 7, strength = 4, agility = 2, passing = 3, armorValue = 9,
    skills = listOf(
        BLOCK.id(),
        DODGE.id(),
        FEND.id(),
        LONER.idTarget(3),
        SPRINT.id(),
        SURE_FEET.id(),
        CONSUMMATE_PROFESSIONAL.id(),
    ),
    specialRules = emptyList(),
    keywords = listOf(PlayerKeyword.BLITZER, PlayerKeyword.HUMAN),
    playsFor = listOf(RegionalSpecialRule.OLD_WORLD_CLASSIC),
    size = PlayerSize.STANDARD,
    icon = SpriteSheet.ini("${iconRootPath}/GriffOberwald.png", 1),
    portrait = SingleSprite.ini("${portraitRootPath}/GriffOberwald.png"),
)

val GRIM_IRONJAW_BB2025 = StarPlayerPosition(
    id = PositionId("grim-ironjaw"),
    title = "Grim Ironjaw",
    shortHand = "Gi",
    cost = 190_000,
    move = 5, strength = 4, agility = 3, passing = 6, armorValue = 9,
    skills = listOf(
        BLOCK.id(),
        DAUNTLESS.id(),
        FRENZY.id(),
        HATRED.id(PlayerKeyword.BIG_GUY),
        LONER.idTarget(4),
        MULTIPLE_BLOCK.id(),
        THICK_SKULL.id(),
        SLAYER.id(),
    ),
    specialRules = emptyList(),
    keywords = listOf(PlayerKeyword.DWARF, PlayerKeyword.SPECIAL),
    playsFor = listOf(RegionalSpecialRule.WORLDS_EDGE_SUPERLEAGUE),
    size = PlayerSize.STANDARD,
    icon = SpriteSheet.ini("${iconRootPath}/GrimIronjaw.png", 1),
    portrait = SingleSprite.ini("${portraitRootPath}/GrimIronjaw.png"),
)

val GROMBRINDAL_BB2025 = StarPlayerPosition(
    id = PositionId("grombrindal"),
    title = "Grombrindal",
    shortHand = "Gm",
    cost = 170_000,
    move = 5, strength = 3, agility = 3, passing = 4, armorValue = 10,
    skills = listOf(
        BLOCK.id(),
        BREAK_TACKLE.id(),
        DAUNTLESS.id(),
        LONER.idTarget(4),
        MIGHTY_BLOW.id(),
        STAND_FIRM.id(),
        SURE_FEET.id(),
        THICK_SKULL.id(),
        WISDOM_OF_THE_WHITE_DWARF.id(),
    ),
    specialRules = emptyList(),
    keywords = listOf(PlayerKeyword.BLOCKER, PlayerKeyword.DWARF),
    playsFor = listOf(RegionalSpecialRule.HAFLING_THIMBLE_CUP, RegionalSpecialRule.OLD_WORLD_CLASSIC, RegionalSpecialRule.WORLDS_EDGE_SUPERLEAGUE),
    size = PlayerSize.STANDARD,
    icon = SpriteSheet.ini("${iconRootPath}/Grombrindal.png", 1),
    portrait = SingleSprite.ini("${portraitRootPath}/Grombrindal.png"),
)

val GUFFLE_PUSMAW_BB2025 = StarPlayerPosition(
    id = PositionId("guffle-pusmaw"),
    title = "Guffle Pusmaw",
    shortHand = "Gp",
    cost = 150_000,
    move = 5, strength = 4, agility = 4, passing = 6, armorValue = 10,
    skills = listOf(
        FOUL_APPEARANCE.id(),
        LONER.idTarget(4),
        MONSTROUS_MOUTH.id(),
        NERVES_OF_STEEL.id(),
        ON_THE_BALL.id(),
        PLAGUE_RIDDEN.id(),
        QUICK_BITE.id(),
    ),
    specialRules = emptyList(),
    keywords = listOf(PlayerKeyword.BLOCKER, PlayerKeyword.HUMAN),
    playsFor = listOf(TeamSpecialRule.FAVOURED_OF_NURGLE),
    size = PlayerSize.STANDARD,
    icon = SpriteSheet.ini("${iconRootPath}/GufflePusmaw.png", 1),
    portrait = SingleSprite.ini("${portraitRootPath}/GufflePusmaw.png"),
)

val HTHARK_THE_UNSTOPPABLE_BB2025 = StarPlayerPosition(
    id = PositionId("hthark-the-unstoppable"),
    title = "H'thark the Unstoppable",
    shortHand = "Hu",
    cost = 300_000,
    move = 6, strength = 6, agility = 4, passing = 6, armorValue = 10,
    skills = listOf(
        BLOCK.id(),
        BREAK_TACKLE.id(),
        DEFENSIVE.id(),
        JUGGERNAUT.id(),
        LONER.idTarget(4),
        SPRINT.id(),
        SURE_FEET.id(),
        THICK_SKULL.id(),
        UNSTEADY.id(),
        UNSTOPPABLE_MOMENTUM.id(),
    ),
    specialRules = emptyList(),
    keywords = listOf(PlayerKeyword.BLITZER, PlayerKeyword.DWARF),
    playsFor = listOf(RegionalSpecialRule.BADLANDS_BRAWL, TeamSpecialRule.FAVOURED_OF_HASHUT),
    size = PlayerSize.STANDARD,
    icon = SpriteSheet.ini("${iconRootPath}/HtharkTheUnstoppable.png", 1),
    portrait = SingleSprite.ini("${portraitRootPath}/HtharkTheUnstoppable.png"),
)

val HAKFLEM_SKUTTLESPIKE_BB2025 = StarPlayerPosition(
    id = PositionId("hakflem-skuttlespike"),
    title = "Hakflem Skuttlespike",
    shortHand = "Hs",
    cost = 200_000,
    move = 8, strength = 3, agility = 2, passing = 3, armorValue = 8,
    skills = listOf(
        DODGE.id(),
        EXTRA_ARMS.id(),
        LONER.idTarget(4),
        PREHENSILE_TAIL.id(),
        TWO_HEADS.id(),
        TREACHEROUS.id(),
    ),
    specialRules = emptyList(),
    keywords = listOf(PlayerKeyword.RUNNER, PlayerKeyword.SKAVEN),
    playsFor = listOf(RegionalSpecialRule.UNDERWORLD_CHALLENGE),
    size = PlayerSize.STANDARD,
    icon = SpriteSheet.ini("${iconRootPath}/HakflemSkuttlespike.png", 1),
    portrait = SingleSprite.ini("${portraitRootPath}/HakflemSkuttlespike.png"),
)

val HELMUT_WULF_BB2025 = StarPlayerPosition(
    id = PositionId("helmut-wulf"),
    title = "Helmut Wulf",
    shortHand = "Hw",
    cost = 140_000,
    move = 6, strength = 3, agility = 3, passing = null, armorValue = 9,
    skills = listOf(
        CHAINSAW.id(),
        LONER.idTarget(4),
        NO_BALL.id(),
        PRO.id(),
        SECRET_WEAPON.id(),
        STAND_FIRM.id(),
        OLD_PRO.id(),
    ),
    specialRules = emptyList(),
    keywords = listOf(PlayerKeyword.HUMAN, PlayerKeyword.SPECIAL),
    playsFor = listOf(RegionalSpecialRule.OLD_WORLD_CLASSIC),
    size = PlayerSize.STANDARD,
    icon = SpriteSheet.ini("${iconRootPath}/HelmutWulf.png", 1),
    portrait = SingleSprite.ini("${portraitRootPath}/HelmutWulf.png"),
)

val IVAN_THE_ANIMAL_DEATHSHROUD_BB2025 = StarPlayerPosition(
    id = PositionId("ivan-the-animal-deathshroud"),
    title = "Ivan 'the Animal' Deathshroud",
    shortHand = "Id",
    cost = 210_000,
    move = 6, strength = 4, agility = 4, passing = 5, armorValue = 9,
    skills = listOf(
        BLOCK.id(),
        DISTURBING_PRESENCE.id(),
        HATRED.id(PlayerKeyword.DWARF),
        JUGGERNAUT.id(),
        LONER.idTarget(4),
        REGENERATION.id(),
        STRIP_BALL.id(),
        TACKLE.id(),
        DWARVEN_SCOURGE.id(),
    ),
    specialRules = emptyList(),
    keywords = listOf(PlayerKeyword.BLITZER, PlayerKeyword.HUMAN, PlayerKeyword.SKELETON, PlayerKeyword.UNDEAD),
    playsFor = listOf(RegionalSpecialRule.SYLVANIAN_SPOTLIGHT),
    size = PlayerSize.STANDARD,
    icon = SpriteSheet.ini("${iconRootPath}/IvanTheAnimalDeathshroud.png", 1),
    portrait = SingleSprite.ini("${portraitRootPath}/IvanTheAnimalDeathshroud.png"),
)

val IVAR_ERIKSSON_BB2025 = StarPlayerPosition(
    id = PositionId("ivar-eriksson"),
    title = "Ivar Eriksson",
    shortHand = "Ie",
    cost = 215_000,
    move = 6, strength = 4, agility = 3, passing = 4, armorValue = 9,
    skills = listOf(
        BLOCK.id(),
        GUARD.id(),
        LONER.idTarget(4),
        TACKLE.id(),
        RAIDING_PARTY.id(),
    ),
    specialRules = emptyList(),
    keywords = listOf(PlayerKeyword.BLITZER, PlayerKeyword.HUMAN),
    playsFor = listOf(RegionalSpecialRule.OLD_WORLD_CLASSIC),
    size = PlayerSize.STANDARD,
    icon = SpriteSheet.ini("${iconRootPath}/IvarEriksson.png", 1),
    portrait = SingleSprite.ini("${portraitRootPath}/IvarEriksson.png"),
)

val JEREMIAH_KOOL_BB2025 = StarPlayerPosition(
    id = PositionId("jeremiah-kool"),
    title = "Jeremiah Kool",
    shortHand = "Jk",
    cost = 300_000,
    move = 8, strength = 3, agility = 1, passing = 2, armorValue = 9,
    skills = listOf(
        BLOCK.id(),
        DODGE.id(),
        DIVING_CATCH.id(),
        DUMP_OFF.id(),
        LONER.idTarget(4),
        NERVES_OF_STEEL.id(),
        ON_THE_BALL.id(),
        PASS.id(),
        SIDESTEP.id(),
        THE_FLASHING_BLADE.id(),
    ),
    specialRules = emptyList(),
    keywords = listOf(PlayerKeyword.ELF, PlayerKeyword.RUNNER),
    playsFor = listOf(RegionalSpecialRule.ELVEN_KINGDOMS_LEAGUE),
    size = PlayerSize.STANDARD,
    icon = SpriteSheet.ini("${iconRootPath}/JeremiahKool.png", 1),
    portrait = SingleSprite.ini("${portraitRootPath}/JeremiahKool.png"),
)

val JORDELL_FRESHBREEZE_BB2025 = StarPlayerPosition(
    id = PositionId("jordell-freshbreeze"),
    title = "Jordell Freshbreeze",
    shortHand = "Jf",
    cost = 280_000,
    move = 8, strength = 3, agility = 1, passing = 3, armorValue = 8,
    skills = listOf(
        BLOCK.id(),
        DIVING_CATCH.id(),
        DODGE.id(),
        LEAP.id(),
        LONER.idTarget(4),
        SIDESTEP.id(),
        STEADY_FOOTING.id(),
        SWIFT_AS_THE_BREEZE.id(),
    ),
    specialRules = emptyList(),
    keywords = listOf(PlayerKeyword.BLITZER, PlayerKeyword.ELF),
    playsFor = listOf(RegionalSpecialRule.ELVEN_KINGDOMS_LEAGUE, RegionalSpecialRule.WOODLAND_LEAGUE),
    size = PlayerSize.STANDARD,
    icon = SpriteSheet.ini("${iconRootPath}/JordellFreshbreeze.png", 1),
    portrait = SingleSprite.ini("${portraitRootPath}/JordellFreshbreeze.png"),
)

val JOSEF_BUGMAN_BB2025 = StarPlayerPosition(
    id = PositionId("josef-bugman"),
    title = "Josef Bugman",
    shortHand = "Jb",
    cost = 180_000,
    move = 5, strength = 3, agility = 3, passing = 4, armorValue = 9,
    skills = listOf(
        BLOCK.id(),
        DRUNKARD.id(),
        FEND.id(),
        LONER.idTarget(3),
        TACKLE.id(),
        TAUNT.id(),
        THICK_SKULL.id(),
        DWARFEN_GRIT.id(),
    ),
    specialRules = emptyList(),
    keywords = listOf(PlayerKeyword.BLOCKER, PlayerKeyword.DWARF),
    playsFor = listOf(RegionalSpecialRule.OLD_WORLD_CLASSIC, RegionalSpecialRule.WORLDS_EDGE_SUPERLEAGUE),
    size = PlayerSize.STANDARD,
    icon = SpriteSheet.ini("${iconRootPath}/_starsffbtest_josefbugman.png", 1),
    portrait = SingleSprite.ini("${portraitRootPath}/_starsffbtest_josefbugman.png"),
)

val KARLA_VON_KILL_BB2025 = StarPlayerPosition(
    id = PositionId("karla-von-kill"),
    title = "Karla von Kill",
    shortHand = "Kk",
    cost = 210_000,
    move = 6, strength = 4, agility = 3, passing = 3, armorValue = 9,
    skills = listOf(
        BLOCK.id(),
        DAUNTLESS.id(),
        DODGE.id(),
        JUMP_UP.id(),
        LONER.idTarget(4),
        INDOMITABLE.id(),
    ),
    specialRules = emptyList(),
    keywords = listOf(PlayerKeyword.BLITZER, PlayerKeyword.HUMAN),
    playsFor = listOf(RegionalSpecialRule.LUSTRIAN_SUPERLEAGUE, RegionalSpecialRule.OLD_WORLD_CLASSIC),
    size = PlayerSize.STANDARD,
    icon = SpriteSheet.ini("${iconRootPath}/KarlaVonKill.png", 1),
    portrait = SingleSprite.ini("${portraitRootPath}/KarlaVonKill.png"),
)

val KIROTH_KRAKENEYE_BB2025 = StarPlayerPosition(
    id = PositionId("kiroth-krakeneye"),
    title = "Kiroth Krakeneye",
    shortHand = "Kr",
    cost = 160_000,
    move = 7, strength = 3, agility = 2, passing = 3, armorValue = 8,
    skills = listOf(
        DISTURBING_PRESENCE.id(),
        FOUL_APPEARANCE.id(),
        LONER.idTarget(4),
        ON_THE_BALL.id(),
        TACKLE.id(),
        TENTACLES.id(),
        BLACK_INK.id(),
    ),
    specialRules = emptyList(),
    keywords = listOf(PlayerKeyword.ELF, PlayerKeyword.RUNNER),
    playsFor = listOf(RegionalSpecialRule.ELVEN_KINGDOMS_LEAGUE),
    size = PlayerSize.STANDARD,
    icon = SpriteSheet.ini("${iconRootPath}/KirothKrakeneye.png", 1),
    portrait = SingleSprite.ini("${portraitRootPath}/KirothKrakeneye.png"),
)

val KREEK_RUSTGOUGER_BB2025 = StarPlayerPosition(
    id = PositionId("kreek-rustgouger"),
    title = "Kreek Rustgouger",
    shortHand = "Ku",
    cost = 180_000,
    move = 4, strength = 7, agility = 4, passing = null, armorValue = 10,
    skills = listOf(
        BALL_AND_CHAIN.id(),
        LONER.idTarget(4),
        MIGHTY_BLOW.id(),
        NO_BALL.id(),
        PREHENSILE_TAIL.id(),
        SECRET_WEAPON.id(),
        ILL_BE_BACK.id(),
    ),
    specialRules = emptyList(),
    keywords = listOf(PlayerKeyword.BIG_GUY, PlayerKeyword.SKAVEN, PlayerKeyword.SPECIAL),
    playsFor = listOf(RegionalSpecialRule.UNDERWORLD_CHALLENGE),
    size = PlayerSize.BIG_GUY,
    icon = SpriteSheet.ini("${iconRootPath}/KreekRustgouger.png", 1),
    portrait = SingleSprite.ini("${portraitRootPath}/KreekRustgouger.png"),
)

val LORD_BORAK_THE_DESPOILER_BB2025 = StarPlayerPosition(
    id = PositionId("lord-borak-the-despoiler"),
    title = "Lord Borak the Despoiler",
    shortHand = "Ld",
    cost = 270_000,
    move = 5, strength = 5, agility = 3, passing = 5, armorValue = 10,
    skills = listOf(
        BLOCK.id(),
        DIRTY_PLAYER.id(),
        LEADER.id(),
        LONER.idTarget(3),
        MIGHTY_BLOW.id(),
        PUT_THE_BOOT_IN.id(),
        SNEAKY_GIT.id(),
        LORD_OF_CHAOS.id(),
    ),
    specialRules = emptyList(),
    keywords = listOf(PlayerKeyword.BLOCKER, PlayerKeyword.HUMAN),
    playsFor = listOf(RegionalSpecialRule.CHAOS_CLASH),
    size = PlayerSize.STANDARD,
    icon = SpriteSheet.ini("${iconRootPath}/LordBorakTheDespoiler.png", 1),
    portrait = SingleSprite.ini("${portraitRootPath}/LordBorakTheDespoiler.png"),
)

val LUCIEN_SWIFT_BB2025 = StarPlayerPosition(
    id = PositionId("lucien-swift"),
    title = "Lucien Swift",
    shortHand = "Ls",
    // Lucien Swift must be hired together with Valen Swift for a combined cost of 300,000.
    // Hiring pairs isn't supported yet, so for now both halves list the full price.
    cost = 300_000,
    move = 7, strength = 3, agility = 2, passing = 3, armorValue = 9,
    skills = listOf(
        BLOCK.id(),
        LONER.idTarget(4),
        MIGHTY_BLOW.id(),
        TACKLE.id(),
        WORKING_IN_TANDEM.id(),
    ),
    specialRules = emptyList(),
    keywords = listOf(PlayerKeyword.BLITZER, PlayerKeyword.ELF),
    playsFor = listOf(RegionalSpecialRule.ELVEN_KINGDOMS_LEAGUE),
    size = PlayerSize.STANDARD,
    icon = SpriteSheet.ini("${iconRootPath}/LucienSwift.png", 1),
    portrait = SingleSprite.ini("${portraitRootPath}/LucienSwift.png"),
)

val MAPLE_HIGHGROVE_BB2025 = StarPlayerPosition(
    id = PositionId("maple-highgrove"),
    title = "Maple Highgrove",
    shortHand = "Mh",
    cost = 210_000,
    move = 3, strength = 5, agility = 5, passing = 5, armorValue = 11,
    skills = listOf(
        BRAWLER.id(),
        GRAB.id(),
        LONER.idTarget(4),
        MIGHTY_BLOW.id(),
        STAND_FIRM.id(),
        TENTACLES.id(),
        THICK_SKULL.id(),
        VICIOUS_VINES.id(),
    ),
    specialRules = emptyList(),
    keywords = listOf(PlayerKeyword.BIG_GUY, PlayerKeyword.TREEMAN),
    playsFor = listOf(RegionalSpecialRule.WOODLAND_LEAGUE),
    size = PlayerSize.BIG_GUY,
    icon = SpriteSheet.ini("${iconRootPath}/MapleHighgrove.png", 1),
    portrait = SingleSprite.ini("${portraitRootPath}/MapleHighgrove.png"),
)

val MAX_SPLEENRIPPER_BB2025 = StarPlayerPosition(
    id = PositionId("max-spleenripper"),
    title = "Max Spleenripper",
    shortHand = "Ms",
    cost = 130_000,
    move = 5, strength = 4, agility = 4, passing = null, armorValue = 9,
    skills = listOf(
        CHAINSAW.id(),
        LONER.idTarget(4),
        NO_BALL.id(),
        SECRET_WEAPON.id(),
        MAXIMUM_CARNAGE.id(),
    ),
    specialRules = emptyList(),
    keywords = listOf(PlayerKeyword.HUMAN, PlayerKeyword.SPECIAL),
    playsFor = listOf(TeamSpecialRule.FAVOURED_OF_KHORNE),
    size = PlayerSize.STANDARD,
    icon = SpriteSheet.ini("${iconRootPath}/MaxSpleenripper.png", 1),
    portrait = SingleSprite.ini("${portraitRootPath}/MaxSpleenripper.png"),
)

val MORG_N_THORG_BB2025 = StarPlayerPosition(
    id = PositionId("morg-n-thorg"),
    title = "Morg 'n' Thorg",
    shortHand = "Mt",
    cost = 340_000,
    move = 6, strength = 6, agility = 3, passing = 4, armorValue = 11,
    skills = listOf(
        BLOCK.id(),
        BULLSEYE.id(),
        HATRED.id(PlayerKeyword.UNDEAD),
        LONER.idTarget(4),
        MIGHTY_BLOW.id(),
        THICK_SKULL.id(),
        THROW_TEAMMATE.id(),
        THE_BALLISTA.id(),
    ),
    specialRules = emptyList(),
    keywords = listOf(PlayerKeyword.BIG_GUY, PlayerKeyword.OGRE),
    playsFor = RegionalSpecialRule.entries.toPersistentList().remove(RegionalSpecialRule.SYLVANIAN_SPOTLIGHT),
    size = PlayerSize.BIG_GUY,
    icon = SpriteSheet.ini("${iconRootPath}/MorgNThorg.png", 1),
    portrait = SingleSprite.ini("${portraitRootPath}/MorgNThorg.png"),
)

val NOBBLA_BLACKWART_BB2025 = StarPlayerPosition(
    id = PositionId("nobbla-blackwart"),
    title = "Nobbla Blackwart",
    shortHand = "Nb",
    cost = 120_000,
    move = 6, strength = 2, agility = 3, passing = null, armorValue = 8,
    skills = listOf(
        BLOCK.id(),
        CHAINSAW.id(),
        DODGE.id(),
        LONER.idTarget(4),
        NO_BALL.id(),
        SABOTEUR.id(),
        SECRET_WEAPON.id(),
        STUNTY.id(),
        KICK_EM_WHILE_THEYRE_DOWN.id(),
    ),
    specialRules = emptyList(),
    keywords = listOf(PlayerKeyword.GOBLIN, PlayerKeyword.SPECIAL),
    playsFor = listOf(RegionalSpecialRule.BADLANDS_BRAWL, RegionalSpecialRule.UNDERWORLD_CHALLENGE),
    size = PlayerSize.STANDARD,
    icon = SpriteSheet.ini("${iconRootPath}/NobblaBlackwart.png", 1),
    portrait = SingleSprite.ini("${portraitRootPath}/NobblaBlackwart.png"),
)

val PUGGY_BACONBREATH_BB2025 = StarPlayerPosition(
    id = PositionId("puggy-baconbreath"),
    title = "Puggy Baconbreath",
    shortHand = "Pb",
    cost = 130_000,
    move = 5, strength = 3, agility = 3, passing = 3, armorValue = 8,
    skills = listOf(
        BLOCK.id(),
        DODGE.id(),
        LONER.idTarget(3),
        NERVES_OF_STEEL.id(),
        RIGHT_STUFF.id(),
        STUNTY.id(),
        HALFLING_LUCK.id(),
    ),
    specialRules = emptyList(),
    keywords = listOf(PlayerKeyword.BLITZER, PlayerKeyword.HALFLING),
    playsFor = listOf(RegionalSpecialRule.HAFLING_THIMBLE_CUP, RegionalSpecialRule.OLD_WORLD_CLASSIC),
    size = PlayerSize.STANDARD,
    icon = SpriteSheet.ini("${iconRootPath}/PuggyBaconbreath.png", 1),
    portrait = SingleSprite.ini("${portraitRootPath}/PuggyBaconbreath.png"),
)

val RASHNAK_BACKSTABBER_BB2025 = StarPlayerPosition(
    id = PositionId("rashnak-backstabber"),
    title = "Rashnak Backstabber",
    shortHand = "Rb",
    cost = 130_000,
    move = 7, strength = 3, agility = 3, passing = 5, armorValue = 8,
    skills = listOf(
        LONER.idTarget(4),
        SHADOWING.id(),
        SIDESTEP.id(),
        SNEAKY_GIT.id(),
        STAB.id(),
        TOXIN_CONNOISSEUR.id(),
    ),
    specialRules = emptyList(),
    keywords = listOf(PlayerKeyword.GOBLIN, PlayerKeyword.SPECIAL),
    playsFor = listOf(RegionalSpecialRule.BADLANDS_BRAWL),
    size = PlayerSize.STANDARD,
    icon = SpriteSheet.ini("${iconRootPath}/RashnakBackstabber.png", 1),
    portrait = SingleSprite.ini("${portraitRootPath}/RashnakBackstabber.png"),
)

val RIPPER_BOLGROT_BB2025 = StarPlayerPosition(
    id = PositionId("ripper-bolgrot"),
    title = "Ripper Bolgrot",
    shortHand = "Ro",
    cost = 250_000,
    move = 5, strength = 6, agility = 5, passing = 4, armorValue = 10,
    skills = listOf(
        BULLSEYE.id(),
        GRAB.id(),
        LONER.idTarget(4),
        MIGHTY_BLOW.id(),
        REGENERATION.id(),
        THROW_TEAMMATE.id(),
        THINKING_MANS_TROLL.id(),
    ),
    specialRules = emptyList(),
    keywords = listOf(PlayerKeyword.BIG_GUY, PlayerKeyword.TROLL),
    playsFor = listOf(RegionalSpecialRule.BADLANDS_BRAWL, RegionalSpecialRule.UNDERWORLD_CHALLENGE),
    size = PlayerSize.BIG_GUY,
    icon = SpriteSheet.ini("${iconRootPath}/RipperBolgrot.png", 1),
    portrait = SingleSprite.ini("${portraitRootPath}/RipperBolgrot.png"),
)

val RODNEY_ROACHBAIT_BB2025 = StarPlayerPosition(
    id = PositionId("rodney-roachbait"),
    title = "Rodney Roachbait",
    shortHand = "Rr",
    cost = 70_000,
    move = 6, strength = 2, agility = 3, passing = 4, armorValue = 7,
    skills = listOf(
        CATCH.id(),
        DIVING_CATCH.id(),
        JUMP_UP.id(),
        LONER.idTarget(4),
        ON_THE_BALL.id(),
        SIDESTEP.id(),
        STUNTY.id(),
        WRESTLE.id(),
        CATCH_OF_THE_DAY.id(),
    ),
    specialRules = emptyList(),
    keywords = listOf(PlayerKeyword.GNOME, PlayerKeyword.SPECIAL),
    playsFor = listOf(RegionalSpecialRule.WOODLAND_LEAGUE),
    size = PlayerSize.STANDARD,
    icon = SpriteSheet.ini("${iconRootPath}/RodneyRoachbait.png", 1),
    // FUMBBL only has a blank placeholder portrait for this Star Player, but we still
    // reference it, in case it is ever updated to the correct one.
    portrait = SingleSprite.ini("${portraitRootPath}/RodneyRoachbait.png"),
)

val ROWANA_FORESTFOOT_BB2025 = StarPlayerPosition(
    id = PositionId("rowana-forestfoot"),
    title = "Rowana Forestfoot",
    shortHand = "Rf",
    cost = 160_000,
    move = 6, strength = 3, agility = 3, passing = 4, armorValue = 8,
    skills = listOf(
        DODGE.id(),
        DUMP_OFF.id(),
        GUARD.id(),
        HORNS.id(),
        JUMP_UP.id(),
        LEAP.id(),
        LONER.idTarget(4),
        BOUNDING_LEAP.id(),
    ),
    specialRules = emptyList(),
    keywords = listOf(PlayerKeyword.BLOCKER, PlayerKeyword.GNOME),
    playsFor = listOf(RegionalSpecialRule.WOODLAND_LEAGUE),
    size = PlayerSize.STANDARD,
    icon = SpriteSheet.ini("${iconRootPath}/RowanaForestfoot.png", 1),
    // FUMBBL only has a blank placeholder portrait for this Star Player, but we still
    // reference it, in case it is ever updated to the correct one.
    portrait = SingleSprite.ini("${portraitRootPath}/RowanaForestfoot.png"),
)

val ROXANNA_DARKNAIL_BB2025 = StarPlayerPosition(
    id = PositionId("roxanna-darknail"),
    title = "Roxanna Darknail",
    shortHand = "Rd",
    cost = 270_000,
    move = 8, strength = 3, agility = 1, passing = 3, armorValue = 8,
    skills = listOf(
        DODGE.id(),
        FRENZY.id(),
        JUMP_UP.id(),
        JUGGERNAUT.id(),
        LEAP.id(),
        LONER.idTarget(4),
        SLASHING_NAILS.id(),
    ),
    specialRules = emptyList(),
    keywords = listOf(PlayerKeyword.ELF, PlayerKeyword.SPECIAL),
    playsFor = listOf(RegionalSpecialRule.ELVEN_KINGDOMS_LEAGUE),
    size = PlayerSize.STANDARD,
    icon = SpriteSheet.ini("${iconRootPath}/RoxannaDarknail.png", 1),
    portrait = SingleSprite.ini("${portraitRootPath}/RoxannaDarknail.png"),
)

val RUMBELOW_SHEEPSKIN_BB2025 = StarPlayerPosition(
    id = PositionId("rumbelow-sheepskin"),
    title = "Rumbelow Sheepskin",
    shortHand = "Rs",
    cost = 170_000,
    move = 6, strength = 3, agility = 3, passing = 5, armorValue = 8,
    skills = listOf(
        BLOCK.id(),
        HORNS.id(),
        JUGGERNAUT.id(),
        LONER.idTarget(4),
        TACKLE.id(),
        THICK_SKULL.id(),
        RAM.id(),
    ),
    specialRules = emptyList(),
    keywords = listOf(PlayerKeyword.BLITZER, PlayerKeyword.HALFLING),
    playsFor = listOf(RegionalSpecialRule.HAFLING_THIMBLE_CUP),
    size = PlayerSize.STANDARD,
    icon = SpriteSheet.ini("${iconRootPath}/RumblelowSheepskin.png", 1),
    portrait = SingleSprite.ini("${portraitRootPath}/RumblelowSheepskin.png"),
)

val SCRAPPA_SOREHEAD_BB2025 = StarPlayerPosition(
    id = PositionId("scrappa-sorehead"),
    title = "Scrappa Sorehead",
    shortHand = "Ss",
    cost = 120_000,
    move = 7, strength = 2, agility = 3, passing = 4, armorValue = 8,
    skills = listOf(
        DIRTY_PLAYER.id(),
        DODGE.id(),
        LONER.idTarget(4),
        POGO_STICK.id(),
        RIGHT_STUFF.id(),
        SPRINT.id(),
        STUNTY.id(),
        SURE_FEET.id(),
        YOINK.id(),
    ),
    specialRules = emptyList(),
    keywords = listOf(PlayerKeyword.GOBLIN, PlayerKeyword.SPECIAL),
    playsFor = listOf(RegionalSpecialRule.BADLANDS_BRAWL, RegionalSpecialRule.UNDERWORLD_CHALLENGE),
    size = PlayerSize.STANDARD,
    icon = SpriteSheet.ini("${iconRootPath}/ScrappaSorehead.png", 1),
    portrait = SingleSprite.ini("${portraitRootPath}/ScrappaSorehead.png"),
)

val SCYLA_ANFINGRIMM_BB2025 = StarPlayerPosition(
    id = PositionId("scyla-anfingrimm"),
    title = "Scyla Anfingrimm",
    shortHand = "Sa",
    cost = 200_000,
    move = 5, strength = 5, agility = 4, passing = 6, armorValue = 10,
    skills = listOf(
        CLAWS.id(),
        FRENZY.id(),
        LONER.idTarget(4),
        MIGHTY_BLOW.id(),
        PREHENSILE_TAIL.id(),
        THICK_SKULL.id(),
        UNCHANNELLED_FURY.id(),
        FURY_OF_THE_BLOOD_GOD.id(),
    ),
    specialRules = emptyList(),
    keywords = listOf(PlayerKeyword.BIG_GUY, PlayerKeyword.SPAWN),
    playsFor = listOf(TeamSpecialRule.FAVOURED_OF_KHORNE),
    size = PlayerSize.BIG_GUY,
    icon = SpriteSheet.ini("${iconRootPath}/ScylaAnfingrimm.png", 1),
    portrait = SingleSprite.ini("${portraitRootPath}/ScylaAnfingrimm.png"),
)

val SKITTER_STAB_STAB_BB2025 = StarPlayerPosition(
    id = PositionId("skitter-stab-stab"),
    title = "Skitter Stab-Stab",
    shortHand = "St",
    cost = 170_000,
    move = 9, strength = 2, agility = 2, passing = 4, armorValue = 8,
    skills = listOf(
        DODGE.id(),
        LONER.idTarget(4),
        PREHENSILE_TAIL.id(),
        SHADOWING.id(),
        STAB.id(),
        MASTER_ASSASSIN.id(),
    ),
    specialRules = emptyList(),
    keywords = listOf(PlayerKeyword.RUNNER, PlayerKeyword.SKAVEN),
    playsFor = listOf(RegionalSpecialRule.UNDERWORLD_CHALLENGE),
    size = PlayerSize.STANDARD,
    icon = SpriteSheet.ini("${iconRootPath}/SkitterStabStab.png", 1),
    portrait = SingleSprite.ini("${portraitRootPath}/SkitterStabStab.png"),
)

val SKRORG_SNOWPELT_BB2025 = StarPlayerPosition(
    id = PositionId("skrorg-snowpelt"),
    title = "Skrorg Snowpelt",
    shortHand = "Sn",
    cost = 240_000,
    move = 5, strength = 5, agility = 4, passing = 6, armorValue = 9,
    skills = listOf(
        BLOCK.id(),
        CLAWS.id(),
        DISTURBING_PRESENCE.id(),
        JUGGERNAUT.id(),
        LONER.idTarget(4),
        MIGHTY_BLOW.id(),
        PUMP_UP_THE_CROWD.id(),
    ),
    specialRules = emptyList(),
    keywords = listOf(PlayerKeyword.BIG_GUY, PlayerKeyword.YHETEE),
    playsFor = listOf(RegionalSpecialRule.OLD_WORLD_CLASSIC, RegionalSpecialRule.WORLDS_EDGE_SUPERLEAGUE),
    size = PlayerSize.BIG_GUY,
    icon = SpriteSheet.ini("${iconRootPath}/SkrorgSnowpelt.png", 1),
    portrait = SingleSprite.ini("${portraitRootPath}/SkrorgSnowpelt.png"),
)

val SKRULL_HALFHEIGHT_BB2025 = StarPlayerPosition(
    id = PositionId("skrull-halfheight"),
    title = "Skrull Halfheight",
    shortHand = "Sh",
    cost = 150_000,
    move = 6, strength = 3, agility = 4, passing = 3, armorValue = 9,
    skills = listOf(
        ACCURATE.id(),
        LONER.idTarget(4),
        NERVES_OF_STEEL.id(),
        PASS.id(),
        REGENERATION.id(),
        SURE_HANDS.id(),
        THICK_SKULL.id(),
        STRONG_PASSING_GAME.id(),
    ),
    specialRules = emptyList(),
    keywords = listOf(PlayerKeyword.DWARF, PlayerKeyword.SKELETON, PlayerKeyword.THROWER, PlayerKeyword.UNDEAD),
    playsFor = listOf(RegionalSpecialRule.SYLVANIAN_SPOTLIGHT, RegionalSpecialRule.WORLDS_EDGE_SUPERLEAGUE),
    size = PlayerSize.STANDARD,
    icon = SpriteSheet.ini("${iconRootPath}/SkrullHalfheight.png", 1),
    portrait = SingleSprite.ini("${portraitRootPath}/SkrullHalfheight.png"),
)

val SWIFTVINE_GLIMMERSHARD_BB2025 = StarPlayerPosition(
    id = PositionId("swiftvine-glimmershard"),
    title = "Swiftvine Glimmershard",
    shortHand = "Sg",
    cost = 110_000,
    move = 7, strength = 2, agility = 3, passing = 5, armorValue = 7,
    skills = listOf(
        DISTURBING_PRESENCE.id(),
        FEND.id(),
        LONER.idTarget(4),
        SIDESTEP.id(),
        STAB.id(),
        STUNTY.id(),
        FURIOUS_OUTBURST.id(),
    ),
    specialRules = emptyList(),
    keywords = listOf(PlayerKeyword.SPECIAL, PlayerKeyword.SPITE),
    playsFor = listOf(RegionalSpecialRule.WOODLAND_LEAGUE),
    size = PlayerSize.STANDARD,
    icon = SpriteSheet.ini("${iconRootPath}/SwiftvineGlimmershard.png", 1),
    portrait = SingleSprite.ini("${portraitRootPath}/SwiftvineGlimmershard.png"),
)

val THE_BLACK_GOBBO_BB2025 = StarPlayerPosition(
    id = PositionId("the-black-gobbo"),
    title = "The Black Gobbo",
    shortHand = "Bg",
    cost = 210_000,
    move = 6, strength = 2, agility = 3, passing = 3, armorValue = 8,
    skills = listOf(
        BOMBARDIER.id(),
        DISTURBING_PRESENCE.id(),
        DODGE.id(),
        LONER.idTarget(3),
        SIDESTEP.id(),
        SNEAKY_GIT.id(),
        STAB.id(),
        SNEAKIEST_OF_THE_LOT.id(),
    ),
    specialRules = emptyList(),
    keywords = listOf(PlayerKeyword.GOBLIN, PlayerKeyword.SPECIAL),
    playsFor = listOf(RegionalSpecialRule.BADLANDS_BRAWL, RegionalSpecialRule.UNDERWORLD_CHALLENGE),
    size = PlayerSize.STANDARD,
    icon = SpriteSheet.ini("${iconRootPath}/TheBlackGobbo.png", 1),
    portrait = SingleSprite.ini("${portraitRootPath}/TheBlackGobbo.png"),
)

val THE_MIGHTY_ZUG_BB2025 = StarPlayerPosition(
    id = PositionId("the-mighty-zug"),
    title = "The Mighty Zug",
    shortHand = "Mz",
    cost = 220_000,
    move = 5, strength = 5, agility = 4, passing = 6, armorValue = 10,
    skills = listOf(
        BLOCK.id(),
        LONER.idTarget(4),
        MIGHTY_BLOW.id(),
        UNSTEADY.id(),
        CRUSHING_BLOW.id(),
    ),
    specialRules = emptyList(),
    keywords = listOf(PlayerKeyword.BLOCKER, PlayerKeyword.HUMAN),
    playsFor = listOf(RegionalSpecialRule.OLD_WORLD_CLASSIC, RegionalSpecialRule.WORLDS_EDGE_SUPERLEAGUE),
    size = PlayerSize.STANDARD,
    icon = SpriteSheet.ini("${iconRootPath}/MightyZug.png", 1),
    portrait = SingleSprite.ini("${portraitRootPath}/MightyZug.png"),
)

val THORSSON_STOUTMEAD_BB2025 = StarPlayerPosition(
    id = PositionId("thorsson-stoutmead"),
    title = "Thorsson Stoutmead",
    shortHand = "Ts",
    cost = 170_000,
    move = 6, strength = 3, agility = 4, passing = 3, armorValue = 8,
    skills = listOf(
        BLOCK.id(),
        DRUNKARD.id(),
        LONER.idTarget(4),
        THICK_SKULL.id(),
        BEER_BARREL_BASH.id(),
    ),
    specialRules = emptyList(),
    keywords = listOf(PlayerKeyword.HUMAN, PlayerKeyword.LINEMAN),
    playsFor = listOf(RegionalSpecialRule.OLD_WORLD_CLASSIC, RegionalSpecialRule.WORLDS_EDGE_SUPERLEAGUE),
    size = PlayerSize.STANDARD,
    icon = SpriteSheet.ini("${iconRootPath}/ThorssonStoutmead.png", 1),
    portrait = SingleSprite.ini("${portraitRootPath}/ThorssonStoutmead.png"),
)

val VALEN_SWIFT_BB2025 = StarPlayerPosition(
    id = PositionId("valen-swift"),
    title = "Valen Swift",
    shortHand = "Vs",
    // Valen Swift must be hired together with Lucien Swift for a combined cost of 300,000.
    // Hiring pairs isn't supported yet, so for now both halves list the full price.
    cost = 300_000,
    move = 7, strength = 3, agility = 2, passing = 2, armorValue = 9,
    skills = listOf(
        ACCURATE.id(),
        LONER.idTarget(4),
        NERVES_OF_STEEL.id(),
        PASS.id(),
        SAFE_PASS.id(),
        SURE_HANDS.id(),
        WORKING_IN_TANDEM.id(),
    ),
    specialRules = emptyList(),
    keywords = listOf(PlayerKeyword.ELF, PlayerKeyword.THROWER),
    playsFor = listOf(RegionalSpecialRule.ELVEN_KINGDOMS_LEAGUE),
    size = PlayerSize.STANDARD,
    icon = SpriteSheet.ini("${iconRootPath}/ValenSwift.png", 1),
    portrait = SingleSprite.ini("${portraitRootPath}/ValenSwift.png"),
)

val VARAG_GHOUL_CHEWER_BB2025 = StarPlayerPosition(
    id = PositionId("varag-ghoul-chewer"),
    title = "Varag Ghoul-Chewer",
    shortHand = "Vc",
    cost = 260_000,
    move = 6, strength = 5, agility = 3, passing = 5, armorValue = 10,
    skills = listOf(
        BLOCK.id(),
        HATRED.id(PlayerKeyword.UNDEAD),
        JUMP_UP.id(),
        LONER.idTarget(4),
        MIGHTY_BLOW.id(),
        THICK_SKULL.id(),
        UNSTEADY.id(),
        KRUMP_AND_SMASH.id(),
    ),
    specialRules = emptyList(),
    keywords = listOf(PlayerKeyword.BLOCKER, PlayerKeyword.ORC),
    playsFor = listOf(RegionalSpecialRule.BADLANDS_BRAWL),
    size = PlayerSize.STANDARD,
    icon = SpriteSheet.ini("${iconRootPath}/VaragGhoulchewer.png", 1),
    portrait = SingleSprite.ini("${portraitRootPath}/VaragGhoulchewer.png"),
)

val WILHELM_CHANEY_BB2025 = StarPlayerPosition(
    id = PositionId("wilhelm-chaney"),
    title = "Wilhelm Chaney",
    shortHand = "Wc",
    cost = 220_000,
    move = 8, strength = 4, agility = 3, passing = 4, armorValue = 9,
    skills = listOf(
        CATCH.id(),
        CLAWS.id(),
        FRENZY.id(),
        LONER.idTarget(4),
        REGENERATION.id(),
        WRESTLE.id(),
        SAVAGE_MAULING.id(),
    ),
    specialRules = emptyList(),
    keywords = listOf(PlayerKeyword.BLITZER, PlayerKeyword.UNDEAD, PlayerKeyword.WEREWOLF),
    playsFor = listOf(RegionalSpecialRule.SYLVANIAN_SPOTLIGHT),
    size = PlayerSize.STANDARD,
    icon = SpriteSheet.ini("${iconRootPath}/WilhelmTheWolfManChaney.png", 1),
    portrait = SingleSprite.ini("${portraitRootPath}/WilhelmTheWolfManChaney.png"),
)

val WILLOW_ROSEBARK_BB2025 = StarPlayerPosition(
    id = PositionId("willow-rosebark"),
    title = "Willow Rosebark",
    shortHand = "Wr",
    cost = 160_000,
    move = 6, strength = 4, agility = 3, passing = 5, armorValue = 9,
    skills = listOf(
        DAUNTLESS.id(),
        LONER.idTarget(4),
        SIDESTEP.id(),
        THICK_SKULL.id(),
        WOODLAND_FURY.id(),
    ),
    specialRules = emptyList(),
    keywords = listOf(PlayerKeyword.BLITZER, PlayerKeyword.DRYAD),
    playsFor = listOf(RegionalSpecialRule.WOODLAND_LEAGUE),
    size = PlayerSize.STANDARD,
    icon = SpriteSheet.ini("${iconRootPath}/WillowRosebark.png", 1),
    portrait = SingleSprite.ini("${portraitRootPath}/WillowRosebark.png"),
)

val WITHERGRASP_DOUBLEDROOL_BB2025 = StarPlayerPosition(
    id = PositionId("withergrasp-doubledrool"),
    title = "Withergrasp Doubledrool",
    shortHand = "Wd",
    cost = 170_000,
    move = 6, strength = 3, agility = 3, passing = 4, armorValue = 9,
    skills = listOf(
        FOUL_APPEARANCE.id(),
        LONER.idTarget(4),
        PREHENSILE_TAIL.id(),
        TACKLE.id(),
        TENTACLES.id(),
        TWO_HEADS.id(),
        WRESTLE.id(),
        WATCH_OUT.id(),
    ),
    specialRules = emptyList(),
    keywords = listOf(PlayerKeyword.BEASTMAN, PlayerKeyword.BLOCKER),
    playsFor = listOf(TeamSpecialRule.FAVOURED_OF_NURGLE),
    size = PlayerSize.STANDARD,
    icon = SpriteSheet.ini("${iconRootPath}/WithergraspDoubledrool.png", 1),
    portrait = SingleSprite.ini("${portraitRootPath}/WithergraspDoubledrool.png"),
)

val ZOLCATH_THE_ZOAT_BB2025 = StarPlayerPosition(
    id = PositionId("zolcath-the-zoat"),
    title = "Zolcath the Zoat",
    shortHand = "Zz",
    cost = 220_000,
    move = 5, strength = 5, agility = 4, passing = 5, armorValue = 10,
    skills = listOf(
        DISTURBING_PRESENCE.id(),
        JUGGERNAUT.id(),
        LONER.idTarget(4),
        MIGHTY_BLOW.id(),
        PREHENSILE_TAIL.id(),
        REGENERATION.id(),
        SURE_FEET.id(),
        EXCUSE_ME_ARE_YOU_A_ZOAT.id(),
    ),
    specialRules = emptyList(),
    keywords = listOf(PlayerKeyword.BIG_GUY, PlayerKeyword.ZOAT),
    playsFor = listOf(RegionalSpecialRule.ELVEN_KINGDOMS_LEAGUE, RegionalSpecialRule.LUSTRIAN_SUPERLEAGUE),
    size = PlayerSize.BIG_GUY,
    icon = SpriteSheet.ini("${iconRootPath}/ZolcathTheZoat.png", 1),
    portrait = SingleSprite.ini("${portraitRootPath}/ZolcathTheZoat.png"),
)

val ZZHARG_MADEYE_BB2025 = StarPlayerPosition(
    id = PositionId("zzharg-madeye"),
    title = "Zzharg Madeye",
    shortHand = "Zm",
    cost = 130_000,
    move = 4, strength = 4, agility = 4, passing = 3, armorValue = 10,
    skills = listOf(
        CANNONEER.id(),
        HAIL_MARY_PASS.id(),
        LONER.idTarget(4),
        NERVES_OF_STEEL.id(),
        SECRET_WEAPON.id(),
        THICK_SKULL.id(),
        BLASTIN_SOLVES_EVERYTHING.id(),
    ),
    specialRules = emptyList(),
    keywords = listOf(PlayerKeyword.DWARF, PlayerKeyword.SPECIAL),
    playsFor = listOf(TeamSpecialRule.FAVOURED_OF_HASHUT),
    size = PlayerSize.STANDARD,
    icon = SpriteSheet.ini("${iconRootPath}/ZzhargMadeye.png", 1),
    portrait = SingleSprite.ini("${portraitRootPath}/ZzhargMadeye.png"),
)

val STAR_PLAYERS = listOf(
    AKHORNE_THE_SQUIRREL_BB2025,
    ANQI_PANQI_BB2025,
    BARIK_FARBLAST_BB2025,
    BILEROT_VOMITFLESH_BB2025,
    BOA_KONSSSTRIKTR_BB2025,
    BOMBER_DRIBBLESNOT_BB2025,
    CAPTAIN_KARINA_VON_RIESZ_BB2025,
    CINDY_PIEWHISTLE_BB2025,
    COUNT_LUTHOR_VON_DRAKENBORG_BB2025,
    CRUMBLEBERRY_BB2025,
    DEEPROOT_STRONGBRANCH_BB2025,
    DRIBL_BB2025,
    DRULL_BB2025,
    ELDRIL_SIDEWINDER_BB2025,
    ESTELLE_LA_VENEAUX_BB2025,
    FUNGUS_THE_LOON_BB2025,
    GLART_SMASHRIP_BB2025,
    GLORIEL_SUMMERBLOOM_BB2025,
    GLOTL_STOP_BB2025,
    GRAK_BB2025,
    GRASHNAK_BLACKHOOF_BB2025,
    GRETCHEN_WACHTER_BB2025,
    GRIFF_OBERWALD_BB2025,
    GRIM_IRONJAW_BB2025,
    GROMBRINDAL_BB2025,
    GUFFLE_PUSMAW_BB2025,
    HAKFLEM_SKUTTLESPIKE_BB2025,
    HELMUT_WULF_BB2025,
    HTHARK_THE_UNSTOPPABLE_BB2025,
    IVAN_THE_ANIMAL_DEATHSHROUD_BB2025,
    IVAR_ERIKSSON_BB2025,
    JEREMIAH_KOOL_BB2025,
    JORDELL_FRESHBREEZE_BB2025,
    JOSEF_BUGMAN_BB2025,
    KARLA_VON_KILL_BB2025,
    KIROTH_KRAKENEYE_BB2025,
    KREEK_RUSTGOUGER_BB2025,
    LORD_BORAK_THE_DESPOILER_BB2025,
    LUCIEN_SWIFT_BB2025,
    MAPLE_HIGHGROVE_BB2025,
    MAX_SPLEENRIPPER_BB2025,
    MORG_N_THORG_BB2025,
    NOBBLA_BLACKWART_BB2025,
    PUGGY_BACONBREATH_BB2025,
    RASHNAK_BACKSTABBER_BB2025,
    RIPPER_BOLGROT_BB2025,
    RODNEY_ROACHBAIT_BB2025,
    ROWANA_FORESTFOOT_BB2025,
    ROXANNA_DARKNAIL_BB2025,
    RUMBELOW_SHEEPSKIN_BB2025,
    SCRAPPA_SOREHEAD_BB2025,
    SCYLA_ANFINGRIMM_BB2025,
    SKITTER_STAB_STAB_BB2025,
    SKRORG_SNOWPELT_BB2025,
    SKRULL_HALFHEIGHT_BB2025,
    SWIFTVINE_GLIMMERSHARD_BB2025,
    THE_BLACK_GOBBO_BB2025,
    THE_MIGHTY_ZUG_BB2025,
    THORSSON_STOUTMEAD_BB2025,
    VALEN_SWIFT_BB2025,
    VARAG_GHOUL_CHEWER_BB2025,
    WILHELM_CHANEY_BB2025,
    WILLOW_ROSEBARK_BB2025,
    WITHERGRASP_DOUBLEDROOL_BB2025,
    ZOLCATH_THE_ZOAT_BB2025,
    ZZHARG_MADEYE_BB2025,
)
