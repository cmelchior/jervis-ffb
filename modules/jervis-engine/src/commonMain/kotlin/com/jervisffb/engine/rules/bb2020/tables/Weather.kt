package com.jervisffb.engine.rules.bb2020.tables

/**
 * Enumeration of different weather types as defined on
 * page 37 in the rulebook.
 */
enum class Weather(val title: String, val description: String) {
    SWELTERING_HEAT(
        "Sweltering Heat",
        "D3 random players from each team on the pitch will faint from the heat and will not be available this drive."
    ),
    VERY_SUNNY(
        "Very Sunny",
        "A -1 modifier applies to all passing rolls."
    ),
    PERFECT_CONDITIONS(
        "Perfect Conditions",
        "Perfect Fantasy Football weather."
    ),
    POURING_RAIN(
        "Pouring Rain",
        "A -1 modifier applies to all catch, intercept, or pick-up rolls."
    ),
    BLIZZARD(
        "Blizzard",
        "Rushes has a -1 modifier and only quick or short passes can be attempted."
    ),
}
