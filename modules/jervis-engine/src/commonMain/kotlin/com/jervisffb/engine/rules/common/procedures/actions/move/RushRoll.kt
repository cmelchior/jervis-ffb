package com.jervisffb.engine.rules.common.procedures.actions.move

import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.context.UpdateContext
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.context.ProcedureContext
import com.jervisffb.engine.model.context.RushRollContext
import com.jervisffb.engine.model.context.assertContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.modifiers.DiceModifier
import com.jervisffb.engine.model.modifiers.RushModifier
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.common.procedures.D6DieRoll
import com.jervisffb.engine.rules.common.procedures.actions.dicerolls.D6WithRerollProcedure
import com.jervisffb.engine.rules.common.procedures.actions.dicerolls.RerollData
import com.jervisffb.engine.rules.common.tables.PrayerToNuffle
import com.jervisffb.engine.rules.common.tables.Weather
import com.jervisffb.engine.utils.sum

/**
 * Handle a player rushing a single square.
 *
 * See page 44 in the BB2020 rulebook.
 * See page 58 in the BB2025 rulebook.
 *
 * This procedure is only responsible for the actual dice roll. The parent
 * procedure must handle the result of the roll, it is not handled here.
 * This also includes modifying the number of rushes left.
 *
 * Designer's Commentary (BB2020):
 * If two rushes are necessary for a Jump/Leap and the first roll is a failure,
 * the player will be knocked down in the starting square, rather than
 * in the ending square.
 *
 * Developer's Commentary:
 * If more than one rush is required, it is up to the caller of this procedure
 * to do so. And also handle each roll result.
 *
 * Skills are optional to use, so technically you could choose to use Sprint
 * as part of doing a Rush, but since Rushing is also optional, we opt for the
 * easier implementation and check for Sprint when starting an action and add
 * either 2 or 3 allowed rushes at that point.
 *
 * Also, an observation about Rushing. It is worded this way in the rules:
 *
 * "Whenever a player performs an action that includes movement.."
 *
 * This means that if, by any means, a player is able to do two actions.
 * They would be able to move 2*Rush distance. Sprint has a similar wording
 * that would allow it to be used in both actions as well.
 *
 * Developer's Commentary (BB2025):
 * The wording in the rulebook does not strictly specify that you can
 * only Rush after using all normal Movement, which means that you can argue
 * that a player can roll for Rush at the beginning of the turn (which could be
 * an advantage in terms of positioning).
 *
 * The opposite argument is that the rulebook uses the wording "..attempt to
 * push themselves and move a little bit further than they normally could...";
 * this phrasing indicates that the roll happens after using normal movement.
 *
 * The BB2020 was clear about this happening after using normal movement.
 *
 * For these reasons Jervis will use the BB2020 behavior in BB2025 as well.
 *
 * @see [StandardMoveStep.ResolveRush]
 */
object RushRoll: D6WithRerollProcedure() {
    override val rollType: DiceRollType = DiceRollType.RUSH
    override val initialNode: Node get() = RollDie
    override fun onEnterRollProcedure(state: Game, rules: Rules): Command? {
        // Check for Rush modifiers
        val modifiers = mutableListOf<DiceModifier>()

        // Blizzard (Weather)
        if (state.weather == Weather.BLIZZARD) {
            modifiers.add(RushModifier.BLIZZARD)
        }
        // Moles under the Pitch (Prayers to Nuffle)
        if (state.homeTeam.activePrayersToNuffle.contains(PrayerToNuffle.MOLES_UNDER_THE_PITCH)) {
            modifiers.add(RushModifier.MOLES_UNDER_THE_PITCH_HOME)
        }
        if (state.awayTeam.activePrayersToNuffle.contains(PrayerToNuffle.MOLES_UNDER_THE_PITCH)) {
            modifiers.add(RushModifier.MOLES_UNDER_THE_PITCH_AWAY)
        }

        return if (modifiers.isNotEmpty()) {
            val rushContext = state.getContext<RushRollContext>()
            UpdateContext(rushContext.copyAndAddModifier(*modifiers.toTypedArray()))
        } else {
            null
        }
    }
    override fun onExitRollProcedure(state: Game, rules: Rules): Command? = null
    override fun isValid(state: Game, rules: Rules) = state.assertContext<RushRollContext>()
    override fun getActionOwner(state: Game): Team = state.getContext<RushRollContext>().player.team

    override val RollDie = object: AbstractRollDie() {
        override fun updateContext(state: Game, rules: Rules, d6: D6Result): ProcedureContext {
            val context = state.getContext<RushRollContext>()
            val success = isRushSuccess(d6, context.modifiers)
            return context.copy(
                roll = D6DieRoll.create(state, d6),
                isSuccess = success,
            )
        }
    }

    override val ChooseReRollSource = object : AbstractChooseRerollSource() {
        override fun getRerollData(state: Game, rules: Rules): RerollData {
            val context = state.getContext<RushRollContext>()
            return RerollData(context.player, context.roll!!, context.isSuccess)
        }
    }

    override val ReRollDie = object : AbstractReRollDie() {
        override fun updateContext(state: Game, rules: Rules, d6: D6Result): ProcedureContext {
            val rushContext = state.getContext<RushRollContext>()
            return rushContext.copy(
                roll = rushContext.roll!!.copyReroll(
                    rerollSource = state.rerollContext!!.source,
                    rerolledResult = d6,
                ),
                isSuccess = isRushSuccess(d6, rushContext.modifiers),
            )
        }
    }

    private fun isRushSuccess(d6: D6Result, modifiers: List<DiceModifier>): Boolean {
        val target = 2
        return d6.value + modifiers.sum() >= target
    }
}
