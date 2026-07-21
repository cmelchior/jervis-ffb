package com.jervisffb.engine.common.context

import com.jervisffb.engine.actions.D16Result
import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.common.procedures.tables.injury.RiskingInjuryMode
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.context.ProcedureContext
import com.jervisffb.engine.model.inducements.Apothecary
import com.jervisffb.engine.model.inducements.MortuaryAssistant
import com.jervisffb.engine.model.locations.PitchCoordinate
import com.jervisffb.engine.model.modifiers.DiceModifier
import com.jervisffb.engine.rules.common.procedures.D6DieRoll
import com.jervisffb.engine.rules.common.tables.CasualtyResult
import com.jervisffb.engine.rules.common.tables.InjuryResult
import com.jervisffb.engine.rules.common.tables.LastingInjuryResult
import com.jervisffb.engine.utils.sum
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf

// What do we need to track?
data class RiskingInjuryContext(
    val player: Player,
    // If the Injury is caused by another player. This allows this player to use their
    // skills to modify the armour/injury rolls.
    val causedBy: Player? = null,
    val isPartOfMultipleBlock: Boolean = false,
    val mode: RiskingInjuryMode = RiskingInjuryMode.KNOCKED_DOWN,

    // For mode = KNOCKED_DOWN, there is a chance that the player avoids being knocked down.
    // This boolean allow callers to respond to that.
    val isKnockedDown: Boolean = false,

    // When rolling for Armour and Injury and using Arm Bar, we need to know the starting
    // coordinates, as that determines which players can participate.
    val startingCoordinatesForArmBar: PitchCoordinate? = null,

    // Armour roll
    val usedIronHardSkin: Boolean = false,
    val armourRoll: PersistentList<D6DieRoll> = persistentListOf(),
    val armourModifiers: PersistentList<DiceModifier> = persistentListOf(),
    val useClawsOnArmourRoll: Boolean = false,
    // An effect caused the Armour Roll to be aborted.
    // E.g. a Chainsaw player having a Kickback during a Foul.
    val armourRollAborted: Boolean = false,

    // Injury roll
    val injuryRoll: PersistentList<D6Result> = persistentListOf(),
    val injuryModifiers: PersistentList<DiceModifier> = persistentListOf(),
    val injuryResult: InjuryResult? = null,
    val useThickSkullOnInjuryRoll: Boolean = false,

    // Casualty roll
    val casualtyRoll: D16Result? = null,
    // Modifiers are also applied to `apothecary*` rolls.
    val casualtyModifiers: PersistentList<DiceModifier> = persistentListOf(),
    val casualtyResult: CasualtyResult? = null,
    val lastingInjuryRoll: D6Result? = null,
    val lastingInjuryResult: LastingInjuryResult? = null,

    // Apothecary + selecting a final casualty result
    val apothecaryUsed: Apothecary? = null,

    // BB7 Apothecary roll
    val apothecaryInjuryRoll: D6Result? = null,
    val apothecaryInjuryRollSuccess: Boolean = false,

    // BB11 Apothecary roll
    val apothecaryCasualtyRoll: D16Result? = null,
    val apothecaryCasualtyResult: CasualtyResult? = null,
    val apothecaryLastingInjuryRoll: D6Result? = null,
    val apothecaryLastingInjuryResult: LastingInjuryResult? = null,

    // Store final casualty rolls result here
    val finalCasualtyResult: CasualtyResult? = null,
    val finalLastingInjury: LastingInjuryResult? = null,

    // Regeneration
    val regenerationRoll: D6DieRoll? = null,
    val regenerationApothecaryUsed: Apothecary? = null,
    val regenerationMortuaryAssistantUsed: MortuaryAssistant? = null,
    val regenerationSuccess: Boolean = false
): ProcedureContext {

    private fun checkIfOpponentCanUseActiveSkills(): Boolean {
        if (causedBy == null) return false
        val rules = causedBy.team.game.rules
        return rules.isStanding(causedBy) && !rules.isDistracted(causedBy)
    }

    // Returns `true` if this injury is caused by another player that is able to use
    // their skills to modify the armour/injury roll
    val canOpponentUseSkills = checkIfOpponentCanUseActiveSkills()

    val injuryRollResult: Int
        get() = injuryRoll.sum() + injuryModifiers.sum()
    val armourResult: Int
        get() = armourRoll.sum() + armourModifiers.sum()
    val armourBroken: Boolean
        get() = (player.armorValue <= armourResult)
}
