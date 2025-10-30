package com.jervisffb.engine.model

/**
 * This class enumerates all keywords available in BB2025.
 *
 */
sealed interface Keyword {
    val description: String
}

enum class PlayerKeyword(override val description: String) : Keyword {
    // Position keywords
    BIG_GUY("Big Guy"),
    BLITZER("Blitzer"),
    BLOCKER("Blocker"),
    CATCHER("Catcher"),
    LINEMAN("Lineman"),
    RUNNER("Runner"),
    SPECIAL("Special"),
    THROWER("Thrower"),

    // Race keyword
    BEASTMAN("Beastman"),
    CONSTRUCT("Construct"),
    DWARF("Dwarf"),
    ELF("Elf"),
    GNOBLAR("Gnoblar"),
    GOBLIN("Goblin"),
    GHOUL("Ghoul"),
    HALFLING("Halfling"),
    HUMAN("Human"),
    LIZARDMAN("Lizardman"),
    MINOTAUR("Minotaur"),
    OGRE("Ogre"),
    ORC("Orc"),
    TROLL("Troll"),
    UNDEAD("Undead"),
    ZOMBIE("Zombie"),
    WEREWOLF("Werewolf"),
    WRAITH("Wraith"),
}

enum class SkillKeyword(override val description: String) : Keyword {
    ACTIVE("Active"),
    PASSIVE("Passive"),
}

