package com.jervisffb.engine.common.actions

import com.jervisffb.engine.GameEngineController
import com.jervisffb.engine.actions.EndActionWhenReady
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.actions.InducementSelection
import com.jervisffb.engine.actions.InducementsSelected
import com.jervisffb.engine.actions.SelectInducements
import com.jervisffb.engine.actions.Undo
import com.jervisffb.engine.common.inducements.BiasedRefereeInducement
import com.jervisffb.engine.common.inducements.BiasedRefereesInducementGroup
import com.jervisffb.engine.common.inducements.InducementSelectionCommon
import com.jervisffb.engine.common.inducements.InducementTypeCommon
import com.jervisffb.engine.common.inducements.InfamousCoachingStaffInducement
import com.jervisffb.engine.common.inducements.InfamousCoachingStaffsInducementGroup
import com.jervisffb.engine.common.inducements.MercenaryInducement
import com.jervisffb.engine.common.inducements.SimpleInducement
import com.jervisffb.engine.common.inducements.StandardMercenaryInducement
import com.jervisffb.engine.common.inducements.StarPlayerInducement
import com.jervisffb.engine.common.inducements.StarPlayersInducementGroup
import com.jervisffb.engine.common.inducements.WizardInducement
import com.jervisffb.engine.common.inducements.WizardsInducementGroup
import com.jervisffb.engine.model.Team
import kotlin.random.Random

/**
 * Developer debugging utility. Makes it easier to inject dice rolls to get the behaviour you want.
 * Should only be used when Dev Mode isn't enough to trigger the bug. Common use cases where
 * this might be nice is debugging race conditions in the UI.
 */
val randomList = mutableListOf<GameAction>(
//    1.d3, // Fan Factor roll (Home)
//    1.d3, // Fan Factor roll (Away)
//    DiceRollResults(3.d6, 4.d6), // Weather Roll
//    CoinTossResult(Coin.HEAD), // Coin Toss
//    DiceRollResults(2.d8, 1.d6), // Kick
//    DiceRollResults(5.d6, 5.d6), // Kickoff event
)

/**
 * Create a random action for the next ActionNode.
 */
fun createRandomAction(
    controller: GameEngineController,
    random: Random = Random,
    canUndo: Boolean = false
): GameAction {

    // Hacky way to inject events. Should probably try to add some kind of Developer UI
    // for this. See the `randomList` above` this function.
    if (randomList.isNotEmpty()) {
        return randomList.removeAt(0)
    }

    // 2% of the time, we will UNDO, rather than progress the game state.
    if (canUndo && controller.history.isNotEmpty() && random.nextInt(100) < 2) {
        return Undo
    }

    // Select a random action but disallow certain ones:
    // - EndAction: Do not call this to prevent a player stopping their turn too soon
    val availableActions = controller.getAvailableActions().actions
    var actionDesc: GameActionDescriptor?
    val filtered = availableActions.filter { it != EndActionWhenReady }
    if (filtered.isEmpty()) {
        actionDesc = availableActions.random(random)
    } else {
        actionDesc = filtered.random(random)
    }

    // Inducements are a bit special as we need access to the rules to create them, so do this manually for now
    return if (actionDesc is SelectInducements) {
        createCommonRandomInducements(random, controller.getAvailableActions().team!!, actionDesc.treasury + actionDesc.pettyCash)
    } else {
        actionDesc.createRandom(random)
    }
}

private fun createCommonRandomInducements(random: Random, team: Team, availableGold: Int): GameAction {
    val settings = team.game.rules.inducements
    var done = false
    var usedGold = 0
    val selectedInducements = mutableListOf<InducementSelection<*>>()
    val availableTypes = InducementTypeCommon.entries.toMutableSet()
    while (!done && availableTypes.isNotEmpty()) {
        // val inducement = settings.entries.random(random)
        val nextType = availableTypes.random(random)
        availableTypes.remove(nextType)
        val inducement = settings[nextType] ?: continue
        when (inducement) {
            is BiasedRefereesInducementGroup -> { /* Do nothing for now */ }
            is InfamousCoachingStaffsInducementGroup -> { /* Do nothing for now */ }
            is StarPlayersInducementGroup -> { /* Do nothing for now */ }
            is WizardsInducementGroup -> { /* Do nothing for now */ }
            is SimpleInducement -> {
                val price = inducement.getPrice(team)
                val count = random.nextInt(1, inducement.max + 1)
                if (usedGold + (price * count) > availableGold) {
                    done = true
                } else {
                    selectedInducements.add(InducementSelectionCommon.Simple(inducement.type, count))
                }
                usedGold += (price * count)
            }
            is StandardMercenaryInducement -> { /* Do nothing for now */ }
            is BiasedRefereeInducement,
            is InfamousCoachingStaffInducement,
            is StarPlayerInducement,
            is MercenaryInducement -> { /* Do nothing for now */ }
            is WizardInducement -> error("Should not appear as a top-level inducement: $inducement")
            else -> error("Unknown inducement type: $inducement")
        }
    }
    return InducementsSelected(selectedInducements)
}
