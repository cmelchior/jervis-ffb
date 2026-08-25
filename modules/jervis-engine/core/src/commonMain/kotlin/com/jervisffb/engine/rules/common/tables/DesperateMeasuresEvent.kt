package com.jervisffb.engine.rules.common.tables

import com.jervisffb.engine.model.inducements.card.SpecialPlayCard

/**
 * A result on the Desperate Measures Table.
 * It is up to each ruleset to define the concrete list of results.
 */
interface DesperateMeasuresEvent {
    val label: String
    val createCard: () -> SpecialPlayCard
}
