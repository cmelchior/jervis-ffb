package com.jervisffb.engine.model

/**
 * This class enumerates all keywords available in BB2025.
 *
 */
sealed interface Keyword {
    val description: String
}

enum class PlayerKeyword(override val description: String) : Keyword {
    ANIMAL("Animal"),
    BEASTMAN("Beastman"),
    BIG_GUY("Big Guy"),
    BLITZER("Blitzer"),
    BLOCKER("Blocker"),
    CATCHER("Catcher"),
    CONSTRUCT("Construct"),
    DWARF("Dwarf"),
    ELF("Elf"),
    FROG("Frog"),
    GHOUL("Ghoul"),
    GNOBLAR("Gnoblar"),
    GNOME("Gnome"),
    GOBLIN("Goblin"),
    HALFLING("Halfling"),
    HUMAN("Human"),
    LINEMAN("Lineman"),
    LIZARDMAN("Lizardman"),
    MINOTAUR("Minotaur"),
    OGRE("Ogre"),
    ORC("Orc"),
    RUNNER("Runner"),
    SKAVEN("Skaven"),
    SKELETON("Skeleton"),
    SPAWN("Spawn"),
    SPECIAL("Special"),
    SQUIRREL("Squirrel"),
    SNAKEMAN("Snakeman"),
    SKINK("Skink"),
    SNOTLING("Snotling"),
    THRALL("Thrall"),
    THROWER("Thrower"),
    TREEMAN("Treeman"),
    TROLL("Troll"),
    UNDEAD("Undead"),
    YHETEE("Yhetee"),
    VAMPIRE("Vampire"),
    WEREWOLF("Werewolf"),
    WRAITH("Wraith"),
    ZOMBIE("Zombie"),
}

enum class SkillKeyword(override val description: String) : Keyword {
    ACTIVE("Active"),
    PASSIVE("Passive"),
    ELITE("Elite"),
}

