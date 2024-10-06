package com.jervisffb.fumbbl.web.api

import kotlinx.serialization.Serializable

@Serializable
data class Position(
    val cost: Int,
    val doubleSkills: List<String>,
    val gender: String,
    val groupSize: Int,
    val icon: Int,
    val iconLetter: String,
    val id: String,
    val keywords: List<String>,
    val megastar: String,
    val name: String,
    val normalSkills: List<String>,
    val portrait: Int,
    val quantity: Int,
    val race: String?,
    val roster: com.jervisffb.fumbbl.web.api.Roster,
//    val ruleset: Ruleset,
    val secretweapon: String,
    val skillSet: String,
    val skills: List<String>,
    val specialRules: List<String>,
    val stats: com.jervisffb.fumbbl.web.api.Stats,
    val thrall: String,
    val title: String,
    val type: String,
    val undead: String,
)
