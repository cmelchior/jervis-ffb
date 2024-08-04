package com.jervis.fumbbl.restapi

data class Redrafting(
    val base: Int,
    val budgetCap: Int,
    val cappedBudget: Int,
    val goldPerGame: Int,
    val goldPerLoss: Int,
    val goldPerTie: Int,
    val goldPerWin: Int,
    val redraftCap: Int,
    val redraftRamp: Int,
    val seasonGames: Int,
    val tooltip: String,
)
