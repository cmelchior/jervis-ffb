package com.jervisffb.fumbbl.web.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Mappings for https://fumbbl.com/api/roster/get/<id>

@Serializable
public data class FumbbleRosterDetails(
    public val id: String,
    public val ownerRuleset: String,
    public val name: String,
    public val nameGenerator: String,
    public val rerollCost: Int,
    public val apothecary: String,
    public val undead: String,
    public val necromancer: String,
    // public val raisePosition: Any?,
    public val rookiePosition: String,
    public val maxBigGuys: Int,
    public val info: String,
    public val stats: RosterStats,
    public val playable: String,
    // public val keywords: List<Any?>,
    public val logos: Logos,
    public val pitch: String,
    public val specialRules: List<SpecialRule>,
    // public val stars: List<Any?>,
    public val positions: List<FumbbleRosterPosition>,
)

@Serializable
public data class RosterStats(
    public val physique: String,
    public val finesse: String,
    public val versatility: String,
)

@Serializable
public data class Logos(
    @SerialName("192")
    public val size192: String,
    @SerialName("128")
    public val size128: String,
    @SerialName("96")
    public val size96: String,
    @SerialName("64")
    public val size64: String,
    @SerialName("48")
    public val size48: String,
    @SerialName("32")
    public val size32: String,
)

@Serializable
public data class SpecialRule(
    public val id: String,
    public val name: String,
//    public val options: Any?,
//    public val option: Any?,
//    public val filter: Any?,
)

@Serializable
public data class FumbbleRosterPosition(
    public val id: String,
    public val type: String,
    public val gender: String?,
    public val title: String,
    public val quantity: Int,
    public val iconLetter: String,
    public val cost: Int,
    public val stats: RosterPositionStats,
    public val portrait: String,
    public val icon: String,
    public val skills: List<String>,
    public val normalSkills: List<String>,
    public val doubleSkills: List<String>,
)

@Serializable
data class RosterPositionStats(
    val MA: Int,
    val ST: Int,
    val AG: Int,
    val PA: Int,
    val AV: Int,
)
