package com.jervisffb.test.utils

import com.jervisffb.engine.actions.CalculatedAction
import com.jervisffb.engine.actions.DicePoolChoice
import com.jervisffb.engine.actions.DicePoolResultsSelected
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.RerollOptionSelected
import com.jervisffb.engine.actions.SelectDicePoolResult
import com.jervisffb.engine.actions.SelectRerollOption
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.PlayerState
import com.jervisffb.engine.model.modifiers.PlayerStatusEffect
import com.jervisffb.engine.rules.builder.GameVersion
import com.jervisffb.engine.rules.common.skills.Skill
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.engine.rules.common.skills.TeamReroll
import com.jervisffb.engine.utils.singleInstanceOf
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalContracts::class)
inline fun <reified T : Any> assertTypeOf(obj: Any?): T {
    contract {
        returns() implies (obj is T)
    }
    assertTrue(obj is T, "Expected ${T::class.simpleName}, got ${obj?.let { it::class.simpleName }}")
    return obj
}

/**
 * Create a calculated game action that will determine which reroll to select
 * based on the provided skill type. If the given skill isn't available, an
 * error is thrown.
 */
@Suppress("TestFunctionName")
fun SelectSkillReroll(type: SkillType): GameAction {
    return CalculatedAction { state, rules ->
        val selectRerolls = getAvailableActions().singleInstanceOf<SelectRerollOption>()
        val skillReroll = selectRerolls.options.first {
            val source = it.getRerollSource(state)
            // Only support single D6 dice rolls for now
            if (source is Skill<*>) {
                source.type == type
            } else {
                false
            }
        }
        RerollOptionSelected(skillReroll, selectRerolls.dicePoolId)
    }
}

/**
 * Select the first available team reroll of the given type.
 *
 * If the reroll type isn't available, an error is thrown.
 */
@Suppress("TestFunctionName")
inline fun <reified T : TeamReroll> SelectTeamReroll(): GameAction {
    return CalculatedAction { state, rules ->
        val selectRerolls = getAvailableActions().singleInstanceOf<SelectRerollOption>()
        val teamReroll = selectRerolls.options.first {
            val source = it.getRerollSource(state)
            source is T
        }
        RerollOptionSelected(teamReroll, selectRerolls.dicePoolId)
    }
}

/**
 * Make it easy to select the final result of a dice pool being rolled. If
 * multiple pools are present, an error is thrown.
 *
 * This function is used to select block dice results.
 */
@Suppress("TestFunctionName")
fun SelectSingleBlockDieResult(index: Int = 0): GameAction {
    return CalculatedAction { _, _ ->
        val dicePools = getAvailableActions().singleInstanceOf<SelectDicePoolResult>()
        if (dicePools.pools.size > 1) error("Too many dice pools: ${dicePools.pools.size}")
        val pool = dicePools.pools.first()
        val poolChoice  = DicePoolChoice(pool.id, listOf(pool.dice[index].let { DicePoolChoice.SelectedDiceRoll(it.id, it.result) }))
        DicePoolResultsSelected(listOf(poolChoice))
    }
}

/**
 * WARNING: Only use this method if you mean a very specific skill in a specific ruleset.
 */
inline fun <reified T: Skill<*>> Player.hasSkill(): Boolean {
    return this.skills.filterIsInstance<T>().isNotEmpty()
}

/**
 * Modify the Player state, so they will be considered Prone.
 */
fun Player.putProne() {
    state = PlayerState.PRONE
    hasTackleZones = false
}

/**
 * Modify the Player state, so they will be considered Distracted.
 * Note, Distracted doesn't exist in BB2020, but here we interpret it as being
 * Standing without TackleZones
 */
fun Player.makeDistracted() {
    if (state != PlayerState.STANDING) error("Player must be standing to be marked as distracted")
    hasTackleZones = false
    if (this.team.game.rules.baseVersion == GameVersion.BB2025) {
        statusEffects.add(PlayerStatusEffect.distracted())
    }
}

/**
 * Helper method, checking if a player is standing
 */
fun Player.assertStanding() {
    assertEquals(PlayerState.STANDING, state)
    assertTrue(hasTackleZones)
    assertTrue(location.isOnField(team.game.rules))
}
