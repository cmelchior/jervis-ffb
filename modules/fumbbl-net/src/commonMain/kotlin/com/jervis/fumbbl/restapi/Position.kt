package com.jervis.fumbbl.restapi

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
    val roster: Roster,
//    val ruleset: Ruleset,
    val secretweapon: String,
    val skillSet: String,
    val skills: List<String>,
    val specialRules: List<String>,
    val stats: Stats,
    val thrall: String,
    val title: String,
    val type: String,
    val undead: String,
)
