package com.jervisffb.engine.model.inducements.wizards

enum class WizardType(val description: String, val named: Boolean) {
    HIRELING_SPORTS_WIZARD("Hireling Sports-Wizard", named = false), // See page 94 in the rulebook
    CHAOS_SORCERER("Chaos Sorcerer", named = false),
    DRUCHII_SPORTS_WIZARD("Druchii Sports-Wizard", named = false),
    ASUR_HIGH_MAGE("Asur High Mage", named = false),
    SLAAN_MAGE_PRIST("Slaan Mage Priest", named = false),
    HORTICULTURALIST_OF_NURGLE("Horticulturalist of Nurgle", named = false),
    SPORTS_NECROTHEURGE("Sports Necrotheurge", named = false),
    WICKED_WITCH("Wicked Witch", named = false),
    WARLOCK_ENGINEER("Warlock Engineer", named = false),
    OGRE_FIREBELLY("Ogre Firebelly", named = false),
    NIGHT_GOBLIN_SHAMAN("Night Goblin Shaman", named = false),

    // Named wizards
    HORATIO_X_SCHOTTENHEIM("Horatio X. Schottenheim", named = true), // See page 32 in the Deathzone rulebook
}
