package com.jervisffb.engine.bb2025.tables

import com.jervisffb.engine.bb2025.inducements.effects.DesperateMeasures
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.rules.common.tables.DesperateMeasuresEvent

/**
 * List all possible outcomes of Desperate Measures in BB2025.
 */
enum class DesperateMeasuresTableResult2025(
    override val label: String,
    override val createCard: (Team) -> DesperateMeasures
): DesperateMeasuresEvent {
    YOU_DOPE("You Dope", createCard = { team -> com.jervisffb.engine.bb2025.inducements.effects.YouDopeCard(team.id) }),
    RAZZLE_DAZZLE("Razzle Dazzle", createCard = { team -> com.jervisffb.engine.bb2025.inducements.effects.RazzleDazzleCard(team.id) }),
    HANGOVER("Hangover", createCard = { team -> com.jervisffb.engine.bb2025.inducements.effects.HangoverCard(team.id) }),
    GRUDGE_MATCH("Grudge Match", createCard = { team -> com.jervisffb.engine.bb2025.inducements.effects.GrudgeMatchCard(team.id) }),
    SET_PIECE("Set Piece", createCard = { team -> com.jervisffb.engine.bb2025.inducements.effects.SetPieceCard(team.id) }),
    SPORTS_ESPIONAGE("Sports Espionage", createCard = { team -> com.jervisffb.engine.bb2025.inducements.effects.SportsEspionageCard(team.id) }),
    DISCARDED_BANANA_SKIN("Discarded Banana Skin", createCard = { team -> com.jervisffb.engine.bb2025.inducements.effects.DiscardedBananaSkinCard(team.id) }),
    MAGIC_SCROLL("Magic Scroll", createCard = { team -> com.jervisffb.engine.bb2025.inducements.effects.MagicScrollCard(team.id) });
}
