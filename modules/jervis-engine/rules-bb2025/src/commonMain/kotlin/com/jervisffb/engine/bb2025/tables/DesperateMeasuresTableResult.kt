package com.jervisffb.engine.bb2025.tables

import com.jervisffb.engine.bb2025.inducements.effects.DesperateMeasures
import com.jervisffb.engine.rules.common.tables.DesperateMeasuresEvent

/**
 * List all possible outcomes of Desperate Measures in BB2025.
 */
enum class BB2025DesperateMeasuresTableResult(
    override val label: String,
    override val createCard: () -> DesperateMeasures
): DesperateMeasuresEvent {
    YOU_DOPE("You Dope", createCard = { com.jervisffb.engine.bb2025.inducements.effects.YouDope() }),
    RAZZLE_DAZZLE("Razzle Dazzle", createCard = { com.jervisffb.engine.bb2025.inducements.effects.RazzleDazzle() }),
    HANGOVER("Hangover", createCard = { com.jervisffb.engine.bb2025.inducements.effects.Hangover() }),
    GRUDGE_MATCH("Grudge Match", createCard = { com.jervisffb.engine.bb2025.inducements.effects.GrudgeMatch() }),
    SET_PIECE("Set Piece", createCard = { com.jervisffb.engine.bb2025.inducements.effects.SetPiece() }),
    SPORTS_ESPIONAGE("Sports Espionage", createCard = { com.jervisffb.engine.bb2025.inducements.effects.SportsEspionage() }),
    DISCARDED_BANANA_SKIN("Discarded Banana Skin", createCard = { com.jervisffb.engine.bb2025.inducements.effects.DiscardedBananaSkin() }),
    MAGIC_SCROLL("Magic Scroll", createCard = { com.jervisffb.engine.bb2025.inducements.effects.MagicScroll() });
}
