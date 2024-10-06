package com.jervisffb.fumbbl.web.api

data class RedraftingLimits(
    val apothecary: Int,
    val budget: Int,
    val cheerleaders: Int,
    val coaches: Int,
    val fans: Int,
    val newTreasury: Int,
    val rerolls: Int,
    val treasury: Int,
)
