package com.jervisffb.test.utils

import com.jervisffb.engine.actions.CalculatedAction
import com.jervisffb.engine.actions.DicePoolChoice
import com.jervisffb.engine.actions.DicePoolResultsSelected
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.RerollOptionSelected
import com.jervisffb.engine.actions.SelectDicePoolResult
import com.jervisffb.engine.actions.SelectRerollOption
import com.jervisffb.engine.rules.bb2020.skills.TeamReroll
import com.jervisffb.engine.rules.common.skills.Skill
import com.jervisffb.engine.rules.common.skills.SkillType
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
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
 * Finds the first element of the expected type.
 */
inline fun <reified T: Any> List<*>.firstInstanceOf(): T {
    return first { it is T } as T
}

/**
 * Finds the first element of the expected type or `null` if no element matched.
 */
inline fun <reified T: Any> List<*>.firstInstanceOfOrNull(): T? {
    return firstOrNull { it is T } as T?
}

/**
 * Create a calculated game action that will determine which reroll to select
 * based on the provided skill type. If the given skill isn't available, an
 * error is thrown.
 */
@Suppress("TestFunctionName")
fun SelectSkillReroll(type: SkillType): GameAction {
    return CalculatedAction { state, rules ->
        val selectRerolls = getAvailableActions().firstInstanceOf<SelectRerollOption>()
        val skillReroll = selectRerolls.options.first {
            val source = it.getRerollSource(state)
            // Only support single D6 dice rolls for now
            if (source is Skill) {
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
 * If the reroll type isn't available
 * and error is thrown.
 */
@Suppress("TestFunctionName")
inline fun <reified T : TeamReroll> SelectTeamReroll(): GameAction {
    return CalculatedAction { state, rules ->
        val selectRerolls = getAvailableActions().firstInstanceOf<SelectRerollOption>()
        val teamReroll = selectRerolls.options.first {
            val source = it.getRerollSource(state)
            source is T
        }
        RerollOptionSelected(teamReroll, selectRerolls.dicePoolId)
    }
}

/**
 * Make it easy to select the final result of a single die being rolled. If
 * multiple pools are present or more than one die is being rolled, an error
 * is thrown.
 *
 * This function is used to select block dice results.
 */
@Suppress("TestFunctionName")
fun SelectSingleBlockDieResult(): GameAction {
    return CalculatedAction { _, _ ->
        val dicePools = getAvailableActions().firstInstanceOf<SelectDicePoolResult>()
        if (dicePools.pools.size > 1) error("Too many dice pools: ${dicePools.pools.size}")
        val pool = dicePools.pools.first()
        if (pool.selectDice != 1) error("Only one dice is supported: ${pool.selectDice}")
        val poolChoice  = DicePoolChoice(pool.id, pool.dice.map { it.result })
        DicePoolResultsSelected(listOf(poolChoice))
    }
}
