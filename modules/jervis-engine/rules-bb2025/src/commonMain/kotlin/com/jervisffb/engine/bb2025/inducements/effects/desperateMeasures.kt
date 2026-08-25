package com.jervisffb.engine.bb2025.inducements.effects

import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.inducements.Timing
import com.jervisffb.engine.model.inducements.card.SpecialPlayCard
import com.jervisffb.engine.model.inducements.card.SpecialPlayCardCategory
import com.jervisffb.engine.rules.common.skills.Duration
import kotlinx.serialization.Serializable
import com.jervisffb.engine.bb2025.procedures.inducements.desperatemeasures.DiscardedBananaSkin as HandleDiscardedBananaSkinProcedure
import com.jervisffb.engine.bb2025.procedures.inducements.desperatemeasures.GrudgeMatch as HandleGrudgeMatchProcedure
import com.jervisffb.engine.bb2025.procedures.inducements.desperatemeasures.Hangover as HandleHangoverProcedure
import com.jervisffb.engine.bb2025.procedures.inducements.desperatemeasures.MagicScroll as HandleMagicScrollProcedure
import com.jervisffb.engine.bb2025.procedures.inducements.desperatemeasures.RazzleDazzle as HandleRazzleDazzleProcedure
import com.jervisffb.engine.bb2025.procedures.inducements.desperatemeasures.SetPiece as HandleSetPieceProcedure
import com.jervisffb.engine.bb2025.procedures.inducements.desperatemeasures.SportsEspionage as HandleSportsEspionageProcedure
import com.jervisffb.engine.bb2025.procedures.inducements.desperatemeasures.YouDope as HandleYouDopeProcedure

/**
 * While not similar to BB2020 Special Play Cards, Desperate Measures have
 * the same functionality, so we treat it like them.
 */
@Serializable
abstract class DesperateMeasures: SpecialPlayCard {
    override val type: SpecialPlayCardCategory = SpecialPlayCardCategory2025.DESPERATE_MEASURES
    // Desperate Measure "cards" are removed as soon as they are used.
    // Their effects will outlive the card itself.
    override val duration: Duration = Duration.IMMEDIATE
    // Not used as they are removed immediately after use.
    override var used: Boolean = false
    // Not used as they are removed immediately after use.
    override var isActive: Boolean = false
}

/**
 * "You Dope!"
 * See page 15 in Spike 22.
 */
@Serializable
class YouDope: DesperateMeasures() {
    override val name: String = "You Dope!"
    override val triggers: List<Timing> = listOf(Timing.AFTER_SETUP)
    override val procedure: Procedure = HandleYouDopeProcedure
}

/**
 * "Razzle-Dazzle"
 * See page 15 in Spike 22.
 */
@Serializable
class RazzleDazzle: DesperateMeasures() {
    override val name: String = "Razzle-Dazzle"
    override val triggers: List<Timing> = listOf(Timing.ACTIVATE_PLAYER)
    override val procedure: Procedure = HandleRazzleDazzleProcedure
}

/**
 * "Hangover"
 * See page 15 in Spike 22.
 */
@Serializable
class Hangover: DesperateMeasures() {
    override val name: String = "Hangover"
    override val triggers: List<Timing> = listOf(Timing.BEFORE_FIRST_SETUP)
    override val procedure: Procedure = HandleHangoverProcedure
}

/**
 * "Grudge Match"
 * See page 15 in Spike 22.
 */
@Serializable
class GrudgeMatch: DesperateMeasures() {
    override val name: String = "Grudge Match"
    override val triggers: List<Timing> = listOf(Timing.ACTIVATE_PLAYER)
    override val procedure: Procedure = HandleGrudgeMatchProcedure
}

/**
 * "Set Piece"
 * See page 15 in Spike 22.
 */
@Serializable
class SetPiece: DesperateMeasures() {
    override val name: String = "Set Piece"
    override val triggers: List<Timing> = listOf(Timing.PERFORM_PASS_ACTION)
    override val procedure: Procedure = HandleSetPieceProcedure
}

/**
 * "Sports Espionage"
 * See page 15 in Spike 22.
 */
@Serializable
class SportsEspionage: DesperateMeasures() {
    override val name: String = "Sports Espionage"
    override val triggers: List<Timing> = listOf(Timing.AFTER_TURNOVER)
    override val procedure: Procedure = HandleSportsEspionageProcedure
}

/**
 * "Discarded Banana Skin"
 * See page 15 in Spike 22.
 */
@Serializable
class DiscardedBananaSkin: DesperateMeasures() {
    override val name: String = "Discarded Banana Skin"
    override val triggers: List<Timing> = listOf(Timing.ENTER_TACKLEZONE)
    override val procedure: Procedure = HandleDiscardedBananaSkinProcedure
}

/**
 * "Magic Scroll"
 * See page 15 in Spike 22.
 */
@Serializable
class MagicScroll: DesperateMeasures() {
    override val name: String = "Magic Scroll"
    override val triggers: List<Timing> = listOf(Timing.BEFORE_SETUP)
    override val procedure: Procedure = HandleMagicScrollProcedure
}
